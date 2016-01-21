package com.mycompany.mavenproject1;


public class AuthenticationData {

    private  String tokenID;
    private  String clientId;
    private  String redirectUri;
    private  String scope;
    private  String type;
    private  String authenticators;
    private int acrValues;
    private  String responseType;
    private int status;

    public int getStatus() {
        return status;

    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAuthenticators() {
        return authenticators;
    }

    public void setAuthenticators(String authenticators) {
        this.authenticators = authenticators;
    }

    public int getAcrValues() {
        return acrValues;
    }

    public void setAcrValues(int acrValues) {
        this.acrValues = acrValues;
    }

}
