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

package com.wso2telco.gsma.authenticators.headerenrich;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.core.sp.config.utils.exception.DataAccessException;
import com.wso2telco.gsma.authenticators.BaseApplicationAuthenticator;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.IPRangeChecker;
import com.wso2telco.gsma.authenticators.apiconsent.AbstractAPIConsent;
import com.wso2telco.gsma.authenticators.attributeshare.AbstractAttributeShare;
import com.wso2telco.gsma.authenticators.attributeshare.AttributeShareFactory;
import com.wso2telco.gsma.authenticators.internal.AuthenticatorEnum;
import com.wso2telco.gsma.authenticators.util.*;
import com.wso2telco.gsma.manager.client.ClaimManagementClient;
import com.wso2telco.gsma.manager.client.LoginAdminServiceClient;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.application.authentication.framework.*;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

// TODO: Auto-generated Javadoc

/**
 * The Class HeaderEnrichmentAuthenticator.
 */
public class HeaderEnrichmentAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator, BaseApplicationAuthenticator {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 4438354156955225674L;

    /**
     * The operatorips.
     */
    static List<String> operatorips = null;

    /**
     * The operators.
     */
    static List<MobileConnectConfig.OPERATOR> operators = null;

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(HeaderEnrichmentAuthenticator.class);

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /**
     * The Map to store each operator name and ip validation is using
     */
    private static Map<String, Boolean> operatorIpValidation = new HashMap<>();

    static {
        // loads operator and ip validation to static map
        operators = configurationService.getDataHolder().getMobileConnectConfig().getHEADERENRICH().getOperators();

        for (MobileConnectConfig.OPERATOR op : operators) {
            operatorIpValidation.put(op.getOperatorName(), Boolean.valueOf(op.getIpValidation()));
        }
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax
     * .servlet.http.HttpServletRequest)
     */
    @Override
    public boolean canHandle(HttpServletRequest request) {

        if (log.isDebugEnabled()) {
            log.debug("Header Enrich Authenticator canHandle invoked");
        }
        return true;
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

        log.info("Processing started");

        DataPublisherUtil
                .updateAndPublishUserStatus((UserStatus) context.getParameter(Constants
                                .USER_STATUS_DATA_PUBLISHING_PARAM),
                        DataPublisherUtil.UserState.HE_AUTH_PROCESSING, "HeaderEnrichmentAuthenticator processing " +
                                "started");
        if (context.isLogoutRequest()) {
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        } else {
            return this.processRequest(request, response, context);
        }
    }

    public AuthenticatorFlowStatus processRequest(HttpServletRequest request, HttpServletResponse response,
                                                  AuthenticationContext context) throws
            AuthenticationFailedException, LogoutFailedException {
        if ((canHandle(request) && !triggerInitiateAuthRequest(context) && (request.getAttribute(FrameworkConstants
                .REQ_ATTR_HANDLED) == null || !(Boolean) request.getAttribute(FrameworkConstants.REQ_ATTR_HANDLED)))) {
            try {

                boolean isAttribute = (boolean) context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE);
                boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
                boolean isAPIConsent = (boolean) context.getProperty(Constants.IS_API_CONSENT);
                String msisdn = context.getProperty(Constants.MSISDN).toString();
                Map<String, String> attributeSet;
                boolean isDisplayScopes;

                if (!isRegistering && isAttribute && Constants.NO.equalsIgnoreCase(context.getProperty(Constants
                        .IS_CONSENTED).toString()) && StringUtils.isNotEmpty(msisdn)) {

                    attributeSet = AttributeShareFactory.getAttributeSharable(context.getProperty(Constants
                            .TRUSTED_STATUS).toString()).getAttributeShareDetails(context);
                    boolean flowStatus = Boolean.valueOf(attributeSet.get(Constants.IS_AUNTHENTICATION_CONTINUE));
                    isDisplayScopes = Boolean.parseBoolean(attributeSet.get(Constants.IS_DISPLAYSCOPE).toString());

                    if (flowStatus) {



                    } else if (!flowStatus && isDisplayScopes) {

                        getConsentFromUser(request, response, context, attributeSet);
                        context.setCurrentAuthenticator(getName());
                        return AuthenticatorFlowStatus.INCOMPLETE;
                    }
                }else if(!isRegistering && isAPIConsent && Constants.NO.equalsIgnoreCase(context.getProperty(Constants
                        .IS_CONSENTED).toString()) && StringUtils.isNotEmpty(msisdn)){
                    AbstractAPIConsent.setApproveNeededScope(context);
                    Map<String, String> approveNeededScopes = (Map<String, String>) context.getProperty(Constants.APPROVE_NEEDED_SCOPES);
                    if((Boolean) context.getProperty(Constants.ALREADY_APPROVED)){
                        AuthenticationContextHelper.setSubject(context, context.getProperty(Constants.MSISDN)
                                .toString());
                        context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
                        return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                    }
                    context.setProperty(Constants.IS_CONSENTED, Constants.YES);
                    getAPIConsentFromUser(request, response, context);
                    context.setCurrentAuthenticator(getName());
                    return AuthenticatorFlowStatus.INCOMPLETE;
                }


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

                request.setAttribute(FrameworkConstants.REQ_ATTR_HANDLED, Boolean.TRUE);
                publishAuthenticationStepAttempt(request, context, context.getSubject(), true);
                return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
            } catch (AuthenticationFailedException e) {
                if (context.getProperty(Constants.DENIED_SCOPE) != null && (Boolean)context.getProperty(Constants.DENIED_SCOPE)){
                    terminateAuthentication(context);
                    return AuthenticatorFlowStatus.INCOMPLETE;
                }
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
                    if (Boolean.valueOf(context.getProperty(Constants.AUTHENTICATED_USER).toString())) {
                        return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                    }
                    throw e;
                }
            } catch (Exception e) {
                if (Boolean.valueOf(context.getProperty(Constants.AUTHENTICATED_USER).toString())) {
                    return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                }
                log.debug("error occurred while doing the attribute share ");
                return null;
            }
        } else {
            try {
                if((Boolean)context.getProperty(Constants.IS_API_CONSENT)) {
                    if((Boolean) context.getProperty(Constants.IS_REGISTERING))
                      DBUtils.removeApprovedAPIsforNewUser(context.getProperty(Constants.MSISDN).toString());

                    AbstractAPIConsent.setApproveNeededScope(context);
                }
                initiateAuthenticationRequest(request, response, context);
            } catch (Exception e) {

                if (context.getProperty(Constants.DENIED_SCOPE) != null && (Boolean)context.getProperty(Constants.DENIED_SCOPE)){
                    terminateAuthentication(context);
                    return AuthenticatorFlowStatus.INCOMPLETE;
                }
                if (Boolean.valueOf(context.getProperty(Constants.AUTHENTICATED_USER).toString())) {
                    return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                }
            }
            context.setProperty(Constants.HE_INITIATE_TRIGGERED, Boolean.TRUE);
            context.setCurrentAuthenticator(getName());
            return AuthenticatorFlowStatus.INCOMPLETE;
        }
    }

    private void publishAuthenticationStepAttempt(HttpServletRequest request, AuthenticationContext context,
                                                  User user, boolean success) {

        AuthenticationDataPublisher authnDataPublisherProxy = FrameworkServiceDataHolder.getInstance()
                .getAuthnDataPublisherProxy();
        if (authnDataPublisherProxy != null && authnDataPublisherProxy.isEnabled(context)) {
            boolean isFederated = this instanceof FederatedApplicationAuthenticator;
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(FrameworkConstants.AnalyticsAttributes.USER, user);
            if (isFederated) {
                // Setting this value to authentication context in order to use in AuthenticationSuccess Event
                context.setProperty(FrameworkConstants.AnalyticsAttributes.HAS_FEDERATED_STEP, true);
                paramMap.put(FrameworkConstants.AnalyticsAttributes.IS_FEDERATED, true);
            } else {
                // Setting this value to authentication context in order to use in AuthenticationSuccess Event
                context.setProperty(FrameworkConstants.AnalyticsAttributes.HAS_LOCAL_STEP, true);
                paramMap.put(FrameworkConstants.AnalyticsAttributes.IS_FEDERATED, false);
            }
            Map<String, Object> unmodifiableParamMap = Collections.unmodifiableMap(paramMap);
            if (success) {
                authnDataPublisherProxy.publishAuthenticationStepSuccess(request, context,
                        unmodifiableParamMap);

            } else {
                authnDataPublisherProxy.publishAuthenticationStepFailure(request, context,
                        unmodifiableParamMap);
            }
        }
    }

    private boolean triggerInitiateAuthRequest(AuthenticationContext context) {
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
        boolean showTnc = (boolean) context.getProperty(Constants.IS_SHOW_TNC);

        return ((context.getProperty(Constants.HE_INITIATE_TRIGGERED) == null || !(Boolean) context.getProperty
                (Constants.HE_INITIATE_TRIGGERED)) && isRegistering && showTnc);

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

        String operator = context.getProperty(Constants.OPERATOR).toString();
        String msisdn = context.getProperty(Constants.MSISDN).toString();

        org.apache.log4j.MDC.put("MSISDN", msisdn);

        log.info("Initiating authentication request");

        AuthenticationContextCache.getInstance().addToCache(new AuthenticationContextCacheKey(context
                .getContextIdentifier()), new AuthenticationContextCacheEntry(context));

        UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);


        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
        boolean showTnc = (boolean) context.getProperty(Constants.IS_SHOW_TNC);
        boolean isExplicitScope = false;

        if (log.isDebugEnabled()) {
            log.debug("Detected MSISDN : " + msisdn);
        }

        try {
            validateOperator(request, context, msisdn, operator, userStatus);
        } catch (AuthenticationFailedException e) {
            // take action based on scope properties
            DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.HE_AUTH_PROCESSING_FAIL,e.getMessage());
            actionBasedOnHEFailureResult(context);
            throw e;
        }

        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                        context.getCallerSessionKey(),
                        context.getContextIdentifier());

        if (log.isDebugEnabled()) {
            log.debug("MSISDN : " + msisdn);
            log.debug("Operator : " + operator);
            log.debug("Query parameters : " + queryParams);
        }


        Map<String, String> attributeSet = new HashMap();

        try {

            boolean isAttribute = (boolean) context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE);

            DataPublisherUtil
                    .updateAndPublishUserStatus((UserStatus) context.getParameter(Constants
                            .USER_STATUS_DATA_PUBLISHING_PARAM), DataPublisherUtil.UserState
                            .REDIRECT_TO_CONSENT_PAGE, "Redirecting to consent page");

            if (isAttribute && StringUtils.isNotEmpty(msisdn)) {
                attributeSet = AttributeShareFactory.getAttributeSharable(context.getProperty(Constants
                        .TRUSTED_STATUS).toString()).getAttributeShareDetails(context);
                isExplicitScope = Boolean.parseBoolean(attributeSet.get(Constants.IS_DISPLAYSCOPE));
            }


            if (Boolean.valueOf(attributeSet.get(Constants.IS_AUNTHENTICATION_CONTINUE))) {
                handleAttributeShareResponse(context);

            } else if (Boolean.parseBoolean(attributeSet.get(Constants.IS_DISPLAYSCOPE))) {

                getConsentFromUser(request, response, context, attributeSet);
            } else {
                String loginPage = getAuthEndpointUrl(context, isExplicitScope);
                if (isRegistering && showTnc) {
                    log.info("Redirecting user to consent page");
                    response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + "registering=true&"+queryParams)) + "&redirect_uri=" +
                            request.getParameter("redirect_uri") + "&authenticators="
                            + getName() + ":" + "LOCAL");
                }else {
                    response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams))
                            + "&redirect_uri=" + request.getParameter("redirect_uri")
                            + "&authenticators=" + getName() + ":" + "LOCAL");
                }
            }
        } catch (IOException e) {
            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus,
                            DataPublisherUtil.UserState.HE_AUTH_PROCESSING_FAIL, e.getMessage());
            throw new AuthenticationFailedException(e.getMessage(), e);

        } catch (DBUtilException | NamingException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
        return;

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

        String msisdn = context.getProperty(Constants.MSISDN).toString();
        String operator = context.getProperty(Constants.OPERATOR).toString();

        org.apache.log4j.MDC.put("MSISDN", msisdn);

        log.info("Processing authentication response");

        UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);

        AuthenticationContextCache.getInstance().addToCache(new AuthenticationContextCacheKey(context
                .getContextIdentifier()), new AuthenticationContextCacheEntry(context));

        String userAction = request.getParameter(Constants.ACTION);
        try {
            if (userAction != null && !userAction.isEmpty()) {
                // Change behaviour depending on user action
                if(context.getProperty(Constants.IS_API_CONSENT) != null && Boolean.parseBoolean(context.getProperty(Constants.IS_API_CONSENT).toString())){
                    String clientID = context.getProperty(Constants.CLIENT_ID).toString();
                    boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
                    Map<String, String> approveNeededScopes = (Map<String, String>) context.getProperty(Constants.APPROVE_NEEDED_SCOPES);
                    for (Map.Entry<String, String> scopeEntry : approveNeededScopes.entrySet()) {
                        String scope = scopeEntry.getKey();
                        if (userAction.equalsIgnoreCase(Constants.STATUS_APPROVEALL)) {
                            DBUtils.insertUserConsentDetails(msisdn, scope, clientID, operator, true);
                        }
                        DBUtils.insertConsentHistoryDetails(msisdn, scope, clientID, operator, userAction);
                    }
                }
                switch (userAction) {
                    case Constants.USER_ACTION_REG_CONSENT:
                        //User agreed to registration consent
                        log.info("User approved the consent");
                        DataPublisherUtil
                                .updateAndPublishUserStatus((UserStatus) context.getParameter(Constants
                                        .USER_STATUS_DATA_PUBLISHING_PARAM), DataPublisherUtil.UserState
                                        .REG_CONSENT_AGREED, "Consent approved");

                        break;
                    case Constants.USER_ACTION_REG_REJECTED:
                        log.info("User rejected the consent");
                        //User rejected to registration consent
                        terminateAuthentication(context);
                        break;
                    case Constants.STATUS_DENY:
                        //User rejected to registration consent
                        log.info("User rejected the consent");
                        terminateAuthentication(context);
                        break;
                }
            }



            int requestedLoa = Integer.parseInt(context.getProperty(Constants.ACR).toString());

            if (log.isDebugEnabled()) {
                log.debug("Redirect URI : " + request.getParameter("redirect_uri"));
            }
            context.setProperty("redirectURI", request.getParameter("redirect_uri"));

            populateAuthEndpointData(request, context);

            boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
            boolean isAttributeScope = (Boolean) context.getProperty(Constants.IS_ATTRIBUTE_SHARING_SCOPE);
            boolean isStatusUpdate = (boolean) context.getProperty(Constants.IS_STATUS_TO_CHANGE);
            String spType = context.getProperty(Constants.TRUSTED_STATUS).toString();
            String attrShareType = context.getProperty(Constants.ATTRSHARE_SCOPE_TYPE).toString();
            boolean isShowConcent = (boolean) context.getProperty(Constants.IS_SHOW_CONSENT);

            validateOperator(request, context, msisdn, operator, userStatus);

            if (requestedLoa == 3) {
                // if acr is 3, pass the user to next authenticator
            }
            if (requestedLoa == 2) {
                if (isRegistering || isStatusUpdate) {
                    // authenticators from step map
                    try {

                        new UserProfileManager().createUserProfileLoa2(msisdn, operator, isAttributeScope,
                                spType, attrShareType);

                        MobileConnectConfig.SMSConfig smsConfig = configurationService.getDataHolder()
                                .getMobileConnectConfig().getSmsConfig();
                        if (!smsConfig.getWelcomeMessageDisabled()) {
                            WelcomeSmsUtil.handleWelcomeSms(context, userStatus, msisdn, operator, smsConfig);
                        }

                        if (isAttributeScope) {
                            handleAttributeShareResponse(context);
                        }

                    } catch (RemoteException | UserRegistrationAdminServiceIdentityException e) {
                        DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                                DataPublisherUtil.UserState.HE_AUTH_PROCESSING_FAIL, e.getMessage());
                        throw new AuthenticationFailedException(e.getMessage(), e);
                    } catch (DataAccessException | IOException e) {
                        log.error("Welcome SMS sending failed", e);
                    }

                } else if (isAttributeScope) {
                    handleAttributeShareResponse(context);
                }
            }
            if(context.getProperty(Constants.API_SCOPES) != null)
                new UserProfileManager().updateMIGUserRoles(msisdn, context.getProperty(Constants.CLIENT_ID).toString(), context.getProperty(Constants.API_SCOPES).toString());
            context.setProperty(Constants.IS_PIN_RESET, false);

            if(!isShowConcent) {
                context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
            }
            // explicitly remove all other authenticators and mark as a success

            AuthenticationContextHelper.setSubject(context, msisdn);

            String rememberMe = request.getParameter("chkRemember");

            if (rememberMe != null && "on".equals(rememberMe)) {
                context.setRememberMe(true);
            }
        } catch (Exception e) {
            // take action based on scope properties
            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.HE_AUTH_PROCESSING_FAIL,
                            e.getMessage());
            actionBasedOnHEFailureResult(context);
            throw new AuthenticationFailedException("Authenicator failed", e);
        }

        log.info("Authentication success");
        DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.HE_AUTH_SUCCESS,"Header Enrichment Authentication success");
    }

    /**
     * IP validation method. Throws AuthenticationFailedException if ip validation is failed
     *
     * @param request    HTTP Request
     * @param context    Authentication context
     * @param msisdn     msisdn
     * @param operator   operator
     * @param userStatus user status for data publishing
     * @throws AuthenticationFailedException
     */
    private void validateOperator(HttpServletRequest request, AuthenticationContext context, String msisdn, String
            operator, UserStatus userStatus) throws AuthenticationFailedException {
        boolean ipValidation = false;
        boolean validOperator = false;

        if (operatorIpValidation.containsKey(operator)) {
            ipValidation = operatorIpValidation.get(operator);
        }

        String ipAddress = (String) context.getProperty(Constants.IP_ADDRESS);
        if (ipAddress == null || StringUtils.isEmpty(ipAddress)) {
            ipAddress = retriveIPAddress(request);
        }

        if (ipAddress == null) {
            if (log.isDebugEnabled()) {
                log.debug("Header ip address not found.");
            }
            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.IP_HEADER_NOT_FOUND,
                            "Missing IP address");

            // RULE : if operator ip validation is enabled and ip address is blank, break the flow
            if (ipValidation) {
                log.info("Header Enrichment Authentication failed due to not having ip address");
                context.setProperty("faileduser", msisdn);
                DataPublisherUtil
                        .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.HE_AUTH_PROCESSING_FAIL,
                                "Unable to proceed with ip validation due to missing IP address");
                throw new AuthenticationFailedException("Authentication Failed");
            }
        }

        if (ipAddress != null && ipValidation) {
            validOperator = validateOperator(operator, ipAddress);
        }

        // RULE : if operator ip validation is enabled and ip validation failed, break the flow
        if (ipValidation && !validOperator) {
            log.info("Header Enrichment Authentication failed");
            context.setProperty("faileduser", msisdn);
            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.IP_HEADER_NOT_IN_RANGE,
                            "IP address not in range");
            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus,
                            DataPublisherUtil.UserState.HE_AUTH_PROCESSING_FAIL, "IP validation failed");
            throw new AuthenticationFailedException("Authentication Failed");
        }
    }

    private void populateAuthEndpointData(HttpServletRequest request, AuthenticationContext context) {

        boolean dataPublisherEnabled = DataHolder.getInstance().getMobileConnectConfig().getDataPublisher().isEnabled();
        if (dataPublisherEnabled) {
            Map<String, String> authMap = (Map<String, String>) context.getProperty(
                    Constants.AUTH_ENDPOINT_DATA_PUBLISHING_PARAM);
            String queryParams = FrameworkUtils
                    .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                            context.getCallerSessionKey(),
                            context.getContextIdentifier());
            String contextIdentifier = context.getContextIdentifier();
            String msisdn = request.getHeader("msisdn");

            boolean isMsisdnHeader = false;
            if (msisdn != null && !msisdn.isEmpty()) {
                isMsisdnHeader = true;
            }

            authMap.put("AppID", context.getSequenceConfig() == null ? null : context.getSequenceConfig()
                    .getApplicationId());
            authMap.put("AuthenticatorStartTime", String.valueOf(new java.util.Date().getTime()));
            authMap.put("sessionId", contextIdentifier);
            authMap.put("InternalCustomerReference", contextIdentifier);
            authMap.put("URLParams", queryParams);
            authMap.put("TransactionId", contextIdentifier);
            authMap.put("UserAgent", request.getHeader("User-Agent"));
            authMap.put("IsMsisdnHeader", Boolean.toString(isMsisdnHeader));

            if (request.getParameter("isNew") != null || request.getParameter("isRegistration") != null) {
                context.setProperty("newReg", true);
            }
            context.setProperty(Constants.AUTH_ENDPOINT_DATA_PUBLISHING_PARAM, authMap);
        }
    }

    /**
     * Take action based on scope properties for HE Failure results
     *
     * @param context Authentication Context
     */
    private void actionBasedOnHEFailureResult(AuthenticationContext context) {
        String heFailureResult = context.getProperty(Constants.HE_FAILURE_RESULT).toString();

        if (heFailureResult == null || heFailureResult.isEmpty()) {
            context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
        } else {
            switch (heFailureResult) {
                case Constants.UNTRUST_MSISDN:
                    // On HE failure, untrust the header msisdn and forwards to next authenticator
                    // setting context MSISDN to null
                    context.setProperty(Constants.MSISDN, null);
                    DataPublisherUtil.updateAndPublishUserStatus(
                            (UserStatus) context.getProperty(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
                            DataPublisherUtil.UserState.MSISDN_CLEARED,
                            "MSISDN value cleared on Header Enrichment Authenticator failure", null);
                    log.info("HE FAILED : UNTRUST_MSISDN");
                    break;

                case Constants.TRUST_HEADER_MSISDN:
                    log.info("HE FAILED : TRUST_HEADER_MSISDN");
                    break;

                case Constants.TRUST_LOGINHINT_MSISDN:
                    // On HE failure, trust the login hint MSISDN and forwards to next authenticator
                    String loginHintValue = null;

                    try {
                        loginHintValue = DecryptionAES.decrypt(context.getProperty(Constants.LOGIN_HINT_MSISDN)
                                .toString());
                    } catch (Exception e) {
                        log.error("Exception Getting the login hint values " + e);
                    }

                    if (loginHintValue != null) {
                        context.setProperty(Constants.MSISDN, loginHintValue);
                        DataPublisherUtil.updateAndPublishUserStatus(
                                (UserStatus) context.getProperty(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
                                DataPublisherUtil.UserState.MSISDN_SET_TO_LOGIN_HINT,
                                "MSISDN set to login hint on Header Enrichment Authenticator failure", loginHintValue);
                    } else {
                        // Clear MSISDN from context if scope parameter is 'TRUST_LOGINHINT_MSISDN' and loginhint is
                        // not provided
                        context.setProperty(Constants.MSISDN, null);
                    }

                    log.info("HE FAILED : TRUST_LOGINHINT_MSISDN");
                    break;

                default:
                    context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
                    log.info("HE FAILED : BREAK THE FLOW");
            }
        }
    }

    private void terminateAuthentication(AuthenticationContext context) throws AuthenticationFailedException {
        log.info("User has terminated the authentication flow");

        context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
        throw new AuthenticationFailedException("Authenticator is terminated");
    }

    /**
     * Retrieves auth endpoint url

     * @return Endpoint
     * @throws UserStoreException
     * @throws AuthenticationFailedException
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
     */
    private String getAuthEndpointUrl(AuthenticationContext context, boolean explicitScope) {

        String loginPage = null;
        boolean isShowTnc = (boolean) context.getProperty(Constants.IS_SHOW_TNC);
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);

        if (isRegistering && isShowTnc) {

            if (explicitScope) {
                loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl() +
                        Constants.ATTRIBUTE_CONSENT_JSP;
            } else if (Boolean.parseBoolean(context.getProperty(Constants.IS_API_CONSENT).toString())){
                log.info("Redirecting user to consent page");
                if (Boolean.parseBoolean(context.getProperty(Constants.IS_API_CONSENT).toString())) {
                    Map<String, String> approveNeededScopes = (Map<String, String>) context.getProperty(Constants.APPROVE_NEEDED_SCOPES);
                    if (!approveNeededScopes.isEmpty()) {
                        DataPublisherUtil.updateAndPublishUserStatus((UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM), DataPublisherUtil.UserState.CONCENT_AUTH_REDIRECT_CONSENT_PAGE, "Redirecting to consent page");
                        loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl() + "/user_consent.do";
                    }
                }
            } else {
                loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl() +
                        Constants.CONSENT_JSP;
            }
        } else if (Boolean.parseBoolean(context.getProperty(Constants.IS_API_CONSENT).toString())){
            log.info("Redirecting user to consent page");
            if (Boolean.parseBoolean(context.getProperty(Constants.IS_API_CONSENT).toString())) {
                Map<String, String> approveNeededScopes = (Map<String, String>) context.getProperty(Constants.APPROVE_NEEDED_SCOPES);
                if (!approveNeededScopes.isEmpty()) {
                    DataPublisherUtil.updateAndPublishUserStatus((UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM), DataPublisherUtil.UserState.CONCENT_AUTH_REDIRECT_CONSENT_PAGE, "Redirecting to consent page");
                    loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl() + "/user_consent.do";
                }
            }
        } else {

            if (explicitScope) {
                loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl() +
                        Constants.ATTRIBUTE_CONSENT_JSP;
            } else {
                loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
            }

        }
        return loginPage;
    }

    /**
     * Retrieves ACR value from request
     *
     * @param request HTTP request
     * @param context Authentication request
     * @return ACR value
     */
    private int getAcr(HttpServletRequest request, AuthenticationContext context) {
        String acr = request.getParameter(Constants.PARAM_ACR);

        if (acr != null && !StringUtils.isEmpty(acr)) {
            return Integer.parseInt(acr);
        } else {
            return (int) context.getProperty(Constants.ACR);
        }
    }

    /**
     * Retrieve MSISDN number
     *
     * @param request               the request
     * @param authenticationContext the authentication context
     * @return
     */
    private String getMsisdn(HttpServletRequest request, AuthenticationContext authenticationContext) {

        String msisdn = request.getParameter(Constants.MSISDN_HEADER);

        if (msisdn != null && !StringUtils.isEmpty(msisdn)) {
            return msisdn;
        } else {
            return (String) authenticationContext.getProperty(Constants.MSISDN);
        }
    }

    /**
     * Retrieve ip address.
     *
     * @param request the request
     * @return the string
     */
    public String retriveIPAddress(HttpServletRequest request) {

        String ipAddress = null;
        try {
            ipAddress = request.getParameter(Constants.IP_ADDRESS);
        } catch (Exception e) {
            log.error("Error occurred Retrieving ip address " + e);
        }

        return ipAddress;
    }

    private boolean isProfileUpgrade(String msisdn, int currentLoa, boolean isUserExits) throws RemoteException,
            LoginAuthenticationExceptionException, RemoteUserStoreManagerServiceUserStoreExceptionException,
            AuthenticationFailedException, UserStoreException {

        if (msisdn != null && isUserExits) {
            String adminURL = configurationService.getDataHolder().getMobileConnectConfig().getAdminUrl();
            LoginAdminServiceClient lAdmin = new LoginAdminServiceClient(adminURL);
            String sessionCookie = lAdmin.authenticate(configurationService.getDataHolder().getMobileConnectConfig()
                            .getAdminUsername(),
                    configurationService.getDataHolder().getMobileConnectConfig().getAdminPassword());
            ClaimManagementClient claimManager = new ClaimManagementClient(adminURL, sessionCookie);
            int registeredLoa = Integer.parseInt(claimManager.getRegisteredLOA(msisdn));

            return currentLoa > registeredLoa;
        } else {
            return false;
        }

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
        return Constants.HE_AUTHENTICATOR_FRIENDLY_NAME;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
     */
    @Override
    public String getName() {
        return Constants.HE_AUTHENTICATOR_NAME;
    }

    /**
     * Validate msisdn.
     *
     * @param msisdn the msisdn
     * @return true, if successful
     */
    private boolean validateMsisdnFormat(String msisdn) {
        if (StringUtils.isNotEmpty(msisdn)) {
            String plaintextMsisdnRegex =
                    configurationService.getDataHolder().getMobileConnectConfig().getMsisdn().getValidationRegex();
            return msisdn.matches(plaintextMsisdnRegex);
        }
        return true;
    }

    /**
     * Validate operator.
     *
     * @param operator the operator
     * @param strip    the strip
     * @return true, if successful
     */
    protected boolean validateOperator(String operator, String strip) {
        boolean isvalid = false;

        operators = configurationService.getDataHolder().getMobileConnectConfig().getHEADERENRICH().getOperators();

        for (MobileConnectConfig.OPERATOR op : operators) {
            if (operator.equalsIgnoreCase(op.getOperatorName())) {
                for (String ids : op.getMobileIPRanges()) {
                    if (ids != null) {
                        String[] iprange = ids.split(":");
                        isvalid = IPRangeChecker.isValidRange(iprange[0], iprange[1], strip);
                        if (isvalid) {
                            break;
                        }
                    }
                }
            }
        }

        return isvalid;
    }

    @Override
    public String getAmrValue(int acr) {
        return "HE_OK";
    }

    private void handleAttributeShareResponse(AuthenticationContext context) throws AuthenticationFailedException {

        if (context.getProperty(Constants.LONGLIVEDSCOPES) != null) {
            try {
                AbstractAttributeShare.persistConsentedScopeDetails(context);
            } catch (Exception e) {
                throw new AuthenticationFailedException("error occurred while persiste data");
            }
        }

        if (!AuthenticatorEnum.TrustedStatus.UNTRUSTED.toString().equalsIgnoreCase(context.getProperty(Constants
                .TRUSTED_STATUS).toString())) {
            boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);

            if (isRegistering) {
                AbstractAttributeShare.createUserProfile(context);
            }
            AuthenticationContextHelper.setSubject(context, context.getProperty(Constants.MSISDN).toString());
            context.setProperty(Constants.AUTHENTICATED_USER, "true");
            context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
            throw new AuthenticationFailedException("Terminate authentication flow");
        }
    }

    private void getConsentFromUser(HttpServletRequest request, HttpServletResponse response,
                                    AuthenticationContext context, Map<String, String> attributeset) throws
            AuthenticationFailedException {

        String loginPage = getAuthEndpointUrl(context, Boolean.parseBoolean(attributeset.get(Constants
                .IS_DISPLAYSCOPE)));
        try {
            response.sendRedirect(response.encodeRedirectURL(loginPage) + "?" + OAuthConstants.SESSION_DATA_KEY + "="
                    + context.getContextIdentifier() + "&skipConsent=true&scope=" + attributeset.get(Constants
                    .DISPLAY_SCOPES) + "&registering=" + attributeset.get(Constants.IS_TNC)
                    + "&redirect_uri=" + request.getParameter("redirect_uri")
                    + "&authenticators=" + getName() + ":" + "LOCAL");
        } catch (IOException e) {
            throw new AuthenticationFailedException("I/O exception occurred");
        }
    }

    private void getAPIConsentFromUser(HttpServletRequest request, HttpServletResponse response,
                                    AuthenticationContext context) throws
            AuthenticationFailedException {

        String loginPage = getAuthEndpointUrl(context, false);
        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                        context.getCallerSessionKey(),
                        context.getContextIdentifier());
        try {
            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams))
                    + "&redirect_uri=" + request.getParameter("redirect_uri")
                    + "&authenticators=" + getName() + ":" + "LOCAL");
        } catch (IOException e) {
            throw new AuthenticationFailedException("I/O exception occurred");
        }
    }
}