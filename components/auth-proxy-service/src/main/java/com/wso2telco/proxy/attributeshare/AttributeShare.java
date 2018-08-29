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
package com.wso2telco.proxy.attributeshare;

import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import com.wso2telco.proxy.dao.AttributeShareDao;
import com.wso2telco.proxy.dao.attsharedaoimpl.AttributeShareDaoImpl;
import com.wso2telco.proxy.model.AuthenticatorException;
import com.wso2telco.proxy.util.AuthProxyConstants;
import com.wso2telco.proxy.util.AuthProxyEnum;
import com.wso2telco.proxy.util.DBUtils;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeShare {

    private static Log log = LogFactory.getLog(AttributeShare.class);

    private static AttributeShareDao attShareDao;
    private static Map<String, String> scopeTypes;

    private AttributeShare() {
    }

    static {

        try {
            attShareDao = new AttributeShareDaoImpl();
            scopeTypes = attShareDao.getScopeParams();
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
    public static Map<String, Object> attributeShareScopesValidation(String scopeName, String operatorName, String
            clientId, String loginHintMsisdn, String msisdn) throws
            AuthenticationFailedException, AuthenticatorException, NamingException {

        String trustedStatus = null;
        boolean isAttributeShare = false;
        boolean isAPIConsent = false;
        String attShareType = null;
        String logoPath = null;
        Map<String, Object> attShareScopeDetails = new HashMap<>();
        try {

            if (scopeName == null || scopeName.isEmpty()) {
                log.error("Scope Name list is null");
                throw new AuthenticationFailedException("Scope Name list is null or empty");
            } else {
                List<String> scopeList = new ArrayList(Arrays.asList(scopeName.split(" ")));

                if (!scopeTypes.isEmpty()) {
                    for (String scope : scopeList) {
                        attShareType = scopeTypes.get(scope);
                        if (AuthProxyEnum.SCOPETYPE.APICONSENT.name().equals(attShareType)) {
                            isAPIConsent = true;
                            if (scopeList != null) {
                                if (scopeList != null && scopeList.size() > 0) {
                                    boolean enableapproveall = true;
                                    Map<String, String> approveNeededScopes = new HashedMap();
                                    List<String> approvedScopes = new ArrayList<>();
                                    for (String scope2 : scopeList) {
                                        String consent[] = DBUtils.getConsentStatus(scope2, clientId, operatorName);
                                        if (consent != null && consent.length == 2 && !consent[0].isEmpty() && consent[0].contains("approve")) {
                                            boolean approved = DBUtils.getUserConsentScopeApproval(msisdn, scope, clientId, operatorName);
                                            if (approved) {
                                                approvedScopes.add(scope2);
                                            } else {
                                                approveNeededScopes.put(scope2, consent[1]);
                                            }
                                            if (consent[0].equalsIgnoreCase("approve")) {
                                                enableapproveall = false;
                                            }
                                        }
                                    }
                                    attShareScopeDetails.put(AuthProxyConstants.APPROVE_NEEDED_SCOPES, approveNeededScopes);
                                    attShareScopeDetails.put(AuthProxyConstants.APPROVED_SCOPES, approvedScopes);
                                    attShareScopeDetails.put(AuthProxyConstants.APPROVE_ALL_ENABLE, enableapproveall);

                                } else {
                                    throw new AuthenticationFailedException("Authenticator failed- Approval needed scopes not found");
                                }
                            } else {
                                throw new AuthenticationFailedException("Authenticator failed- Approval needed scopes not found");
                            }
                            break;
                        }else if (attShareType != null){
                            isAttributeShare = true;
                            AttributeSharable attributeShare = ScopeFactory.getAttributeSharable(attShareType);
                            if (attributeShare != null) {
                                trustedStatus = attributeShare.getAttributeShareDetails
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
        attShareScopeDetails.put(AuthProxyConstants.ATTR_SHARE_SCOPE, Boolean.toString(isAttributeShare));
        attShareScopeDetails.put(AuthProxyConstants.TRUSTED_STATUS, trustedStatus);
        attShareScopeDetails.put(AuthProxyConstants.ATTR_SHARE_SCOPE_TYPE, attShareType);
        attShareScopeDetails.put(AuthProxyConstants.IS_API_CONSENT, Boolean.toString(isAPIConsent));


        return attShareScopeDetails;
    }
}
