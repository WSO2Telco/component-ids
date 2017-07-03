package com.wso2telco.entity;

import org.json.JSONArray;

import java.util.List;

public class RegisterUserResponseBuilderRequest {

    private JSONArray msisdnArr;
    private String operator;
    private UserRegistrationResponse response;
    private List<RegisterUserStatusInfo> userRegistrationStatusList;

    public JSONArray getMsisdnArr() {
        return msisdnArr;
    }

    public void setMsisdnArr(JSONArray msisdnArr) {
        this.msisdnArr = msisdnArr;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public UserRegistrationResponse getResponse() {
        return response;
    }

    public void setResponse(UserRegistrationResponse response) {
        this.response = response;
    }

    public List<RegisterUserStatusInfo> getUserRegistrationStatusList() {
        return userRegistrationStatusList;
    }

    public void setUserRegistrationStatusList(List<RegisterUserStatusInfo> userRegistrationStatusList) {
        this.userRegistrationStatusList = userRegistrationStatusList;
    }

}
