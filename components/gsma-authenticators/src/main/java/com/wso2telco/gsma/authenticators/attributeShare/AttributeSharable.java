package com.wso2telco.gsma.authenticators.attributeShare;


import com.wso2telco.gsma.authenticators.model.SPConsent;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.List;
import java.util.Map;

/**
 * Created by aushani on 7/30/17.
 */
public interface AttributeSharable {

     Map<String,List<String>> getAttributeMap(AuthenticationContext context) throws Exception;

    public Map<String,String> getAttributeShareDetails(AuthenticationContext context) throws Exception;



}
