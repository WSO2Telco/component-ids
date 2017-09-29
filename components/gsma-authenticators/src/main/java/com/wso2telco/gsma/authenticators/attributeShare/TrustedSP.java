package com.wso2telco.gsma.authenticators.attributeShare;

import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.authenticators.util.UserProfileManager;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;

import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

/**
 * Use this class to implement TSP1 related functionality
 * which are not concluded yet.
 */
public class TrustedSP extends AbstractAttributeShare {

    @Override
    public Map<String, List<String>> getAttributeMap(AuthenticationContext context) throws SQLException, NamingException  {

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
    public Map<String, String> getAttributeShareDetails(AuthenticationContext context) throws SQLException, NamingException,AuthenticationFailedException {

        String displayScopes = "";
        String isDisplayScope = "false";
        String authenticationFlowStatus="true";

        String msisdn = context.getProperty(Constants.MSISDN).toString();
        String operator = context.getProperty(Constants.OPERATOR).toString();
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
        boolean isAttributeScope = (Boolean)context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE);
        Map<String, String> attributeShareDetails = new HashMap();

        context.setProperty(Constants.IS_CONSENTED,Constants.YES);

        attributeShareDetails.put("isDisplayScope", isDisplayScope);
        attributeShareDetails.put("displayScopes", displayScopes);
        attributeShareDetails.put("authenticationFlowStatus",authenticationFlowStatus);

        try {
            if(isRegistering){
                int requestedLoa = Integer.parseInt(context.getProperty(Constants.ACR).toString());
                if(requestedLoa == 2){
                    new UserProfileManager().createUserProfileLoa2(msisdn, operator,isAttributeScope);
                }
            }
        } catch (RemoteException | UserRegistrationAdminServiceIdentityException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }

        AuthenticationContextHelper.setSubject(context, context.getProperty(Constants.MSISDN).toString());
        context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
        return attributeShareDetails;
    }

}
