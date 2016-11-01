package com.wso2telco.proxy.model;

import java.util.Date;

/**
 * This class is using to keep operators' properties.
 */
public class Operator {
    private int operatorId;
    private String operatorName;
    private String description;
    private String createdUser;
    private Date createdDate;
    private String lastUpdatedUser;
    private Date lastUpdatedDate;
    private String refreshToken;
    private Double tokenValidity;
    private Double tokenTime;
    private String token;
    private String tokenUrl;
    private String tokenAuth;
    private boolean isRequiredIpValidation;
    private String ipHeader;

    public String getIpHeader() {
        return ipHeader;
    }

    public void setIpHeader(String ipHeader) {
        this.ipHeader = ipHeader;
    }

    public int getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(int operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public String getLastUpdatedUser() {
        return lastUpdatedUser;
    }

    public void setLastUpdatedUser(String lastUpdatedUser) {
        this.lastUpdatedUser = lastUpdatedUser;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public Double getTokenValidity() {
        return tokenValidity;
    }

    public void setTokenValidity(Double tokenValidity) {
        this.tokenValidity = tokenValidity;
    }

    public Double getTokenTime() {
        return tokenTime;
    }

    public void setTokenTime(Double tokenTime) {
        this.tokenTime = tokenTime;
    }

    public String getTokenAuth() {
        return tokenAuth;
    }

    public void setTokenAuth(String tokenAuth) {
        this.tokenAuth = tokenAuth;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public boolean isRequiredIpValidation() {
        return isRequiredIpValidation;
    }

    public void setRequiredIpValidation(boolean requiredIpValidation) {
        isRequiredIpValidation = requiredIpValidation;
    }
}
