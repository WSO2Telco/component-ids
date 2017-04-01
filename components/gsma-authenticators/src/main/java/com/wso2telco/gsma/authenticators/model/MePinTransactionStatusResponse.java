package com.wso2telco.gsma.authenticators.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by isuru on 3/30/17.
 */
public class MePinTransactionStatusResponse {

    private String status;

    @SerializedName("status_text")
    private String statusText;

    @SerializedName("transaction_id")
    private String transactionId;

    private String identifier;

    @SerializedName("transaction_status")
    private String transactionStatus;

    private String allow;

    private String signature;

    @SerializedName("signature_source")
    private String signatureSource;

    @SerializedName("confirmation_method")
    private String confirmationMethod;

    @SerializedName("confirmed_at")
    private String confirmedAt;

    @SerializedName("public_key")
    private String publicKey;

    @SerializedName("user_agent_info")
    private String userAgentInfo;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getAllow() {
        return allow;
    }

    public void setAllow(String allow) {
        this.allow = allow;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignatureSource() {
        return signatureSource;
    }

    public void setSignatureSource(String signatureSource) {
        this.signatureSource = signatureSource;
    }

    public String getConfirmationMethod() {
        return confirmationMethod;
    }

    public void setConfirmationMethod(String confirmationMethod) {
        this.confirmationMethod = confirmationMethod;
    }

    public String getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(String confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getUserAgentInfo() {
        return userAgentInfo;
    }

    public void setUserAgentInfo(String userAgentInfo) {
        this.userAgentInfo = userAgentInfo;
    }
}
