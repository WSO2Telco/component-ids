package com.wso2telco.entity;

import com.google.gson.annotations.SerializedName;

public class MePinInteractionCreateRequest {

    private String action;

    @SerializedName("app_id")
    private String appId;

    private String identifier;

    @SerializedName("interaction_type")
    private String interactionType;

    @SerializedName("callback_url")
    private String callbackUrl;

    @SerializedName("mepin_id")
    private String mePinId;

    @SerializedName("interaction_url")
    private String interactionUrl;

    @SerializedName("u2f_user_id")
    private String u2fUserId;

    @SerializedName("public_key_hash")
    private String publicKeyHash;

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

    @SerializedName("resource_url")
    private String resourceUrl;

    @SerializedName("confirmation_policy")
    private String confirmationPolicy;

    @SerializedName("resource_params")
    private MePinInteractionRequestResourceParams mePinInteractionRequestResourceParams;

    public String getInteractionType() {
        return interactionType;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public MePinInteractionRequestResourceParams getMePinInteractionRequestResourceParams() {
        return mePinInteractionRequestResourceParams;
    }

    public void setMePinInteractionRequestResourceParams(MePinInteractionRequestResourceParams mePinInteractionRequestResourceParams) {
        this.mePinInteractionRequestResourceParams = mePinInteractionRequestResourceParams;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public String getPublicKeyHash() {
        return publicKeyHash;
    }

    public void setPublicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
    }

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

    public String getInteractionUrl() {
        return interactionUrl;
    }

    public void setInteractionUrl(String interactionUrl) {
        this.interactionUrl = interactionUrl;
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
