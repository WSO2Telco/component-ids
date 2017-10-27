package com.wso2telco.gsma.authenticators.attributeShare;

import com.wso2telco.core.config.model.ScopeDetailsConfig;
import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtil;
import com.wso2telco.gsma.authenticators.attributeShare.internal.UserConsentStatus;
import com.wso2telco.gsma.authenticators.attributeShare.internal.ValidityType;
import com.wso2telco.gsma.authenticators.dao.AttributeConfigDAO;
import com.wso2telco.gsma.authenticators.dao.impl.AttributeConfigDAOimpl;
import com.wso2telco.gsma.authenticators.internal.AuthenticatorEnum;
import com.wso2telco.gsma.authenticators.model.SPConsent;
import com.wso2telco.gsma.authenticators.model.UserConsentDetails;
import com.wso2telco.gsma.authenticators.model.UserConsentHistory;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
public abstract class AbstractAttributeShare implements AttributeSharable {

    private static Log log = LogFactory.getLog(AbstractAttributeShare.class);
    private static ScopeDetailsConfig scopeDetailsConfigs = null;
    private static Map<String, ScopeDetailsConfig.Scope> scopeMap = null;
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    static {
        //Load scope-config.xml file.
        scopeDetailsConfigs = configurationService.getDataHolder().getScopeDetailsConfig();

        //Load scope related request optional parameters.
        scopeMap = new HashMap<>();
        List<ScopeDetailsConfig.Scope> scopes = scopeDetailsConfigs.getPremiumScopes();

        for (ScopeDetailsConfig.Scope sc : scopes) {
            scopeMap.put(sc.getName(), sc);
        }
    }

    public Map<String, List<String>> getAttributeMap(AuthenticationContext context) throws SQLException, NamingException {

        List<String> explicitScopes = new ArrayList();
        List<String> implicitScopes = new ArrayList();
        List<String> noConsentScopes = new ArrayList();
        Map<String, List<String>> scopesList = new HashMap();
        List<String> longlivedScopes = new ArrayList();
        AttributeConfigDAO attributeConfigDAO = new AttributeConfigDAOimpl();
        List<ScopeParam> scopeParamList = attributeConfigDAO.getScopeParams(context.getProperty(Constants.TELCO_SCOPE).toString());

        for (ScopeParam scopeParam : scopeParamList) {
            String consentType = scopeParam.getConsentType();
            String validityType = scopeParam.getConsentValidityType();
            String scope = scopeParam.getScope();
            Map<String, String> validityMap = getValiditeProcess(context, validityType, scope);

            if (consentType.equalsIgnoreCase(AuthenticatorEnum.ConsentType.EXPLICIT.name()) && "true".equalsIgnoreCase(validityMap.get(Constants.IS_CONSENT))) {
                explicitScopes = getScopestoDisplay(explicitScopes,scope);
                if (validityMap.get(Constants.VALIDITY_TYPE).equalsIgnoreCase(ValidityType.LONG_LIVE.name())) {
                    longlivedScopes.add(scope);
                }

            } else if (consentType.equalsIgnoreCase(AuthenticatorEnum.ConsentType.IMPLICIT.name()) && "true".equalsIgnoreCase(validityMap.get(Constants.IS_CONSENT))){
                implicitScopes.add(scope);
            }else if (consentType.equalsIgnoreCase(AuthenticatorEnum.ConsentType.NOCONSENT.name())){
                noConsentScopes.add(scope);
            }
        }
        scopesList.put(Constants.EXPLICIT_SCOPES, explicitScopes);
        scopesList.put(Constants.IMPLICIT_SCOPES, implicitScopes);
        scopesList.put(Constants.NO_CONSENT_SCOPES,noConsentScopes);
        if (!longlivedScopes.isEmpty()) {
            context.setProperty(Constants.LONGLIVEDSCOPES, longlivedScopes.toString());
        }
        return scopesList;
    }

    private UserConsentDetails getUserConsentDetails(AuthenticationContext context, String scope) throws SQLException, NamingException {
        AttributeConfigDAO attributeConfigDAO = new AttributeConfigDAOimpl();
        UserConsentDetails userConsentDetails = new UserConsentDetails();
        userConsentDetails.setConsumerKey(context.getProperty(Constants.CLIENT_ID).toString());
        userConsentDetails.setOperatorID((DBUtil.getOperatorDetails(context.getProperty(Constants.OPERATOR).toString())).getOperatorId());
        userConsentDetails.setScope(scope);
        userConsentDetails.setMsisdn(context.getProperty(Constants.MSISDN).toString());
        return attributeConfigDAO.getUserConsentDetails(userConsentDetails);

    }

    private Map<String, String> getValiditeProcess(AuthenticationContext context, String validityTyp, String scope) throws SQLException, NamingException {

        ValidityType validityType = ValidityType.get(validityTyp);
        Map<String, String> valityMap = new HashMap();
        switch (validityType) {

            case TRANSACTIONAL:
                valityMap.put(Constants.VALIDITY_TYPE, ValidityType.TRANSACTIONAL.name());
                valityMap.put(Constants.IS_CONSENT, "true");
                return valityMap;

            case LONG_LIVE:

                valityMap.put(Constants.VALIDITY_TYPE, ValidityType.LONG_LIVE.name());
                if (isLongLiveConsent(context, scope)) {
                    valityMap.put(Constants.IS_CONSENT, "true");
                } else {
                    valityMap.put(Constants.IS_CONSENT, "false");
                }
                return valityMap;

            default:
                valityMap.put(Constants.VALIDITY_TYPE, ValidityType.UNDEFINED.name());
                valityMap.put(Constants.IS_CONSENT, "false");
                return valityMap;
        }

    }

    private boolean isLongLiveConsent(AuthenticationContext context, String scope) throws SQLException, NamingException {

        boolean isConsent = false;

        try {

            UserConsentDetails userConsentDetails = getUserConsentDetails(context, scope);
            if (userConsentDetails == null) {
                isConsent = true;

            } else {

                if (userConsentDetails.getRevokeStatus().equalsIgnoreCase(UserConsentStatus.ACTIVE.name())) {
                    Date today = new Date();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                    if (today.after(dateFormat.parse(userConsentDetails.getConsentExpireDatetime()))) {
                        isConsent = true;
                    }

                } else if (userConsentDetails.getRevokeStatus().equalsIgnoreCase(UserConsentStatus.REVOKED.name())) {
                    isConsent = true;
                }

            }


        } catch (SQLException|NamingException e) {
            log.debug("error occurred while accessing the database table" + e);

        } catch (ParseException e) {
            log.debug("error occurred while formatting the date");
        }
        return isConsent;
    }

    public static List<String> getScopestoDisplay(List<String> attributeSet, String scope) {

        List<String> consentAttribute = attributeSet;
        List<String> displayAttributeSet ;

        displayAttributeSet = scopeMap.get(scope).getDisplayAttributes();
        for (int j = 0; j < displayAttributeSet.size(); j++) {
            if (!consentAttribute.contains(displayAttributeSet.get(j))) {
                consentAttribute.add(displayAttributeSet.get(j));
            }
        }
        return consentAttribute;
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

    public static void createUserProfile(AuthenticationContext context) throws AuthenticationFailedException{

        String msisdn = context.getProperty(Constants.MSISDN).toString();
        String operator = context.getProperty(Constants.OPERATOR).toString();
        boolean isAttributeScope = (Boolean)context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE);
        String spType = context.getProperty(Constants.TRUSTED_STATUS).toString();
        String attrShareType = context.getProperty(Constants.ATTRSHARE_SCOPE_TYPE).toString();

        try {
                new UserProfileManager().createUserProfileLoa2(msisdn, operator,isAttributeScope,spType,attrShareType);

        } catch (RemoteException | UserRegistrationAdminServiceIdentityException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }
}
