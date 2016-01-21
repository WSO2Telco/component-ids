package com.axiata.mife.user.impl;


import org.apache.amber.oauth2.common.error.OAuthError;
import org.wso2.carbon.identity.oauth.user.UserInfoEndpointException;
import org.wso2.carbon.identity.oauth.user.UserInfoRequestValidator;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

public class ClaimInfoRequestValidator implements UserInfoRequestValidator {

    public String validateRequest(HttpServletRequest request) throws UserInfoEndpointException {

        String schema = request.getParameter("schema");
        String authzHeaders = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authzHeaders == null) {
            throw new UserInfoEndpointException(OAuthError.ResourceResponse.INVALID_REQUEST,
                    "Authorization header missing");
        }

        String[] authzHeaderInfo = ((String) authzHeaders).trim().split(" ");
        if (!"Bearer".equals(authzHeaderInfo[0])) {
            throw new UserInfoEndpointException(OAuthError.ResourceResponse.INVALID_REQUEST, "Bearer token missing");
        }

        String contextPath = request.getContextPath();
        System.setProperty("request.context.path", contextPath);
        return authzHeaderInfo[1];
    }
}
