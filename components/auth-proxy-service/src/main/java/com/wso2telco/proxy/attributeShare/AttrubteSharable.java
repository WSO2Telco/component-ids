package com.wso2telco.proxy.attributeShare;


import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

/**
 * Created by aushani on 8/31/17.
 */
public interface AttrubteSharable {

    String attShareDetails (String operatorName, String clientId,String loginhintMsisdn,String msisdn) throws AuthenticationFailedException;


}
