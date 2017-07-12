package com.wso2telco.entity;

public class RegisterUserStatusInfo {
    private String msisdn;
    private registerStatus status;

    public static enum registerStatus {
        OK,
        INUSE,
        INUSE_OPERATOR_VERIFICATION_FAILED,
        FAILED,
        DENIED,
        INVALID_MSISDN_FORMAT,
        MSISDN_EMPTY,
        UPDATED,
        OPERATOR_VERIFICATION_FAILED,
        MSISDN_NOT_FOUND,
        PROCESSING_FAILED,
    }


    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getMsisdn() {
        return msisdn;
    }


    public void setStatus(registerStatus status) {
        this.status = status;
    }

    public registerStatus getStatus() {
        return status;
    }
}
