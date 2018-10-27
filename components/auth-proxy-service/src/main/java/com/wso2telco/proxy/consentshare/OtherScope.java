package com.wso2telco.proxy.consentshare;

import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

public class OtherScope extends  AbstractConsentShare{
    @Override
    public void mandatoryFieldValidation() {

    }

    @Override
    public void scopeAndClaimMatching() {

    }

    @Override
    public void shaAlgorithmValidation() {

    }

    @Override
    public String getConsentShareDetails(String operatorName, String clientId, String loginHintMsisdn, String msisdn) throws AuthenticationFailedException {
        return getTrustedStatus(operatorName, clientId, loginHintMsisdn, msisdn);
    }
}
