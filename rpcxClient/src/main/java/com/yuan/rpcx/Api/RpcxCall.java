package com.yuan.rpcx.Api;

import com.yuan.rpcx.Enum.ReqMedType;

import java.util.Map;

public interface RpcxCall {

    Object call(String service,String method,Map<String, String> params, ReqMedType reqMedType) throws Exception;

    String callForTest(String service,String method,Map<String, String> params, ReqMedType reqMedType) throws Exception;

}
