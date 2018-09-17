/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com)
 * <p>
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.gsma.authenticators.sms;

import com.wso2telco.Util;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.sp.config.utils.exception.DataAccessException;
import com.wso2telco.core.sp.config.utils.service.SpConfigService;
import com.wso2telco.core.sp.config.utils.service.impl.SpConfigServiceImpl;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.BaseApplicationAuthenticator;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.cryptosystem.AESencrp;
import com.wso2telco.gsma.authenticators.model.SMSMessage;
import com.wso2telco.gsma.authenticators.util.*;
import com.wso2telco.gsma.shorten.SelectShortUrl;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hashids.Hashids;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc

/**
 * The Class SMSAuthenticator.
 */
public class SMSAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator, BaseApplicationAuthenticator {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -1189332409518227376L;

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(SMSAuthenticator.class);

    /**
     * The Configuration service
     */
    protected static ConfigurationService configurationService = new ConfigurationServiceImpl();

    protected SpConfigService spConfigService = new SpConfigServiceImpl();

    private static final String AUTH_FAILED = "Authentication failed";

    private static final String AUTH_FAILED_DETAILED = "SMS Authentication failed while trying to authenticate";

    /**
     * The Enum UserResponse.
     */
    protected enum UserResponse {

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
        REJECTED,

        /**
         * The Expired.
         */
        EXPIRED

    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax
     * .servlet.http.HttpServletRequest)
     */
    @Override
    public boolean canHandle(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug(this.getClass().getName() + " canHandle invoked");
        }

        return Boolean.valueOf(request.getParameter("canHandle"));
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#process
     * (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity
     * .application.authentication.framework.context.AuthenticationContext)
     */
    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,

                                           AuthenticationContext context) throws AuthenticationFailedException,
            LogoutFailedException {
        DataPublisherUtil.updateAndPublishUserStatus(
                (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
                DataPublisherUtil.UserState.SMS_AUTH_PROCESSING, this.getClass().getName() + " processing started");
        return initAuthFlowStatus(request, response, context);
    }

    protected AuthenticatorFlowStatus initAuthFlowStatus(HttpServletRequest request, HttpServletResponse response,
                                                         AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException {
        if (context.isLogoutRequest()) {
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        } else {
            return super.process(request, response, context);
        }
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework
     * .AbstractApplicationAuthenticator#initiateAuthenticationRequest(javax.servlet.http.HttpServletRequest, javax
     * .servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context
     * .AuthenticationContext)
     */
    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {
        log.info("Initiating authentication request");

        DataPublisherUtil.updateAndPublishUserStatus(
                (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
                DataPublisherUtil.UserState.SMS_AUTH_PROCESSING, this.getClass().getName() + " processing started");

        UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);
        SMSMessage smsMessage = getRedirectInitAuthentication(response, context, userStatus);
        if (smsMessage != null && smsMessage.getRedirectURL() != null && !smsMessage.getRedirectURL().isEmpty()) {
            try {
                BasicFutureCallback futureCallback =
                        userStatus != null ? new SMSFutureCallback(userStatus.cloneUserStatus(), "SMS") : new
                                SMSFutureCallback();
                smsMessage.setFutureCallback(futureCallback);
                String smsResponse = new SendSMS()
                        .sendSMS(smsMessage.getMsisdn(), smsMessage.getMessageText(), smsMessage.getOperator(),
                                smsMessage.getFutureCallback());
                response.sendRedirect(smsMessage.getRedirectURL());
            } catch (IOException e) {
                DataPublisherUtil
                        .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.SMS_AUTH_PROCESSING_FAIL,
                                e.getMessage());
                log.error(AUTH_FAILED, e);
                throw new AuthenticationFailedException(e.getMessage(), e);
            }
        } else {
            log.error(AUTH_FAILED_DETAILED);
            throw new AuthenticationFailedException(AUTH_FAILED_DETAILED);
        }
    }

    protected SMSMessage getRedirectInitAuthentication(HttpServletResponse response, AuthenticationContext context,
                                                       UserStatus userStatus) throws AuthenticationFailedException {
        SMSMessage smsMessage = null;
        String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                        context.getCallerSessionKey(),
                        context.getContextIdentifier());

        if (log.isDebugEnabled()) {
            log.debug("Query parameters : " + queryParams);
        }

        try {
            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            } else {
                // Insert entry to DB only if this is not a retry
                DBUtils.insertUserResponse(context.getContextIdentifier(), UserResponse.PENDING.name());
            }

            //MSISDN will be saved in the context in the MSISDNAuthenticator
            String msisdn = (String) context.getProperty(Constants.MSISDN);
            Application application = new Application();

            MobileConnectConfig connectConfig = configurationService.getDataHolder().getMobileConnectConfig();
            MobileConnectConfig.SMSConfig smsConfig = connectConfig.getSmsConfig();

            String encryptedContextIdentifier = AESencrp.encrypt(context.getContextIdentifier());
            String messageURL = connectConfig.getSmsConfig().getAuthUrl() + Constants.AUTH_URL_ID_PREFIX;

            Map<String, String> paramMap = Util.createQueryParamMap(queryParams);
            String client_id = paramMap.get(Constants.CLIENT_ID);
            String operator = (String) context.getProperty(Constants.OPERATOR);

            if (smsConfig.isShortUrl()) {
                // If a URL shortening service is enabled, then we need to encrypt the context identifier, create the
                // message URL and shorten it.
                log.info("URL shortening service is enabled");
                SelectShortUrl selectShortUrl = new SelectShortUrl();
                messageURL = selectShortUrl.getShortUrl(smsConfig.getShortUrlClass(),
                        messageURL + response.encodeURL(encryptedContextIdentifier), smsConfig.getAccessToken(),
                        smsConfig.getShortUrlService());
            } else {
                // If a URL shortening service is not enabled, we need to created a hash key for the encrypted
                // context identifier and insert a database entry mapping ths hash key to the context identifier.
                // This is done to shorten the message URL as much as possible.
                log.info("Generating hash key for the SMS");
                String hashForContextId = getHashForContextId(encryptedContextIdentifier);
                messageURL += hashForContextId;
                DBUtils.insertHashKeyContextIdentifierMapping(hashForContextId, context.getContextIdentifier());
            }

            // prepare the USSD message from template
            HashMap<String, String> variableMap = new HashMap<String, String>();
            variableMap.put("application", application
                    .changeApplicationName(context.getSequenceConfig().getApplicationConfig().getApplicationName()));
            variableMap.put("link", messageURL);
            boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
            OutboundMessage.MessageType messageType = OutboundMessage.MessageType.SMS_LOGIN;

            if (isRegistering) {
                messageType = OutboundMessage.MessageType.SMS_REGISTRATION;
            }
            String messageText = OutboundMessage
                    .prepare(client_id, messageType, variableMap, operator);

            if (log.isDebugEnabled()) {
                log.debug("Message URL: " + messageURL);
                log.debug("Message: " + messageText);
                log.debug("Operator: " + operator);
            }

            DBUtils.insertAuthFlowStatus(msisdn, Constants.STATUS_PENDING, context.getContextIdentifier());

            smsMessage = new SMSMessage();
            smsMessage.setMsisdn(msisdn);
            smsMessage.setMessageText(messageText);
            smsMessage.setOperator(operator);
            smsMessage.setClient_id(client_id);
            smsMessage.setRedirectURL(
                    response.encodeRedirectURL(loginPage + ("?" + queryParams)) + "&authenticators=" + getName() + ":"
                            + "LOCAL" + retryParam);

        } catch (Exception e) {
            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.SMS_AUTH_PROCESSING_FAIL,
                            e.getMessage());
            log.error(AUTH_FAILED, e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
        return smsMessage;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework
     * .AbstractApplicationAuthenticator#processAuthenticationResponse(javax.servlet.http.HttpServletRequest, javax
     * .servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context
     * .AuthenticationContext)
     */
    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {
        log.info("Processing authentication response");

        UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);
        String sessionDataKey = request.getParameter("sessionDataKey");
        String msisdn = (String) context.getProperty("msisdn");
        String operator = (String) context.getProperty("operator");
        String userAction = request.getParameter(Constants.ACTION);

        if (log.isDebugEnabled()) {
            log.debug("SessionDataKey : " + sessionDataKey);
        }
        try {

            if (userAction != null && !userAction.isEmpty()) {

                log.info("User action from UI : " + userAction);

                // Change behaviour depending on user action
                switch (userAction) {
                    case Constants.USER_ACTION_USER_CANCELED:
                        //User clicked cancel button from login
                        DataPublisherUtil
                                .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState
                                                .SMS_AUTH_PROCESSING_FAIL,

                                        "User Terminated Authentication Flow");
                        terminateAuthentication(context, sessionDataKey, UserResponse.REJECTED.name(), UserResponse
                                .EXPIRED.name());
                        break;
                    case Constants.USER_ACTION_REG_REJECTED:
                        DataPublisherUtil
                                .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState
                                                .SMS_AUTH_PROCESSING_FAIL,
                                        "User Registration Rejected");
                        //User clicked cancel button from registration
                        terminateAuthentication(context, sessionDataKey, UserResponse.REJECTED.name(), UserResponse
                                .EXPIRED.name());
                        break;
                    case Constants.USER_ACTION_USER_TIMEOUT:
                        DataPublisherUtil
                                .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState
                                                .SMS_AUTH_PROCESSING_FAIL,
                                        "User Timeout occurred");
                        //User timeout at login
                        terminateAuthentication(context, sessionDataKey, UserResponse.REJECTED.name(), UserResponse
                                .EXPIRED.name());
                        break;
                }
            }

            // Check if the user has provided consent
            String responseStatus = DBUtils.getAuthFlowStatus(sessionDataKey);
            if (!responseStatus.equalsIgnoreCase(UserResponse.APPROVED.name())) {
                log.error(AUTH_FAILED);
                throw new AuthenticatorException(AUTH_FAILED);
            } else {
                boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
                boolean isStatusUpdate = (boolean) context.getProperty(Constants.IS_STATUS_TO_CHANGE);
                boolean isAttributeScope = (Boolean) context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE);
                String spType = context.getProperty(Constants.TRUSTED_STATUS).toString();
                String attrShareType = context.getProperty(Constants.ATTRSHARE_SCOPE_TYPE).toString();

                if (isRegistering || isStatusUpdate) {
                    UserProfileManager userProfileManager = new UserProfileManager();
                    userProfileManager.createUserProfileLoa2(msisdn, operator, isAttributeScope, spType, attrShareType);

                    MobileConnectConfig.SMSConfig smsConfig = configurationService.getDataHolder()
                            .getMobileConnectConfig().getSmsConfig();
                    if (!smsConfig.getWelcomeMessageDisabled()) {
                        WelcomeSmsUtil.handleWelcomeSms(context, userStatus, msisdn, operator, smsConfig);
                        log.info("Welcome SMS sent");
                    }
                }
                if(context.getProperty(Constants.API_SCOPES) != null)
                    new UserProfileManager().updateMIGUserRoles(msisdn,context.getProperty(Constants.CLIENT_ID).toString(),context.getProperty(Constants.API_SCOPES).toString());

            }

        } catch (AuthenticatorException | RemoteException | UserRegistrationAdminServiceIdentityException e) {
            log.error(AUTH_FAILED_DETAILED, e);
            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.SMS_AUTH_PROCESSING_FAIL,
                            e.getMessage());
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (IOException | DataAccessException e) {
            log.error("Welcome SMS sending failed in processAuthenticationResponse in SMSAuthenticator", e);
        }
        AuthenticationContextHelper.setSubject(context, msisdn);

        log.info("Authentication success");

        DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.SMS_AUTH_SUCCESS,
                "SMS Authentication success");

        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
    }

    /**
     * Terminates the authenticator due to user implicit action
     *
     * @param context        Authentication Context
     * @param authFlowStatus Authflow status
     * @param sessionID      sessionID
     * @param userResponse   user response status
     * @throws AuthenticationFailedException
     */

    private void terminateAuthentication(AuthenticationContext context, String sessionID, String userResponse, String
            authFlowStatus) throws AuthenticationFailedException {
        log.info("User has terminated the authentication flow");
        context.setProperty(Constants.IS_TERMINATED, true);
        try {
            DBUtils.updateUserResponse(sessionID, userResponse);
            if (!DBUtils.getAuthFlowStatus(sessionID).equalsIgnoreCase(UserResponse.APPROVED.name()))
                DBUtils.updateAuthFlowStatus(sessionID, authFlowStatus);
        } catch (AuthenticatorException e) {
            log.error("Authentication Exception occurred in terminateAuthentication method in SMSAuthenticator", e);
        }
        throw new AuthenticationFailedException("Authenticator is terminated");
    }

    protected String getHashForContextId(String contextIdentifier) {
        int hashLength = 7;

        Hashids hashids = new Hashids(contextIdentifier, hashLength);

        return hashids.encode(new Date().getTime());
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework
     * .AbstractApplicationAuthenticator#retryAuthenticationEnabled()
     */
    @Override
    protected boolean retryAuthenticationEnabled() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework
     * .ApplicationAuthenticator#getContextIdentifier(javax.servlet.http.HttpServletRequest)
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
        return Constants.SMS_AUTHENTICATOR_FRIENDLY_NAME;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
     */
    @Override
    public String getName() {
        return Constants.SMS_AUTHENTICATOR_NAME;
    }

    @Override
    public String getAmrValue(int acr) {
        return "SMS_URL_OK";
    }
}
