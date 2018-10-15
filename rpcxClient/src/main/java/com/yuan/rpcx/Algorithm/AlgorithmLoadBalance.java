package com.yuan.rpcx.Algorithm;

import com.yuan.rpcx.Entity.ServiceWeight;

public interface AlgorithmLoadBalance {

    ServiceWeight getService() throws Exception;

    //just for easy test,must be cancelled for real version
    void testLogPrint();
}
