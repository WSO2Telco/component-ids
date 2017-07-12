package com.wso2telco.util;

public enum ErrorCode {

    ERR_INVALID_MESSAGE_FORMAT("EX0001", "Invalid message format."),
    ERR_MSISDN_LIST_EMPTY("EX0002", "msisdn list cannot be empty."),
    ERR_MSISDN_EXCEED_LIMIT("EX0003", "Provided list of numbers exceeds allowed limit."),
    ERR_INVALID_OPERATOR("EX0003", "Invalid operator.");

    private final String code;
    private final String description;

    private ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

}
