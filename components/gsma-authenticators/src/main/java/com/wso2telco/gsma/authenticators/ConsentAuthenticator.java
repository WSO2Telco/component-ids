
/*******************************************************************************
 * Copyright (c) 2015, WSO2.Telco Inc. (http://www.wso2telco.com)
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

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.authenticators.util.FrameworkServiceDataHolder;
import com.wso2telco.gsma.authenticators.util.UserProfileManager;
import com.wso2telco.gsma.authenticators.util.WelcomeSmsUtil;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.*;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.common.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


/**
 * The Class ConsentAuthenticator.
 */
public class ConsentAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator, BaseApplicationAuthenticator {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 7969235421318308000L;

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(ConsentAuthenticator.class);

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.identity.application.authentication.framework.
     * ApplicationAuthenticator#canHandle(javax
     * .servlet.http.HttpServletRequest)
     */
    @Override
    public boolean canHandle(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("Consent Authenticator canHandle invoked");
        }

        if ((request.getParameter(Constants.ACTION) != null && !request.getParameter(Constants.ACTION).isEmpty())
                || (request.getParameter(Constants.MSISDN) != null
                && !request.getParameter(Constants.MSISDN).isEmpty())) {
            log.info("msisdn forwarding ");
            return true;
        }
        return false;
    }

    private boolean canProcessResponse(AuthenticationContext context) {
        return ((context.getProperty(Constants.MSISDN) != null
                && !context.getProperty(Constants.MSISDN).toString().isEmpty())
                && (context.getProperty(Constants.REDIRECT_CONSENT) == null
                || !(Boolean) context.getProperty(Constants.REDIRECT_CONSENT)));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.identity.application.authentication.framework.
     * AbstractApplicationAuthenticator#process
     * (javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity
     * .application.authentication.framework.context.AuthenticationContext)
     */
    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
                                           AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {
        Boolean APICONSENT = false;
        Object scope_types_obj = context.getProperty(Constants.SCOPE_TYPES);
        if (scope_types_obj != null) {
            String scope_types = scope_types_obj.toString();
            if (scope_types != null && !scope_types.isEmpty()) {
                String[] scope_types_array = scope_types.split("-");
                if (scope_types_array != null && scope_types_array.length > 0) {
                    for (String scopeTypeFromString : scope_types_array) {
                        if (scopeTypeFromString.equalsIgnoreCase(ScopeParam.scopeTypes.APICONSENT.name())) {
                            APICONSENT = true;
                        }
                    }
                }
            }
        }
        if (context.isLogoutRequest() || !APICONSENT) {
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        } else {
            return processRequest(request, response, context);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.identity.application.authentication.framework
     * .AbstractApplicationAuthenticator#initiateAuthenticationRequest(javax.ide
     * servlet.http.HttpServletRequest, javax .servlet.http.HttpServletResponse,
     * org.wso2.carbon.identity.application.authentication.framework.context
     * .AuthenticationContext)
     */
    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {
        log.info("Initiating authentication request");
        try {
            String msisdn = context.getProperty(Constants.MSISDN).toString();
            if (msisdn != null && !msisdn.isEmpty()) {
                Object scopes = context.getProperty(Constants.TELCO_SCOPE);
                if (scopes != null) {
                    String[] scopesArray = scopes.toString().split("\\s+");
                    String[] api_Scopes = Arrays.copyOfRange(scopesArray, 1, scopesArray.length);
                    if (api_Scopes != null && api_Scopes.length > 0) {
                        String operator = context.getProperty(Constants.OPERATOR).toString();
                        boolean enableapproveall = true;
                        Map<String, String> approveNeededScopes = new HashedMap();
                        List<String> approvedScopes = new ArrayList<>();
                        String clientID = context.getProperty(Constants.CLIENT_ID).toString();
                        for (String scope : api_Scopes) {
                            String consent[] = DBUtils.getConsentStatus(scope, clientID, operator);
                            if (consent != null && consent.length == 2 && !consent[0].isEmpty() && consent[0].contains("approve")) {
                                boolean approved = DBUtils.getUserConsentScopeApproval(msisdn, scope, clientID, operator);
                                if (approved) {
                                    approvedScopes.add(scope);
                                } else {
                                    approveNeededScopes.put(scope, consent[1]);
                                }
                                if (consent[0].equalsIgnoreCase("approve")) {
                                    enableapproveall = false;
                                }
                            }
                        }
                        context.setProperty(Constants.APPROVE_NEEDED_SCOPES, approveNeededScopes);
                        context.setProperty(Constants.APPROVED_SCOPES, approvedScopes);
                        context.setProperty(Constants.APPROVE_ALL_ENABLE, enableapproveall);
                        boolean isConsentGiven = Constants.USER_ACTION_REG_CONSENT.equals(request.getParameter(Constants.ACTION));
                        String logoPath = DBUtils.getSPConfigValue(operator, clientID, Constants.SP_LOGO);
                        if (logoPath != null && !logoPath.isEmpty()) {
                            context.setProperty(Constants.SP_LOGO, logoPath);
                        }
                        boolean registering = (boolean) context.getProperty(Constants.IS_REGISTERING);
                        if (!approveNeededScopes.isEmpty()) {
                            DataPublisherUtil.updateAndPublishUserStatus((UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM), DataPublisherUtil.UserState.CONCENT_AUTH_REDIRECT_CONSENT_PAGE, "Redirecting to consent page");
                            if (isConsentGiven) {
                                response.sendRedirect("/authenticationendpoint/user_consent.do?sessionDataKey=" + context.getContextIdentifier());
                            } else {
                                response.sendRedirect("/authenticationendpoint/user_consent.do?sessionDataKey=" + context.getContextIdentifier() + "&registering=" + registering);
                            }
                        } else {
                            if (!approvedScopes.isEmpty()) {
                                response.sendRedirect("/commonauth/?sessionDataKey=" + context.getContextIdentifier() + "&action=default");
                            } else {
                                throw new AuthenticationFailedException("Authenticator failed- Approval needed scopes not found");
                            }
                        }
                    } else {
                        throw new AuthenticationFailedException("Authenticator failed- Approval needed scopes not found");
                    }
                } else {
                    throw new AuthenticationFailedException("Authenticator failed- Approval needed scopes not found");
                }
            } else {
                throw new AuthenticationFailedException("Authenticator failed- MSISDN not found");
            }
        } catch (Exception e) {
            log.error("Error occurred while processing request", e);
            DataPublisherUtil.updateAndPublishUserStatus((UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM), DataPublisherUtil.UserState.CONCENT_AUTH_PROCESSING_FAIL, e.getMessage());
            throw new AuthenticationFailedException("Authenticator failed", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.identity.application.authentication.framework
     * .AbstractApplicationAuthenticator#processAuthenticationResponse(javax.
     * servlet.http.HttpServletRequest, javax .servlet.http.HttpServletResponse,
     * org.wso2.carbon.identity.application.authentication.framework.context
     * .AuthenticationContext)
     */
    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {
        log.info("Processing authentication response");
        try {
            String approval = request.getParameter(Constants.ACTION);
            if (approval != null && !approval.isEmpty()) {
                String msisdn = context.getProperty(Constants.MSISDN).toString();
                String clientID = context.getProperty(Constants.CLIENT_ID).toString();
                String operator = context.getProperty(Constants.OPERATOR).toString();
                boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
                Map<String, String> approveNeededScopes = (Map<String, String>) context.getProperty(Constants.APPROVE_NEEDED_SCOPES);
                for (Map.Entry<String, String> scopeEntry : approveNeededScopes.entrySet()) {
                    String scope = scopeEntry.getKey();
                    if (approval.equalsIgnoreCase(Constants.STATUS_APPROVEALL)) {
                        DBUtils.insertUserConsentDetails(msisdn, scope, clientID, operator, true);
                    }
                    DBUtils.insertConsentHistoryDetails(msisdn, scope, clientID, operator, approval);
                }
                //onnet flow concent succeed
                if (!Boolean.valueOf(String.valueOf(context.getProperty(Constants.IS_OFFNET_FLOW))) && ((int) context.getProperty(Constants.ACR) == 2)) {
                    context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
                    if (approval.equalsIgnoreCase(Constants.STATUS_APPROVEALL) || approval.equalsIgnoreCase(Constants.STATUS_APPROVE)) {
                        if (isRegistering) {
                            boolean isAttributeScope = (Boolean) context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE);
                            String spType = context.getProperty(Constants.TRUSTED_STATUS).toString();
                            String attrShareType = context.getProperty(Constants.ATTRSHARE_SCOPE_TYPE).toString();
                            new UserProfileManager().createUserProfileLoa2(msisdn, operator, isAttributeScope, spType, attrShareType);
                            ;
                            MobileConnectConfig.SMSConfig smsConfig = configurationService.getDataHolder().getMobileConnectConfig().getSmsConfig();
                            if (!smsConfig.getWelcomeMessageDisabled()) {
                                UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);
                                WelcomeSmsUtil.handleWelcomeSms(context, userStatus, msisdn, operator, smsConfig);
                            }
                        }
                    }
                }
                if (approval.equalsIgnoreCase(Constants.STATUS_DENY)) {
                    terminateAuthentication(context);
                }
                AuthenticationContextHelper.setSubject(context, msisdn);
                log.info("Authentication success");
                DataPublisherUtil.updateAndPublishUserStatus(
                        (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
                        DataPublisherUtil.UserState.CONCENT_AUTH_SUCCESS,
                        "Consent Authentication success");

                String rememberMe = request.getParameter("chkRemember");
                if (rememberMe != null && "eon".equals(rememberMe)) {
                    context.setRememberMe(true);
                }
            } else {
                throw new AuthenticationFailedException("Authenticator failed- No Approval provided");
            }
        } catch (Exception ex) {
            DataPublisherUtil.updateAndPublishUserStatus((UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM), DataPublisherUtil.UserState.CONCENT_AUTH_PROCESSING_FAIL, ex.getMessage());
            throw new AuthenticationFailedException("Authenticator failed", ex);
        }

    }

    public AuthenticatorFlowStatus processRequest(HttpServletRequest request, HttpServletResponse response,
                                                  AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {
//        Boolean APICONSENT=false;
//        String scope_types = request.getParameter(Constants.SCOPE_TYPES);
//        if(scope_types!=null && !scope_types.isEmpty()){
//            String[] scope_types_array=scope_types.split(",");
//            if(scope_types_array!=null  && scope_types_array.length>0){
//                for(String scopeTypeFromString:scope_types_array){
//                    if(scopeTypeFromString.equalsIgnoreCase(ScopeParam.scopeTypes.APICONSENT.name())){
//                        APICONSENT=true;
//                    }
//                }
//            }
//        }if (!APICONSENT){
//            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
//        } else
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
        } else if ((canHandle(request) || canProcessResponse(context))
                && (request.getAttribute("commonAuthHandled") == null
                || !(Boolean) request.getAttribute("commonAuthHandled"))) {
            try {
                processAuthenticationResponse(request, response, context);
                if (this instanceof LocalApplicationAuthenticator
                        && !context.getSequenceConfig().getApplicationConfig().isSaaSApp()) {
                    String e = context.getSubject().getTenantDomain();
                    String stepMap1 = context.getTenantDomain();
                    if (!StringUtils.equals(e, stepMap1)) {
                        context.setProperty("UserTenantDomainMismatch", Boolean.valueOf(true));
                        throw new AuthenticationFailedException("Service Provider tenant domain must be equal to user"
                                + " tenant domain for non-SaaS applications");
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

    private void publishAuthenticationStepAttempt(HttpServletRequest request, AuthenticationContext context, User user,
                                                  boolean success) {
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

    /* (non-Javadoc)
    * @see org.wso2.carbon.identity.application.authentication.framework
    * .AbstractApplicationAuthenticator#retryAuthenticationEnabled()
    */
    @Override
    protected boolean retryAuthenticationEnabled() {
        return false;
    }


    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.identity.application.authentication.framework
     * .ApplicationAuthenticator#getContextIdentifier(javax.servlet.http.
     * HttpServletRequest)
     */
    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        return request.getParameter("sessionDataKey");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.identity.application.authentication.framework.ideide
     * ApplicationAuthenticator#getFriendlyName()
     */
    @Override
    public String getFriendlyName() {
        return Constants.CONSENT_AUTHENTICATOR_FRIENDLY_NAME;
    }


    /*
     * (non-Javadoc)
     *
     * @see org.wso2.carbon.identity.application.authentication.framework.
     * ApplicationAuthenticator#getName()
     */
    @Override
    public String getName() {
        return Constants.CONSENT_AUTHENTICATOR_NAME;
    }

    @Override
    public String getAmrValue(int acr) {
        return null;
    }
}
