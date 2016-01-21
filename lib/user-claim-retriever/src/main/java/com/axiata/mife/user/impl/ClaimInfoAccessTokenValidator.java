package com.axiata.mife.user.impl;

import com.axiata.mife.util.EndpointUtil;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.wso2.carbon.identity.oauth.user.UserInfoAccessTokenValidator;
import org.wso2.carbon.identity.oauth.user.UserInfoEndpointException;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;

public class ClaimInfoAccessTokenValidator implements UserInfoAccessTokenValidator {

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
        OAuth2TokenValidationResponseDTO.AuthorizationContextToken authorizationContextToken = response.new AuthorizationContextToken(accessToken.getTokenType(), accessToken.getIdentifier());
        response.setAuthorizationContextToken(authorizationContextToken);
        return response;
    }
}