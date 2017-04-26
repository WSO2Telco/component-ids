package com.wso2telco.exception;

/**
 * Created by isuru on 2/14/17.
 */
public class EmptyResultSetException extends Exception {

    public EmptyResultSetException(String message) {
        super(message);
    }

    public EmptyResultSetException(String message, Throwable cause) {
        super(message, cause);
    }
}
