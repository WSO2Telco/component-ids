package src.main.java.com.wso2telco.exception;

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