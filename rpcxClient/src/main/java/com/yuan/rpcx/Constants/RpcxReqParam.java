package com.yuan.rpcx.Constants;

import com.yuan.rpcx.Enum.RPCXSerializeType;

/**
 * RpcxReqParam
 *
 * @author yuanqing
 * @create 2018-10-12 上午8:30
 **/
public class RpcxReqParam {

    private RpcxReqParam() {
    }

    public final static String REQ_POST_METHOD = "POST";
    public final static String REQ_GET_METHOD = "GET";

    public final static String REQ_RPCX_SERVICEPATH = "X-RPCX-ServicePath";
    public final static String REQ_RPCX_SERVICEMETHOD = "X-RPCX-ServiceMethod";


    public final static String REQ_CONTENT_TYPE = "Content-Type";
    public final static String REQ_CONTENT_TYPE_PRE = "application/";

    public final static String REQ_CHARSET = "charset";
    public final static String REQ_CHARSET_VAR = "utf-8";

    public final static String REQ_CONTENT_LENGTH = "Content-Length";

    public final static String REQ_RPCX_MESSAGEID = "X-RPCX-MessageID";
    public final static String REQ_RPCX_MESSAGEID_VAR = "12345678";

    public final static String REQ_RPCX_MESSSAGETYPE = "X-RPCX-MesssageType";
    public final static String REQ_RPCX_MESSSAGETYPE_VAR = "0";

    public final static String REQ_RPCX_SERIALIZETYPE = "X-RPCX-SerializeType";
    public final static String REQ_RPCX_SERIALIZETYPE_VAR = RPCXSerializeType.JSON.getCode();


}
