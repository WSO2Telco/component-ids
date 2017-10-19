package com.wso2telco.proxy.attributeShare;

import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;


/**
 * Created by aushani on 8/31/17.
 */
public class ProvisionScope extends  AbstractAttributeShare{
    

    @Override
    public void mandatoryFeildValidation() {
      /*mandatory feild validation in next phase*/
    }

    @Override
    public void scopeNClaimMatching() {
     /*
     * this should be implemented in the next phase*/
    }

    @Override
    public void shaAlgortithemValidation() {
        /*
     * this should be implemented in the next phase*/
    }

    @Override
    public String attShareDetails(String operatorName, String clientId,String loginhintMsisdn,String msisdn) throws AuthenticationFailedException {

        return getTrsutedStatus(operatorName, clientId,loginhintMsisdn,msisdn);
    }
}
