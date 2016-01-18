/*
 * USSDPinAuthenticator.java
 * Sep 23, 2014  5:22:01 PM
 * Roshan.Saputhanthri
 *
 * Copyright (C) Dialog Axiata PLC. All Rights Reserved.
 */
package com.gsma.authenticators.ussd;

import com.gsma.authenticators.AuthenticatorException;
import com.gsma.authenticators.Constants;
import com.gsma.authenticators.DBUtils;
import com.gsma.authenticators.DataHolder;
import com.gsma.authenticators.internal.CustomAuthenticatorServiceComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.OAuthAppDO;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.BaseCache;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * <TO-DO>
 * <code>USSDPinAuthenticator</code>
 *
 * @version $Id: USSDPinAuthenticator.java,v 1.00.000
 */
public class USSDPinAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private static final long serialVersionUID = 7785133722588291678L;
    private static Log log = LogFactory.getLog(USSDPinAuthenticator.class);
    private static final String PIN_CLAIM = "http://wso2.org/claims/pin";

    @Override
    public boolean canHandle(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("USSD Authenticator canHandle invoked");
        }

//        if (request.getParameter("msisdn") != null) {
//            return true;
//        }
        return true;
    }

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request,
            HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException {

        if (context.isLogoutRequest()) {
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        } else {
            return super.process(request, response, context);
        }
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
            HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                context.getCallerSessionKey(),
                context.getContextIdentifier());

        try {
            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            } else {
                // Insert entry to DB only if this is not a retry
                DBUtils.insertUserResponse(context.getContextIdentifier(), String.valueOf(USSDPinAuthenticator.UserResponse.PENDING));
            }

            //MSISDN will be saved in the context in the MSISDNAuthenticator
            String msisdn = (String) context.getProperty("msisdn");


            String serviceProviderName = null;

            serviceProviderName = context.getSequenceConfig().getApplicationConfig().getApplicationName();

            log.info("Service Provider Name = " + serviceProviderName);
            if (serviceProviderName.equals("wso2_sp_dashboard")) {
                serviceProviderName = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getDashBoard();

            }

            // String pinEnabled =
            // DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getPinauth();
            String ussdResponse = null;

            ussdResponse = new SendUSSD().sendUSSDPIN(msisdn, context.getContextIdentifier(),
                    serviceProviderName);

            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams))
                    + "&authenticators=" + getName() + ":" + "LOCAL" + retryParam);

        } catch (IOException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (AuthenticatorException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
            HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        String sessionDataKey = request.getParameter("sessionDataKey");

        boolean isAuthenticated = false;
        String msisdn = null;

        // Check if the user has provided consent
        try {

            //String pinEnabled = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getPinauth();

            Pinresponse pinresponse = DBUtils.getUserPinResponse(sessionDataKey);
            if (pinresponse.getUserPin() != null) {

                //MSISDN will be saved in the context in the MSISDNAuthenticator
                msisdn = ((String) context.getProperty("msisdn")).replace("+", "").trim();
                try {
                    int tenantId = IdentityUtil.getTenantIdOFUser(msisdn);
                    UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService()
                            .getTenantUserRealm(tenantId);

                    if (userRealm != null) {
                        UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
                        String profilepin = userStoreManager.getUserClaimValue(msisdn, PIN_CLAIM, null);

                        if (log.isDebugEnabled()) {
                            log.debug("profile pin: " + profilepin);
                        }

                        if (profilepin != null) {
                            String userpin = pinresponse.getUserPin();
                            String hashedPin = getHashedPin(userpin);
                            if (log.isDebugEnabled()) {
                                log.debug("User pin: " + userpin + ":" + profilepin);
                            }

                            if (profilepin.equalsIgnoreCase(hashedPin)) {
                                isAuthenticated = true;
                            }
                        }

                    } else {
                        throw new AuthenticationFailedException("Cannot find the user realm for the given tenant: " + tenantId);
                    }

                } catch (IdentityException e) {
                    log.error("USSD Pin Authentication failed while trying to get the tenant ID of the user", e);
                    throw new AuthenticationFailedException(e.getMessage(), e);
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    log.error("USSD Pin Authentication failed while trying to authenticate", e);
                    throw new AuthenticationFailedException(e.getMessage(), e);
                }
            }

        } catch (AuthenticatorException e) {
            log.error("USSD Pin Authentication failed while trying to authenticate", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }

        if (!isAuthenticated) {
            log.info("USSD Pin Authenticator authentication failed ");
            context.setProperty("faileduser", msisdn);
            
            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to user not providing consent.");
            }

            throw new AuthenticationFailedException("Authentication Failed");
        }
        log.info("USSD Pin Authenticator authentication success");

        context.setSubject(msisdn);
        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
    }

    private String getHashedPin(String pinvalue) {
        String hashString = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pinvalue.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            hashString = hexString.toString();

        } catch (UnsupportedEncodingException ex) {
            log.info("Error getHashValue");
        } catch (NoSuchAlgorithmException ex) {
            log.info("Error getHashValue");
        }

        return hashString;

    }

    /**
     * @param clientID
     * @return
     * @throws IdentityOAuth2Exception
     * @throws InvalidOAuthClientException
     */
    private static OAuthAppDO getAppInformation(String clientID)
            throws IdentityOAuth2Exception, InvalidOAuthClientException {
        BaseCache<String, OAuthAppDO> appInfoCache = new BaseCache<String, OAuthAppDO>(
                "AppInfoCache"); //$NON-NLS-1$
        if (null != appInfoCache) {
            if (log.isDebugEnabled()) {
                log.debug("Successfully created AppInfoCache under " //$NON-NLS-1$
                        + OAuthConstants.OAUTH_CACHE_MANAGER);
            }
        }

        OAuthAppDO oAuthAppDO = appInfoCache.getValueFromCache(clientID);
        if (oAuthAppDO != null) {
            return oAuthAppDO;
        } else {
            oAuthAppDO = new OAuthAppDAO().getAppInformation(clientID);
            appInfoCache.addToCache(clientID, oAuthAppDO);
            return oAuthAppDO;
        }
    }

    @Override
    protected boolean retryAuthenticationEnabled() {
        return false;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        return request.getParameter("sessionDataKey");
    }

    @Override
    public String getFriendlyName() {
        return Constants.USSDPIN_AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {
        return Constants.USSDPIN_AUTHENTICATOR_NAME;
    }

    private enum UserResponse {

        PENDING,
        APPROVED,
        REJECTED
    }
}
