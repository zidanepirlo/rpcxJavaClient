package com.yuan.rpcx.Enum;

public enum ReqMedType {

    POST,
    GET;

    public static ReqMedType getType(String status) {
        try {
            return Enum.valueOf(ReqMedType.class, status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void main(String[] args) {

        ReqMedType reqMedType = getType("POST");
        System.out.println(reqMedType.toString());
    }

}
