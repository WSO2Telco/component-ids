package com.wso2telco.gsma.authenticators.model;

/**
 * Created on 7/28/17.
 */
public class UserConsentDetails {

    private String msisdn;

    private String consumerKey;

    private String operatorName;

    private String scope;

    private String revokeStatus;

    private String consentExpireDatetime;
    private String consentRevokeDatetime;

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRevokeStatus() {
        return revokeStatus;
    }

    public void setRevokeStatus(String revokeStatus) {
        this.revokeStatus = revokeStatus;
    }

    public String getConsentExpireDatetime() {
        return consentExpireDatetime;
    }

    public void setConsentExpireDatetime(String consentExpireDatetime) {
        this.consentExpireDatetime = consentExpireDatetime;
    }

    public String getConsentRevokeDatetime() {
        return consentRevokeDatetime;
    }

    public void setConsentRevokeDatetime(String consentRevokeDatetime) {
        this.consentRevokeDatetime = consentRevokeDatetime;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
}
