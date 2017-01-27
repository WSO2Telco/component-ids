package com.wso2telco.ids.datapublisher.model;

import java.sql.Timestamp;

public class UserStatus {


    private int id ;
    private Timestamp time;
    private String status;
    private String  msisdn;
    private String  state;
    private String  nonce;
    private String  scope;
    private String  acrValue;
    private String sessionId;
    private int isMsisdnHeader;
    private String ipHeader;
    private int isNewUser;
    private String loginHint;
    private String  operator;
    private String userAgent;
    private String comment;
    private String consumerKey;
    private String appId;
    private String telcoScope;
    private String xForwardIP;

    public String getxForwardIP() {
        return xForwardIP;
    }

    public void setxForwardIP(String xForwardIP) {
        this.xForwardIP = xForwardIP;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAcrValue() {
        return acrValue;
    }

    public void setAcrValue(String acrValue) {
        this.acrValue = acrValue;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }


    public String getIpHeader() {
        return ipHeader;
    }

    public void setIpHeader(String ipHeader) {
        this.ipHeader = ipHeader;
    }


    public String getLoginHint() {
        return loginHint;
    }

    public void setLoginHint(String loginHint) {
        this.loginHint = loginHint;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getIsMsisdnHeader() {
        return isMsisdnHeader;
    }

    public void setIsMsisdnHeader(int isMsisdnHeader) {
        this.isMsisdnHeader = isMsisdnHeader;
    }

    public int getIsNewUser() {
        return isNewUser;
    }

    public void setIsNewUser(int isNewUser) {
        this.isNewUser = isNewUser;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getTelcoScope() {
        return telcoScope;
    }

    public void setTelcoScope(String telcoScope) {
        this.telcoScope = telcoScope;
    }
}
