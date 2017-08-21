package com.wso2telco.gsma.authenticators.attributeShare;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use this class to implement TSP1 related functionality
 * which are not concluded yet.
 */
public class TrustedSP extends AbstractAttributeShare {

    @Override
    public Map<String, List<String>> getAttributeMap(AuthenticationContext context) throws Exception {

        List<String> explicitScopes = new ArrayList();
        List<String> implicitScopes = new ArrayList();
        Map<String, List<String>> scopesList = new HashMap();
        List<String> longlivedScopes = new ArrayList();

        scopesList.put("explicitScopes", explicitScopes);
        scopesList.put("implicitScopes", implicitScopes);
        if (!longlivedScopes.isEmpty()) {
            context.setProperty("longlivedScopes", longlivedScopes.toString());
        }
        return scopesList;


        //return super.getAttributeMap(context);
    }
}
