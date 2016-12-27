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
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.ussd.command.SendLoginUssdCommand;
import com.wso2telco.gsma.authenticators.ussd.command.SendRegistrationUssdCommand;
import com.wso2telco.gsma.authenticators.ussd.command.SendUssdCommand;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.manager.client.UserRegistrationAdminServiceClient;
import com.wso2telco.gsma.manager.util.UserProfileClaimsConstant;
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
import org.wso2.carbon.identity.user.registration.stub.dto.UserDTO;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;

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

//        if (request.getParameter("msisdn") != null) {
//            return true;
//        }
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
        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                        context.getCallerSessionKey(),
                        context.getContextIdentifier());

        try {
            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            } else {
                // todo : This is moved to Msisdn Authenticator. Remove if required
//                DBUtils.insertUserResponse(context.getContextIdentifier(), String.valueOf(UserResponse.PENDING));
            }

            //MSISDN will be saved in the context in the MSISDNAuthenticator
            String msisdn = (String) context.getProperty(Constants.MSISDN);

            loginPage = getAuthEndpointUrl(context);
            //String pinEnabled = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getPinauth();
            String ussdResponse = null;

            //Changing SP dashboard Name
            String serviceProviderName = null;

            serviceProviderName = context.getSequenceConfig().getApplicationConfig().getApplicationName();


            log.info("Service Provider Name = " + serviceProviderName);
            if (serviceProviderName.equals("wso2_sp_dashboard")) {
                serviceProviderName = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getDashBoard();

            }
            //String operator= request.getParameter("operator");
            String operator = (String) context.getProperty(Constants.OPERATOR);

            log.info("operator:" + operator);

//            new SendUSSD().sendUSSD(msisdn, context.getContextIdentifier(), serviceProviderName,operator);

            boolean isUserExists = (boolean) context.getProperty(Constants.IS_USER_EXISTS);
            sendUssd(context, msisdn, serviceProviderName, operator, isUserExists);

            log.info("query params: " + queryParams);

            log.info("Context_RedirectURI:" + (String) context.getProperty("redirectURI"));
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
            loginPage = DataHolder.getInstance().getMobileConnectConfig().getAuthEndpointUrl() + Constants.VIEW_REGISTRATION_WAITING;
        } else {
            loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        }
        return loginPage;
    }

    private void sendUssd(AuthenticationContext context, String msisdn, String serviceProviderName, String operator, boolean isUserExists) throws IOException {
        SendUssdCommand sendUssdCommand;

        if (isUserExists) {
            sendUssdCommand = new SendLoginUssdCommand();
        } else {
            sendUssdCommand = new SendRegistrationUssdCommand();
        }
        sendUssdCommand.execute(msisdn, context.getContextIdentifier(), serviceProviderName, operator);
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#processAuthenticationResponse(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
     */
    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        String sessionDataKey = request.getParameter("sessionDataKey");
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
        String msisdn = (String) context.getProperty(Constants.MSISDN);
        String openator = (String) context.getProperty(Constants.OPERATOR);

        boolean isAuthenticated = false;

        // Check if the user has provided consent
        try {

            //String pinEnabled = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getPinauth();

            String responseStatus = getResponseStatus(context, sessionDataKey);

            if (responseStatus != null && responseStatus.equalsIgnoreCase(UserResponse.APPROVED.toString())) {
                isAuthenticated = true;

                if (isRegistering) {
                    createUserProfile(msisdn, openator, Constants.SCOPE_MNV);
                }
            }

        } catch (AuthenticatorException e) {
            throw new AuthenticationFailedException("USSD Authentication failed while trying to authenticate", e);
        } catch (UserRegistrationAdminServiceIdentityException | RemoteException e) {
            throw new AuthenticationFailedException("Error occurred while creating user profile", e);
        }

        if (!isAuthenticated) {
            log.info("USSD Authenticator authentication failed ");
            context.setProperty("faileduser", (String) context.getProperty("msisdn"));

            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to user not providing consent.");
            }

            throw new AuthenticationFailedException("Authentication Failed");
        }
//        AuthenticatedUser user=new AuthenticatedUser();
//        context.setSubject(user);
        AuthenticationContextHelper.setSubject(context, msisdn);

        log.info("USSD Authenticator authentication success");

//        context.setSubject(msisdn);
        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
    }

    public boolean createUserProfile(String username, String operator, String scope) throws UserRegistrationAdminServiceIdentityException, RemoteException {
        boolean isNewUser = false;

                    /* reading admin url from application properties */
        String adminURL = DataHolder.getInstance().getMobileConnectConfig().getAdminUrl() +
                UserProfileClaimsConstant.SERVICE_URL;
        if (log.isDebugEnabled()) {
            log.debug(adminURL);
        }
            /*  getting user registration admin service */
        UserRegistrationAdminServiceClient userRegistrationAdminServiceClient = new UserRegistrationAdminServiceClient(
                adminURL);

                    /*  by sending the claim dialects, gets existing claims list */
        UserFieldDTO[] userFieldDTOs = new UserFieldDTO[0];
        try {
            userFieldDTOs = userRegistrationAdminServiceClient
                    .readUserFieldsForUserRegistration(CLAIM);
        } catch (UserRegistrationAdminServiceIdentityException e) {
            log.error("UserRegistrationAdminServiceIdentityException : " + e.getMessage());
        } catch (RemoteException e) {
            log.error("RemoteException : " + e.getMessage());
        }


        for (int count = 0; count < userFieldDTOs.length; count++) {

            if (OPERATOR_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                userFieldDTOs[count].setFieldValue(operator);
            } else if (LOA_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {

                if (scope.equals(SCOPE_CPI)) {
                    userFieldDTOs[count].setFieldValue(LOA_CPI_VALUE);
                } else if (scope.equals(SCOPE_MNV)) {
                    userFieldDTOs[count].setFieldValue(LOA_MNV_VALUE);
                } else {
                    //nop
                }

            } else if (MOBILE_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                userFieldDTOs[count].setFieldValue(username);
            } else {
                userFieldDTOs[count].setFieldValue("");
            }

            if (log.isDebugEnabled()) {
                log.debug("Value :" + userFieldDTOs[count].getFieldValue() + " : Claim " + userFieldDTOs[count].getClaimUri() + " : Name " + userFieldDTOs[count].getFieldName());
            }
        }
        // setting properties of user DTO
        UserDTO userDTO = new UserDTO();
        userDTO.setOpenID(SCOPE_OPENID);
        userDTO.setPassword(DataHolder.getInstance().getMobileConnectConfig().getAdminPassword());
        userDTO.setUserFields(userFieldDTOs);
        userDTO.setUserName(username);

        // add user DTO to the user registration admin service client
        try {
            userRegistrationAdminServiceClient.addUser(userDTO);

            log.info("User successfully added [ " + username + " ] ");

            isNewUser = true;
        } catch (Exception e) {
            log.error("Error occurred while adding User", e);
        }

        return isNewUser;
    }

    private String getResponseStatus(AuthenticationContext context, String sessionDataKey) throws AuthenticatorException {
        String responseStatus;

        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
        if (isRegistering) {
            responseStatus = DBUtils.getUserRegistrationResponse(sessionDataKey);
        } else {
            responseStatus = DBUtils.getUserLoginResponse(sessionDataKey);
        }
        return responseStatus;
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
