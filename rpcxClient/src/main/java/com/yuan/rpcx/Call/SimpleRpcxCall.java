package com.yuan.rpcx.Call;

import com.yuan.rpcx.Algorithm.AlgorithmLoadBalance;
import com.yuan.rpcx.Entity.Service;
import com.yuan.rpcx.Enum.LoadBalanceType;
import com.yuan.rpcx.Algorithm.RoundRobinLB;
import com.yuan.rpcx.Api.RpcxCall;
import com.yuan.rpcx.Entity.ServiceWeight;
import com.yuan.rpcx.Enum.ReqMedType;
import com.yuan.rpcx.Exception.ServiceException;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.yuan.rpcx.Utils.RpcxCallUtils.synRpcxCall;

/**
 * SimpleRpcxCall
 *
 * @author yuanqing
 * @create 2018-10-07 下午8:53
 **/
public class SimpleRpcxCall implements RpcxCall, InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Getter
    @Setter
    private String appName;

    @Getter
    @Setter
    private List<String> servicesName;

    @Setter
    private int failRetrytimes = 3;

    @Setter
    private LoadBalanceType loadBalanceType = LoadBalanceType.ROUND_ROBIN;

    private Map<String,AlgorithmLoadBalance> SrvToAlgorithm = new ConcurrentHashMap<>();


    @Override
    public Object call(String service, String method, Map<String, String> params, ReqMedType reqMedType) throws Exception {
        return null;
    }

    @Override
    public String callForTest(String service,String method,Map<String, String> params, ReqMedType reqMedType) throws Exception {

        String result = null;
        if (SrvToAlgorithm.containsKey(service)){

            ServiceWeight serviceWeight = null;
            for (int i = 0; i < failRetrytimes; i++) {

                AlgorithmLoadBalance algorithmLoadBalance = SrvToAlgorithm.get(service);
                serviceWeight = algorithmLoadBalance.getService();
                Service srv = serviceWeight.getService();
                if (null == serviceWeight) {
                throw new ServiceException("service  " + service + " not existed!");
//                    logger.error("service serviceName={} not existed!",service);
//                    break;
                }

                try {
                    if (!serviceWeight.getService().getIsAvailable().get()) {
                        serviceWeight.getFailAmt().incrementAndGet();
                        logger.info("callForTest not Available failRetrytimes={} serviceWeight={}", i,serviceWeight);
                        continue;
                    } else {
                        result = synRpcxCall(appName,serviceWeight.getService().getIpAndPort(),service,method,params,reqMedType);
                        logger.info("result={},success receiver={}",result,srv.getIpAndPort());
                        serviceWeight.getSuccessAmt().incrementAndGet();
                        logger.info("callForTest success failRetrytimes={} serviceWeight={}", i,serviceWeight);
                        algorithmLoadBalance.testLogPrint();
                        break;
                    }

                }
                catch (Exception ex) {
                    logger.info("callForTest fail failRetrytimes={} serviceWeight={}", i,serviceWeight);
                    serviceWeight.getFailAmt().incrementAndGet();
                    logger.error(ex.getMessage(), ex);
                    continue;
                }
            }
        }
        else{
            logger.error("service serviceName={} not existed!",service);
            throw new ServiceException("service  " + service + " not existed!");
        }

        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        for (String srvName : servicesName){

            switch (loadBalanceType){
                case ROUND_ROBIN:
                    SrvToAlgorithm.put(srvName,new RoundRobinLB(appName,srvName));
                    break;
                default:
                    SrvToAlgorithm.put(srvName,new RoundRobinLB(appName,srvName));
                    break;
            }
        }
        logger.info("SimpleRpcxCall init AlgorithmLoadBalance");
    }

}
