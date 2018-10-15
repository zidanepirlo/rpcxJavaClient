package com.yuan.rpcx.Utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * ZKUtils
 *
 * @author yuanqing
 * @create 2018-10-03 下午9:44
 **/
public class ZkUtils {

    private ZkUtils(){
    }

    private final static String SRV_STORE_KEY_JOINTER = "-";

    private static final String APP_SRV_JOINTER = "/";

    private static final String IP_PORT_JOINTER = ":";

    public static String getIpPortKey(final String ip,final String port){
        return StringUtils.isEmpty(ip) || StringUtils.isEmpty(ip) ? null : ip + IP_PORT_JOINTER +port;
    }

    public static String getSrvStoreKey(final String ip, final String port){
        return StringUtils.isEmpty(ip) || StringUtils.isEmpty(port) ? null : ip + IP_PORT_JOINTER +port;
    }

    public static String getAppSrvToEntityKey(final String appName, final String serviceName) {

        return StringUtils.isEmpty(appName) || StringUtils.isEmpty(serviceName) ? null : APP_SRV_JOINTER + appName + APP_SRV_JOINTER + serviceName;
    }


    // data store in Child node,for example  tcp@192.168.1.102:8972
    // parse child data
    // return map
    //  example for  tcp@192.168.1.102:8972
    // {protocal : tcp },{ip : 192.168.1.102 },{port : 8972}
    public static Map<String,String>  ParseZKChildData(final String childData){

        if (StringUtils.isEmpty(childData))
            return null;
        else{
            Map<String,String> map = new HashMap<>();
            String[] str1 = childData.split("@");
            map.put("protocal",str1[0]);
            String[] str2 = str1[1].split(":");
            map.put("ip",str2[0]);
            map.put("port",str2[1]);
            return map;
        }
    }


    // str[0]  = appName , str[1] = serviceName
    public static String[] parsePath(final String path){

        if (StringUtils.isEmpty(path))
            return null;
        String [] result = new String[2];
        String []strs = path.split(APP_SRV_JOINTER);
        result[0] = strs[1];
        result[1] = strs[2];
        return result;
    }

    public static String buildAppPath(final String appName) {
        return APP_SRV_JOINTER + appName;
    }

    public static String buildSrvPath(final String appName, final String serviceName) {
        return APP_SRV_JOINTER + appName + APP_SRV_JOINTER + serviceName;
    }

    public static void main(String[] args) {

//        String str = "tcp@192.168.1.102:8972";
//        String[] str1 = str.split("@");
//        String[] str2 = str1[1].split(":");
//        System.out.println(str1[0]);
//        System.out.println(str2[0]);
//        System.out.println(str2[1]);

        String []strs = parsePath("/aaa/bbb");

    }
}
