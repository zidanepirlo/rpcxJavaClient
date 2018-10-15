package com.yuan.rpcx.Entity;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import static com.yuan.rpcx.Utils.ZkUtils.getSrvStoreKey;

/**
 * ServiceEntity
 *
 * @author yuanqing
 * @create 2018-10-02 下午11:41
 **/

@Getter
public  class ServiceEntity {

     private String serviceName;

     // /appName/serviceName
     private String servicePath;

     //key = ip:port, value = Service
     private Map<String,Service> services;

     public ServiceEntity(String serviceName,String servicePath){
          this.serviceName = serviceName;
          this.servicePath = servicePath;
     }

     public Service getSrv(final String ip, final String port){
          if (null == services)
               return null;
          return services.get(getSrvStoreKey(ip,port));
     }

     public boolean srvIsExisted(final String ip, final String port){
          return getSrv(ip,port) == null ? false:true;
     }

     public void storeSrv(final String ip, final String port, final String protocal) {
          if (null == services) {
               services = new HashMap<>();
          }
          services.put(getSrvStoreKey(ip,port), new Service(ip, port, protocal));
     }

     public void storeSrv(final Service service) {
          if (null == service){
               return;
          }
          if (null == services) {
               services = new HashMap<>();
          }
          services.put(getSrvStoreKey(service.getIP(),service.getPort()), service);
     }

     public void setSrvAvail(final String ip, final String port){
          if (srvIsExisted(ip,port)){
//               getSrv(ip,port).setAvailable(true);
               getSrv(ip,port).getIsAvailable().set(true);
          }
     }

     public void setSrvUnAvail(final String ip, final String port){
          if (srvIsExisted(ip,port)){
//               getSrv(ip,port).setAvailable(false);
              getSrv(ip,port).getIsAvailable().set(false);
          }
     }

     public boolean hasAvailSrvs() {
          if (null == services)
               return false;
          for (Map.Entry<String, Service> m : services.entrySet()) {
//               if (m.getValue().isAvailable())
               if (m.getValue().getIsAvailable().get())
                    return true;
          }
          return false;
     }

     public int countUnAvailSrvs(){
          if (null == services)
               return 0;
          int count = 0;
          for (Map.Entry<String, Service> m : services.entrySet()) {
//               if (!m.getValue().isAvailable())
               if (!m.getValue().getIsAvailable().get())
                    count++;
          }
          return count;
     }

     public int countAvailSrvs(){
          return countSrvs() - countUnAvailSrvs();
     }

     public int countSrvs(){
          if (null == services)
               return 0;
          int count = 0;
          for (Map.Entry<String, Service> m : services.entrySet()) {
                    count++;
          }
          return count;
     }


     public void disableAllSrvs() {
          if (null != services) {
               for (Map.Entry<String, Service> m : services.entrySet()) {

//                    if (m.getValue().isAvailable()) {
//                         m.getValue().setAvailable(false);
//                    }
                   m.getValue().getIsAvailable().compareAndSet(true,false);
               }
          }
     }

    /**
     *
     * @param validSrvs map <ip:port , Service> </>
     */

     public void processValidSrvs(Map<String,Service> validSrvs) {

          if (null != services && validSrvs != null) {

               Set<String> srvKeys = new HashSet<>();
               Set<String> validSrvsKeys = validSrvs.keySet();

               for(String key : services.keySet()){
                    srvKeys.add(key);
               }
               srvKeys.removeAll(validSrvsKeys);

               //deal validate services
               for (String validSrv : validSrvsKeys){
                    //not existed add and  set Available = true
                    if ( null == services.get(validSrv) ){
                         Service service = validSrvs.get(validSrv);
                         service.getIsAvailable().set(true);
                         services.put(validSrv,service);
                    }
                    //existed set Available = true
                    else{
                         services.get(validSrv).getIsAvailable().set(true);
                    }
               }
               //end

               //deal unValidate services
               for (String srvKey:srvKeys){
                    if ( null != services.get(srvKey)){
                         services.get(srvKey).getIsAvailable().set(false);
                    }
               }
               //end
          }
     }

     public void clearAllSrvs(){
          if (null != services) {
               services.clear();
               services = null;
          }
     }

    @Override
    public int hashCode() {

        int result = 17;
        if (null != services) {

            for (Map.Entry<String, Service> m : services.entrySet()) {
                result = 31 * result + m.getValue().hashCode();
            }

        }
        result = 31 * result + serviceName.hashCode();
        return result;
    }

     @Override
     public String toString() {
          return "ServiceEntity {" +
                  ", serviceName=" + serviceName +
                  ", servicePath=" + servicePath +
                  ", services='" + services + '\'' +
                  '}';
     }

    public static void main(String[] args) {

//        ServiceEntity serviceEntity1 = new ServiceEntity("aaa","bbb");
//
//        Service srv1 = new Service("192.168.1.100","8080","tcp");
//        Service srv2 = new Service("192.168.1.101","8080","tcp");
//        Service srv3 = new Service("192.168.1.102","8080","tcp");
//
//        srv2.getIsAvailable().set(true);
//
////        serviceEntity1.storeSrv(srv1);
//        serviceEntity1.storeSrv(srv2);
//        serviceEntity1.storeSrv(srv3);
//
//        ServiceEntity serviceEntity2 = new ServiceEntity("aaa","bbb");
//        Service srv4 = new Service("192.168.1.101","8080","tcp");
//        Service srv5 = new Service("192.168.1.102","8080","tcp");
//        Service srv6 = new Service("192.168.1.103","8080","tcp");
//
//        serviceEntity2.storeSrv(srv4);
//        serviceEntity2.storeSrv(srv5);
////        serviceEntity2.storeSrv(srv6);
//
//        int hasCode1 = serviceEntity1.hashCode();
//        int hasCode2 = serviceEntity2.hashCode();
//
//        System.out.println(hasCode1);
//        System.out.println(hasCode2);
//
//        System.out.println(hasCode2 == hasCode1);

        Set<Service> set = new HashSet<>();

        Service srv1 = new Service("192.168.1.100", "8080", "tcp");
        Service srv2 = new Service("192.168.1.100", "8080", "tcp");
        Service srv3 = new Service("192.168.1.100", "8080", "tcp");

        set.add(srv1);
        set.add(srv1);
//        set.add(srv3);

        System.out.println("--------");

    }


}
