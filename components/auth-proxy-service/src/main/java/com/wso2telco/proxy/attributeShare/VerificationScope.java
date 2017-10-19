package com.wso2telco.proxy.attributeShare;

import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;


/**
 * Created by aushani on 8/31/17.
 */
public class VerificationScope extends AbstractAttributeShare {

    @Override
    public void mandatoryFeildValidation() {
         /*this method for do the mandatory field validation in attribute verification*/
    }

    @Override
    public void scopeNClaimMatching() {
       /*this method for do the scope and claim value matching in attribute verification*/
    }

    @Override
    public void shaAlgortithemValidation() {
        /*this method for do the scope and claim value matching in attribute verification*/
    }

    @Override
    public String attShareDetails(String operatorName, String clientId,String loginhintMsisdn,String msisdn) throws AuthenticationFailedException {
        log.debug(" verification scope validation ");

        return getTrsutedStatus(operatorName, clientId,loginhintMsisdn,msisdn);

    }

}
