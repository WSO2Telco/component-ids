/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.gsma.authenticators.ussd;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.core.entity.PinConfig;
import com.wso2telco.core.util.PinConfigUtil;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.internal.CustomAuthenticatorServiceComponent;
import com.wso2telco.gsma.authenticators.ussd.command.PinLoginUssdCommand;
import com.wso2telco.gsma.authenticators.ussd.command.PinRegistrationUssdCommand;
import com.wso2telco.gsma.authenticators.ussd.command.UssdCommand;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.authenticators.util.UserProfileUtil;
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
import org.wso2.carbon.identity.application.common.cache.BaseCache;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;


// TODO: Auto-generated Javadoc

/**
 * The Class USSDPinAuthenticator.
 */
public class USSDPinAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 7785133722588291678L;

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(USSDPinAuthenticator.class);

    /**
     * The Constant PIN_CLAIM.
     */
    private static final String PIN_CLAIM = "http://wso2.org/claims/pin";

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public boolean canHandle(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("USSD Authenticator canHandle invoked");
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
     */
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

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#initiateAuthenticationRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
     */
    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);

        try {

            String loginPage = getAuthEndpointUrl(context);

            String queryParams = FrameworkUtils
                    .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                            context.getCallerSessionKey(),
                            context.getContextIdentifier());

            String retryParam = "";

            String msisdn = (String) context.getProperty("msisdn");

            String serviceProviderName = context.getSequenceConfig().getApplicationConfig().getApplicationName();

            log.info("Service Provider Name = " + serviceProviderName);
            if (serviceProviderName.equals("wso2_sp_dashboard")) {
                serviceProviderName = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getDashBoard();

            }
            String operator = (String) context.getProperty("operator");

            PinConfigUtil.savePinConfigToContext(context, msisdn);
            saveLoa3PropertiesToContext(request, context);

            sendUssd(context, isRegistering, msisdn, serviceProviderName, operator);
            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams))
                    + "&redirect_uri=" + context.getProperty("redirectURI")
                    + "&authenticators=" + getName() + ":" + "LOCAL" + retryParam);

        } catch (IOException e) {
            log.error("Error occurred while redirecting the request", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (SQLException | AuthenticatorException e) {
            log.error("Error occurred while inserting registration status", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }

    private void sendUssd(AuthenticationContext context, boolean isRegistering, String msisdn, String serviceProviderName, String operator) throws SQLException, AuthenticatorException, IOException {
        UssdCommand ussdCommand;
        if (isRegistering) {
            ussdCommand = new PinRegistrationUssdCommand();
            DBUtils.insertRegistrationStatus(msisdn, Constants.STATUS_PENDING, context.getContextIdentifier()); // TODO: 1/3/17 take db insertion in to a single utility
        } else {
            ussdCommand = new PinLoginUssdCommand();
        }
        ussdCommand.execute(msisdn, context.getContextIdentifier(), serviceProviderName, operator);
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#processAuthenticationResponse(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
     */
    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        String msisdn = (String) context.getProperty(Constants.MSISDN);
        String openator = (String) context.getProperty(Constants.OPERATOR);

        PinConfig pinConfig = (PinConfig) context.getProperty(com.wso2telco.core.util.Constants.PIN_CONFIG_OBJECT);
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);

        try {
            if (isRegistering) {
                handleUserRegistration(context, msisdn, openator, pinConfig);
            } else {
                handleUserLogin(msisdn, pinConfig);
            }
            AuthenticationContextHelper.setSubject(context, msisdn);

        } catch (UserRegistrationAdminServiceIdentityException | RemoteException e) {
            log.error("Error occurred while creating user profile", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {

            log.error("Error occurred while accessing admin services", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }

    private void handleUserRegistration(AuthenticationContext context, String msisdn, String openator, PinConfig pinConfig) throws UserRegistrationAdminServiceIdentityException, RemoteException, AuthenticationFailedException {
        String challengeAnswer1 = (String) context.getProperty(Constants.CHALLENGE_ANSWER_1);
        String challengeAnswer2 = (String) context.getProperty(Constants.CHALLENGE_ANSWER_2);

        if (pinConfig.isPinsMatched()) {
            UserProfileUtil.createUserProfileLoa3(msisdn, openator, challengeAnswer1, challengeAnswer2,
                    pinConfig.getRegisteredPin());
        } else {
            throw new AuthenticationFailedException("Authentication failed for due to mismatch in entered and confirmed pin");
        }
    }

    private void handleUserLogin(String msisdn, PinConfig pinConfig) throws org.wso2.carbon.user.api.UserStoreException, AuthenticationFailedException {
        log.info("Handling user login for msisdn [ " + msisdn + "]");

        int tenantId = -1234;
        UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService()
                .getTenantUserRealm(tenantId);

        if (userRealm != null) {
            UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
            String profilepin = userStoreManager.getUserClaimValue(msisdn, PIN_CLAIM, null);

            if (log.isDebugEnabled()) {
                log.debug("profile pin: " + profilepin);
            }

            if (profilepin != null) {
                String userpin = pinConfig.getRegisteredPin();
                String hashedPin = getHashedPin(userpin);
                if (log.isDebugEnabled()) {
                    log.debug("User pin: " + userpin + ":" + profilepin);
                }

                if (profilepin.equalsIgnoreCase(hashedPin)) {
                    log.info("User entered a correct pin. Authentication Success");
                }else {
                    log.error("Authentication failed. User entered an incorrect pin");
                    throw new AuthenticationFailedException("Authentication failed due to incorrect pin");
                }
            }

        } else {
            throw new AuthenticationFailedException("Cannot find the user realm for the given tenant: " + tenantId);
        }
    }

    private void saveLoa3PropertiesToContext(HttpServletRequest request, AuthenticationContext context) {

        context.setProperty(Constants.CHALLENGE_QUESTION_1, request.getParameter(Constants.CHALLENGE_QUESTION_1));
        context.setProperty(Constants.CHALLENGE_QUESTION_2, request.getParameter(Constants.CHALLENGE_QUESTION_2));
        context.setProperty(Constants.CHALLENGE_ANSWER_1, request.getParameter(Constants.CHALLENGE_ANSWER_1));
        context.setProperty(Constants.CHALLENGE_ANSWER_2, request.getParameter(Constants.CHALLENGE_ANSWER_2));
        context.setProperty(Constants.NO_OF_ATTEMPTS, 0);
    }

    private String getAuthEndpointUrl(AuthenticationContext context) {
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
        String loginPage;

        if (isRegistering) {
            context.setProperty(Constants.IS_REGISTERING, true);
            loginPage = DataHolder.getInstance().getMobileConnectConfig().getAuthEndpointUrl()
                    + Constants.VIEW_PIN_REGISTRATION_WAITING;
        } else {
            loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        }
        return loginPage;
    }

    /**
     * Gets the hashed pin.
     *
     * @param pinvalue the pinvalue
     * @return the hashed pin
     */
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
            log.error("Error getHashValue" + ex);
        } catch (NoSuchAlgorithmException ex) {
            log.error("Error getHashValue" + ex);
        }

        return hashString;

    }


    /**
     * Gets the app information.
     *
     * @param clientID the client id
     * @return the app information
     * @throws IdentityOAuth2Exception     the identity o auth2 exception
     * @throws InvalidOAuthClientException the invalid o auth client exception
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

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#retryAuthenticationEnabled()
     */
    @Override
    protected boolean retryAuthenticationEnabled() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getContextIdentifier(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        return request.getParameter("sessionDataKey");
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getFriendlyName()
     */
    @Override
    public String getFriendlyName() {
        return Constants.USSDPIN_AUTHENTICATOR_FRIENDLY_NAME;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
     */
    @Override
    public String getName() {
        return Constants.USSDPIN_AUTHENTICATOR_NAME;
    }

    /**
     * The Enum UserResponse.
     */
    private enum UserResponse {

        /**
         * The pending.
         */
        PENDING,

        /**
         * The approved.
         */
        APPROVED,

        /**
         * The rejected.
         */
        REJECTED
    }
}
