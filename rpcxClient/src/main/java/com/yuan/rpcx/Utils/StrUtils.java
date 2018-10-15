package com.yuan.rpcx.Utils;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * StrUtils
 *
 * @author yuanqing
 * @create 2018-10-13 上午7:29
 **/
public class StrUtils {

    private StrUtils(){
    }

    public static String mapToJson(Map map){
        return JSONObject.toJSONString(map);
    }

    public static void main(String[] args) {

        Map<String,String> map = new HashMap<>();

    }
}
