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
package com.wso2telco.gsma.authenticators;

import com.wso2telco.Util;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.attributeShare.AttributeShareFactory;
import com.wso2telco.gsma.authenticators.util.AdminServiceUtil;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.authenticators.util.FrameworkServiceDataHolder;
import com.wso2telco.gsma.authenticators.util.UserProfileManager;
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
import org.wso2.carbon.identity.oauth.common.OAuthConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

// TODO: Auto-generated Javadoc

/**
 * The Class MSISDNAuthenticator.
 */
public class MSISDNAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator, BaseApplicationAuthenticator {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 6817280268460894001L;

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(MSISDNAuthenticator.class);

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    private static final String STATUS_ACTIVE = "ACTIVE";

    private static final String STATUS_PARTIALLY_ACTIVE = "PARTIALLY_ACTIVE";

    private static final int ACR3 = 3;

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax
     * .servlet.http.HttpServletRequest)
     */
    @Override
    public boolean canHandle(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("MSISDN Authenticator canHandle invoked");
        }


        if ((request.getParameter(Constants.ACTION) != null && !request.getParameter(Constants.ACTION).isEmpty()) ||
                (request.getParameter(Constants.MSISDN) != null && !request.getParameter(Constants.MSISDN).isEmpty())) {
            log.info("msisdn forwarding ");
            return true;
        }

        return false;
    }


    private boolean canProcessResponse(AuthenticationContext context) {
        return ((context.getProperty(Constants.MSISDN) != null && !context.getProperty(Constants.MSISDN).toString()
                .isEmpty()) && (context.getProperty(Constants.REDIRECT_CONSENT) == null || !(Boolean) context
                .getProperty(Constants.REDIRECT_CONSENT)));
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

        DataPublisherUtil.updateAndPublishUserStatus((UserStatus) context.getParameter(Constants
                        .USER_STATUS_DATA_PUBLISHING_PARAM),
                DataPublisherUtil.UserState.MSISDN_AUTH_PROCESSING, "MSISDNAuthenticator processing started");

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

        String loginPage;
        boolean expliciteScope = false;
        String displayScopes = "";
        try {

            loginPage = getAuthEndpointUrl(context,expliciteScope);

            String queryParams = FrameworkUtils
                    .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                            context.getCallerSessionKey(),
                            context.getContextIdentifier());
            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            }

            DataPublisherUtil
                    .updateAndPublishUserStatus((UserStatus) context.getParameter(Constants
                            .USER_STATUS_DATA_PUBLISHING_PARAM), DataPublisherUtil.UserState
                            .REDIRECT_TO_CONSENT_PAGE, "Redirecting to consent page");


            boolean isattribute = (boolean) context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE);

            if(isattribute){
                String operator = context.getProperty(Constants.OPERATOR).toString();;
                String clientId = context.getProperty(Constants.CLIENT_ID).toString();
                Map<String, List<String>> attributeset = AttributeShareFactory.getAttributeSharable(context.getProperty(Constants.TRUSTED_STATUS).toString()).getAttributeMap(context);
                if(!attributeset.get("explicitScopes").isEmpty()){
                    expliciteScope = true;
                    displayScopes = Arrays.toString(attributeset.get("explicitScopes").toArray());
                }

            }

            if(expliciteScope){
                response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams)) + "&redirect_uri=" +
                        request.getParameter("redirect_uri") + "&authenticators="
                        + getName() + ":" + "LOCAL" + retryParam + OAuthConstants.SESSION_DATA_KEY + "="
                        + context.getContextIdentifier() + "&skipConsent=true&scope=" + displayScopes + "&registering=" + (boolean) context.getProperty(Constants.IS_REGISTERING));
            } else {
                response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams)) + "&redirect_uri=" +
                        request.getParameter("redirect_uri") + "&authenticators="
                        + getName() + ":" + "LOCAL" + retryParam);
            }

        } catch (IOException e) {
            log.error("Error occurred while redirecting request", e);
            DataPublisherUtil
                    .updateAndPublishUserStatus((UserStatus) context.getParameter(Constants
                            .USER_STATUS_DATA_PUBLISHING_PARAM), DataPublisherUtil.UserState
                            .MSISDN_AUTH_PROCESSING_FAIL, e.getMessage());
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error occurred while redirecting request", e);
            DataPublisherUtil
                    .updateAndPublishUserStatus((UserStatus) context.getParameter(Constants
                            .USER_STATUS_DATA_PUBLISHING_PARAM), DataPublisherUtil.UserState
                            .MSISDN_AUTH_PROCESSING_FAIL, e.getMessage());
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
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

        String msisdn;
        boolean isShowTnC = (boolean) context.getProperty(Constants.IS_SHOW_TNC);

        try {
            if (context.getProperty(Constants.MSISDN) == null && (request.getParameter(Constants.MSISDN) != null &&
                    !request.getParameter(Constants.MSISDN).isEmpty())) {
                msisdn = request.getParameter(Constants.MSISDN);
                context.setProperty(Constants.MSISDN, msisdn);
                boolean isUserExists = false;
                boolean isConvertToActive = false;

                if (AdminServiceUtil.isUserExists(msisdn)) {
                    if (AdminServiceUtil.getUserStatus(msisdn).equalsIgnoreCase(STATUS_ACTIVE) || AdminServiceUtil.getUserStatus(msisdn).equalsIgnoreCase(STATUS_PARTIALLY_ACTIVE)) {
                        isUserExists = true;
                        if ((AdminServiceUtil.getUserStatus(msisdn).equalsIgnoreCase(STATUS_PARTIALLY_ACTIVE)) && (!(Boolean) context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE))) {
                            isConvertToActive = true;
                        }
                    }
                }
                context.setProperty(Constants.IS_REGISTERING, !isUserExists);
                context.setProperty(Constants.IS_STATUS_TO_CHANGE, isConvertToActive);
                DataPublisherUtil.updateAndPublishUserStatus(
                        (UserStatus) context.getProperty(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
                        DataPublisherUtil.UserState.MSISDN_SET_TO_USER_INPUT,
                        "MSISDN set to user input in MSISDNAuthenticator", msisdn, isUserExists ? 0 : 1);
                int requestedLoa = (int) context.getProperty(Constants.ACR);
                String operator = (String) context.getProperty(Constants.OPERATOR);
                boolean isProfileUpgrade = Util.isProfileUpgrade(msisdn, requestedLoa, isUserExists);
                context.setProperty(Constants.IS_PROFILE_UPGRADE, isProfileUpgrade);

                if (!isUserExists && isShowTnC) {
                    retryAuthenticatorForConsent(context);
                }

                if ((requestedLoa == ACR3 ) && (isConvertToActive)) {
                    new UserProfileManager().createUserProfileLoa3(msisdn, operator, null, null,
                            null, (Boolean) context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE));
                }
            } else {
                msisdn = context.getProperty(Constants.MSISDN).toString();
                String userAction = request.getParameter(Constants.ACTION);
                if (userAction != null && !userAction.isEmpty()) {
                    // Change behaviour depending on user action
                    switch (userAction) {
                        case Constants.USER_ACTION_REG_CONSENT:

                            DataPublisherUtil
                                    .updateAndPublishUserStatus((UserStatus) context.getParameter(Constants
                                            .USER_STATUS_DATA_PUBLISHING_PARAM), DataPublisherUtil.UserState
                                            .REG_CONSENT_AGREED, "Consent approved");

                            //User agreed to registration consent
                            break;
                        case Constants.USER_ACTION_REG_REJECTED:
                            //User rejected to registration consent
                            terminateAuthentication(context);
                            break;
//                        case Constants.USER_ACTION_UPGRADE_CONSENT:
//                            //User agreed to registration consent
//                            break;
//                        case Constants.USER_ACTION_UPGRADE_REJECTED:
//                            //User rejected to registration consent
//                            terminateAuthentication(context);
//                            break;
                    }
                } else {
                    boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
                    boolean isConvertToActive = (boolean) context.getProperty(Constants.IS_STATUS_TO_CHANGE);
                    int requestedLoa = (int) context.getProperty(Constants.ACR);
                    String operator = (String) context.getProperty(Constants.OPERATOR);

                    if (isRegistering && isShowTnC) {
                        retryAuthenticatorForConsent(context);
                    }

                    if ((requestedLoa == Integer.getInteger(Constants.LOA3)) && (isConvertToActive)) {
                        new UserProfileManager().createUserProfileLoa3(msisdn, operator, null, null,
                                null, (Boolean) context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE));
                    }
                }
            }
            AuthenticationContextHelper.setSubject(context, msisdn);
            log.info("Authentication success");
            DataPublisherUtil.updateAndPublishUserStatus(
                    (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
                    DataPublisherUtil.UserState.MSISDN_AUTH_SUCCESS,
                    "MSISDN Authentication success");

            String rememberMe = request.getParameter("chkRemember");
            if (rememberMe != null && "eon".equals(rememberMe)) {
                context.setRememberMe(true);
            }

            DataPublisherUtil
                    .updateAndPublishUserStatus(
                            (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
                            DataPublisherUtil.UserState.MSISDN_AUTH_SUCCESS, "MSISDN Authentication success");
        } catch (Exception ex) {
            DataPublisherUtil
                    .updateAndPublishUserStatus(
                            (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
                            DataPublisherUtil.UserState.MSISDN_AUTH_PROCESSING_FAIL, ex.getMessage());
            throw new AuthenticationFailedException("Authenicator failed", ex);
        }
    }

    private void retryAuthenticatorForConsent(AuthenticationContext context) throws AuthenticationFailedException {
        context.setProperty(Constants.REDIRECT_CONSENT, Boolean.TRUE);
        throw new AuthenticationFailedException("Moving to get consent or profile upgrade");
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
        } else if ((canHandle(request) || canProcessResponse(context)) && (request.getAttribute("commonAuthHandled")
                == null || !(Boolean) request.getAttribute("commonAuthHandled"))) {
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

    /**
     * Terminates the authenticator due to user implicit action
     *
     * @param context Authentication Context
     * @throws AuthenticationFailedException
     */
    private void terminateAuthentication(AuthenticationContext context) throws AuthenticationFailedException {
        log.info("User has terminated the authentication flow");

        context.setProperty(Constants.IS_TERMINATED, true);
        throw new AuthenticationFailedException("Authenticator is terminated");
    }


    private String getAuthEndpointUrl(AuthenticationContext context, boolean explicitScope) {

        String loginPage;

        if (context.getProperty(Constants.MSISDN) != null) {

            boolean isShowTnC = (boolean) context.getProperty(Constants.IS_SHOW_TNC);
            boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);

            if (isShowTnC && isRegistering) {

                if (explicitScope) {
                    loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl() + Constants.ATTRIBUTE_CONSENT_JSP;
                } else {
                    loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl() +
                            Constants.CONSENT_JSP;
                }

            } else {
                loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
            }

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
        // Setting retry to true as we need the correct MSISDN to continue
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
        return Constants.MSISDN_AUTHENTICATOR_FRIENDLY_NAME;
    }

    /**
     * Gets the private key file.
     *
     * @return the private key file
     */
    private String getPrivateKeyFile() {
        return Constants.PRIVATE_KEYFILE;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
     */
    @Override
    public String getName() {
        return Constants.MSISDN_AUTHENTICATOR_NAME;
    }

    @Override
    public String getAmrValue(int acr) {
        return null;
    }
}
