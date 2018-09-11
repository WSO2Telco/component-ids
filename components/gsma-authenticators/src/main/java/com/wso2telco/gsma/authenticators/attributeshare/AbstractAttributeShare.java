/*******************************************************************************
 * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.gsma.authenticators.attributeshare;

import com.wso2telco.core.config.model.ScopeDetailsConfig;
import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.attributeshare.internal.ValidityType;
import com.wso2telco.gsma.authenticators.dao.AttributeConfigDao;
import com.wso2telco.gsma.authenticators.dao.impl.AttributeConfigDaoImpl;
import com.wso2telco.gsma.authenticators.internal.AuthenticatorEnum;
import com.wso2telco.gsma.authenticators.model.SpConsent;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public Map<String, List<String>> getAttributeMap(AuthenticationContext context) throws NamingException,
            DBUtilException {

        List<String> explicitScopes = new ArrayList();
        List<String> implicitScopes = new ArrayList();
        List<String> noConsentScopes = new ArrayList();
        Map<String, List<String>> scopesList = new HashMap();
        List<String> longLivedScopes = new ArrayList();
        AttributeConfigDao attributeConfigDao = new AttributeConfigDaoImpl();
        String operator = context.getProperty(Constants.OPERATOR).toString();
        String clientId = context.getProperty(Constants.CLIENT_ID).toString();
        List<ScopeParam> scopeParamList = attributeConfigDao.getScopeParams(context.getProperty(Constants
                .TELCO_SCOPE).toString(), operator, clientId);

        for (ScopeParam scopeParam : scopeParamList) {
            String consentType = scopeParam.getConsentType();
            String validityType = scopeParam.getConsentValidityType();
            String scope = scopeParam.getScope();
            Map<String, String> validityMap = getValidateProcess(context, validityType, scope);

            if (consentType.equalsIgnoreCase(AuthenticatorEnum.ConsentType.EXPLICIT.name()) && "true"
                    .equalsIgnoreCase(validityMap.get(Constants.IS_CONSENT))) {
                explicitScopes = getScopesToDisplay(explicitScopes, scope);
                if (validityMap.get(Constants.VALIDITY_TYPE).equalsIgnoreCase(ValidityType.LONG_LIVE.name())) {
                    longLivedScopes.add(scope);
                }

            } else if (consentType.equalsIgnoreCase(AuthenticatorEnum.ConsentType.IMPLICIT.name()) && "true"
                    .equalsIgnoreCase(validityMap.get(Constants.IS_CONSENT))) {
                implicitScopes.add(scope);
            } else if (consentType.equalsIgnoreCase(AuthenticatorEnum.ConsentType.NOCONSENT.name())) {
                noConsentScopes.add(scope);
            }
        }
        scopesList.put(Constants.EXPLICIT_SCOPES, explicitScopes);
        scopesList.put(Constants.IMPLICIT_SCOPES, implicitScopes);
        scopesList.put(Constants.NO_CONSENT_SCOPES, noConsentScopes);
        if (!longLivedScopes.isEmpty()) {
            context.setProperty(Constants.LONGLIVEDSCOPES, longLivedScopes.toString().replaceAll(", ", ","));
        }
        return scopesList;
    }

    private UserConsentDetails getUserConsentDetails(AuthenticationContext context, String scope) throws
            NamingException, DBUtilException {
        AttributeConfigDao attributeConfigDao = new AttributeConfigDaoImpl();
        UserConsentDetails userConsentDetails = new UserConsentDetails();
        userConsentDetails.setOperatorName(context.getProperty(Constants.OPERATOR).toString());
        userConsentDetails.setConsumerKey(context.getProperty(Constants.CLIENT_ID).toString());
        userConsentDetails.setScope(scope);
        userConsentDetails.setMsisdn(context.getProperty(Constants.MSISDN).toString());
        return attributeConfigDao.getUserConsentDetails(userConsentDetails);

    }

    private Map<String, String> getValidateProcess(AuthenticationContext context, String validityType, String scope)
            throws NamingException, DBUtilException {

        ValidityType validityTypeValue = ValidityType.get(validityType);
        Map<String, String> valityMap = new HashMap();
        switch (validityTypeValue) {

            case TRANSACTIONAL:
                valityMap.put(Constants.VALIDITY_TYPE, ValidityType.TRANSACTIONAL.name());
                valityMap.put(Constants.IS_CONSENT, "true");
                break;
            case LONG_LIVE:

                valityMap.put(Constants.VALIDITY_TYPE, ValidityType.LONG_LIVE.name());
                if (isLongLiveConsent(context, scope)) {
                    valityMap.put(Constants.IS_CONSENT, "true");
                } else {
                    valityMap.put(Constants.IS_CONSENT, "false");
                }
                break;
            default:
                valityMap.put(Constants.VALIDITY_TYPE, ValidityType.UNDEFINED.name());
                valityMap.put(Constants.IS_CONSENT, "false");
        }
        return valityMap;
    }

    private boolean isLongLiveConsent(AuthenticationContext context, String scope) throws NamingException,
            DBUtilException {

        boolean isConsent = false;

        try {

            UserConsentDetails userConsentDetails = getUserConsentDetails(context, scope);
            if (userConsentDetails == null) {
                isConsent = true;

            } else {

                if (userConsentDetails.getRevokeStatus().equalsIgnoreCase(Constants.TRUE)) {
                    Date today = new Date();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                    if (today.after(dateFormat.parse(userConsentDetails.getConsentExpireDatetime()))) {
                        isConsent = true;
                    }

                } else if (userConsentDetails.getRevokeStatus().equalsIgnoreCase(Constants.FALSE)) {
                    isConsent = true;
                }
            }

        } catch (DBUtilException | NamingException e) {
            log.error("error occurred while accessing the database table : " + e.getMessage());

        } catch (ParseException e) {
            log.error("error occurred while formatting the date : " + e.getMessage());
        }
        return isConsent;
    }

    public static List<String> getScopesToDisplay(List<String> attributeSet, String scope) {

        List<String> consentAttributeSet = attributeSet;
        List<String> displayAttributeSet;

        displayAttributeSet = scopeMap.get(scope).getDisplayAttributes();
        for (int j = 0; j < displayAttributeSet.size(); j++) {
            if (!consentAttributeSet.contains(displayAttributeSet.get(j))) {
                consentAttributeSet.add(displayAttributeSet.get(j));
            }
        }
        return consentAttributeSet;
    }

    public static void persistConsentedScopeDetails(AuthenticationContext context) throws DBUtilException,
            NamingException {

        AttributeConfigDao attributeConfigDao = new AttributeConfigDaoImpl();

        String msisdn = context.getProperty(Constants.MSISDN).toString();
        String operator = context.getProperty(Constants.OPERATOR).toString();
        String clientId = context.getProperty(Constants.CLIENT_ID).toString();

        List<SpConsent> spConsentDetailsList = attributeConfigDao.getScopeExpireTime(operator, clientId, context
                .getProperty(Constants.LONGLIVEDSCOPES).toString());
        List<UserConsentHistory> userConsentHistoryList = new ArrayList();

        for (SpConsent spConsent : spConsentDetailsList) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date today = new Date();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DATE, spConsent.getExpPeriod());

            UserConsentHistory userConsentHistory = new UserConsentHistory();
            userConsentHistory.setMsisdn(msisdn);
            userConsentHistory.setConsentId(spConsent.getConsentId());
            userConsentHistory.setConsentExpireTime(dateFormat.format(calendar.getTime()));
            userConsentHistory.setConsentStatus(Constants.TRUE);
            userConsentHistory.setClientId(clientId);
            userConsentHistory.setOperatorName(operator);

            userConsentHistoryList.add(userConsentHistory);
        }
        attributeConfigDao.saveUserConsentedAttributes(userConsentHistoryList);
    }

    public static void createUserProfile(AuthenticationContext context) throws AuthenticationFailedException {

        String msisdn = context.getProperty(Constants.MSISDN).toString();
        String operator = context.getProperty(Constants.OPERATOR).toString();
        boolean isAttributeScope = (Boolean) context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE);
        String spType = context.getProperty(Constants.TRUSTED_STATUS).toString();
        String attrShareType = context.getProperty(Constants.ATTRSHARE_SCOPE_TYPE).toString();

        try {
            new UserProfileManager().createUserProfileLoa2(msisdn, operator, isAttributeScope, spType, attrShareType);
            new UserProfileManager().updateMIGUserRoles(msisdn, context.getProperty(Constants.CLIENT_ID).toString(),context.getProperty(Constants.API_SCOPES).toString());


        } catch (RemoteException | UserRegistrationAdminServiceIdentityException e) {
            log.error("error occurred while create user profile : " + e.getMessage());
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }
}
