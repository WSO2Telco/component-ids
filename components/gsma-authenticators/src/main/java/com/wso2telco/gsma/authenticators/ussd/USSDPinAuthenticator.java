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

import com.wso2telco.Util;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.model.PinConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.config.util.PinConfigUtil;
import com.wso2telco.core.sp.config.utils.exception.DataAccessException;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.BaseApplicationAuthenticator;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.ussd.command.PinLoginUssdCommand;
import com.wso2telco.gsma.authenticators.ussd.command.PinRegistrationUssdCommand;
import com.wso2telco.gsma.authenticators.ussd.command.UssdCommand;
import com.wso2telco.gsma.authenticators.util.*;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.*;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


// TODO: Auto-generated Javadoc

/**
 * The Class USSDPinAuthenticator.
 */
public class USSDPinAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator, BaseApplicationAuthenticator {

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

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax
     * .servlet.http.HttpServletRequest)
     */
    @Override
    public boolean canHandle(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("USSD Authenticator canHandle invoked");
        }
        return true;
    }

    private boolean canProcessResponse(AuthenticationContext context) {
        return context.getProperty(Constants.REDIRECT_CONSENT) == null || !(Boolean) context.getProperty(Constants
                .REDIRECT_CONSENT);
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#process
     * (javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity
     * .application.authentication.framework.context.AuthenticationContext)
     */
    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request,
                                           HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException {

        DataPublisherUtil
                .updateAndPublishUserStatus((UserStatus) context.getParameter(
                        Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
                        DataPublisherUtil.UserState.USSDPIN_AUTH_PROCESSING,
                        "USSDPinAuthenticator processing started");

        if (context.isLogoutRequest()) {
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        } else {
            return processRequest(request, response, context);
        }
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework
     * .AbstractApplicationAuthenticator#initiateAuthenticationRequest(javax.servlet.http.HttpServletRequest, javax
     * .servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context
     * .AuthenticationContext)
     */
    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {
        log.info("Initiating authentication request");

        UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);

        if (context.getProperty(Constants.IS_PIN_RESET) == null) {
            context.setProperty(Constants.IS_PIN_RESET, false);
        }
        String retryParam = "";
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
        boolean isPinReset = (boolean) context.getProperty(Constants.IS_PIN_RESET);
        boolean isProfileUpgrade = (boolean) context.getProperty(Constants.IS_PROFILE_UPGRADE);
        boolean securityQuestionsShown = context.getProperty(Constants.IS_SECURITY_QUESTIONS_SHOWN) != null &&
                (boolean) context.getProperty(Constants.IS_SECURITY_QUESTIONS_SHOWN);

        String msisdn = (String) context.getProperty(Constants.MSISDN);

        // USSDPinAuthenticator cannot proceed without an msisdn
        if (StringUtils.isEmpty(msisdn)) {
            terminateAuthentication(context);
        }

        String serviceProviderName = context.getSequenceConfig().getApplicationConfig().getApplicationName();

        if (log.isDebugEnabled()) {
            log.debug("Registering : " + isRegistering);
            log.debug("Pin reset : " + isPinReset);
            log.debug("MSISDN : " + msisdn);
            log.debug("Service provider : " + serviceProviderName);
        }

        try {

            String loginPage = getAuthEndpointUrl(context);

            String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(),
                    context.getCallerSessionKey(), context.getContextIdentifier());

            if (serviceProviderName.equals("wso2_sp_dashboard")) {
                serviceProviderName = configurationService.getDataHolder().getMobileConnectConfig().getUssdConfig()
                        .getDashBoard();
            }
            String operator = (String) context.getProperty("operator");

            savePinConfigToContext(context, isRegistering, msisdn, isPinReset, isProfileUpgrade);

            // send ussd message only when, request is not pin reset and any of followings,
            // securityQuestionsShown : when user has entered security questions when profile upgrade flow
            // or when user comes via LOA 3 login
            if (securityQuestionsShown || (!isRegistering && !isProfileUpgrade && !isPinReset)) {
                DBUtils.insertAuthFlowStatus(msisdn, Constants.STATUS_PENDING, context.getContextIdentifier());
                USSDPinFutureCallback futureCallback = userStatus != null ?
                        new USSDPinFutureCallback(userStatus.cloneUserStatus()) :
                        new USSDPinFutureCallback();
                sendUssd(context, isRegistering, msisdn, serviceProviderName, operator, futureCallback);
            }

            String redirectUrl = response.encodeRedirectURL(loginPage + ("?" + queryParams))
                    + "&redirect_uri=" + context.getProperty("redirectURI")
                    + "&authenticators=" + getName() + ":" + "LOCAL" + retryParam + "&sessionDataKey=" +
                    context.getContextIdentifier();

            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.USSDPIN_REDIRECT,
                            "Redirect URL : " + redirectUrl);

            response.sendRedirect(redirectUrl);

        } catch (IOException e) {
            DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                    DataPublisherUtil.UserState.USSDPIN_AUTH_PROCESSING_FAIL,
                    e.getMessage());
            log.error("Error occurred while redirecting the request", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (SQLException | AuthenticatorException e) {
            DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                    DataPublisherUtil.UserState.USSDPIN_AUTH_PROCESSING_FAIL,
                    e.getMessage());
            log.error("Error occurred while inserting registration status", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
            DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                    DataPublisherUtil.UserState.USSDPIN_AUTH_PROCESSING_FAIL,
                    e.getMessage());
            log.error("Error occurred while getting user pin", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }

    private void savePinConfigToContext(AuthenticationContext context, boolean isRegistering, String msisdn,
                                        boolean isPinReset, boolean isProfileUpgrade)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {


        PinConfig pinConfig;
        if (isRegistering || isProfileUpgrade) {

            pinConfig = new PinConfig();
            pinConfig.setInvalidFormatAttempts(0);
            pinConfig.setCurrentStep(PinConfig.CurrentStep.REGISTRATION);
        } else {
            UserProfileManager userProfileManager = new UserProfileManager();
            if (isPinReset) {
                pinConfig = PinConfigUtil.getPinConfig(context);

                Map<String, String> challengeQuestionAnswerMap = new UserProfileManager()
                        .getChallengeQuestionAndAnswers(msisdn);

                String challengeQuestionAndAnswer1 = challengeQuestionAnswerMap.get(
                        Constants.CHALLENGE_QUESTION_1_CLAIM);
                String challengeQuestionAndAnswer2 = challengeQuestionAnswerMap.get(
                        Constants.CHALLENGE_QUESTION_2_CLAIM);

                pinConfig.setChallengeQuestion1(challengeQuestionAndAnswer1.split("!")[0]);
                pinConfig.setChallengeQuestion2(challengeQuestionAndAnswer2.split("!")[0]);
                pinConfig.setChallengeAnswer1(challengeQuestionAndAnswer1.split("!")[1]);
                pinConfig.setChallengeAnswer2(challengeQuestionAndAnswer2.split("!")[1]);

            } else {
                pinConfig = new PinConfig();
                String registeredPin = userProfileManager.getCurrentPin(msisdn);
                pinConfig.setRegisteredPin(registeredPin);
                pinConfig.setCurrentStep(PinConfig.CurrentStep.LOGIN);
            }
        }
        pinConfig.setMsisdn(msisdn);
        pinConfig.setPinMismatchAttempts(0);
        pinConfig.setSessionId(context.getContextIdentifier());
        pinConfig.setTotalAttempts(0);

        PinConfigUtil.savePinConfigToContext(pinConfig, context);
    }

    private void sendUssd(AuthenticationContext context, boolean isRegistering, String msisdn,
                          String serviceProviderName, String operator, BasicFutureCallback futureCallback)
            throws SQLException, AuthenticatorException, IOException {
        UssdCommand ussdCommand;
        boolean isProfileUpgrade = (boolean) context.getProperty(Constants.IS_PROFILE_UPGRADE);
        if (isRegistering || isProfileUpgrade) {
            ussdCommand = new PinRegistrationUssdCommand();
        } else {
            ussdCommand = new PinLoginUssdCommand();
        }

        String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(),
                context.getCallerSessionKey(), context.getContextIdentifier());
        Map<String, String> paramMap = Util.createQueryParamMap(queryParams);
        String client_id = paramMap.get(Constants.CLIENT_ID);

        ussdCommand.execute(msisdn, context.getContextIdentifier(), serviceProviderName, operator, client_id,
                futureCallback);
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
        UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);

        log.info("Processing authentication response");

        String msisdn = (String) context.getProperty(Constants.MSISDN);
        PinConfig pinConfig = PinConfigUtil.getPinConfig(context);

        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
        boolean isStatusUpdate =  (boolean)context.getProperty(Constants.IS_STATUS_TO_CHANGE);
        boolean isProfileUpgrade = (boolean) context.getProperty(Constants.IS_PROFILE_UPGRADE);
        boolean isPinReset = isPinReset(pinConfig);
        boolean isPinResetConfirmation = isPinResetConfirmation(pinConfig);

        if (log.isDebugEnabled()) {
            log.debug("MSISDN : " + msisdn);
            log.debug("Registering : " + isRegistering);
            log.debug("Profile upgrade : " + isProfileUpgrade);
            log.debug("Pin reset : " + isPinReset);
            log.debug("Pin reset confirmation : " + isPinResetConfirmation);
        }

        String userAction = request.getParameter(Constants.ACTION);
        if (userAction != null && !userAction.isEmpty()) {
            // Change behaviour depending on user action
            switch (userAction) {
                case Constants.USER_ACTION_USER_CANCELED:
                    //User rejected to login consent
                    terminateAuthentication(context);
                    break;
                case Constants.USER_ACTION_REG_REJECTED:
                    //User rejected to registration consent
                    terminateAuthentication(context);
                    break;
                case Constants.USER_ACTION_UPGRADE_REJECTED:
                    //User rejected to profile upgrade consent
                    terminateAuthentication(context);
                    break;
            }
        }

        try {
            if (isRegistering || isStatusUpdate) {
                handleUserRegistration(context, userStatus);
            } else {
                if (isProfileUpgrade) {
                    handleProfileUpgrade(context);
                } else if (isPinReset) {
                    retryAuthenticatorForPinReset(context);
                } else if (isPinResetConfirmation) {
                    handlePinResetConfirmation(msisdn, pinConfig);
                } else {
                    handleUserLogin(context);
                }
            }
            AuthenticationContextHelper.setSubject(context, msisdn);

            context.setRememberMe(false);
            log.info("Authentication success");

        } catch (UserRegistrationAdminServiceIdentityException | RemoteException e) {
            DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                    DataPublisherUtil.UserState.USSDPIN_AUTH_PROCESSING_FAIL,
                    e.getMessage());
            log.error("Error occurred while creating user profile", e);
            terminateAuthentication(context);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                    DataPublisherUtil.UserState.USSDPIN_AUTH_PROCESSING_FAIL,
                    e.getMessage());
            log.error("Error occurred while accessing admin services", e);
            terminateAuthentication(context);
        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
            DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                    DataPublisherUtil.UserState.USSDPIN_AUTH_PROCESSING_FAIL,
                    e.getMessage());
            log.error("Error occurred while updating user profile", e);
            terminateAuthentication(context);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                    DataPublisherUtil.UserState.USSDPIN_AUTH_PROCESSING_FAIL,
                    e.getMessage());
            log.error("Error occurred while hashing the pin", e);
            terminateAuthentication(context);
        }
    }

    private void terminateAuthentication(AuthenticationContext context) throws AuthenticationFailedException {
        UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);
        DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                DataPublisherUtil.UserState.USSDPIN_AUTH_PROCESSING_FAIL,
                "User has terminated the authentication flow");
        log.info("User has terminated the authentication flow");

        context.setProperty(Constants.IS_TERMINATED, true);
        throw new AuthenticationFailedException("Authenticator is terminated");
    }

    public AuthenticatorFlowStatus processRequest(HttpServletRequest request, HttpServletResponse response,
                                                  AuthenticationContext context) throws
            AuthenticationFailedException, LogoutFailedException {

        if (context.isLogoutRequest()) {
            try {
                if (!canHandle(request)) {
                    context.setCurrentAuthenticator(getName());
                    initiateLogoutRequest(request, response, context);
                    return AuthenticatorFlowStatus.INCOMPLETE;
                } else {
                    processLogoutResponse(request, response, context);
                    return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                }
            } catch (UnsupportedOperationException var8) {
                if (log.isDebugEnabled()) {
                    log.debug("Ignoring UnsupportedOperationException.", var8);
                }

                return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
            }
        } else if (canHandle(request) && (request.getAttribute("commonAuthHandled") == null || !(Boolean) request
                .getAttribute("commonAuthHandled"))) {
            try {
                processAuthenticationResponse(request, response, context);
                if (this instanceof LocalApplicationAuthenticator && !context.getSequenceConfig()
                        .getApplicationConfig().isSaaSApp()) {
                    String e = context.getSubject().getTenantDomain();
                    String stepMap1 = context.getTenantDomain();
                    if (!StringUtils.equals(e, stepMap1)) {
                        context.setProperty("UserTenantDomainMismatch", Boolean.valueOf(true));
                        throw new AuthenticationFailedException("Service Provider tenant domain must be equal to user" +
                                " tenant domain for non-SaaS applications");
                    }
                }

                request.setAttribute("commonAuthHandled", Boolean.TRUE);
                publishAuthenticationStepAttempt(request, context, context.getSubject(), true);
                return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
            } catch (AuthenticationFailedException e) {
                Object property = context.getProperty(Constants.IS_TERMINATED);
                boolean isTerminated = false;
                if (property != null) {
                    isTerminated = (boolean) property;
                }

                Map stepMap = context.getSequenceConfig().getStepMap();
                boolean stepHasMultiOption = false;
                publishAuthenticationStepAttempt(request, context, e.getUser(), false);
                if (stepMap != null && !stepMap.isEmpty()) {
                    StepConfig stepConfig = (StepConfig) stepMap.get(Integer.valueOf(context.getCurrentStep()));
                    if (stepConfig != null) {
                        stepHasMultiOption = stepConfig.isMultiOption();
                    }
                }

                if (isTerminated) {
                    throw new AuthenticationFailedException("Authenticator is terminated");
                }
                if (retryAuthenticationEnabled() && !stepHasMultiOption) {
                    context.setRetrying(true);
                    context.setCurrentAuthenticator(getName());
                    initiateAuthenticationRequest(request, response, context);
                    return AuthenticatorFlowStatus.INCOMPLETE;
                } else {
                    throw e;
                }
            }
        } else {
            initiateAuthenticationRequest(request, response, context);
            context.setCurrentAuthenticator(getName());
            return AuthenticatorFlowStatus.INCOMPLETE;
        }
    }

    private void publishAuthenticationStepAttempt(HttpServletRequest request, AuthenticationContext context, User
            user, boolean success) {
        AuthenticationDataPublisher authnDataPublisherProxy = FrameworkServiceDataHolder.getInstance()
                .getAuthnDataPublisherProxy();
        if (authnDataPublisherProxy != null && authnDataPublisherProxy.isEnabled(context)) {
            boolean isFederated = this instanceof FederatedApplicationAuthenticator;
            HashMap paramMap = new HashMap();
            paramMap.put("user", user);
            if (isFederated) {
                context.setProperty("hasFederatedStep", Boolean.valueOf(true));
                paramMap.put("isFederated", Boolean.valueOf(true));
            } else {
                context.setProperty("hasLocalStep", Boolean.valueOf(true));
                paramMap.put("isFederated", Boolean.valueOf(false));
            }

            Map unmodifiableParamMap = Collections.unmodifiableMap(paramMap);
            if (success) {
                authnDataPublisherProxy.publishAuthenticationStepSuccess(request, context, unmodifiableParamMap);
            } else {
                authnDataPublisherProxy.publishAuthenticationStepFailure(request, context, unmodifiableParamMap);
            }
        }

    }

    private void handlePinResetConfirmation(String msisdn, PinConfig pinConfig) throws RemoteException,
            NoSuchAlgorithmException,
            RemoteUserStoreManagerServiceUserStoreExceptionException, UnsupportedEncodingException {

        new UserProfileManager().setCurrentPin(msisdn, pinConfig.getConfirmedPin());
    }

    private boolean isPinResetConfirmation(PinConfig pinConfig) {
        return pinConfig.getCurrentStep() == PinConfig.CurrentStep.PIN_RESET_CONFIRMATION;
    }

    private void retryAuthenticatorForPinReset(AuthenticationContext context) throws AuthenticationFailedException,
            RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        log.info("Retrying authenticator for pin reset flow");
        String msisdn = (String) context.getProperty(Constants.MSISDN);


        Map<String, String> challengeQuestionAnswerMap = new UserProfileManager().getChallengeQuestionAndAnswers
                (msisdn);

        String challengeQuestionAndAnswer1 = challengeQuestionAnswerMap.get(Constants.CHALLENGE_QUESTION_1_CLAIM);
        String challengeQuestionAndAnswer2 = challengeQuestionAnswerMap.get(Constants.CHALLENGE_QUESTION_2_CLAIM);


        String challengeQuestion1 = challengeQuestionAndAnswer1.split("!")[0];
        String challengeQuestion2 = challengeQuestionAndAnswer2.split("!")[0];
        String challengeAnswer1 = challengeQuestionAndAnswer1.split("!")[1];
        String challengeAnswer2 = challengeQuestionAndAnswer2.split("!")[1];

        context.setProperty(Constants.IS_PIN_RESET, true);
        context.setProperty(Constants.CHALLENGE_QUESTION_1, challengeQuestion1);
        context.setProperty(Constants.CHALLENGE_QUESTION_2, challengeQuestion2);

        PinConfig pinConfig = PinConfigUtil.getPinConfig(context);
        pinConfig.setChallengeQuestion1(challengeQuestion1);
        pinConfig.setChallengeQuestion2(challengeQuestion2);
        pinConfig.setChallengeAnswer1(challengeAnswer1);
        pinConfig.setChallengeAnswer1(challengeAnswer2);

        throw new AuthenticationFailedException("User entered an incorrect pin for login. Moving to pin reset");
    }

    private boolean isPinReset(PinConfig pinConfig) {
        return pinConfig.getCurrentStep() == PinConfig.CurrentStep.PIN_RESET;
    }

    private void handleProfileUpgrade(AuthenticationContext context) throws
            RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException, UnsupportedEncodingException,
            NoSuchAlgorithmException, AuthenticationFailedException {
        boolean securityQuestionsShown = context.getProperty(Constants.IS_SECURITY_QUESTIONS_SHOWN) != null &&
                (boolean) context.getProperty(Constants.IS_SECURITY_QUESTIONS_SHOWN);
        boolean pinRegistered = context.getProperty(Constants.IS_SECURITY_QUESTIONS_ANSWERED) != null && (boolean)
                context.getProperty(Constants.IS_SECURITY_QUESTIONS_ANSWERED);

        if (securityQuestionsShown && pinRegistered) {
            String challengeAnswer1 = (String) context.getProperty(Constants.CHALLENGE_ANSWER_1);
            String challengeAnswer2 = (String) context.getProperty(Constants.CHALLENGE_ANSWER_2);
            String challengeQuestion1 = (String) context.getProperty(Constants.CHALLENGE_QUESTION_1);
            String challengeQuestion2 = (String) context.getProperty(Constants.CHALLENGE_QUESTION_2);
            String msisdn = (String) context.getProperty(Constants.MSISDN);
            PinConfig pinConfig = (PinConfig) context.getProperty(com.wso2telco.core.config.util.Constants
                    .PIN_CONFIG_OBJECT);

            if (log.isDebugEnabled()) {
                log.debug("Updating user profile from LOA2 to LOA3 flow [ msisdn : " + msisdn + " , challenge " +
                        "question 1 : " +
                        challengeQuestion1 + " , challenge answer 1 : " + challengeAnswer1 + " , challenge question 2" +
                        " : " +
                        challengeQuestion2 + " , challenge answer 2 : " + challengeAnswer2 + " ] ");
            }

            challengeAnswer1 = challengeQuestion1 + Constants.USER_CHALLENGE_SEPARATOR + challengeAnswer1;
            challengeAnswer2 = challengeQuestion2 + Constants.USER_CHALLENGE_SEPARATOR + challengeAnswer2;

            new UserProfileManager().updateUserProfileForLOA3(challengeAnswer1, challengeAnswer2, pinConfig
                    .getConfirmedPin(), msisdn);
        } else {
            context.setProperty(Constants.IS_SECURITY_QUESTIONS_ANSWERED, Boolean.TRUE);

            // throw authentication failed exception to retry the authenticator
            throw new AuthenticationFailedException("Authenticator retry");
        }
    }

    private void handleUserRegistration(AuthenticationContext context, UserStatus userStatus) throws
            UserRegistrationAdminServiceIdentityException, RemoteException, AuthenticationFailedException {
        String challengeAnswer1 = (String) context.getProperty(Constants.CHALLENGE_ANSWER_1);
        String challengeAnswer2 = (String) context.getProperty(Constants.CHALLENGE_ANSWER_2);
        String challengeQuestion1 = (String) context.getProperty(Constants.CHALLENGE_QUESTION_1);
        String challengeQuestion2 = (String) context.getProperty(Constants.CHALLENGE_QUESTION_2);
        String msisdn = (String) context.getProperty(Constants.MSISDN);
        String operator = (String) context.getProperty(Constants.OPERATOR);
        boolean isAttributeScope = (Boolean)context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE);
        String spType = context.getProperty(Constants.TRUSTED_STATUS).toString();
        String attrShareType = context.getProperty(Constants.ATTRSHARE_SCOPE_TYPE).toString();
        PinConfig pinConfig = PinConfigUtil.getPinConfig(context);

        if (pinConfig.isPinsMatched()) {

            if (log.isDebugEnabled()) {
                log.debug("Creating user profile for LOA3 flow [ msisdn : " + msisdn + " , challenge question 1 : " +
                        challengeQuestion1 + " , challenge answer 1 : " + challengeAnswer1 + " , challenge question 2" +
                        " : " +
                        challengeQuestion2 + " , challenge answer 2 : " + challengeAnswer2 + " ] ");
            }

            challengeAnswer1 = challengeQuestion1 + Constants.USER_CHALLENGE_SEPARATOR + challengeAnswer1;
            challengeAnswer2 = challengeQuestion2 + Constants.USER_CHALLENGE_SEPARATOR + challengeAnswer2;

            new UserProfileManager().createUserProfileLoa3(msisdn, operator, challengeAnswer1, challengeAnswer2,
                    pinConfig.getRegisteredPin(),isAttributeScope,spType,attrShareType);

            MobileConnectConfig.SMSConfig smsConfig = configurationService.getDataHolder().getMobileConnectConfig().getSmsConfig();
            if (!smsConfig.getWelcomeMessageDisabled()) {
                try {
                    WelcomeSmsUtil.handleWelcomeSms(context, userStatus, msisdn, operator, smsConfig);
                } catch (DataAccessException | IOException e) {
                    log.error("Welcome SMS sending failed", e);
                }
            }
        } else {
            String errMsg = "Authentication failed for due to mismatch in entered and confirmed pin";
            DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                    DataPublisherUtil.UserState.USSDPIN_AUTH_PROCESSING_FAIL,
                    errMsg);
            throw new AuthenticationFailedException(errMsg);
        }
    }

    private void handleUserLogin(AuthenticationContext context) throws org.wso2.carbon.user.api.UserStoreException,
            AuthenticationFailedException, RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        PinConfig pinConfig = PinConfigUtil.getPinConfig(context);

        if (pinConfig != null) {
            try {
                validatePin(pinConfig, context);
            } catch (AuthenticationFailedException e) {
                //if user did not respond
                terminateAuthentication(context);
            }
        } else {
            throw new AuthenticationFailedException("Cannot find pin information ");
        }
    }

    private void validatePin(PinConfig pinConfig, AuthenticationContext context) throws AuthenticationFailedException {
        UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);

        if (pinConfig.isPinsMatched()) {
            log.info("User entered a correct pin. Authentication Success");
        } else {
            StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(context.getCurrentStep());
            stepConfig.setMultiOption(true);

            DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                    DataPublisherUtil.UserState.USSDPIN_AUTH_PROCESSING_FAIL,
                    "Authentication failed. User entered an incorrect pin");
            log.error("Authentication failed. User entered an incorrect pin");
            throw new AuthenticationFailedException("Authentication failed due to incorrect pin");
        }
    }


    private String getAuthEndpointUrl(AuthenticationContext context) {
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
        boolean isPinReset = (boolean) context.getProperty(Constants.IS_PIN_RESET);
        boolean isProfileUpgrade = (boolean) context.getProperty(Constants.IS_PROFILE_UPGRADE);
        boolean securityQuestionsShown = context.getProperty(Constants.IS_SECURITY_QUESTIONS_SHOWN) != null &&
                (boolean) context.getProperty(Constants.IS_SECURITY_QUESTIONS_SHOWN);
        String loginPage;

        if (securityQuestionsShown) {
            loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl()
                    + Constants.PIN_REGISTRATION_WAITING_JSP;
        } else if (isProfileUpgrade) {
            loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl()
                    + Constants.PROFILE_UPGRADE_JSP;
            context.setProperty(Constants.IS_SECURITY_QUESTIONS_SHOWN, true);
        } else if (isRegistering) {
            loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl()
                    + Constants.PIN_REGISTRATION_JSP;
            context.setProperty(Constants.IS_SECURITY_QUESTIONS_SHOWN, true);
        } else if (isPinReset) {
            loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl()
                    + Constants.PIN_RESET_JSP;
        } else {
            loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        }
        return loginPage;
    }


    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework
     * .AbstractApplicationAuthenticator#retryAuthenticationEnabled()
     */
    @Override
    protected boolean retryAuthenticationEnabled() {
        return true;
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
        return Constants.USSDPIN_AUTHENTICATOR_FRIENDLY_NAME;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
     */
    @Override
    public String getName() {
        return Constants.USSDPIN_AUTHENTICATOR_NAME;
    }

    @Override
    public String getAmrValue(int acr) {
        return "USSD_PIN";
    }

    private enum AuthenticatorState {
        Initiating
    }
}
