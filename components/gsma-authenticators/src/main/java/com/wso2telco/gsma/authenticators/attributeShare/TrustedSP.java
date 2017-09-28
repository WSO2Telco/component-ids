package com.wso2telco.gsma.authenticators.attributeShare;

import com.wso2telco.gsma.authenticators.Constants;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.*;

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

    }

    @Override
    public Map<String, String> getAttributeShareDetails(AuthenticationContext context) throws Exception {
        String displayScopes = "";
        String isDisplayScope = "false";
        String isTNCForNewUser = "false";
        String authenticationFlowStatus="true";

        Map<String, String> attributeShareDetails = new HashMap();

        context.setProperty(Constants.IS_CONSENT,Constants.YES);

        attributeShareDetails.put("isDisplayScope", isDisplayScope);
        attributeShareDetails.put("displayScopes", displayScopes);
        attributeShareDetails.put("authenticationFlowStatus",authenticationFlowStatus);

        return attributeShareDetails;
    }

}
