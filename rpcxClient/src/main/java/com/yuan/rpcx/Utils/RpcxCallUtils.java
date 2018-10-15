package com.yuan.rpcx.Utils;

import com.yuan.rpcx.Constants.RpcxReqParam;
import com.yuan.rpcx.Enum.RPCXSerializeType;
import com.yuan.rpcx.Enum.ReqMedType;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * RpcxCallUtils
 *
 * @author yuanqing
 * @create 2018-10-13 上午7:54
 **/
public class RpcxCallUtils {

    private RpcxCallUtils(){
    }

    private final static String HTTP_PRE = "http://";
    private final static String URL_SPE = "/";

    private final static int READ_LEN = 100;

    public static String synRpcxCall(String appName,String IPAndPort,String srvName, String srvMethod,
                                     Map<String,String> params , ReqMedType reqMedType) throws Exception {

        Reader in = null;
        HttpURLConnection conn = null;
        DataOutputStream wr = null;
        StringBuilder sb = new StringBuilder();
        char[] repData = new char[READ_LEN];

        try {
            String arg = JsonUtil.collectToString(params);
            byte[] requestedPayload = arg.getBytes(RpcxReqParam.REQ_CHARSET_VAR);
            URL url = new URL(HTTP_PRE + IPAndPort + URL_SPE);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod(reqMedType.toString());
            conn.setRequestProperty(RpcxReqParam.REQ_CONTENT_TYPE, RpcxReqParam.REQ_CONTENT_TYPE_PRE + appName);
            conn.setRequestProperty(RpcxReqParam.REQ_CHARSET, RpcxReqParam.REQ_CHARSET_VAR);
            conn.setRequestProperty(RpcxReqParam.REQ_CONTENT_LENGTH, Integer.toString(requestedPayload.length));
            conn.setRequestProperty(RpcxReqParam.REQ_RPCX_MESSAGEID, RpcxReqParam.REQ_RPCX_MESSAGEID_VAR);
            conn.setRequestProperty(RpcxReqParam.REQ_RPCX_MESSSAGETYPE, RpcxReqParam.REQ_RPCX_MESSSAGETYPE_VAR);
            conn.setRequestProperty(RpcxReqParam.REQ_RPCX_SERIALIZETYPE, RPCXSerializeType.JSON.getCode());
            conn.setRequestProperty(RpcxReqParam.REQ_RPCX_SERVICEPATH, srvName);
            conn.setRequestProperty(RpcxReqParam.REQ_RPCX_SERVICEMETHOD, srvMethod);
            conn.setUseCaches(false);

            wr = new DataOutputStream(conn.getOutputStream());
            wr.write(requestedPayload);

            // read reply
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), RpcxReqParam.REQ_CHARSET_VAR));

            for (int count; (count = in.read(repData)) >= 0; ) {
                if (count < READ_LEN) {
                    for (int i = 0; i < count; i++) {
                        sb.append(String.valueOf(repData[i]));
                    }
                } else {
                    sb.append(String.valueOf(repData));
                }
            }
        } catch (Exception ex) {
            throw ex;
        } finally {

            if (null != in) {
                in.close();
            }

            if (null != wr) {
                wr.close();
            }
        }

        return sb.toString();
    }
}
