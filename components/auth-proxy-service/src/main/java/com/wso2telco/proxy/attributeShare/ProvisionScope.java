package com.wso2telco.proxy.attributeShare;

import com.wso2telco.proxy.util.AuthProxyEnum;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import java.util.Map;

/**
 * Created by aushani on 8/31/17.
 */
public class ProvisionScope extends  AbstractAttributeShare{
    

    @Override
    public void mandatoryFeildValidation() {

    }

    @Override
    public void scopeNClaimMatching() {
     /*ToDo
     * this should be implemented in the next phase*/
    }

    @Override
    public void shaAlgortithemValidation() {

    }

    @Override
    public String attShareDetails(String operatorName, String clientId,String loginhint_msisdn,String msisdn) throws AuthenticationFailedException {

        return getTrsutedStatus(operatorName, clientId,loginhint_msisdn,msisdn);
    }
}
