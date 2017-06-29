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
        UPDATED
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
