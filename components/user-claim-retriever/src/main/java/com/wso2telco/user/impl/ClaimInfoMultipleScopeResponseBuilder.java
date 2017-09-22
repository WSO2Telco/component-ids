/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
package com.wso2telco.user.impl;

import com.google.gson.Gson;
import com.wso2telco.claims.ClaimsRetrieverFactory;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.model.ScopeDetailsConfig;
import com.wso2telco.dao.DBConnection;
import com.wso2telco.dao.ScopeDetails;
import com.wso2telco.util.ClaimUtil;
import com.wso2telco.util.ClaimsRetrieverType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.wso2.carbon.identity.oauth.user.UserInfoEndpointException;
import org.wso2.carbon.identity.oauth.user.UserInfoResponseBuilder;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * The Class ClaimInfoMultipleScopeResponseBuilder.
 */
public class ClaimInfoMultipleScopeResponseBuilder implements UserInfoResponseBuilder {

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(ClaimInfoMultipleScopeResponseBuilder.class);

    private static String operatorClaim = "operator";

    /**
     * The openid scopes.
     */
    List<String> openidScopes = Arrays.asList("profile", "email", "address", "phone", "openid","mc_identity_phonenumber_hashed");

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.oauth.user.UserInfoResponseBuilder#getResponseString(org.wso2.carbon.identity
     * .oauth2.dto.OAuth2TokenValidationResponseDTO)
     */
    public String getResponseString(OAuth2TokenValidationResponseDTO tokenResponse) throws UserInfoEndpointException,
            OAuthSystemException {

        List<ScopeDetailsConfig.Scope> scopes;


        if (log.isDebugEnabled()) {
            log.debug("Generating Claim Info for Access token : " + tokenResponse.getAuthorizationContextToken()
                    .getTokenString());
        }

        Map<String, Object> claims = null;
        //read claimValues per scope from scope-config.xml
        ScopeDetailsConfig scopeConfigs =com.wso2telco.core.config.ConfigLoader.getInstance().getScopeDetailsConfig();

        try {
            claims = ClaimUtil.getClaimsFromUserStore(tokenResponse);
        } catch (Exception e) {
            throw new UserInfoEndpointException("Error while retrieving claims from user store.");
        }

        String contextPath = System.getProperty("request.context.path");
        String[] requestedScopes = tokenResponse.getScope();
        scopes=scopeConfigs.getPremiumScopes();
        if (contextPath.equals("/oauth2")) {
            //oauth2/userinfo requests should serve scopes in openid connect onl
            requestedScopes = getValidScopes(requestedScopes);
            scopes=scopeConfigs.getUserInfoScope();

        }

        Map<String, Object> requestedClaims = null;
        try {
            DBConnection dbConnection = DBConnection.getInstance();
            ScopeDetails scopeDetails = dbConnection.getScopeFromAcessToken(tokenResponse
                    .getAuthorizationContextToken().getTokenString());

            List<MobileConnectConfig.OperatorData> operators= com.wso2telco.core.config.ConfigLoader.getInstance().getMobileConnectConfig().getOperatorsList().getOperatorData();
            String  operatorName =(String)claims.get(operatorClaim);
            String claimsRetrieverType= ClaimsRetrieverType.LOCAL.toString();

            for (MobileConnectConfig.OperatorData operator : operators) {
                if(operator.getOperatorName().equalsIgnoreCase(operatorName)){
                    claimsRetrieverType=operator.getUserInfoEndPointType();
                    break;
                }
            }

            requestedClaims= ClaimsRetrieverFactory.getClaimsRetriever(claimsRetrieverType).getRequestedClaims(requestedScopes, scopes, claims);
            requestedClaims.put("sub", scopeDetails.getPcr());

        } catch (NoSuchAlgorithmException e) {
            throw new UserInfoEndpointException("Error while generating hashed claim values.");
        } catch (Exception e) {
            throw new UserInfoEndpointException("Error while generating sub value");
        }

        Gson gson = new Gson();
        String userInfoJson = gson.toJson(requestedClaims);
        log.debug("User data JSON " + userInfoJson);
        return userInfoJson;
    }

    /**
     * Gets the valid scopes.
     *
     * @param requestedScopes the requested scopes
     * @return the valid scopes
     */
    private String[] getValidScopes(String[] requestedScopes) {
        List<String> validScopes = new ArrayList();
        for (String scope : requestedScopes) {
            if (!openidScopes.contains(scope)) {
                continue;
            }
            validScopes.add(scope);
        }
        return validScopes.toArray(new String[validScopes.size()]);
    }

}
