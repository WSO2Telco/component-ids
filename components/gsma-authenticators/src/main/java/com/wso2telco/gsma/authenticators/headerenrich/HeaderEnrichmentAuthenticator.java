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

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.IPRangeChecker;
import com.wso2telco.gsma.authenticators.internal.CustomAuthenticatorServiceComponent;
import com.wso2telco.gsma.authenticators.util.AdminServiceUtil;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.authenticators.util.DecryptionAES;
import com.wso2telco.gsma.authenticators.util.UserProfileManager;
import com.wso2telco.gsma.manager.client.ClaimManagementClient;
import com.wso2telco.gsma.manager.client.LoginAdminServiceClient;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: Auto-generated Javadoc

/**
 * The Class HeaderEnrichmentAuthenticator.
 */
public class HeaderEnrichmentAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

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
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public boolean canHandle(HttpServletRequest request) {

        if (log.isDebugEnabled()) {
            log.debug("Header Enrich Authenticator canHandle invoked");
        }
        String msisdn = null;
        int currentLoa = 0;
        String acr = request.getParameter(Constants.PARAM_ACR);
        if (acr != null && !StringUtils.isEmpty(acr)) {
            currentLoa = Integer.parseInt(acr);
        }

        boolean isUserExists;
        try {
            msisdn = DecryptionAES.decrypt(request.getParameter(Constants.MSISDN_HEADER));

            boolean isShowTnC = Boolean.parseBoolean(request.getParameter(Constants.IS_SHOW_TNC));
            /*this validation is used to identify the first request. when context retry happens msisdn_header parameter
            * comes empty. When the user exists, we need to get user to the consent page by executing method initiateAuthenticationRequest.
            * If the user exists, we need to complete the authenticator by executing processAuthenticationResponse
            */
            if (msisdn != null && !StringUtils.isEmpty(msisdn)) {
                isUserExists = AdminServiceUtil.isUserExists(msisdn);

                if ((isUserExists && !isProfileUpgrade(msisdn, currentLoa, isUserExists)) || !isShowTnC) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } catch (NullPointerException e) {
            // Deliberately return false since headers are null
            return false;
        } catch (UserStoreException e) {
            log.error(e);
        } catch (AuthenticationFailedException e) {
            log.error(e);
        } catch (Exception e) {
            log.error(e);
        }
        return false;
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

        log.info("Initiating authentication request");

        boolean ipValidation = false;
        boolean validOperator = true;
        boolean isProfileUpgrade = false;
        int acr = getAcr(request, context);
        boolean isUserExists = false;

        String operator = request.getParameter(Constants.OPERATOR);
        String msisdn = getMsisdn(request, context); //request.getParameter(Constants.MSISDN_HEADER);

        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                        context.getCallerSessionKey(),
                        context.getContextIdentifier());

        if (log.isDebugEnabled()) {
            log.debug("MSISDN : " + msisdn);
            log.debug("Operator : " + operator);
            log.debug("Query parameters : " + queryParams);
        }

        context.setProperty(Constants.ACR, Integer.parseInt(request.getParameter(Constants.PARAM_ACR)));

        try {
            msisdn = DecryptionAES.decrypt(msisdn);
            if (!validateMsisdnFormat(msisdn)) {
                throw new AuthenticationFailedException("Invalid MSISDN number : " + msisdn);
            }
            context.setProperty(Constants.MSISDN, msisdn);
        } catch (AuthenticationFailedException e) {
            throw e;
        } catch (Exception ex) {
            Logger.getLogger(HeaderEnrichmentAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            int tenantId = -1234;
            UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService()
                    .getTenantUserRealm(tenantId);

            if (userRealm != null) {
                UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
                isUserExists = userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(msisdn));
                isProfileUpgrade = isProfileUpgrade(msisdn, acr, isUserExists);

                context.setProperty(Constants.IS_REGISTERING, !isUserExists);
                context.setProperty(Constants.IS_PROFILE_UPGRADE, isProfileUpgrade);
            }

            String loginPage = getAuthEndpointUrl(msisdn, isProfileUpgrade, Boolean.parseBoolean(request.getParameter(Constants.IS_SHOW_TNC)), context);
            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            }

            String ipAddress = context.getProperty(Constants.IP_ADDRESS) != null ? (String) context.getProperty(Constants.IP_ADDRESS) : null;

            if (ipAddress == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Header IpAddress not found.");
                }
            }

            if (operatorIpValidation.containsKey(operator)) {
                ipValidation = operatorIpValidation.get(operator);
            }

            if (ipAddress != null && ipValidation) {
                validOperator = validateOperator(operator, ipAddress);
            }

            // Throw error when ip validation failure
            if (ipValidation && !validOperator) {
                log.info("HeaderEnrichment Authentication failed from request");
                context.setProperty("faileduser", msisdn);
                throw new AuthenticationFailedException("Authentication Failed");
            }

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            }

            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams))
                    + "&redirect_uri=" + request.getParameter("redirect_uri")
                    + "&authenticators=" + getName() + ":" + "LOCAL" + retryParam);


        } catch (IOException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e);
        }

        return;

    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#processAuthenticationResponse(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
     */
    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        log.info("Processing authentication response");

        AuthenticationContextCache.getInstance().addToCache(new AuthenticationContextCacheKey(context.getContextIdentifier()), new AuthenticationContextCacheEntry(context));

        try {
            int acr = getAcr(request, context);
            boolean isUserExists = false;
            String msisdn = null;
            String operator = null;
            boolean ipValidation = false;
            String trimmedMsisdn = null;

            operator = request.getParameter(Constants.OPERATOR);
            msisdn = getMsisdn(request, context);

            if (operatorIpValidation.containsKey(operator)) {
                ipValidation = operatorIpValidation.get(operator);
            }

            if (log.isDebugEnabled()) {
                log.debug("Redirect URI : " + request.getParameter("redirect_uri"));
            }
            context.setProperty(Constants.OPERATOR, operator);
            context.setProperty("redirectURI", request.getParameter("redirect_uri"));

            try {
                msisdn = DecryptionAES.decrypt(msisdn);
                if (!validateMsisdnFormat(msisdn)) {
                    throw new AuthenticationFailedException("Invalid MSISDN number : " + msisdn);
                }

                context.setProperty(Constants.MSISDN, msisdn);
            } catch (AuthenticationFailedException e) {
                throw e;
            } catch (Exception ex) {
                Logger.getLogger(HeaderEnrichmentAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
            }

            String ipAddress = (String) context.getProperty(Constants.IP_ADDRESS);
            if (ipAddress == null || StringUtils.isEmpty(ipAddress)) {
                ipAddress = retriveIPAddress(request);
            }

            if (ipAddress == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Header ip address not found.");
                }

                // RULE : if operator ip validation is enabled and ip address is blank, break the flow
                if (ipValidation) {
                    log.info("HeaderEnrichment Authentication failed due to not having ip address");
                    context.setProperty("faileduser", msisdn);
                    throw new AuthenticationFailedException("Authentication Failed");
                }
            }

            boolean validOperator = true;

            if (ipAddress != null && ipValidation) {
                validOperator = validateOperator(operator, ipAddress);
            }

            // RULE : if operator ip validation is enabled and ip validation failed, break the flow
            if (ipValidation && !validOperator) {
                log.info("HeaderEnrichment Authentication failed");
                context.setProperty("faileduser", msisdn);
                throw new AuthenticationFailedException("Authentication Failed");
            }

            if (validOperator) {
                if (msisdn != null && msisdn.length() > 1 && (!msisdn.isEmpty())) {
                    // Check the authentication by checking if username exists
                    log.info("Check whether user account exists");
                    try {
                        int tenantId = -1234;
                        UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService()
                                .getTenantUserRealm(tenantId);

                        if (userRealm != null) {
                            UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
                            trimmedMsisdn = msisdn.replace("+", "").trim();
                            isUserExists = userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(trimmedMsisdn));

                            context.setProperty(Constants.IS_REGISTERING, !isUserExists);
                            context.setProperty(Constants.IS_PROFILE_UPGRADE, isProfileUpgrade(msisdn, acr, isUserExists));
                            context.setProperty(Constants.IS_PIN_RESET, false);

                            //TODO: Check if we really need to save the status as pending in HE authenticator
                            DBUtils.insertRegistrationStatus(msisdn, Constants.STATUS_PENDING, context.getContextIdentifier());

                            if (acr == 3) {
                                // if acr is 3, pass the user to next authenticator

                            }

                            if (acr == 2) {
                                if (!isUserExists) {
                                    // if acr is 2, do the registration. register user if a new msisdn and remove other authenticators from step map
                                    new UserProfileManager().createUserProfileLoa2(msisdn, operator, Constants.SCOPE_MNV);
                                }

                                // explicitly remove all other authenticators and mark as a success
                                context.setProperty("removeFollowingSteps", "true");
                            }
                        } else {
                            throw new AuthenticationFailedException("Cannot find the user realm for the given tenant : " + tenantId);
                        }
                    } catch (org.wso2.carbon.user.api.UserStoreException e) {
                        log.error("HeaderEnrichment Authentication failed while trying to authenticate", e);
                        throw new AuthenticationFailedException(e.getMessage(), e);
                    } catch (SQLException e) {
                        log.error(e);
                        throw new AuthenticationFailedException(e.getMessage(), e);
                    } catch (AuthenticatorException e) {
                        log.error(e);
                        throw new AuthenticationFailedException(e.getMessage(), e);
                    } catch (RemoteException e) {
                        log.error(e);
                        throw new AuthenticationFailedException(e.getMessage(), e);
                    } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
                        log.error(e);
                        throw new AuthenticationFailedException(e.getMessage(), e);
                    } catch (LoginAuthenticationExceptionException e) {
                        log.error(e);
                        throw new AuthenticationFailedException(e.getMessage(), e);
                    } catch (UserRegistrationAdminServiceIdentityException e) {
                        throw new AuthenticationFailedException("Error occurred while creating user profile", e);
                    }

                    AuthenticationContextHelper.setSubject(context, msisdn);
                }
            }

            String rememberMe = request.getParameter("chkRemember");

            if (rememberMe != null && "on".equals(rememberMe)) {
                context.setRememberMe(true);
            }
        } catch (AuthenticationFailedException e) {
            // take action based on scope properties
            actionBasedOnHEFailureResult(context, request);
            throw e;
        }

        log.info("Authentication success");
    }

    /**
     * Take action based on scope properties for HE Failure results
     *
     * @param context Authentication Context
     * @param request HTTP request
     */
    private void actionBasedOnHEFailureResult(AuthenticationContext context, HttpServletRequest request) {
        String heFailureResult = request.getParameter(Constants.HE_FAILURE_RESULT);

        if (heFailureResult == null || heFailureResult.isEmpty()) {
            context.setProperty("removeFollowingSteps", "true");
        } else {
            switch (heFailureResult) {
                case Constants.UNTRUST_MSISDN:
                    // On HE failure, untrust the header msisdn and forwards to next authenticator
                    // setting context MSISDN to null
                    context.setProperty(Constants.MSISDN, null);
                    context.setProperty(Constants.MSISDN_HEADER, null);
                    context.setProperty(Constants.INVALIDATE_QUERY_STRING_MSISDN, true);
                    log.info("HE FAILED : UNTRUST_MSISDN");
                    break;

                case Constants.TRUST_HEADER_MSISDN:
                    // On HE failure, trust the header msisdn and forwards to next authenticator
                    context.setProperty(Constants.MSISDN_HEADER, context.getProperty(Constants.MSISDN));
                    context.setProperty(Constants.INVALIDATE_QUERY_STRING_MSISDN, true);
                    log.info("HE FAILED : TRUST_HEADER_MSISDN");
                    break;

                case Constants.TRUST_LOGINHINT_MSISDN:
                    // On HE failure, trust the login hint MSISDN and forwards to next authenticator
                    String loginHintValue = null;

                    try {
                        loginHintValue = DecryptionAES.decrypt(request.getParameter(Constants.LOGIN_HINT_MSISDN));
                    } catch (Exception e) {
                        log.error("Exception Getting the login hint values " + e);
                    }

                    if(loginHintValue != null) {
                        context.setProperty(Constants.MSISDN, loginHintValue);
                        context.setProperty(Constants.MSISDN_HEADER, loginHintValue);
                        context.setProperty(Constants.INVALIDATE_QUERY_STRING_MSISDN, true);
                    }

                    log.info("HE FAILED : TRUST_LOGINHINT_MSISDN");
                    break;

                default:
                    context.setProperty("removeFollowingSteps", "true");
                    log.info("HE FAILED : BREAK THE FLOW");
            }
        }
    }

    /**
     * Retrieves auth endpoint url
     * @param msisdn MSISDN
     * @param isProfileUpgrade True if profile upgrade request
     * @param isShowTnc True if T&C visible
     * @param context Auth Context
     * @return Endpoint
     * @throws UserStoreException
     * @throws AuthenticationFailedException
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
     */
    private String getAuthEndpointUrl(String msisdn, boolean isProfileUpgrade, boolean isShowTnc,
                                      AuthenticationContext context) throws UserStoreException,
            AuthenticationFailedException,
            RemoteException,
            LoginAuthenticationExceptionException,
            RemoteUserStoreManagerServiceUserStoreExceptionException {

        String loginPage;
        if (msisdn != null && !AdminServiceUtil.isUserExists(msisdn) && isShowTnc) {
            context.setProperty(Constants.IS_REGISTERING, true);
            loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl() + Constants.CONSENT_JSP;
        } else {

            if (isProfileUpgrade) {
                loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl()
                        + Constants.PROFILE_UPGRADE_JSP;
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

    private boolean isProfileUpgrade(String msisdn, int currentLoa, boolean isUserExits) throws RemoteException, LoginAuthenticationExceptionException, RemoteUserStoreManagerServiceUserStoreExceptionException, AuthenticationFailedException, UserStoreException {

        if (msisdn != null && isUserExits) {
            String adminURL = configurationService.getDataHolder().getMobileConnectConfig().getAdminUrl();
            LoginAdminServiceClient lAdmin = new LoginAdminServiceClient(adminURL);
            String sessionCookie = lAdmin.authenticate(configurationService.getDataHolder().getMobileConnectConfig().getAdminUsername(),
                    configurationService.getDataHolder().getMobileConnectConfig().getAdminPassword());
            ClaimManagementClient claimManager = new ClaimManagementClient(adminURL, sessionCookie);
            int registeredLoa = Integer.parseInt(claimManager.getRegisteredLOA(msisdn));

            return currentLoa > registeredLoa;
        } else {
            return false;
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
}
