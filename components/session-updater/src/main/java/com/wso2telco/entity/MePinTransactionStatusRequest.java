package com.wso2telco.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by isuru on 4/2/17.
 */
public class MePinTransactionStatusRequest {

    private String action;

    @SerializedName("app_id")
    private String appId;

    @SerializedName("transaction_id")
    private String transactionId;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
