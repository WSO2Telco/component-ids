package com.wso2telco.gsma.authenticators.attributeShare;

import com.wso2telco.gsma.authenticators.Constants;
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
        String authenticationFlowStatus="false";

        Map<String, List<String>> attributeset = getAttributeMap(context);
        Map<String,String> attributeShareDetails = new HashMap();


        if(!attributeset.get("explicitScopes").isEmpty()){
            isDisplayScope = "true";
            displayScopes = Arrays.toString(attributeset.get("explicitScopes").toArray());
        }

        context.setProperty(Constants.IS_CONSENT,Constants.YES);
        attributeShareDetails.put("isDisplayScope",isDisplayScope);
        attributeShareDetails.put("displayScopes",displayScopes);
        attributeShareDetails.put("authenticationFlowStatus",authenticationFlowStatus);

        return attributeShareDetails;
    }
}
