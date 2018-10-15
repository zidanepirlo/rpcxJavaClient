package com.yuan.rpcx.Algorithm;

import com.yuan.rpcx.Entity.Service;
import com.yuan.rpcx.Entity.ServiceWeight;
import com.yuan.rpcx.Exception.ServiceException;
import com.yuan.rpcx.Register.ZkCliRegister;
import com.yuan.rpcx.Utils.ZkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * RoundRobinLB
 *
 * @author yuanqing
 * @create 2018-10-06 下午6:07
 **/
public class RoundRobinLB implements AlgorithmLoadBalance {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final ReadWriteLock IpPortToSrvLock = new ReentrantReadWriteLock(true);

    private String appName;

    private String serviceName;

    private final int defalultWeight = 2;

    //key = ip:port, value = ServiceWeight
    private Map<String, ServiceWeight> IpPortToSrv ;

    private AtomicInteger calCount;

    //check whether services has been updated
    private Integer srvEntityHasCode = null;

    public RoundRobinLB(String appName, String serviceName) {

        this.appName = appName;
        this.serviceName = serviceName;
        IpPortToSrv = new ConcurrentHashMap<>();
        calCount = new AtomicInteger(0);
        init();
    }

    protected void init() {

        IpPortToSrvLock.writeLock().lock();
        try {

            if (null == srvEntityHasCode){
                srvEntityHasCode = new Integer(ZkCliRegister.getSrvEntityHashCode(appName,serviceName));
                loadNewData();
                initEffeWeight();
                logger.info("init  loadNewData");
                testLogPrint();
            }
            else{
                int hashCode = ZkCliRegister.getSrvEntityHashCode(appName,serviceName);
                //services change, should be reload service data
                if (srvEntityHasCode.intValue() != hashCode){
                    srvEntityHasCode = hashCode;
                    loadNewData();
                    initEffeWeight();
                    logger.info("init  loadNewData");
                    testLogPrint();
                }
                else{
                    initData();
                    logger.info("init  initData");
                    testLogPrint();
                }
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }finally {
            IpPortToSrvLock.writeLock().unlock();
        }
    }


    private void initData() throws Exception{
        calCount.set(0);
        adjEffWeight();
        setCurrWtEqlEffWt();
        for (Map.Entry<String, ServiceWeight> m : IpPortToSrv.entrySet()) {
            m.getValue().getSuccessAmt().set(0);
            m.getValue().getFailAmt().set(0);
        }
    }

    private void loadNewData() throws Exception{
        IpPortToSrv.clear();
        List<Service> services = ZkCliRegister.getAvailSrvs(appName, serviceName);
        if (null != services) {
            for (Service srv : services) {
                if (srv.getIsAvailable().get()) {
                    IpPortToSrv.put(ZkUtils.getIpPortKey(srv.getIP(), srv.getPort()), new ServiceWeight(srv,new AtomicInteger(defalultWeight)));
                }
            }
        }
        //set CurrentWeight = weight
        for (Map.Entry<String, ServiceWeight> m : IpPortToSrv.entrySet()){
            m.getValue().getCurrentWeight().set(m.getValue().getWeight().intValue());
        }
        calCount.set(0);
    }

    private boolean isOver(){

        IpPortToSrvLock.readLock().lock();
        try{

            if (calCount.intValue() >=this.getTotalWeight()){
                return true;
            }
            for (Map.Entry<String, ServiceWeight> m : IpPortToSrv.entrySet()) {
                if (m.getValue().getCurrentWeight().intValue() > 0) {
                    return false;
                }
            }

            return true;
        }
        finally {
            IpPortToSrvLock.readLock().unlock();
        }
    }



    private Set<String> getAvailableSet(){

        Set<String> result = new HashSet<>();
        for (Map.Entry<String, ServiceWeight> m:IpPortToSrv.entrySet()){
            if (m.getValue().getService().getIsAvailable().get()){
                result.add(m.getKey());
            }
        }
        return result;
    }

    private Set<String> getUnAvailableSet(){

        Set<String> result = new HashSet<>();
        for (Map.Entry<String, ServiceWeight> m:IpPortToSrv.entrySet()){
            if (!m.getValue().getService().getIsAvailable().get()){
                result.add(m.getKey());
            }
        }
        return result;
    }

    private void adjEffWeight() {

        for (Map.Entry<String, ServiceWeight> m : IpPortToSrv.entrySet()) {

            ServiceWeight serviceWeight = m.getValue();

            AtomicInteger weight = serviceWeight.getWeight();
            AtomicInteger effectWeight = serviceWeight.getEffectWeight();
            AtomicInteger successAmt = serviceWeight.getSuccessAmt();
            AtomicInteger failAmt = serviceWeight.getFailAmt();

            if (failAmt.intValue()>0){
                if (effectWeight.addAndGet(-failAmt.intValue()) < 0) {
                    effectWeight.set(weight.intValue());
                }
            }
            else{
                if (effectWeight.intValue() < weight.intValue()){
                    effectWeight.getAndIncrement();
                }
            }
        }

//        logger.info("---------adjEffWeight begin--------------");
//        testLogPrint();
//        logger.info("---------adjEffWeight end----------------");
    }

    private ServiceWeight getSrvByMaxCurWeight(){

        IpPortToSrvLock.readLock().lock();
        try{
            int maxWeight = 0;
            ServiceWeight bestSrvWeight = null;
            for (Map.Entry<String, ServiceWeight> m : IpPortToSrv.entrySet()){
                if (m.getValue().getCurrentWeight().intValue()>maxWeight){
                    bestSrvWeight = m.getValue();
                    maxWeight = bestSrvWeight.getCurrentWeight().intValue();
                }
            }
            return bestSrvWeight;

        }finally {
            IpPortToSrvLock.readLock().unlock();
        }
    }

    private void setCurrWtEqlEffWt() {

        for (Map.Entry<String, ServiceWeight> m : IpPortToSrv.entrySet()) {
            m.getValue().getCurrentWeight().set(m.getValue().getEffectWeight().intValue());
        }
    }

    private void initEffeWeight(){

        for (Map.Entry<String, ServiceWeight> m : IpPortToSrv.entrySet()){
            m.getValue().getEffectWeight().set(m.getValue().getWeight().intValue());
        }
    }

    private int getTotalWeight() {

        int totalWeight = 0;
        for (Map.Entry<String, ServiceWeight> m : IpPortToSrv.entrySet()){
            totalWeight += m.getValue().getEffectWeight().intValue();
        }
        return totalWeight;
    }

    private void currWeightAddTotalWeight(final ServiceWeight serviceWeight) throws Exception {

        IpPortToSrvLock.writeLock().lock();
        try {
            if(null == serviceWeight){
                throw new ServiceException("service not existed!");
            }
            String key = ZkUtils.getIpPortKey(serviceWeight.getService().getIP(), serviceWeight.getService().getPort());
            ServiceWeight srv = IpPortToSrv.get(key);
            srv.getCurrentWeight().set(srv.getCurrentWeight().intValue() - getTotalWeight());
//            logger.info("currWeightAddTotalWeight");
//            testLogPrint();

        }catch (Exception ex){
            logger.error(ex.getMessage(),ex);
        }
        finally {
            IpPortToSrvLock.writeLock().unlock();
        }
    }

    private void currWtAddEffWt() {
        for (Map.Entry<String, ServiceWeight> m : IpPortToSrv.entrySet()) {
            m.getValue().getCurrentWeight().addAndGet(m.getValue().getEffectWeight().intValue());
        }
//        logger.info("currWtAddEffWt");
//        testLogPrint();

    }

    @Override
    public ServiceWeight getService() throws Exception {

        if (isOver()){
            init();
        }
        //get best service
        ServiceWeight serviceWeight = getSrvByMaxCurWeight();
        calCount.incrementAndGet();
        currWeightAddTotalWeight(serviceWeight);
        currWtAddEffWt();

        return serviceWeight;
    }

    @Override
    public void testLogPrint(){
        logger.info("------------testLogPrint begin-----------");
        for (Map.Entry<String, ServiceWeight> m : IpPortToSrv.entrySet()){
            logger.info("------------testLogPrint calCount={} ServiceWeight={} ",calCount.intValue(),m.getValue());
        }
        logger.info("------------testLogPrint end-------------");
    }
}
