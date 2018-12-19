package com.wso2telco.commonutils;

import com.wso2telco.dbutils.DataBaseConnectUtils;
import com.wso2telco.exception.CommonAuthenticatorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.apimgt.keymgt.token.JWTGenerator;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Map;

public class CustomToknGenerator extends JWTGenerator{

    private static Log log = LogFactory.getLog(CustomToknGenerator.class);

    public Map<String, String> populateStandardClaims(TokenValidationContext validationContext)
            throws APIManagementException {
        Map<String, String> claims = super.populateStandardClaims(validationContext);
        boolean isApplicationToken =
                validationContext.getValidationInfoDTO().getUserType().equalsIgnoreCase(APIConstants.ACCESS_TOKEN_USER_TYPE_APPLICATION) ? true : false;
        String dialect = getDialectURI();
        if (claims.get(dialect + "/enduser") != null) {
            if (isApplicationToken) {
                claims.put(dialect + "/enduser", "null");
                claims.put(dialect + "/enduserTenantId", "null");
            } else {
                String enduser = claims.get(dialect + "/enduser");
                if (enduser.endsWith("@carbon.super")) {
                    enduser = enduser.replace("@carbon.super", "");
                    claims.put(dialect + "/enduser", enduser);
                }
            }
        }

        return claims;

    }

    public Map<String, String> populateCustomClaims(TokenValidationContext validationContext) throws APIManagementException {
        Map<String,String> customClaims = new HashMap<String, String>();
        String trustedstatus = "";
        String mobile = validationContext.getValidationInfoDTO().getEndUserName();
        try {
            trustedstatus = DataBaseConnectUtils.getTrustedStatus(validationContext.getValidationInfoDTO().getConsumerKey());
        }catch (CommonAuthenticatorException | ConfigurationException e) {
            log.error("Error occurred while getting trusted status");
            throw new APIManagementException(e);
        }
        if(!trustedstatus.equals("")) {
            customClaims.put("trustedstatus", trustedstatus);
            if (mobile.endsWith("@carbon.super")) {
                mobile = mobile.replace("@carbon.super", "");
            }
            customClaims.put("mobile", mobile);
        }

        return customClaims;
    }

}
