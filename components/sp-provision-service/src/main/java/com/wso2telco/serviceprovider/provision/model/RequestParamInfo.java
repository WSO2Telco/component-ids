package com.wso2telco.serviceprovider.provision.model;

import java.util.List;

public class RequestParamInfo {
    private String userName;
    private String applicationName;
    private String firstName;
    private String lastName;
    private String developerEmail;
    private String callbackUrl;
    private String applicationTier;
    private String newConsumerKey;
    private String newConsumerSecret;
    private List<String> api;
    private List<String> scopes;
    private boolean trustedServiceProvider;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDeveloperEmail() {
        return developerEmail;
    }

    public void setDeveloperEmail(String developerEmail) {
        this.developerEmail = developerEmail;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getApplicationTier() {
        return applicationTier;
    }

    public void setApplicationTier(String applicationTier) {
        this.applicationTier = applicationTier;
    }

    public String getNewConsumerKey() {
        return newConsumerKey;
    }

    public void setNewConsumerKey(String newConsumerKey) {
        this.newConsumerKey = newConsumerKey;
    }

    public String getNewConsumerSecret() {
        return newConsumerSecret;
    }

    public void setNewConsumerSecret(String newConsumerSecret) {
        this.newConsumerSecret = newConsumerSecret;
    }

    public List<String> getApi() {
        return api;
    }

    public void setApi(List<String> api) {
        this.api = api;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public boolean isTrustedServiceProvider() {
        return trustedServiceProvider;
    }

    public void setTrustedServiceProvider(boolean trustedServiceProvider) {
        this.trustedServiceProvider = trustedServiceProvider;
    }
}
