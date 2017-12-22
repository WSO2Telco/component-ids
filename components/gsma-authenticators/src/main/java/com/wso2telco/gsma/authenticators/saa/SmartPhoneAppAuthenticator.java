/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
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

package com.wso2telco.gsma.authenticators.saa;

import com.google.gson.Gson;
import com.wso2telco.Util;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.sp.config.utils.service.SpConfigService;
import com.wso2telco.core.sp.config.utils.service.impl.SpConfigServiceImpl;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.exception.SaaException;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class SmartPhoneAppAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private static Log log = LogFactory.getLog(SmartPhoneAppAuthenticator.class);

    private static final String IS_FLOW_COMPLETED = "isFlowCompleted";
    private static final String MSISDN = "msisdn";
    private static final String CLIENT_ID = "relyingParty";
    private static final String ACR = "acr_values";
    private SpConfigService spConfigService = new SpConfigServiceImpl();
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    private static final String AUTH_FAILED = "Authentication failed";

    private static final String AUTH_FAILED_DETAILED = "Smart phone Authentication failed while trying to authenticate";

    /**
     * The Enum UserResponse.
     */
    protected enum UserResponse {

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

    @Override
    public boolean canHandle(HttpServletRequest request) {
        return true;
    }

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request,
                                           HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException {
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

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        log.debug("Initiating authentication request from Smart Phone Authenticator");

        boolean isFlowCompleted = false;

        String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(),
                context.getCallerSessionKey(), context.getContextIdentifier());

        Map<String, String> paramMap = Util.createQueryParamMap(queryParams);
        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();

        String msisdn = (String) context.getProperty(MSISDN);
        String clientId = paramMap.get(CLIENT_ID);
        String applicationName = applicationConfig.getApplicationName();

        if (log.isDebugEnabled()) {
            log.debug("MSISDN : " + msisdn);
            log.debug("Client ID : " + clientId);
            log.debug("Application name : " + applicationName);
        }

        String url = configurationService.getDataHolder().getMobileConnectConfig().getSaaConfig()
                .getAuthenticationEndpoint().replace("{msisdn}", msisdn);

        handleRetry(request, context, msisdn);
        try {
            DBUtils.insertAuthFlowStatus(msisdn, Constants.STATUS_PENDING, context.getContextIdentifier());

            fallbackIfMsisdnNotRegistered(msisdn);

            SaaRequest saaRequest = createSaaRequest(paramMap, clientId, applicationName, context
                    .getContextIdentifier());

            StringEntity postData = new StringEntity(new Gson().toJson(saaRequest));

            postDataToSaaServer(url, postData);

        } catch (IOException e) {
            log.error("Error occurred while posting data to SAA server", e);
            isFlowCompleted = true;
        } catch (SaaException e) {
            log.error("SAA server returned invalid http status", e);
            isFlowCompleted = true;
        } catch (AuthenticatorException e) {
            log.error("Error occurred while retrieving authentication details form database", e);
            isFlowCompleted = true;
        } catch (Exception e) {
            log.error("Error occurred", e);
            isFlowCompleted = true;
        } finally {
            handleRedirect(response, context, isFlowCompleted);
        }
    }

    private void handleRedirect(HttpServletResponse response, AuthenticationContext context, boolean isFlowCompleted)
            throws AuthenticationFailedException {

        String loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl() +
                Constants.SAA_WAITING_JSP;
        context.setProperty(IS_FLOW_COMPLETED, isFlowCompleted);

        if (!isFlowCompleted) {
            String redirectUrl = response.encodeRedirectURL(loginPage + ("?" + context.getQueryParams())) + "&scope="
                    + (String) context.getProperty("scope")
                    + "&redirect_uri=" + context.getProperty("redirectURI")
                    + "&authenticators=" + getName() + ":" + "LOCAL";

            log.info("Sent request to the SAA server successfully. Redirecting to [ " + redirectUrl + " ] ");

            try {
                response.sendRedirect(redirectUrl);
            } catch (IOException e) {
                log.error("Error occurred while redirecting to waiting page. Passing control to the next " +
                        "authenticator");
            }
        } else {
            log.info("Passing control to the next authenticator");
        }
    }

    private void fallbackIfMsisdnNotRegistered(String msisdn) throws IOException, SaaException {
        HttpClient httpClient = new DefaultHttpClient();

        String url = configurationService.getDataHolder().getMobileConnectConfig().getSaaConfig()
                .getRegistrationEndpoint().replace("{msisdn}", msisdn);

        HttpGet httpGet = new HttpGet(url);

        HttpResponse httpResponse = httpClient.execute(httpGet);

        IsRegisteredResponse isRegisteredResponse = new Gson()
                .fromJson(EntityUtils.toString(httpResponse.getEntity()), IsRegisteredResponse.class);

        if (!isRegisteredResponse.isRegistered()) {
            throw new SaaException("msisdn [ " + msisdn + " ] is not registered in SAA server");
        }
    }

    private void postDataToSaaServer(String url, StringEntity postData) throws SaaException, IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);

        postData.setContentType("application/json");
        httpPost.setEntity(postData);

        HttpResponse httpResponse = httpClient.execute(httpPost);

        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            log.error("SAA server replied with invalid HTTP status [ " + httpResponse.getStatusLine().getStatusCode()
                    + "] ");

            throw new SaaException("Error occurred while posting data to SAA server");
        }
    }

    private void handleRetry(HttpServletRequest request, AuthenticationContext context, String msisdn) {
        if (context.isRetrying()) {
            UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);
            String comment = null;
            if (msisdn != null && !msisdn.isEmpty()) {
                comment = "Initializing Failed";
                userStatus.setIsNewUser(1);
            }
            DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                    DataPublisherUtil.UserState.MSISDN_AUTH_PROCESSING_FAIL,
                    comment);
        }
    }

    private SaaRequest createSaaRequest(Map<String, String> paramMap, String clientId, String applicationName, String
            sessionId) throws Exception {
        SaaRequest saaRequest = new SaaRequest();
        saaRequest.setApplicationName(applicationName);
        saaRequest.setMessage(spConfigService.getSaaMessage(clientId));
        saaRequest.setAcr(paramMap.get(ACR));
        saaRequest.setSpImgUrl(spConfigService.getSaaImageUrl(clientId));
        saaRequest.setReferenceID(sessionId);
        return saaRequest;
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        if (request.getParameter("isTerminated") != null && "true".equals(request.getParameter("isTerminated"))) {
            throw new AuthenticationFailedException("Request timed out");
        }

        UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);
        String sessionDataKey = request.getParameter("sessionDataKey");
        String msisdn = (String) context.getProperty("msisdn");
        String userAction = request.getParameter(Constants.ACTION);

        if (log.isDebugEnabled()) {
            log.debug("SessionDataKey : " + sessionDataKey);
        }
        try {
            if (userAction != null && !userAction.isEmpty()) {
                // Change behaviour depending on user action
                switch (userAction) {
                    case Constants.USER_ACTION_USER_CANCELED:
                        //User clicked cancel button from login or from cancel the transaction from app
                        terminateAuthentication(context, sessionDataKey, UserResponse.REJECTED.name(), UserResponse
                                .EXPIRED.name());
                        break;
                    case Constants.USER_ACTION_REG_REJECTED:
                        //User clicked cancel button from registration
                        terminateAuthentication(context, sessionDataKey, UserResponse.REJECTED.name(), UserResponse
                                .EXPIRED.name());
                        break;
                    case Constants.USER_ACTION_USER_TIMEOUT:
                        //User timeout at login
                        terminateAuthentication(context, sessionDataKey, UserResponse.REJECTED.name(), UserResponse
                                .EXPIRED.name());
                        break;
                    case Constants.USER_APPROVE:
                        context.setProperty(IS_FLOW_COMPLETED, true);
                        context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
                        break;
                }
            }

            // Check if the user has provided consent
            String responseStatus = DBUtils.getAuthFlowStatus(sessionDataKey);
            if (!responseStatus.equalsIgnoreCase(UserResponse.APPROVED.name())) {
                log.error(AUTH_FAILED);
                throw new AuthenticatorException(AUTH_FAILED);
            }

        } catch (AuthenticatorException e) {
            log.error(AUTH_FAILED_DETAILED, e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
        AuthenticationContextHelper.setSubject(context, msisdn);

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
            log.error("Authentication Exception occurred in terminateAuthentication method in Smart Phone " +
                    "Authenticator", e);
        }
        throw new AuthenticationFailedException("Authenticator is terminated");
    }

    @Override
    protected boolean retryAuthenticationEnabled() {
        return false;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        return request.getParameter("sessionDataKey");
    }

    @Override
    public String getFriendlyName() {
        return Constants.SAA_AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {
        return Constants.SAA_AUTHENTICATOR_NAME;
    }

}