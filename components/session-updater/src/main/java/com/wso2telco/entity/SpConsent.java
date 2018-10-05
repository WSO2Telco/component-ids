package com.wso2telco.entity;

public class SpConsent {

    private String consumerKey;

    private String operatorId;

    private int scopeId;

    private int expPeriod;

    private String consentType;

    private String validityType;

    private int consentId;


    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getOperatorID() {
        return operatorId;
    }

    public void setOperatorID(String operatorID) {
        this.operatorId = operatorId;
    }

    public int getScope() {
        return scopeId;
    }

    public void setScope(int scope) {
        this.scopeId = scope;
    }

    public int getExpPeriod() {
        return expPeriod;
    }

    public void setExpPeriod(int expPeriod) {
        this.expPeriod = expPeriod;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    public String getValidityType() {
        return validityType;
    }

    public void setValidityType(String validityType) {
        this.validityType = validityType;
    }

    public int getConsentId() {
        return consentId;
    }

    public void setConsentId(int consentId) {
        this.consentId = consentId;
    }
}
