package com.yuan.rpcx.Exception;

import lombok.Getter;
import lombok.Setter;

/**
 * ServiceException
 *
 * @author yuanqing
 * @create 2018-10-09 下午5:05
 **/

@Setter
@Getter
public class ServiceException extends Exception {

    private int errorCode;
    private String message;

    public ServiceException(String message) {
        super(message);
        this.message = message;
    }

    public ServiceException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
}
