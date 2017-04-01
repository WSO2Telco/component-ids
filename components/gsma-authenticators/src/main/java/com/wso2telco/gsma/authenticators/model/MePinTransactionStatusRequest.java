package com.wso2telco.gsma.authenticators.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by isuru on 3/30/17.
 */
public class MePinTransactionStatusRequest {

    private String action;

    private String appId;

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

    @SerializedName("app_id")
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @SerializedName("transaction_id")
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
