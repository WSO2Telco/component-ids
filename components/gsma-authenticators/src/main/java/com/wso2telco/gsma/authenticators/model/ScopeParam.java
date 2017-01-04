package com.wso2telco.gsma.authenticators.model;

public class ScopeParam {

    public enum msisdnMismatchResultTypes {
        ERROR_RETURN,
        OFFNET_FALLBACK
    }

    public enum loginHintFormat {
        PLAINTEXT,
        ENCRYPTED;
    }

    private boolean isLoginHintMandatory;
    private loginHintFormat loginHintFormat;
    private boolean isTncVisible;
    private msisdnMismatchResultTypes msisdnMismatchResult;

    public void setLoginHintFormat(ScopeParam.loginHintFormat loginHintFormat) {
        this.loginHintFormat = loginHintFormat;
    }

    public ScopeParam.loginHintFormat getLoginHintFormat() {
        return loginHintFormat;
    }

    public void setLoginHintMandatory(boolean isLoginHintMandatory) {
        this.isLoginHintMandatory = isLoginHintMandatory;
    }

    public boolean isLoginHintMandatory() {
        return isLoginHintMandatory;
    }

    public void setMsisdnMismatchResult(msisdnMismatchResultTypes msisdnMismatchResult) {
        this.msisdnMismatchResult = msisdnMismatchResult;
    }

    public msisdnMismatchResultTypes getMsisdnMismatchResult() {
        return msisdnMismatchResult;
    }

    public void setTncVisible(boolean isTncVisible) {
        this.isTncVisible = isTncVisible;
    }

    public boolean isTncVisible() {
        return isTncVisible;
    }
}
