package com.wso2telco.gsma.authenticators.attributeShare;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aushani on 8/24/17.
 */
public class NormalSP extends AbstractAttributeShare {
    @Override
    public Map<String, List<String>> getAttributeMap(AuthenticationContext context) throws Exception {
        return super.getAttributeMap(context);
    }

    @Override
    public Map<String, String> getAttributeShareDetails(AuthenticationContext context) throws Exception {
        String displayScopes = "";
        String isDisplayScope = "false";
        String isTNCForNewUser ="false";

        Map<String, List<String>> attributeset = getAttributeMap(context);
        Map<String,String> attributeShareDetails = new HashMap();


        if(!attributeset.get("explicitScopes").isEmpty()){
            isDisplayScope = "true";
            isTNCForNewUser = "true";
            displayScopes = Arrays.toString(attributeset.get("explicitScopes").toArray());
        }
        attributeShareDetails.put("isDisplayScope",isDisplayScope);
        attributeShareDetails.put("isTNCForNewUser",isTNCForNewUser);
        attributeShareDetails.put("displayScopes",displayScopes);

        return attributeShareDetails;
    }
}
