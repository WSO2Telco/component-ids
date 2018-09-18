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
package com.wso2telco.proxy.consentshare;

import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.proxy.dao.ConsentShareDao;
import com.wso2telco.proxy.dao.attsharedaoimpl.ConsentShareDaoImpl;
import com.wso2telco.proxy.model.AuthenticatorException;
import com.wso2telco.proxy.util.AuthProxyConstants;
import com.wso2telco.proxy.util.AuthProxyEnum;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsentShare {

    private static Log log = LogFactory.getLog(ConsentShare.class);

    private static ConsentShareDao conentShareDao;
    private static Map<String, String> scopeTypes;

    private ConsentShare() {
    }

    static {

        try {
            conentShareDao = new ConsentShareDaoImpl();
            scopeTypes = conentShareDao.getScopeParams();
        } catch (DBUtilException e) {
            log.error("Error occurred while scope retrieving the data from database : " + e);
        }
    }

    /**
     * Identifying the availability of attribute sharing scopes in the request
     *
     * @param scopeName
     * @param operatorName
     * @param clientId
     * @param loginHintMsisdn
     * @param msisdn
     * @return Map containing details about scopes
     * @throws AuthenticationFailedException
     */
    public static Map<String, Object> scopesValidation(String scopeName, String operatorName, String
            clientId, String loginHintMsisdn, String msisdn) throws
            AuthenticationFailedException, AuthenticatorException, NamingException {

        String trustedStatus = null;
        boolean isAttributeShare = false;
        boolean isAPIConsent = false;
        String scopeType = null;
        String logoPath = null;
        Map<String, Object> scopeDetails = new HashMap<>();
        try {

            if (scopeName == null || scopeName.isEmpty()) {
                log.error("Scope Name list is null");
                throw new AuthenticationFailedException("Scope Name list is null or empty");
            } else {
                List<String> scopeList = new ArrayList(Arrays.asList(scopeName.split(" ")));

                if (!scopeTypes.isEmpty()) {
                    for (String scope : scopeList) {
                        scopeType = scopeTypes.get(scope);
                        if (AuthProxyEnum.SCOPETYPE.APICONSENT.name().equals(scopeType)) {
                            isAPIConsent = true;
                            ConsentSharable consentShare = ScopeFactory.getConsentSharable(scopeType);
                            if (consentShare != null) {
                                trustedStatus = consentShare.getConsentShareDetails
                                        (operatorName, clientId, loginHintMsisdn, msisdn);
                            }
                            break;
                        }else if (scopeType != null){
                            isAttributeShare = true;
                            ConsentSharable consentShare = ScopeFactory.getConsentSharable(scopeType);
                            if (consentShare != null) {
                                trustedStatus = consentShare.getConsentShareDetails
                                        (operatorName, clientId, loginHintMsisdn, msisdn);
                            }
                            break;
                        }
                    }
                }
            }

        } catch (AuthenticationFailedException e) {
            log.error("Authentication Exception in validateAttributeShareScopes : " + e.getMessage());
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
        scopeDetails.put(AuthProxyConstants.ATTR_SHARE_SCOPE, Boolean.toString(isAttributeShare));
        scopeDetails.put(AuthProxyConstants.TRUSTED_STATUS, trustedStatus);
        scopeDetails.put(AuthProxyConstants.ATTR_SHARE_SCOPE_TYPE, scopeType);
        scopeDetails.put(AuthProxyConstants.IS_API_CONSENT, Boolean.toString(isAPIConsent));


        return scopeDetails;
    }
}
