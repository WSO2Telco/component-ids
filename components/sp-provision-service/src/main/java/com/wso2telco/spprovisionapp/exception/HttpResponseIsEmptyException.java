package com.wso2telco.spprovisionapp.exception;

import java.io.IOException;

public class HttpResponseIsEmptyException  extends IOException{

    public HttpResponseIsEmptyException() {
        super();
    }

    public HttpResponseIsEmptyException(String message) {
        super(message);
    }

    public HttpResponseIsEmptyException(String message, Throwable cause) {
        super(message, cause);
    }
}
