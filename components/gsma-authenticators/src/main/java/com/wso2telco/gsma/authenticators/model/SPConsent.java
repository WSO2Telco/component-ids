package com.wso2telco.gsma.authenticators.model;

/**
 * Created by aushani on 7/26/17.
 */
public class SPConsent {

    private String consumerKey;

    private int operatorID;

    private String scope;

    private int expPeriod;

    private String consentType;

    private String validityType;


    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public int getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(int operatorID) {
        this.operatorID = operatorID;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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
}
