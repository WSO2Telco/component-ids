package com.wso2telco.entity;

public class UserConsent {

    private String msisdn;
    private String consentExpireTime;
    private String consentStatus;
    private int consentId;
    private String clientId;
    private String operatorName;

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getConsentExpireTime() {
        return consentExpireTime;
    }

    public void setConsentExpireTime(String consentExpireTime) {
        this.consentExpireTime = consentExpireTime;
    }

    public String getConsentStatus() {
        return consentStatus;
    }

    public void setConsentStatus(String consentStatus) {
        this.consentStatus = consentStatus;
    }

    public int getConsentId() {
        return consentId;
    }

    public void setConsentId(int consentId) {
        this.consentId = consentId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
}

