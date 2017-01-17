package com.wso2telco.openid.extension.scope;

public class ScopeValidationResult {

    private boolean isValidationFail;
    private String validationFailMessage;
    private String redirectionURL;

    public ScopeValidationResult(boolean isValidationFail, String redirectionURL) {
        this.isValidationFail = isValidationFail;
        this.redirectionURL = redirectionURL;
    }

    public  ScopeValidationResult(boolean isValidationFail){
        this.isValidationFail = isValidationFail;
    }

    public boolean isValidationFail() {
        return isValidationFail;
    }

    public void setIsValidationFail(boolean isValidationFail) {
        this.isValidationFail = isValidationFail;
    }

    public String getValidationFailMessage() {
        return validationFailMessage;
    }

    public void setValidationFailMessage(String validationFailMessage) {
        this.validationFailMessage = validationFailMessage;
    }

    public String getRedirectionURL() {
        return redirectionURL;
    }

    public void setRedirectionURL(String redirectionURL) {
        this.redirectionURL = redirectionURL;
    }
}
