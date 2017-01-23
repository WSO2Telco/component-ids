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

import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.ussd.command.LoginUssdCommand;
import com.wso2telco.gsma.authenticators.ussd.command.RegistrationUssdCommand;
import com.wso2telco.gsma.authenticators.ussd.command.UssdCommand;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.authenticators.util.UserProfileManager;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.RemoteException;

// TODO: Auto-generated Javadoc
//import org.wso2.carbon.identity.core.dao.OAuthAppDAO;


/**
 * The Class USSDAuthenticator.
 */
public class USSDAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 7785133722588291677L;

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(USSDAuthenticator.class);

    /**
     * The Constant PIN_CLAIM.
     */
    private static final String PIN_CLAIM = "http://wso2.org/claims/pin";

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    private static final String CLAIM = "http://wso2.org/claims";

    private static final String OPERATOR_CLAIM_NAME = "http://wso2.org/claims/operator";

    private static final String LOA_CLAIM_NAME = "http://wso2.org/claims/loa";

    private static final String SCOPE_MNV = "mnv";

    private static final String LOA_CPI_VALUE = "1";

    private static final String LOA_MNV_VALUE = "2";

    private static final String SCOPE_OPENID = "openid";

    private static final String SCOPE_CPI = "cpi";

    private static final String MOBILE_CLAIM_NAME = "http://wso2.org/claims/mobile";

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

        String loginPage;
        String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(),
                context.getCallerSessionKey(), context.getContextIdentifier());

        String msisdn = (String) context.getProperty(Constants.MSISDN);
        boolean isUserExists = (boolean) context.getProperty(Constants.IS_USER_EXISTS);
        String serviceProviderName = context.getSequenceConfig().getApplicationConfig().getApplicationName();

        try {
            String retryParam = "";

            loginPage = getAuthEndpointUrl(context);

            log.info("Service Provider Name = " + serviceProviderName);
            if (serviceProviderName.equals("wso2_sp_dashboard")) {
                serviceProviderName = configurationService.getDataHolder().getMobileConnectConfig().getUssdConfig().getDashBoard();

            }
            String operator = (String) context.getProperty(Constants.OPERATOR);

            log.info("operator:" + operator);

            sendUssd(context, msisdn, serviceProviderName, operator, isUserExists);

            log.info("query params: " + queryParams);

            log.info("Context_RedirectURI:" + context.getProperty("redirectURI"));
            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams)) + "&redirect_uri=" + (String) context.getProperty("redirectURI") + "&authenticators="
                    + getName() + ":" + "LOCAL" + retryParam);

        } catch (IOException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }

    private String getAuthEndpointUrl(AuthenticationContext context) {
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
        String loginPage;

        if (isRegistering) {
            context.setProperty(Constants.IS_REGISTERING, true);
            loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl() + Constants.REGISTRATION_JSP;
        } else {
            loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        }
        return loginPage;
    }

    private void sendUssd(AuthenticationContext context, String msisdn, String serviceProviderName, String operator, boolean isUserExists) throws IOException {
        UssdCommand ussdCommand;

        if (isUserExists) {
            ussdCommand = new LoginUssdCommand();
        } else {
            ussdCommand = new RegistrationUssdCommand();
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

        if ("true".equals(request.getParameter("smsrequested"))) {
            //This logic would get hit if the user hits the link to get an SMS so in that case
            //We need to fallback. Therefore we through AuthenticationFailedException
            throw new AuthenticationFailedException("USSD Authentication is skipped and moving forward to SMSAuthenticator");
        } else {
            //This logic would get hit whenever normal USSD Authentication flow is happening and in that case
            //we don't need the SMSAuthenticator to be hit. Therefore, we set this property so that in the
            //MIFEAuthenticationStepHandler, the steps following USSDAuthenticator will be removed.
            //But please note that, this cause ANY step following USSDAuthenticator to be removed.
            //Therefore, when redesigning, need to take this into consideration!
            context.setProperty("removeFollowingSteps","true");
        }

        String sessionDataKey = request.getParameter("sessionDataKey");
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
        String msisdn = (String) context.getProperty(Constants.MSISDN);
        String openator = (String) context.getProperty(Constants.OPERATOR);

        try {
            String responseStatus = DBUtils.getUserRegistrationResponse(sessionDataKey);

            if (responseStatus != null && responseStatus.equalsIgnoreCase(UserResponse.APPROVED.toString())) {

                if (isRegistering) {
                    new UserProfileManager().createUserProfileLoa2(msisdn, openator, Constants.SCOPE_MNV);
                }
            } else {
                log.info("USSD Authenticator authentication failed ");
                context.setProperty("faileduser", (String) context.getProperty("msisdn"));

                if (log.isDebugEnabled()) {
                    log.debug("User authentication failed due to user not providing consent.");
                }
                throw new AuthenticationFailedException("Authentication Failed");
            }

        } catch (AuthenticatorException e) {
            throw new AuthenticationFailedException("USSD Authentication failed while trying to authenticate", e);
        } catch (UserRegistrationAdminServiceIdentityException | RemoteException e) {
            throw new AuthenticationFailedException("Error occurred while creating user profile", e);
        }
        AuthenticationContextHelper.setSubject(context, msisdn);

        log.info("USSD Authenticator authentication success");
        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
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
        return Constants.USSD_AUTHENTICATOR_FRIENDLY_NAME;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
     */
    @Override
    public String getName() {
        return Constants.USSD_AUTHENTICATOR_NAME;
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
