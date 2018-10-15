package com.yuan.rpcx.Register;

import com.yuan.rpcx.Entity.Service;
import com.yuan.rpcx.Entity.ServiceEntity;
import com.yuan.rpcx.Utils.ZkUtils;
import com.yuan.rpcx.ZkApiCli;
import lombok.Getter;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.yuan.rpcx.Utils.ZkUtils.getAppSrvToEntityKey;
import static com.yuan.rpcx.Utils.ZkUtils.getSrvStoreKey;
import static com.yuan.rpcx.Utils.ZkUtils.parsePath;

/**
 * zkCliConstants
 *
 * @author yuanqing
 * @create 2018-10-02 下午4:31
 **/

public class ZkCliRegister {

    protected ZkCliRegister(){

    }

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Getter
    //key = appName ,value = serviceName
    private static final Map<String, Set<String>> appToServices = new ConcurrentHashMap<>();

    @Getter
    //key = /appName/serviceName, value = ServiceEntity
    private static final Map<String, ServiceEntity> appSrvToSrvEntity = new ConcurrentHashMap<>();

    private static final Map<String, ZooKeeper> zooKeeperMap = new HashMap<>();

    @Getter
    private static final ReadWriteLock appSrvLock = new ReentrantReadWriteLock(true);

    public static ZooKeeper getZooKeeper(final String appName){
        return zooKeeperMap.get(appName);
    }

    public static void setZooKeeper(final String appName,final ZooKeeper zooKeeper){
        zooKeeperMap.put(appName,zooKeeper);
    }

    public static void releaseZK() throws Exception{
        for (Map.Entry<String, ZooKeeper> m : zooKeeperMap.entrySet()) {
            if (m.getValue() != null) {
                m.getValue().close();
            }
        }
    }

    public static void initZKMonitor() throws Exception{
        for (Map.Entry<String, ZooKeeper> m : zooKeeperMap.entrySet()) {
            ZooKeeper zk = m.getValue();
            zk.exists("/" + m.getKey(), true);
        }
    }

    public static void InitAppData(final String appName){
        appToServices.put(appName,new HashSet<>());
    }

    public static void clearServices(final String appName) throws Exception {

            try{
                appSrvLock.writeLock().lockInterruptibly();
                if (null != appToServices.get(appName)) {
                    for (String service : appToServices.get(appName)) {
                        appSrvToSrvEntity.remove(getAppSrvToEntityKey(appName, service));
                    }
                }
                appToServices.get(appName).clear();
                appToServices.remove(appName);
            }
            finally {
                appSrvLock.writeLock().unlock();
            }
    }

    public static void storeServices(final String appName, final ServiceEntity service) throws Exception {

        try {
            appSrvLock.writeLock().lockInterruptibly();
            if (null == appToServices.get(appName)) {
                InitAppData(appName);
            }
            appSrvToSrvEntity.put(getAppSrvToEntityKey(appName, service.getServiceName()), service);
        } finally {
            appSrvLock.writeLock().unlock();
        }
    }

    public static ServiceEntity getServices(final String appName,final String serviceName)  throws Exception {

            try{
                appSrvLock.readLock().lock();
                if (null == appToServices.get(appName)){
                    return null;
                }
                else{
                    return appSrvToSrvEntity.get(getAppSrvToEntityKey(appName,serviceName));
                }

            }finally {
                appSrvLock.readLock().unlock();
            }
    }

    public static void addService(final String appName, final String serviceName, final ServiceEntity service) throws Exception {

        try {
            appSrvLock.writeLock().lockInterruptibly();
            if (null == appToServices.get(appName)) {
                InitAppData(appName);
            }
            appSrvToSrvEntity.put(getAppSrvToEntityKey(appName, service.getServiceName()), service);
        } finally {
            appSrvLock.writeLock().unlock();
        }
    }

    public static void reConnect(final String appName, final Watcher watcher) throws Exception {

        appSrvLock.writeLock().lockInterruptibly();
        try {
            ZooKeeper zk = ZkCliRegister.getZooKeeper(appName);
            if (null != zk) {
                zk.close();
            }
            ZkCliRegister.setZooKeeper(appName, new ZooKeeper(ZkApiCli.getZookeeperIP(), ZkApiCli.getSessionTimeOut(), watcher));
        } finally {
            appSrvLock.writeLock().unlock();
        }

    }

    public static void processNodeCreated(final String appName)  throws Exception {

        appSrvLock.writeLock().lockInterruptibly();
        try{
            if(null == appToServices.get(appName)){
                appToServices.put(appName,new HashSet<>());
            }
        }finally {
            appSrvLock.writeLock().unlock();
        }
    }

    public static void processNodeDeleted(final String appName) throws Exception {

        appSrvLock.writeLock().lockInterruptibly();
        try {
            if (null != appToServices.get(appName)) {

                Set<String> services = appToServices.get(appName);
                if (null != services){

                    Iterator<String> iterator=services.iterator();

                    while (iterator.hasNext()){

                        String serviceName = iterator.next();
                        String key = getAppSrvToEntityKey(appName, serviceName);
                        ServiceEntity serviceEntity = appSrvToSrvEntity.get(key);
                        if (null != serviceEntity && serviceEntity.countSrvs() == serviceEntity.countUnAvailSrvs()){
                            serviceEntity.clearAllSrvs();
                            appSrvToSrvEntity.remove(key);
                            iterator.remove();
                            if (0 == appToServices.get(appName).size()) {
                                appToServices.remove(appName);
                            }
                        }
                    }
                }
            }

        } finally {
            appSrvLock.writeLock().unlock();
        }
    }

    public static void processNodeDataChanged(final String appName, final List<String> childNodes) throws Exception{

        appSrvLock.writeLock().lockInterruptibly();
        try{
            if(null != appToServices.get(appName)){
                appToServices.put(appName,new HashSet<String>());
            }

        }finally {
            appSrvLock.writeLock().unlock();
        }
    }

    public static void processNodeChildrenChanged(final String appName,final String path,final List<String> childNodes) throws Exception {

        appSrvLock.writeLock().lockInterruptibly();
        try{

            //disable all services for app
            if (null == childNodes || childNodes.size() == 0) {
                ServiceEntity serviceEntity = appSrvToSrvEntity.get(path);
                if (null != serviceEntity) {
                    serviceEntity.disableAllSrvs();
                }
            }
            //deal validate services
            else{
                String serviceName = parsePath(path)[1];
                //add app, notice just app ,not contain service
                if (null == appToServices.get(appName)){

                    Set<String> srvsSet = new HashSet<>();
                    appToServices.put(appName,srvsSet);
                }

                //add not existed service
                if (!appToServices.get(appName).contains(serviceName)){
                    appToServices.get(appName).add(serviceName);
                }

                //just store validate service
                if (null == appSrvToSrvEntity.get(path)){

                    ServiceEntity serviceEntity = new ServiceEntity(appName,path);
                    Map<String, Service> serviceMap = parseChildNodeToSrv(childNodes);
                    for (Map.Entry<String,Service> m : serviceMap.entrySet()){
                        Service srv = m.getValue();
//                        srv.setAvailable(true);
                        srv.getIsAvailable().set(true);
                        serviceEntity.storeSrv(srv);
                    }
                    appSrvToSrvEntity.put(path,serviceEntity);
                }
                //set available = false for unvalidate service ( not in  childNodes),set available =  true for validate services ( in childNodes)
                else {
                    ServiceEntity serviceEntity = appSrvToSrvEntity.get(path);
                    Map<String, Service> validateSrvs = parseChildNodeToSrv(childNodes);
                    serviceEntity.processValidSrvs(validateSrvs);
                }

            }

        }finally {
            appSrvLock.writeLock().unlock();
        }

    }

    // key = ip-port value = service
    private static Map<String, Service> parseChildNodeToSrv(final List<String> childNodes){

        Map<String, Service> validSrvs = null;
        if (null != childNodes && childNodes.size() > 0) {

            validSrvs = new HashMap<>();
            for (String child : childNodes) {
                Map<String,String> map = ZkUtils.ParseZKChildData(child);
                if (null!=map){
                    String key = getSrvStoreKey(map.get("ip"),map.get("port"));
                    validSrvs.put(key,new Service(map.get("ip"),map.get("port"),map.get("protocal")));
                }

            }
        }
        return validSrvs;
    }

    public static List<Service> getAvailSrvs(final String appName, final String serviceName) throws Exception {

        appSrvLock.readLock().lockInterruptibly();
        List<Service> result = new ArrayList<>();
        try {

            ServiceEntity serviceEntity = appSrvToSrvEntity.get(ZkUtils.buildSrvPath(appName, serviceName));
            if (null == serviceEntity) {
                return null;
            }
            if (null == serviceEntity.getServices() || 0 == serviceEntity.getServices().size()) {
                return null;
            }

            Map<String, Service> services = serviceEntity.getServices();
            for (Map.Entry<String, Service> m : services.entrySet()) {
//                if (m.getValue().isAvailable()) {
                if (m.getValue().getIsAvailable().get()) {
                    result.add(m.getValue());
                }
            }
        } finally {
            appSrvLock.readLock().unlock();
        }

        return result;
    }

    //List<key> , key = ip:port
    public static Set<String> getAvailSrvsKeySet(final String appName, final String serviceName) throws Exception {

        Set<String> result = new HashSet<>();
        List<Service> services = getAvailSrvs(appName,serviceName);
        for (Service srv : services){
//            if (srv.isAvailable()){
            if (srv.getIsAvailable().get()){
                result.add(ZkUtils.getIpPortKey(srv.getIP(),srv.getPort()));
            }
        }
        return result;
    }

    public static int getSrvEntityHashCode(final String appName, final String srvName) {

        appSrvLock.readLock().lock();
        try {
            String key = ZkUtils.getAppSrvToEntityKey(appName, srvName);
            if (null != appSrvToSrvEntity.get(key)) {
                return appSrvToSrvEntity.get(key).hashCode();
            } else {
                return 0;
            }
        } finally {
            appSrvLock.readLock().unlock();
        }
    }

    public static void main(String[] args) {

        List<String> childNodes = new ArrayList<>();
        childNodes.add("tcp@127.0.0.1:8080");
        childNodes.add("tcp@127.0.0.2:8080");

        Map<String, Service> map = parseChildNodeToSrv(childNodes);
        System.out.println("----");

    }

}
