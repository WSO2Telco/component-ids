package com.wso2telco.proxy.exception;

public class AuthProxyServiceException extends Exception {
    public AuthProxyServiceException(String msg) {
        super(msg);
    }

    public AuthProxyServiceException(String msg, Throwable e) {
        super(msg, e);
    }

    public AuthProxyServiceException(Throwable throwable) {
        super(throwable);
    }
}