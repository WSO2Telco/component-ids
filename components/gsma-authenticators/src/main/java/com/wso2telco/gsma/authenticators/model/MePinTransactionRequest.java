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

    @SerializedName("short_message")
    private String shortMessage;

    private String header;

    private String message;

    @SerializedName("logo_url")
    private String logoUrl;

    @SerializedName("sp_name")
    private String spName;

    @SerializedName("bg_image_url")
    private String bgImageName;

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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getSpName() {
        return spName;
    }

    public void setSpName(String spName) {
        this.spName = spName;
    }

    public String getBgImageName() {
        return bgImageName;
    }

    public void setBgImageName(String bgImageName) {
        this.bgImageName = bgImageName;
    }
}
