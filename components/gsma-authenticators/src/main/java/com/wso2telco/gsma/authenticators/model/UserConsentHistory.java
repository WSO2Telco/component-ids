package com.wso2telco.gsma.authenticators.model;

/**
 * Created by aushani on 8/8/17.
 */
public class UserConsentHistory {

  private String  msisdn;
    private String client_id;
    private int scope_id;
    private int operator_id;
    private String consent_date;
    private String consent_expire_time;
    private String consent_revoked_time;
    private String consent_status;

    public enum CONSENT_STATUS_TYPES{
        ACTIVE,
        REVOKED
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public int getScope_id() {
        return scope_id;
    }

    public void setScope_id(int scope_id) {
        this.scope_id = scope_id;
    }

    public int getOperator_id() {
        return operator_id;
    }

    public void setOperator_id(int operator_id) {
        this.operator_id = operator_id;
    }

    public String getConsent_date() {
        return consent_date;
    }

    public void setConsent_date(String consent_date) {
        this.consent_date = consent_date;
    }

    public String getConsent_expire_time() {
        return consent_expire_time;
    }

    public void setConsent_expire_time(String consent_expire_time) {
        this.consent_expire_time = consent_expire_time;
    }

    public String getConsent_revoked_time() {
        return consent_revoked_time;
    }

    public void setConsent_revoked_time(String consent_revoked_time) {
        this.consent_revoked_time = consent_revoked_time;
    }

    public String getConsent_status() {
        return consent_status;
    }

    public void setConsent_status(String consent_status) {
        this.consent_status = consent_status;
    }
}
