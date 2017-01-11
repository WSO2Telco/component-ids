package com.wso2telco.entity;

/**
 * Created by isuru on 1/9/17.
 */
public enum StatusCode {

    SUCCESS("S1000"), VALIDATION_ERROR("E1001"), USSD_ERROR("E1002");

    private String code;

    StatusCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

