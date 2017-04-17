package com.wso2telco.gsma.authenticators.model;

import com.google.gson.annotations.SerializedName;

public class MePinTransactionRequest {

    private String action;

    @SerializedName("app_id")
    private String appId;

    private String identifier;

    @SerializedName("callback_url")
    private String callbackUrl;

    @SerializedName("mepin_id")
    private String mePinId;

    @SerializedName("u2f_user_id")
    private String u2fUserId;

    @SerializedName("oath_user_id")
    private String authUserId;

    @SerializedName("short_message")
    private String shortMessage;

    private String header;

    private String message;

    @SerializedName("sp_icon")
    private String spIcon;

    @SerializedName("expiry_time")
    private int expiryTimeInSeconds;

    @SerializedName("confirmation_policy")
    private String confirmationPolicy;

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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getMePinId() {
        return mePinId;
    }

    public void setMePinId(String mePinId) {
        this.mePinId = mePinId;
    }

    public String getU2fUserId() {
        return u2fUserId;
    }

    public void setU2fUserId(String u2fUserId) {
        this.u2fUserId = u2fUserId;
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(String authUserId) {
        this.authUserId = authUserId;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSpIcon() {
        return spIcon;
    }

    public void setSpIcon(String spIcon) {
        this.spIcon = spIcon;
    }

    public int getExpiryTimeInSeconds() {
        return expiryTimeInSeconds;
    }

    public void setExpiryTimeInSeconds(int expiryTimeInSeconds) {
        this.expiryTimeInSeconds = expiryTimeInSeconds;
    }

    public String getConfirmationPolicy() {
        return confirmationPolicy;
    }

    public void setConfirmationPolicy(String confirmationPolicy) {
        this.confirmationPolicy = confirmationPolicy;
    }
}
