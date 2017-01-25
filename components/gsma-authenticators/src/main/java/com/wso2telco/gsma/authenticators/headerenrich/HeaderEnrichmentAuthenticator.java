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
        boolean isUserExists;
        try {
            msisdn = DecryptionAES.decrypt(request.getParameter(Constants.MSISDN_HEADER));

            /*this validation is used to identify the first request. when context retry happens msisdn_header parameter
            * comes empty. When the user exists, we need to get user to the consent page by executing method initiateAuthenticationRequest.
            * If the user exists, we need to complete the authenticator by executing processAuthenticationResponse
            */
            if (msisdn != null && !StringUtils.isEmpty(msisdn)) {
                isUserExists = AdminServiceUtil.isUserExists(msisdn);

                if (isUserExists) {
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
            e.printStackTrace();
        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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

        boolean ipValidation = false;
        boolean validOperator = true;
        String operator = request.getParameter(Constants.OPERATOR);
        String msisdn = getMsisdn(request, context); //request.getParameter(Constants.MSISDN_HEADER);

        log.info("Initiating authentication request");

        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                        context.getCallerSessionKey(),
                        context.getContextIdentifier());

        context.setProperty(Constants.ACR, Integer.parseInt(request.getParameter(Constants.PARAM_ACR)));

        try {
            msisdn = DecryptionAES.decrypt(msisdn);
            if(!validateMsisdnFormat(msisdn)){
                throw new AuthenticationFailedException("Invalid MSISDN number : " + msisdn);
            }
            context.setProperty(Constants.MSISDN, msisdn);
        } catch (AuthenticationFailedException e) {
            throw e;
        } catch (Exception ex) {
            Logger.getLogger(HeaderEnrichmentAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            String loginPage = getAuthEndpointUrl(msisdn);
            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            }

            String ipAddress = request.getParameter(Constants.IP_ADDRESS);

            if (ipAddress == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Header IpAddress not found.");
                }
            }

            if(operatorIpValidation.containsKey(operator)){
                ipValidation = operatorIpValidation.get(operator);
            }

            if (ipAddress != null && ipValidation) {
                validOperator = validateOperator(operator, ipAddress);
            }

            // Throw error when ip validation failure
            if(ipValidation && !validOperator){
                log.info("HeaderEnrichment Authentication failed from request");
                context.setProperty("faileduser", msisdn);
                context.setRequestAuthenticated(false);
                throw new AuthenticationFailedException("Authentication Failed");
            }

            context.setProperty(Constants.IP_ADDRESS, ipAddress);

            log.info("MSISDN after decryption=" + msisdn);

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
        } catch (UserStoreException e) {
            e.printStackTrace();
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

        log.info("Processing authentication request");

        int acr = getAcr(request, context);
        boolean isUserExists = false;
        String msisdn = null;
        String operator = null;
        boolean ipValidation = false;
        String trimmedMsisdn = null;

        operator = request.getParameter(Constants.OPERATOR);
        msisdn = getMsisdn(request, context);

        if(operatorIpValidation.containsKey(operator)){
            ipValidation = operatorIpValidation.get(operator);
        }

        log.info("HeaderEnrichment redirect URI : " + request.getParameter("redirect_uri"));
        context.setProperty(Constants.OPERATOR, operator);
        context.setProperty("redirectURI", request.getParameter("redirect_uri"));

        try {
            msisdn = DecryptionAES.decrypt(msisdn);
            if(!validateMsisdnFormat(msisdn)){
                throw new AuthenticationFailedException("Invalid MSISDN number : " + msisdn);
            }
        } catch (AuthenticationFailedException e) {
            throw e;
        } catch (Exception ex) {
            Logger.getLogger(HeaderEnrichmentAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
        }

        log.info("msisdn after decryption : " + msisdn);

        String ipAddress = (String)context.getProperty(Constants.IP_ADDRESS);
        if(ipAddress == null  || StringUtils.isEmpty(ipAddress)) {
            ipAddress = retriveIPAddress(request);
        }

        log.info("ip address : " + ipAddress);

        if (ipAddress == null) {
            if (log.isDebugEnabled()) {
                log.debug("Header ip address not found.");
            }

            // RULE : if operator ip validation is enabled and ip address is blank, break the flow
            if (ipValidation) {
                log.info("HeaderEnrichment Authentication failed due to not having ip address");
                context.setProperty("faileduser", msisdn);
                context.setRequestAuthenticated(false);
                throw new AuthenticationFailedException("Authentication Failed");
            }
        }

        boolean validOperator = true;

        if (ipAddress != null && ipValidation) {
            validOperator = validateOperator(operator, ipAddress);
        }

        // RULE : if operator ip validation is enabled and ip validation failed, break the flow
        if(ipValidation && !validOperator){
            log.info("HeaderEnrichment Authentication failed");
            context.setProperty("faileduser", msisdn);
            context.setRequestAuthenticated(false);
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

                        if(acr == 3) {
                            // if acr is 3, pass the user to next authenticator
                            context.setProperty(Constants.IS_REGISTERING, !isUserExists);
                            context.setProperty(Constants.IS_PROFILE_UPGRADE, isProfileUpgrade(msisdn, acr, isUserExists));
                            context.setProperty(Constants.IS_PIN_RESET, false);

                            DBUtils.insertRegistrationStatus(msisdn, Constants.STATUS_PENDING, context.getContextIdentifier());
                        }

                        if (!isUserExists && acr == 2) {
                            // if acr is 2, do the registration
                            // register user if a new msisdn
                            new UserProfileManager().createUserProfileLoa2(msisdn, operator, Constants.SCOPE_MNV);
                        }
                    } else {
                        throw new AuthenticationFailedException("Cannot find the user realm for the given tenant : " + tenantId);
                    }

                    context.setProperty(Constants.MSISDN, msisdn);
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    log.error("HeaderEnrichment Authentication failed while trying to authenticate", e);
                    throw new AuthenticationFailedException(e.getMessage(), e);
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new AuthenticationFailedException(e.getMessage(), e);
                } catch (AuthenticatorException e) {
                    e.printStackTrace();
                    throw new AuthenticationFailedException(e.getMessage(), e);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    throw new AuthenticationFailedException(e.getMessage(), e);
                } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
                    e.printStackTrace();
                    throw new AuthenticationFailedException(e.getMessage(), e);
                } catch (LoginAuthenticationExceptionException e) {
                    e.printStackTrace();
                    throw new AuthenticationFailedException(e.getMessage(), e);
                }catch (UserRegistrationAdminServiceIdentityException e) {
                    throw new AuthenticationFailedException("Error occurred while creating user profile", e);
                }

                AuthenticationContextHelper.setSubject(context, msisdn);
            }
        }

        AuthenticationContextCache.getInstance().addToCache(new AuthenticationContextCacheKey(context.getContextIdentifier()),
                new AuthenticationContextCacheEntry(context));
        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
        log.info("HeaderEnrichment Authenticator authentication success for MSISDN : " + msisdn);
    }

    /**
     * Retrieves auth endpoint url
     * @param msisdn msisdn
     * @return auth endpoint url
     * @throws UserStoreException
     * @throws AuthenticationFailedException
     */
    private String getAuthEndpointUrl(String msisdn) throws UserStoreException, AuthenticationFailedException {
        String loginPage;
        if (msisdn != null && !AdminServiceUtil.isUserExists(msisdn)) {
            loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl() + Constants.CONSENT_JSP;
        } else {
            loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        }
        return loginPage;
    }

    /**
     * Retrieves ACR value from request
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
     * @param request the request
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
            log.error("Error occured Retriving ip address " + e);
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

        log.info("Operator name  " + operator);

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
