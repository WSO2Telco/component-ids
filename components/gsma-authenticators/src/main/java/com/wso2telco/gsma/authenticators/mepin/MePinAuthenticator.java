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

package com.wso2telco.gsma.authenticators.mepin;

import com.google.gson.Gson;
import com.wso2telco.Util;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.sp.config.utils.service.SpConfigService;
import com.wso2telco.core.sp.config.utils.service.impl.SpConfigServiceImpl;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.exception.MePinException;
import com.wso2telco.gsma.authenticators.exception.SaaException;
import com.wso2telco.gsma.authenticators.model.MePinTransactionRequest;
import com.wso2telco.gsma.authenticators.model.MePinTransactionResponse;
import com.wso2telco.gsma.authenticators.saa.IsRegisteredResponse;
import com.wso2telco.gsma.authenticators.saa.SaaRequest;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.authenticators.util.UserProfileManager;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hashids.Hashids;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

public class MePinAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private static Log log = LogFactory.getLog(MePinAuthenticator.class);

    private static final String IS_FLOW_COMPLETED = "isFlowCompleted";
    private static final String PIN_CLAIM = "http://wso2.org/claims/pin";
    private static final String MSISDN = "msisdn";
    private static final String CLIENT_ID = "relyingParty";
    private static final String ACR = "acr_values";
    private SpConfigService spConfigService = new SpConfigServiceImpl();
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    @Override
    public boolean canHandle(HttpServletRequest request) {
        return true;
    }

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request,
                                           HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException {

        if (context.isLogoutRequest()) {
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        } else {
            super.process(request, response, context);

            boolean isFlowCompleted = (boolean) context.getProperty(IS_FLOW_COMPLETED);

            if (isFlowCompleted) {
                return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
            } else {
                return AuthenticatorFlowStatus.INCOMPLETE;
            }
        }
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        log.info("Initiating authentication request");

        boolean isFlowCompleted = false;

        String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(),
                context.getCallerSessionKey(), context.getContextIdentifier());

        Map<String, String> paramMap = Util.createQueryParamMap(queryParams);
        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();

        String msisdn = (String) context.getProperty(MSISDN);
        String applicationName = applicationConfig.getApplicationName();

        if (log.isDebugEnabled()) {
            log.debug("MSISDN : " + msisdn);
            log.debug("Application name : " + applicationName);
        }

        MobileConnectConfig.MePinConfig mePinConfig = configurationService.getDataHolder().getMobileConnectConfig().getMePinConfig();
        String username = mePinConfig.getUsername();
        String password = mePinConfig.getPassword();
        String authEndPoint = mePinConfig.getAuthEndPoint();

        int acr = (int) context.getProperty(Constants.ACR);

//        handleRetry(request, context, msisdn);

        try {
            DBUtils.insertAuthFlowStatus(msisdn, Constants.STATUS_PENDING, context.getContextIdentifier());

            MePinTransactionRequest mePinTransactionRequest = new MePinTransactionRequest();

            String mePinId = DBUtils.getMePinId(msisdn);
            if (mePinId == null || "".equals(mePinId)) {
                throw new MePinException("No me pin registration found");
            }

            Hashids hashids = new Hashids(UUID.randomUUID().toString(), 31);
            String idetifier = hashids.encode(new java.util.Date().getTime());

            mePinTransactionRequest.setMePinId(mePinId);
            mePinTransactionRequest.setAction("transactions/create");
//            mePinTransactionRequest.setAppId("bcb54836a5a71b698844e8c1923f8a42");
            mePinTransactionRequest.setAppId("5497e675-ecb8-45e2-83c7-a9b12d3f290e");
            mePinTransactionRequest.setIdentifier(idetifier);
            mePinTransactionRequest
                    .setCallbackUrl("http://52.53.173.127:9763/sessionupdater/tnspoints/endpoint/mepin/response");
            mePinTransactionRequest.setIdentifier(mePinId);
            mePinTransactionRequest.setShortMessage("Enrollment Completed");
            mePinTransactionRequest.setHeader("Welcome to MobileConnect");

            mePinTransactionRequest.setExpiryTimeInSeconds(60);
            mePinTransactionRequest.setLogoUrl("");
            mePinTransactionRequest.setSpName("");
            mePinTransactionRequest.setBgImageName("");

            if (acr == 2) {
                mePinTransactionRequest.setMessage("Please swipe to authenticate");
                mePinTransactionRequest.setConfirmationPolicy("mepin_swipe");
            } else if (acr == 3) {
                mePinTransactionRequest.setMessage("Please provide fingerprint to authenticate");
                mePinTransactionRequest.setConfirmationPolicy("mepin_fp");
            }

            String authHeader = username + ":" + password;
            HttpPost httpPost = new HttpPost(authEndPoint);
            String encoding = Base64.getEncoder().encodeToString(authHeader.getBytes("utf-8"));

            SSLContext sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    System.out.println("getAcceptedIssuers =============");
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs,
                                               String authType) {
                    System.out.println("checkClientTrusted =============");
                }

                public void checkServerTrusted(X509Certificate[] certs,
                                               String authType) {
                    System.out.println("checkServerTrusted =============");
                }
            }}, new SecureRandom());

            SSLSocketFactory sf = new SSLSocketFactory(sslContext);
            sf.setHostnameVerifier(new X509HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }

                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                }

                public void verify(String host, X509Certificate cert) throws SSLException {
                }

                public void verify(String host, SSLSocket ssl) throws IOException {
                }
            });


            Scheme httpsScheme = new Scheme("https", 443, sf);
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(httpsScheme);

            ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);
            HttpClient httpClient = new DefaultHttpClient(cm);

            httpPost.setHeader("Authorization", "Basic " + encoding);
            List<NameValuePair> params = new ArrayList<>();

            String jsonData = new Gson().toJson(mePinTransactionRequest);

            params.add(new BasicNameValuePair("mepin_data", jsonData));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            log.info("yyyy : " + jsonData);

            HttpResponse transactionCreateResponse = httpClient.execute(httpPost);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(transactionCreateResponse.getEntity().getContent()));

            StringBuilder transactionCreateResult = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                transactionCreateResult.append(line);
            }

            log.info("xxxxxxxxxx " + transactionCreateResult.toString());
            bufferedReader.close();

            MePinTransactionResponse mePinTransactionResponse = new Gson().fromJson(transactionCreateResult.toString(), MePinTransactionResponse.class);

            DBUtils.insertMePinTrnsaction(context.getContextIdentifier(), mePinTransactionResponse.getTransactionId()
                    , mePinId);
        } catch (AuthenticatorException e) {
            log.info("Error occurred while retrieving authentication details form database", e);
            isFlowCompleted = true;
        } catch (MePinException e) {
            log.info("No me pin id found for msisdn", e);
            isFlowCompleted = true;
        } catch (Exception e) {
            log.info("Error occurred", e);
            isFlowCompleted = true;
        } finally {
            handleRedirect(response, context, isFlowCompleted);
        }
    }

    private void handleRedirect(HttpServletResponse response, AuthenticationContext context, boolean isFlowCompleted)
            throws AuthenticationFailedException {

        String loginPage = configurationService.getDataHolder().getMobileConnectConfig().getAuthEndpointUrl() +
                Constants.ME_PIN_WAITING_JSP;
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
                log.info("Error occurred while redirecting to waiting page. Passing control to the next authenticator");
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

    private void postDataToMePin(MePinTransactionRequest mePinTransactionRequest) throws SaaException, IOException {
        String transactionUrl = configurationService.getDataHolder().getMobileConnectConfig().getMePinConfig()
                .getTransactionEndpoint();

        StringEntity postData = new StringEntity(new Gson().toJson(mePinTransactionRequest));

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(transactionUrl);

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

        AuthenticationContextHelper.setSubject(context, (String) context.getProperty(Constants.MSISDN));

        if("true".equals(request.getParameter(Constants.IS_TERMINATED))){
            context.setProperty(Constants.IS_TERMINATED, true);
            throw new AuthenticationFailedException("Authenticator is terminated");
        }else {
            context.setProperty(IS_FLOW_COMPLETED, true);
            context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
        }

        int acr = (int) context.getProperty(Constants.ACR);
        String msisdn = (String) context.getProperty(Constants.MSISDN);
        String operator = (String) context.getProperty(Constants.OPERATOR);
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);

        log.info("Msisdn : " + msisdn + " operator : " + operator + " acr : " + acr);


        if(isRegistering){
            UserProfileManager userProfileManager = new UserProfileManager();

            try {
                if (acr == 2) {
                    userProfileManager.createUserProfileLoa2(msisdn, operator, Constants.SCOPE_MNV);
                } else if (acr == 3) {
                    userProfileManager.createUserProfileLoa3(msisdn, operator, "", "", "");
                }
            } catch (UserRegistrationAdminServiceIdentityException e) {
                throw new AuthenticationFailedException("Error occurred while creating profile");
            } catch (RemoteException e) {
                throw new AuthenticationFailedException("Error occurred while creating profile");
            }

        }
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
        return Constants.ME_PIN_AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {
        return Constants.ME_PIN_AUTHENTICATOR_NAME;
    }

    private enum UserResponse {

        PENDING,
        APPROVED,
        REJECTED
    }
}