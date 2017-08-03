package com.wso2telco.gsma.authenticators.attributeShare;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.*;


public class ConsentedSP extends AbstractAttributeShare {

    private static Log log = LogFactory.getLog(ConsentedSP.class);

    @Override
    public Map<String,List<String>> getAttributeMap(AuthenticationContext context) throws Exception {

       return super.getAttributeMap(context);
    }
}
