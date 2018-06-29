package com.wso2telco.serviceprovider.provision.model;

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
    private String api;
    private String scopes;
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

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public boolean isTrustedServiceProvider() {
        return trustedServiceProvider;
    }

    public void setTrustedServiceProvider(boolean trustedServiceProvider) {
        this.trustedServiceProvider = trustedServiceProvider;
    }

    @Override
    public String toString() {
        return String.format("RequestParamInfo [userName: %s\n" +
                "applicationName: %s\n" +
                "firstName: %s\n" +
                "lastName: %s\n" +
                "developerEmail: %s\n" +
                "callbackUrl: %s\n" +
                "applicationTier: %s\n" +
                "newConsumerKey: %s\n" +
                "newConsumerSecret: %s\n" +
                "api: %s\n" +
                "scopes: %s\n" +
                "trustedServiceProvider: %s]\n",
                userName,
                applicationName,
                firstName,
                lastName,
                developerEmail,
                callbackUrl,
                applicationTier,
                newConsumerKey,
                newConsumerSecret,
                api,
                scopes,
                trustedServiceProvider);
    }
}
