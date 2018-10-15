package com.yuan.rpcx.Enum;


import lombok.Getter;
import lombok.Setter;

@Getter
public enum  RPCXSerializeType {

    JSON("1","JSON"),
    PROTOBUFFER("2","PROTOBUFFER"),
    MSGPACK("3","MSGPACK"),
    THRIFT("4","THRIFT");

    String code;
    String desc;

    RPCXSerializeType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
