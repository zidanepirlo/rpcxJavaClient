package com.yuan.rpcx.RpcWatch;

import com.yuan.rpcx.Register.ZkCliRegister;
import lombok.Getter;
import lombok.Setter;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.yuan.rpcx.Utils.ZkUtils.buildAppPath;
import static com.yuan.rpcx.Utils.ZkUtils.buildSrvPath;

/**
 * CreateSessionSync
 *
 * @author yuanqing
 * @create 2018-10-01 ����8:52
 **/

@Getter
@Setter
public class RpcxSyncWatch implements Watcher {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String appName;

    private class MyChildrenCallback implements  AsyncCallback.ChildrenCallback
    {

        private String serviceName;

        public MyChildrenCallback(String serviceName){
            this.serviceName = serviceName;
        }

        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children) {

//            logger.info("----------------processResult begin");
//
//            logger.info("----------------processResult begin serviceName={}",serviceName);
//
//            logger.info("rc={}",rc);
//            logger.info("path={}",path);
//            logger.info("ctx={}",ctx);
//
//            for (String child:children){
//                logger.info("children={}",child);
//            }
//            logger.info("----------------processResult end");

//            logger.info("MyChildrenCallback --- processResult,path={},children={}",path,children);
            try{

                ZkCliRegister.processNodeChildrenChanged(appName,path,children);
            }catch (Exception ex){
                logger.error(ex.getMessage(),ex);
            }
            testLogPrint();
        }
    }

    private List<String> refreshMonitor(){
        List<String> childNodes = null;
        try {
            ZooKeeper zk = ZkCliRegister.getZooKeeper(appName);
            if (zk.exists(buildAppPath(appName),true)!=null){
                childNodes = zk.getChildren(buildAppPath(appName),true);
                if (childNodes.size()>0){
                    for (String childNode:childNodes){
                        zk.getChildren(buildSrvPath(appName,childNode),true,new MyChildrenCallback(childNode),this);
//                        logger.info("watchedEvent childNode={}",childNode);
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return childNodes;
    }


    protected void NodeCreated(String path) throws Exception {

        ZkCliRegister.processNodeCreated(appName);
        testLogPrint();
    }

    protected void NodeDeleted() throws Exception {

        ZkCliRegister.processNodeDeleted(appName);
        testLogPrint();
    }

//    protected void NodeDataChanged() throws Exception {
//
//    }

//    protected void NodeChildrenChanged() throws Exception {
//
//    }



    @Override
    public void process(WatchedEvent watchedEvent) {

//        logger.info("--------process---------");

        Event.KeeperState state = watchedEvent.getState();
        Event.EventType type = watchedEvent.getType();
        String path = watchedEvent.getPath();
//        logger.info("listening path={},state{},type={}", path,state, type);

        refreshMonitor();

        if (state == Event.KeeperState.SyncConnected){

            if (Event.EventType.NodeCreated == type){
                try {
                    NodeCreated(path);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(),ex);
                }
            }
            if(Event.EventType.NodeDeleted == type){
                try {
                    NodeDeleted();
                } catch (Exception ex) {
                    logger.error(ex.getMessage(),ex);
                }
            }
        }

        if (state == Event.KeeperState.Expired){
            try{
                ZkCliRegister.reConnect(appName,this);
                refreshMonitor();
            }catch (Exception ex){
                logger.error(ex.getMessage(),ex);
            }
        }

        testLogPrint();
    }


    private void testLogPrint(){

        ZkCliRegister.getAppSrvLock().readLock().lock();
        try {

            logger.info("testLogPrint ---------- appToServices = {}",ZkCliRegister.getAppToServices());
            logger.info("testLogPrint ---------- appSrvToSrvEntity = {}",ZkCliRegister.getAppSrvToSrvEntity());

        }finally {
            ZkCliRegister.getAppSrvLock().readLock().unlock();
        }

    }

    public static void main(String[] args) {

        List<String> list1 = new ArrayList<>();
        list1.add("tcp@192.168.1.102:8972");

        List<String> list2 = new ArrayList<>();
        list2.add("tcp@192.168.1.102:8972");

        try{
            ZkCliRegister.processNodeChildrenChanged("rpcx_test","/rpcx_test/OrgSrv",list1);
            ZkCliRegister.processNodeChildrenChanged("rpcx_test","/rpcx_test/StudentSrv",list2);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }

        System.out.println("--------------------");

    }
}