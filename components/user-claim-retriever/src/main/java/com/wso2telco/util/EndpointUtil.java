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
package com.wso2telco.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.OAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.model.OAuth2Parameters;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.ui.CarbonUIUtil;

// TODO: Auto-generated Javadoc

/**
 * The Class EndpointUtil.
 */
public class EndpointUtil {

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(EndpointUtil.class);

    /**
     * Gets the o auth2 service.
     *
     * @return the o auth2 service
     */
    public static OAuth2Service getOAuth2Service() {
        return (OAuth2Service) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(OAuth2Service
                .class);
    }

    /**
     * Gets the o auth server configuration.
     *
     * @return the o auth server configuration
     */
    public static OAuthServerConfiguration getOAuthServerConfiguration() {
        return (OAuthServerConfiguration) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(
                OAuthServerConfiguration.class);
    }

    /**
     * Gets the o auth2 token validation service.
     *
     * @return the o auth2 token validation service
     */
    public static OAuth2TokenValidationService getOAuth2TokenValidationService() {
        return (OAuth2TokenValidationService) PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(
                OAuth2TokenValidationService.class);
    }

    /**
     * Gets the user info request validator.
     *
     * @return the user info request validator
     * @throws OAuthSystemException the o auth system exception
     */
    public static String getUserInfoRequestValidator() throws OAuthSystemException {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointRequestValidator();
    }

    /**
     * Gets the access token validator.
     *
     * @return the access token validator
     */
    public static String getAccessTokenValidator() {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointAccessTokenValidator();
    }

    /**
     * Gets the user info response builder.
     *
     * @return the user info response builder
     */
    public static String getUserInfoResponseBuilder() {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointResponseBuilder();
    }

    /**
     * Gets the user info claim retriever.
     *
     * @return the user info claim retriever
     */
    public static String getUserInfoClaimRetriever() {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointClaimRetriever();
    }

    /**
     * Gets the user info claim dialect.
     *
     * @return the user info claim dialect
     */
    public static String getUserInfoClaimDialect() {
        return getOAuthServerConfiguration().getOpenIDConnectUserInfoEndpointClaimDialect();
    }

    /**
     * Extract credentials from authz header.
     *
     * @param authorizationHeader the authorization header
     * @return the string[]
     * @throws OAuthClientException the o auth client exception
     */
    public static String[] extractCredentialsFromAuthzHeader(String authorizationHeader) throws OAuthClientException {
        String[] splitValues = authorizationHeader.trim().split(" ");
        byte[] decodedBytes = Base64Utils.decode(splitValues[1].trim());
        if (decodedBytes != null) {
            String userNamePassword = new String(decodedBytes);
            return userNamePassword.split(":");
        } else {
            String errMsg = "Error decoding authorization header. Could not retrieve client id and client secret.";
            throw new OAuthClientException(errMsg);
        }
    }

    /**
     * Gets the error page url.
     *
     * @param errorCode    the error code
     * @param errorMessage the error message
     * @param appName      the app name
     * @param redirect_uri the redirect_uri
     * @return the error page url
     */
    public static String getErrorPageURL(String errorCode, String errorMessage, String appName, String redirect_uri) {

        String errorPageUrl = null;
        if (redirect_uri != null && !redirect_uri.equals("")) {
            errorPageUrl = redirect_uri;
        } else {
            errorPageUrl = CarbonUIUtil.getAdminConsoleURL("/") + "../authenticationendpoint/oauth2_error.do";
        }
        try {
            errorPageUrl += "?" + OAuthConstants.OAUTH_ERROR_CODE + "=" + URLEncoder.encode(errorCode, "UTF-8") + "&"
                    + OAuthConstants.OAUTH_ERROR_MESSAGE + "=" + URLEncoder.encode(errorMessage, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }

        if (appName != null) {
            try {
                errorPageUrl += "application" + "=" + URLEncoder.encode(appName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // ignore
            }
        }

        return errorPageUrl;
    }

    /**
     * Gets the login page url.
     *
     * @param clientId            the client id
     * @param sessionDataKey      the session data key
     * @param forceAuthenticate   the force authenticate
     * @param checkAuthentication the check authentication
     * @param scopes              the scopes
     * @return the login page url
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public static String getLoginPageURL(String clientId, String sessionDataKey, boolean forceAuthenticate,
                                         boolean checkAuthentication, Set<String> scopes) throws
            UnsupportedEncodingException {

        try {
            SessionDataCacheEntry entry = (SessionDataCacheEntry) SessionDataCache.getInstance().getValueFromCache(
                    new SessionDataCacheKey(sessionDataKey));

            return getLoginPageURL(clientId, sessionDataKey, forceAuthenticate, checkAuthentication, scopes,
                    entry.getParamMap());
        } finally {
            OAuth2Util.clearClientTenantId();
        }
    }

    /**
     * Gets the login page url.
     *
     * @param clientId            the client id
     * @param sessionDataKey      the session data key
     * @param forceAuthenticate   the force authenticate
     * @param checkAuthentication the check authentication
     * @param scopes              the scopes
     * @param reqParams           the req params
     * @return the login page url
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public static String getLoginPageURL(String clientId, String sessionDataKey, boolean forceAuthenticate,
                                         boolean checkAuthentication, Set<String> scopes, Map<String, String[]>
                                                 reqParams)
            throws UnsupportedEncodingException {

        try {

            String type = "oauth2";

            if (scopes != null && scopes.contains("openid")) {
                type = "oidc";
            }

            String commonAuthURL = CarbonUIUtil.getAdminConsoleURL("/");
            commonAuthURL = commonAuthURL.replace("carbon", "commonauth");
            String selfPath = "/oauth2/authorize";
            AuthenticationRequest authenticationRequest = new AuthenticationRequest();

            // Build the authentication request context.
            authenticationRequest.setCommonAuthCallerPath(selfPath);
            // authenticationRequest.setForceAuth(String.valueOf(forceAuthenticate));
            authenticationRequest.setForceAuth(forceAuthenticate);
            // authenticationRequest.setPassiveAuth(String.valueOf(checkAuthentication));
            authenticationRequest.setPassiveAuth(checkAuthentication);
            authenticationRequest.setRelyingParty(clientId);
            authenticationRequest.addRequestQueryParam("tenantId",
                    new String[]{String.valueOf(OAuth2Util.getClientTenatId())});
            authenticationRequest.setRequestQueryParams(reqParams);

            // Build an AuthenticationRequestCacheEntry which wraps
            // AuthenticationRequestContext
            AuthenticationRequestCacheEntry authRequest = new AuthenticationRequestCacheEntry(authenticationRequest);
            FrameworkUtils.addAuthenticationRequestToCache(sessionDataKey, authRequest);

            String loginQueryParams = "?sessionDataKey=" + sessionDataKey + "&" + "type" + "=" + type;

            return commonAuthURL + loginQueryParams;

        } finally {
            OAuth2Util.clearClientTenantId();
        }
    }

    /**
     * Gets the user consent url.
     *
     * @param params         the params
     * @param loggedInUser   the logged in user
     * @param sessionDataKey the session data key
     * @param isOIDC         the is oidc
     * @return the user consent url
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public static String getUserConsentURL(OAuth2Parameters params, String loggedInUser, String sessionDataKey,
                                           boolean isOIDC) throws UnsupportedEncodingException {
        String queryString = "";
        if (log.isDebugEnabled()) {
            log.debug("Received Session Data Key is :  " + sessionDataKey);
            if (params == null) {
                log.debug("Received OAuth2 params are Null for UserConsentURL");
            }
        }
        SessionDataCacheEntry entry = (SessionDataCacheEntry) SessionDataCache.getInstance().getValueFromCache(
                new SessionDataCacheKey(sessionDataKey));

        if (entry == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cache Entry is Null from SessionDataCache ");
            }
        } else {
            queryString = URLEncoder.encode(entry.getQueryString(), "UTF-8");
        }

        String consentPage = null;
        if (isOIDC) {
            consentPage = CarbonUIUtil.getAdminConsoleURL("/") + "../authenticationendpoint/oauth2_consent.do";
        } else {
            consentPage = CarbonUIUtil.getAdminConsoleURL("/") + "../authenticationendpoint/oauth2_authz.do";
        }
        consentPage += "?" + OAuthConstants.OIDC_LOGGED_IN_USER + "=" + URLEncoder.encode(loggedInUser, "UTF-8") + "&"
                + "application" + "=" + URLEncoder.encode(params.getApplicationName(), "ISO-8859-1") + "&"
                + OAuthConstants.OAuth20Params.SCOPE + "="
                + URLEncoder.encode(EndpointUtil.getScope(params), "ISO-8859-1") + "&"
                + OAuthConstants.SESSION_DATA_KEY_CONSENT + "=" + URLEncoder.encode(sessionDataKey, "UTF-8") + "&"
                + "spQueryParams" + "=" + queryString;
        return consentPage;
    }

    /**
     * Gets the scope.
     *
     * @param params the params
     * @return the scope
     */
    public static String getScope(OAuth2Parameters params) {
        StringBuffer scopes = new StringBuffer();
        for (String scope : params.getScopes()) {
            scopes.append(EndpointUtil.getSafeText(scope) + " ");
        }
        return scopes.toString().trim();
    }

    /**
     * Gets the safe text.
     *
     * @param text the text
     * @return the safe text
     */
    public static String getSafeText(String text) {
        if (text == null) {
            return text;
        }
        text = text.trim();
        if (text.indexOf('<') > -1) {
            text = text.replace("<", "&lt;");
        }
        if (text.indexOf('>') > -1) {
            text = text.replace(">", "&gt;");
        }
        return text;
    }

    /**
     * Gets the realm info.
     *
     * @return the realm info
     */
    public static String getRealmInfo() {
        return "Basic realm=" + getHostName();
    }

    /**
     * Gets the host name.
     *
     * @return the host name
     */
    public static String getHostName() {
        return ServerConfiguration.getInstance().getFirstProperty("HostName");
    }

}
