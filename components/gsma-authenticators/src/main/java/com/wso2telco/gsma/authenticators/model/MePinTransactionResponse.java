package com.wso2telco.gsma.authenticators.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by isuru on 3/30/17.
 */
public class MePinTransactionResponse {

    @SerializedName("transaction_id")
    private String transactionId;

    @SerializedName("oath_challenge")
    private String oauthChallenge;

    @SerializedName("otp_index")
    private String otpIndexInUse;

    @SerializedName("otp_qty")
    private String otpCodesLeft;

    private String status;

    @SerializedName("status_text")
    private String statusText;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getOauthChallenge() {
        return oauthChallenge;
    }

    public void setOauthChallenge(String oauthChallenge) {
        this.oauthChallenge = oauthChallenge;
    }

    public String getOtpIndexInUse() {
        return otpIndexInUse;
    }

    public void setOtpIndexInUse(String otpIndexInUse) {
        this.otpIndexInUse = otpIndexInUse;
    }

    public String getOtpCodesLeft() {
        return otpCodesLeft;
    }

    public void setOtpCodesLeft(String otpCodesLeft) {
        this.otpCodesLeft = otpCodesLeft;
    }

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

    @Override
    public String toString() {
        return "MePinTransactionResponse{" +
                "otpIndexInUse='" + otpIndexInUse + '\'' +
                ", otpCodesLeft='" + otpCodesLeft + '\'' +
                ", status='" + status + '\'' +
                ", statusText='" + statusText + '\'' +
                '}';
    }
}
