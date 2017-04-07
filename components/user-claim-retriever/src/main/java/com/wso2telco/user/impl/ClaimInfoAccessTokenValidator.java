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

import org.apache.amber.oauth2.common.error.OAuthError;
import org.wso2.carbon.identity.oauth.user.UserInfoAccessTokenValidator;
import org.wso2.carbon.identity.oauth.user.UserInfoEndpointException;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;

import com.wso2telco.util.EndpointUtil;

// TODO: Auto-generated Javadoc

/**
 * The Class ClaimInfoAccessTokenValidator.
 */
public class ClaimInfoAccessTokenValidator implements UserInfoAccessTokenValidator {

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.oauth.user.UserInfoAccessTokenValidator#validateToken(java.lang.String)
     */
    public OAuth2TokenValidationResponseDTO validateToken(String accessTokenIdentifier)
            throws UserInfoEndpointException {

        OAuth2TokenValidationRequestDTO dto = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO.OAuth2AccessToken accessToken = dto.new OAuth2AccessToken();
        accessToken.setTokenType("bearer");
        accessToken.setIdentifier(accessTokenIdentifier);
        dto.setAccessToken(accessToken);
        OAuth2TokenValidationResponseDTO response =
                EndpointUtil.getOAuth2TokenValidationService()
                        .validate(dto);
        // invalid access token
        if (!response.isValid()) {
            throw new UserInfoEndpointException(OAuthError.ResourceResponse.INVALID_TOKEN,
                    "Access token validation failed");
        }
        // check the scope
        boolean isOpenIDScope = false;
        String[] scope = response.getScope();
        for (String curScope : scope) {
            if ("openid".equals(curScope)) {
                isOpenIDScope = true;
            }
        }
        if (!isOpenIDScope) {
            throw new UserInfoEndpointException(OAuthError.ResourceResponse.INSUFFICIENT_SCOPE,
                    "Access token does not have the openid scope");
        }
        if (response.getAuthorizedUser() == null) {
            throw new UserInfoEndpointException(OAuthError.ResourceResponse.INVALID_TOKEN,
                    "Access token is not valid. No authorized user found. Invalid grant");
        }
        OAuth2TokenValidationResponseDTO.AuthorizationContextToken authorizationContextToken = response.new
                AuthorizationContextToken(accessToken.getTokenType(), accessToken.getIdentifier());
        response.setAuthorizationContextToken(authorizationContextToken);
        return response;
    }
}
