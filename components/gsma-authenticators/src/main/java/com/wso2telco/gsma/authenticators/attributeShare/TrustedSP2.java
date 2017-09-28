package com.wso2telco.gsma.authenticators.attributeShare;

import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.dao.AttributeConfigDAO;
import com.wso2telco.gsma.authenticators.dao.impl.AttributeConfigDAOimpl;
import com.wso2telco.gsma.authenticators.model.SPConsent;
import com.wso2telco.gsma.authenticators.model.UserConsentHistory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class TrustedSP2 extends AbstractAttributeShare {

    private static Log log = LogFactory.getLog(TrustedSP2.class);

    @Override
    public Map<String, List<String>> getAttributeMap(AuthenticationContext context) throws Exception {

       return super.getAttributeMap(context);
    }

    @Override
    public Map<String, String> getAttributeShareDetails(AuthenticationContext context) throws Exception {

        String displayScopes = "";
        String isDisplayScope = "false";
        String isTNCForNewUser ="false";
        String authenticationFlowStatus="false";


        Map<String, List<String>> attributeset = getAttributeMap(context);
        Map<String,String> attributeShareDetails = new HashMap();


        if(!attributeset.get(Constants.EXPLICIT_SCOPES).isEmpty()){
            isDisplayScope = "true";
            isTNCForNewUser = "false";
            displayScopes = Arrays.toString(attributeset.get(Constants.EXPLICIT_SCOPES).toArray());
        }

        context.setProperty(Constants.IS_CONSENT,Constants.YES);
        attributeShareDetails.put(Constants.IS_DISPLAYSCOPE,isDisplayScope);
        attributeShareDetails.put("authenticationFlowStatus",authenticationFlowStatus);
        attributeShareDetails.put(Constants.DISPLAY_SCOPES,displayScopes);

        return attributeShareDetails;

    }


    public static void persistConsentedScopeDetails(AuthenticationContext context) throws Exception {

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
