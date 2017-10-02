package com.wso2telco.gsma.authenticators.attributeShare;

import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.dao.AttributeConfigDAO;
import com.wso2telco.gsma.authenticators.dao.impl.AttributeConfigDAOimpl;
import com.wso2telco.gsma.authenticators.model.SPConsent;
import com.wso2telco.gsma.authenticators.model.UserConsentHistory;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.authenticators.util.UserProfileManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;

import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class TrustedSP2 extends AbstractAttributeShare {

    private static Log log = LogFactory.getLog(TrustedSP2.class);


    @Override
    public Map<String, String> getAttributeShareDetails(AuthenticationContext context) throws SQLException, NamingException,AuthenticationFailedException {

        String displayScopes = "";
        String isDisplayScope = "false";
        String authenticationFlowStatus="false";
        String isTNCForNewUser="false";


        Map<String, List<String>> attributeset = getAttributeMap(context);
        Map<String,String> attributeShareDetails = new HashMap();
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
        String msisdn = context.getProperty(Constants.MSISDN).toString();
        String operator = context.getProperty(Constants.OPERATOR).toString();
        boolean isAttributeScope = (Boolean)context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE);


        if(!attributeset.get(Constants.EXPLICIT_SCOPES).isEmpty()){
            isDisplayScope = "true";
            displayScopes = Arrays.toString(attributeset.get(Constants.EXPLICIT_SCOPES).toArray());
            log.debug("Found the explicite scopes to gt the consent" + displayScopes );
        }

        try {
            if(isRegistering){
                int requestedLoa = Integer.parseInt(context.getProperty(Constants.ACR).toString());
                if(requestedLoa == 2){
                    new UserProfileManager().createUserProfileLoa2(msisdn, operator,isAttributeScope);
                }
                AuthenticationContextHelper.setSubject(context, context.getProperty(Constants.MSISDN).toString());
                context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
                context.setProperty(Constants.AUTHENTICATED_USER,"true");
            }
        } catch (RemoteException | UserRegistrationAdminServiceIdentityException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
        context.setProperty(Constants.IS_CONSENTED,Constants.YES);
        attributeShareDetails.put(Constants.IS_DISPLAYSCOPE,isDisplayScope);
        attributeShareDetails.put(Constants.IS_AUNTHENTICATION_CONTINUE,authenticationFlowStatus);
        attributeShareDetails.put(Constants.DISPLAY_SCOPES,displayScopes);
        attributeShareDetails.put(Constants.IS_TNC,isTNCForNewUser);

        return attributeShareDetails;

    }


    public static void persistConsentedScopeDetails(AuthenticationContext context) throws SQLException,NamingException {

        AttributeConfigDAO attributeConfigDAO = new AttributeConfigDAOimpl();

        String msisdn = context.getProperty(Constants.MSISDN).toString();
        String operator = context.getProperty(Constants.OPERATOR).toString();
        String clientId = context.getProperty(Constants.CLIENT_ID).toString();

        List<SPConsent> spConsentDetailsList = attributeConfigDAO.getScopeExprieTime(operator,clientId,context.getProperty(Constants.LONGLIVEDSCOPES).toString());
        List<UserConsentHistory> userConsentHistoryList = new ArrayList();

        for(SPConsent spConsent: spConsentDetailsList){
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date today = new Date();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DATE, spConsent.getExpPeriod());

            UserConsentHistory userConsentHistory = new UserConsentHistory();
            userConsentHistory.setClient_id(clientId);
            userConsentHistory.setOperator_id(spConsent.getOperatorID());
            userConsentHistory.setMsisdn(msisdn);
            userConsentHistory.setConsent_date(dateFormat.format(today));
            userConsentHistory.setScope_id(spConsent.getScope());
            userConsentHistory.setConsent_expire_time(dateFormat.format(calendar.getTime()));
            userConsentHistory.setConsent_status(UserConsentHistory.CONSENT_STATUS_TYPES.ACTIVE.name());

            userConsentHistoryList.add(userConsentHistory);
        }
        attributeConfigDAO.saveUserConsentedAttributes(userConsentHistoryList);

    }


}
