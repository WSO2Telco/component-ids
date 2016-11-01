package com.wso2telco.proxy.model;

/**
 * RedirectUrlInfo is using to construct redirect url.
 */
public class RedirectUrlInfo {
    private String queryString;
    private String authorizeUrl;
    private String operatorName;
    private String msisdnHeader;
    private String ipAddress;
    private String telcoScope;

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getTelcoScope() {
        return telcoScope;
    }

    public void setTelcoScope(String telcoScope) {
        this.telcoScope = telcoScope;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMsisdnHeader() {
        return msisdnHeader;
    }

    public void setMsisdnHeader(String msisdnHeader) {
        this.msisdnHeader = msisdnHeader;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public void setAuthorizeUrl(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }
}
