/*******************************************************************************
 * Copyright  (c) 2015-2018, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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

package com.wso2telco.proxy.entity;

import com.google.gson.Gson;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.wso2telco.adminServiceUtil.client.OAuthAdminServiceClient;
import com.wso2telco.model.backchannel.BackChannelOauthResponse;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.model.backchannel.BackChannelRequestDetails;
import com.wso2telco.proxy.model.AuthenticatorException;
import com.wso2telco.proxy.util.AuthProxyConstants;
import com.wso2telco.proxy.util.DBUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import com.wso2telco.dbUtil.DataBaseConnectUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceStub;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceUserRegistrationException;

import javax.naming.ConfigurationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Path("/mc")
public class ServerInitiatedServiceEndpoints {
    private static Log log = LogFactory.getLog(ServerInitiatedServiceEndpoints.class);

    private static MobileConnectConfig mobileConnectConfigs = null;

    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    static {
        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
    }

    @POST
    @Path("/si-authorize/{operatorName}")
    public Response siEndpoint(@Context HttpServletRequest httpServletRequest, @Context
            HttpServletResponse httpServletResponse, @Context HttpHeaders httpHeaders, @Context UriInfo uriInfo,
                               @PathParam("operatorName") String operatorName, @FormParam("client_id") String
                                       formParamClientId, @FormParam("response_type") String
                                       formParamResponseType, @FormParam("scope") String formParamScopeList,
                               @FormParam("request") String request) throws Exception {

        operatorName = operatorName.toLowerCase();
        JWSObject jwsObject;
        boolean isBackChannelAllowed = true;
        String iss;
        String aud = null;
        String version = null;
        String correlationId;
        String loginHint;
        String scopeName;
        String acrValue;
        String responseType;
        String notificationUrl;
        String state;
        String nonce;
        String clientId;
        String clientNotificationToken;
        String redirectUrl;
        BackChannelOauthResponse backChannelOauthResponse = new BackChannelOauthResponse();
        String authorizeEndpointUrl = mobileConnectConfigs.getBackChannelConfig().getAuthorizeEndpoint();

        try {
            jwsObject = processJWE(request);

            if (jwsObject == null) {
                backChannelOauthResponse.setError(Response.Status.BAD_REQUEST.getReasonPhrase());
                backChannelOauthResponse.setErrorDescription("Payload is null or invalid");
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(new Gson().toJson
                        (backChannelOauthResponse)).build();
            }

            Payload payload = jwsObject.getPayload();
            if (log.isDebugEnabled()) {
                log.debug("Recovered payload message: " + payload);
            }

            JSONObject payloadObj = new JSONObject(payload.toJSONObject());
            iss = payloadObj.get(AuthProxyConstants.ISS).toString();
            aud = payloadObj.get(AuthProxyConstants.AUD).toString();
            version = payloadObj.get(AuthProxyConstants.VERSION).toString();
            loginHint = payloadObj.get(AuthProxyConstants.LOGIN_HINT).toString();
            scopeName = payloadObj.get(AuthProxyConstants.SCOPE).toString();
            acrValue = payloadObj.get(AuthProxyConstants.ACR_VALUE).toString();
            responseType = payloadObj.get(AuthProxyConstants.RESPONSE_TYPE).toString();
            notificationUrl = payloadObj.get(AuthProxyConstants.NOTIFICATION_URI).toString();
            state = payloadObj.get(AuthProxyConstants.STATE).toString();
            nonce = payloadObj.get(AuthProxyConstants.NONCE).toString();
            clientId = payloadObj.get(AuthProxyConstants.CLIENT_ID).toString();
            clientNotificationToken = payloadObj.get(AuthProxyConstants.CLIENT_NOTIFICATION_TOKEN).toString();

            if (!payloadObj.has(AuthProxyConstants.CORRELATION_ID)) {
                correlationId = UUID.randomUUID().toString();
            } else {
                correlationId = payloadObj.get(AuthProxyConstants.CORRELATION_ID).toString();
            }

            String sharedKey = getSharedKey(clientId);

            if (!(formParamClientId.equals(clientId)) && !(formParamResponseType.equals(responseType)) && !
                    (formParamScopeList.equals(scopeName))) {
                backChannelOauthResponse.setCorrelationId(correlationId);
                backChannelOauthResponse.setError(Response.Status.BAD_REQUEST.getReasonPhrase());
                backChannelOauthResponse.setErrorDescription("Missing required parameters");
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(new Gson().toJson
                        (backChannelOauthResponse)).build();
            } else if (!isVerifiedSignature(jwsObject, sharedKey)) {
                log.error("Couldn't verify signature: " + jwsObject);
                backChannelOauthResponse.setCorrelationId(correlationId);
                backChannelOauthResponse.setError(Response.Status.BAD_REQUEST.getReasonPhrase());
                backChannelOauthResponse.setErrorDescription("Signed Object is not verified");
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(new Gson().toJson
                        (backChannelOauthResponse)).build();
            } else if (!responseType.equalsIgnoreCase("mc_si_async_code")) {
                log.error("Response type should be mc_si_async_code");
                backChannelOauthResponse.setCorrelationId(correlationId);
                backChannelOauthResponse.setError(Response.Status.BAD_REQUEST.getReasonPhrase());
                backChannelOauthResponse.setErrorDescription("Invalid Request");
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(new Gson().toJson
                        (backChannelOauthResponse)).build();
            } else if (!iss.equals(clientId)) {
                log.error("Issuer ID should be equals to the client ID");
                backChannelOauthResponse.setCorrelationId(correlationId);
                backChannelOauthResponse.setError(Response.Status.BAD_REQUEST.getReasonPhrase());
                backChannelOauthResponse.setErrorDescription("Invalid Request");
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(new Gson().toJson
                        (backChannelOauthResponse)).build();
            } else if (!isUserExists(loginHint)) {
                log.error("User is not registered in IDGW");
                backChannelOauthResponse.setCorrelationId(correlationId);
                backChannelOauthResponse.setError(Response.Status.BAD_REQUEST.getReasonPhrase());
                backChannelOauthResponse.setErrorDescription("User is not a registered user");
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(new Gson().toJson
                        (backChannelOauthResponse)).build();
            } else if (!DataBaseConnectUtils.isBackChannelAllowedScope(scopeName)) {
                log.error("Requested scope should be Back Channel support");
                backChannelOauthResponse.setCorrelationId(correlationId);
                backChannelOauthResponse.setError(Response.Status.BAD_REQUEST.getReasonPhrase());
                backChannelOauthResponse.setErrorDescription("Requested scope/s is/are invalid");
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(new Gson().toJson
                        (backChannelOauthResponse)).build();
            } else if (StringUtils.isEmpty(notificationUrl) || StringUtils.isEmpty(scopeName) || StringUtils.isEmpty
                    (responseType)) {
                backChannelOauthResponse.setCorrelationId(correlationId);
                backChannelOauthResponse.setError(Response.Status.BAD_REQUEST.getReasonPhrase());
                backChannelOauthResponse.setErrorDescription("Bad Request");
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(new Gson().toJson
                        (backChannelOauthResponse)).build();
            } else {
                redirectUrl = getPersistedCallbackUrl(clientId);
                StringBuilder codeUrlBuilder = new StringBuilder();
                BackChannelRequestDetails backChannelRequestDetails = new BackChannelRequestDetails();

                if (StringUtils.isEmpty(redirectUrl)) {
                    backChannelOauthResponse.setCorrelationId(correlationId);
                    backChannelOauthResponse.setError(Response.Status.BAD_REQUEST.getReasonPhrase());
                    backChannelOauthResponse.setErrorDescription("Callback is not a registered callback URL");
                    return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(new Gson().toJson
                            (backChannelOauthResponse)).build();
                }

                backChannelRequestDetails.setCorrelationId(correlationId);
                backChannelRequestDetails.setMsisdn(loginHint);
                backChannelRequestDetails.setNotificationBearerToken(clientNotificationToken);
                backChannelRequestDetails.setNotificationUrl(notificationUrl);
                backChannelRequestDetails.setClientId(clientId);
                backChannelRequestDetails.setRedirectUrl(redirectUrl);

                DataBaseConnectUtils.addBackChannelRequestDetails(backChannelRequestDetails);

                //Currenty it supports only code grant type
                responseType = AuthProxyConstants.CODE;

                if (!isValidNotificationUrl(clientId, notificationUrl)) {
                    backChannelOauthResponse.setCorrelationId(correlationId);
                    backChannelOauthResponse.setError(Response.Status.UNAUTHORIZED.getReasonPhrase());
                    backChannelOauthResponse.setErrorDescription("Notification URL is not a valid URL");
                    return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).entity(new Gson().toJson
                            (backChannelOauthResponse)).build();
                }

                codeUrlBuilder.append(authorizeEndpointUrl)
                        .append("/operator/").append(operatorName)
                        .append("?response_type=").append(responseType)
                        .append("&scope=").append(scopeName)
                        .append("&redirect_uri=").append(redirectUrl)
                        .append("&nonce=").append(nonce)
                        .append("&state=").append(state)
                        .append("&client_id=").append(clientId)
                        .append("&acr_values=").append(acrValue)
                        .append("&operator=").append(operatorName)
                        .append("&login_hint=").append(loginHint)
                        .append("&correlation_id=").append(correlationId)
                        .append("&is_backChannel_allowed=").append(isBackChannelAllowed);

                String code = getAuthCode(codeUrlBuilder.toString());
                log.info("code returned for BackChannel Request: " + code);

                if (code == null) {
                    backChannelOauthResponse.setCorrelationId(correlationId);
                    backChannelOauthResponse.setError(Response.Status.UNAUTHORIZED.getReasonPhrase());
                    backChannelOauthResponse.setErrorDescription("Unauthorized request");
                    return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).entity(new Gson().toJson
                            (backChannelOauthResponse)).build();
                } else {
                    backChannelOauthResponse.setCorrelationId(correlationId);
                    backChannelOauthResponse.setAuthReqId(code);
                    DataBaseConnectUtils.updateCodeInBackChannel(correlationId, code);
                    return Response.status(Response.Status.OK.getStatusCode()).entity(new Gson().toJson
                            (backChannelOauthResponse))
                            .build();
                }
            }
        } catch (ParseException e) {
            backChannelOauthResponse.setError(Response.Status.BAD_REQUEST.getReasonPhrase());
            backChannelOauthResponse.setErrorDescription("Missing required parameters");
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(new Gson().toJson
                    (backChannelOauthResponse)).build();
        } catch (JSONException e) {
            backChannelOauthResponse.setError(Response.Status.BAD_REQUEST.getReasonPhrase());
            backChannelOauthResponse.setErrorDescription("Missing required parameters");
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(new Gson().toJson
                    (backChannelOauthResponse)).build();
        } catch (RemoteException | LoginAuthenticationExceptionException | IdentityOAuthAdminException |
                AuthenticatorException | UserRegistrationAdminServiceUserRegistrationException e) {
            log.error("Error while retrieving Callback Url via Admin calls. ", e);
            backChannelOauthResponse.setError(Response.Status.BAD_REQUEST.getReasonPhrase());
            backChannelOauthResponse.setErrorDescription("IDGW rejected the request");
            return Response.status(Response.Status.FORBIDDEN.getStatusCode()).entity(new Gson().toJson
                    (backChannelOauthResponse)).build();
        }
    }

    private boolean isUserExists(String userName) throws RemoteException,
            UserRegistrationAdminServiceUserRegistrationException {
        UserRegistrationAdminService userRegistrationAdminService = new UserRegistrationAdminServiceStub();
        return userRegistrationAdminService.isUserExist(userName);

    }

    private boolean isValidNotificationUrl(String clientId, String notificationUrl) {
        List<String> allowedURLs = null;
        boolean validity = true;
        try {
            allowedURLs = DBUtils.getNotificationUrls(clientId);
        } catch (Exception ex) {
            log.error("Error while fetching Notification URL list. ", ex);
            validity = false;
        }

        if (!allowedURLs.contains(notificationUrl)) {
            log.error("Invalid Notification URL : " + notificationUrl);
            validity = false;
        }
        return validity;
    }

    private String getPersistedCallbackUrl(String clientId) throws RemoteException,
            LoginAuthenticationExceptionException, IdentityOAuthAdminException {
        OAuthAdminServiceClient oAuthAdminServiceClient = new OAuthAdminServiceClient();
        OAuthConsumerAppDTO oAuthConsumerAppDTO = oAuthAdminServiceClient.getOAuthApplicationData(clientId);
        return oAuthConsumerAppDTO.getCallbackUrl();
    }

    private String getAuthCode(String url) {
        String code = null;
        while (code == null) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);

            HttpParams params = new BasicHttpParams();
            params.setParameter("http.protocol.handle-redirects", false);
            httpGet.setParams(params);

            HttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(httpGet);
            } catch (IOException e) {
                log.info("Error while handling httpClient redirection. ", e);
                return null;
            }
            if (httpResponse.getStatusLine().getStatusCode() == 302) {
                url = httpResponse.getFirstHeader("location").getValue();
                if (url.contains("code=")) {
                    try {
                        code = getParamValueFromURL("code", url);
                        return code;
                    } catch (URISyntaxException e) {
                        log.info(url + " - URL is malformed ", e);
                        return null;
                    }
                }
            } else {
                break;
            }
        }
        return null;
    }

    private String getParamValueFromURL(String requiredParamName, String url) throws URISyntaxException {
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), "UTF-8");
        for (NameValuePair param : params) {
            if (requiredParamName.equals(param.getName())) {
                return param.getValue();
            }
        }
        return null;
    }

    private String getSharedKey(String clientId) throws AuthenticatorException, ConfigurationException {

        String sharedKey = DBUtils.getSpRequestEncryptedKey(clientId);
        return sharedKey;
    }

    private JWSObject processJWE(String message) throws ParseException {
        JWSObject jwsObject;
        try {
            jwsObject = JWSObject.parse(message);
            if (log.isDebugEnabled()) {
                log.debug("JWS object successfully parsed");
            }
        } catch (java.text.ParseException e) {
            log.error("Couldn't parse JWS object: " + e.getMessage());
            throw e;
        }
        return jwsObject;
    }

    private boolean isVerifiedSignature(JWSObject jwsObject, String sharedKey) throws JOSEException, ParseException,
            GeneralSecurityException, IOException {

        RSAPublicKey publicKey = loadPublicKey(sharedKey);
        JWSVerifier verifier = new RSASSAVerifier(publicKey);
        boolean verifiedSignature;
        try {
            if (sharedKey != null)
                verifiedSignature = jwsObject.verify(verifier);
            else {
                log.error("Shared Key is null: " + jwsObject);
                return false;
            }
        } catch (JOSEException e) {
            log.error("Couldn't verify signature: " + jwsObject);
            return false;
        }
        log.info("Verified JWS signature! " + verifiedSignature);
        return verifiedSignature;
    }

    //Generate RSAPublicKey public key
    private RSAPublicKey loadPublicKey(String publicKeyContent) throws GeneralSecurityException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        return  (RSAPublicKey) kf.generatePublic(pubSpec);
    }
}

