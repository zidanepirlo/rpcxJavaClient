package com.yuan.rpcx;

import com.yuan.rpcx.Register.ZkCliRegister;
import com.yuan.rpcx.RpcWatch.RpcxSyncWatch;
import lombok.Setter;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;


/**
 * zkApiCli
 *
 * @author yuanqing
 * @create 2018-10-01 ����9:01
 **/

public class ZkApiCli implements InitializingBean, DisposableBean {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static String zookeeperIP;

    private static int sessionTimeOut;

    @Setter
    private List<RpcxSyncWatch> watchers;

    public static String getZookeeperIP() {
        return zookeeperIP;
    }

    public void setZookeeperIP(String zookeeperIP) {
        ZkApiCli.zookeeperIP = zookeeperIP;
    }

    public static int getSessionTimeOut() {
        return sessionTimeOut;
    }

    public  void setSessionTimeOut(int sessionTimeOut) {
        ZkApiCli.sessionTimeOut = sessionTimeOut;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        InitZkConstants();
    }

    protected void InitZkConstants() throws Exception {

        logger.info(" init zooKeeperMap begin");
        try {
            for (RpcxSyncWatch createSessionSync : watchers) {
                ZkCliRegister.setZooKeeper(createSessionSync.getAppName(), new ZooKeeper(zookeeperIP, sessionTimeOut, createSessionSync));
            }
        } catch (Exception ex) {
            logger.error("init zooKeeperMap fail!");
            throw new Exception("init zooKeeperMap fail!");
        }
        logger.info(" init zooKeeperMap end");


        logger.info(" init zk listening begin");
        try {
            ZkCliRegister.initZKMonitor();
        } catch (Exception ex) {
            logger.error("init zk listening fail!");
            logger.error(ex.getMessage(),ex);
            throw new Exception("init zk listening fail!");
        }
        logger.info(" init zk listening end");
    }


    @Override
    public void destroy() throws Exception {
        try {
            ZkCliRegister.releaseZK();
        } catch (Exception ex) {
            logger.error("destroy zooKeeper fail!");
            logger.error(ex.getMessage(), ex);
            throw new Exception(ex);
        }
        logger.info("destroy zooKeeper");
    }
}
