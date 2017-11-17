package com.wso2telco.gsma.authenticators.model;

/**
 * Created by aushani on 8/8/17.
 */
public class UserConsentHistory {

  private String  msisdn;
    private String consentExpireTime;
    private String consentStatus;
    private int consentId;


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
}
