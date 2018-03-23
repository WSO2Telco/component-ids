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

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.wso2telco.adminServiceUtil.client.OAuthAdminServiceClient;
import com.wso2telco.model.BackChannelRequestDetails;
import com.wso2telco.proxy.model.AuthenticatorException;
import com.wso2telco.proxy.util.AuthProxyConstants;
import com.wso2telco.proxy.util.DBUtils;
import org.apache.axis2.AxisFault;
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
import org.json.JSONObject;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

@Path("/mc")
public class ServerInitiatedServiceEndpoints {
    private static Log log = LogFactory.getLog(ServerInitiatedServiceEndpoints.class);
    String authorizeEndpointUrl = "https://localhost:9443/authproxy/oauth2/authorize";

//    //sample request to create signed  jwe
//    @POST
//    @Path("/oauth2/sign")
//    public void sign(@Context HttpServletRequest httpServletRequest, @Context
//            HttpServletResponse httpServletResponse, @Context HttpHeaders httpHeaders, @Context UriInfo uriInfo,
//                     @PathParam("operatorName") String operatorName, String jsonBody) throws Exception {
//
//        String sharedKey = "a0a2abd8-6162-41c3-83d6-1cf559b46afc";
//        String jwe = signJson(jsonBody, sharedKey);
//        log.info("JWE: " + jwe);
//    }
//
//    private String signJson(String message, String sharedKey) throws JOSEException, ParseException {
//        Payload payload = new Payload(message);
//        if (log.isDebugEnabled()) {
//            log.debug("JWS payload message: " + message);
//        }
//
//        // Create JWS header with HS256 algorithm
//        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
//        header.setContentType("text/plain");
//
//        if (log.isDebugEnabled()) {
//            log.debug("JWS header: " + header.toJSONObject());
//        }
//
//        // Create JWS object
//        JWSObject jwsObject = new JWSObject(header, payload);
//
//        if (log.isDebugEnabled()) {
//            log.debug("HMAC key: " + sharedKey);
//        }
//
//
//        JWSSigner signer = new MACSigner(sharedKey.getBytes());
//
//        try {
//            jwsObject.sign(signer);
//        } catch (JOSEException e) {
//            log.error("Couldn't sign JWS object: " + e.getMessage());
//            throw e;
//        }
//
//        // Serialise JWS object to compact format
//        String serializedJWE = jwsObject.serialize();
//
//        if (log.isDebugEnabled()) {
//            log.debug("Serialised JWS object: " + serializedJWE);
//        }
//
//        return serializedJWE;
//    }


    //String url = "http://localhost:9763/oauth2/authorize?login_hint=911111111111&scope=openid&acr_values=500
    // &response_type=code&redirect_uri=http://localhost:9763/playground2/oauth2client&state=state22&nonce=nounce1222
    // &client_id=96sgoYjKb2fJ7AvaCbql0nZhAL8a&operator=spark&&operator=spark&telco_scope=openid&isShowTnc=true
    // &headerMismatchResult=CONTINUE_WITH_HEADER&heFailureResult=TRUST_LOGINHINT_MSISDN&loginhintMsisdn=3Ro
    // %2F1blCTvHN9X%2F%2BJNmy9g%3D%3D&transactionId=9c457c52-c149-4ecb-842a-1656d14268db";

           /* String url = authorizeEndpointUrl+"?login_hint="+loginHint+"&scope="+scopeName+"" +
                    "&acr_values="+acrValue+"&response_type="+responseType+
                    "&redirect_uri="+redirectURL+"&state="+state+"&nonce="+nonce+
                    "&client_id="+clientId+"&operator="+operatorName+"&telco_scope="+scopeName+
                    "&isShowTnc=false&headerMismatchResult=CONTINUE_WITH_HEADER&heFailureResult=TRUST_LOGINHINT_MSISDN";
*/

    // String url="https://localhost:9443/authproxy/oauth2/authorize/operator/spark?response_type=code&scope=openid
    // &redirect_uri=http%3A%2F%2Flocalhost%3A9763%2Fplayground2%2Foauth2client&nonce=nounce1222&state=state22
    // &client_id=96sgoYjKb2fJ7AvaCbql0nZhAL8a&acr_values=500&operator=spark&login_hint=911111111111";

    @POST
    @Path("/si-authorize/{operatorName}")
    public Response siEndpoint(@Context HttpServletRequest httpServletRequest, @Context
            HttpServletResponse httpServletResponse, @Context HttpHeaders httpHeaders, @Context UriInfo uriInfo,
                               @PathParam("operatorName") String operatorName, String jsonBody) throws Exception {
        operatorName = operatorName.toLowerCase();
        JWSObject jwsObject;
        boolean isBackChannelAllowed = true;

        try {
            jwsObject = processJWE(jsonBody);
        } catch (ParseException e) {
            throw new AuthenticatorException(e.getMessage());
        }

        if (jwsObject == null) {
            throw new AuthenticatorException("Payload is null or invalid");
        }

        Payload payload = jwsObject.getPayload();
        if (log.isDebugEnabled()) {
            log.debug("Recovered payload message: " + payload);
        }

        JSONObject payloadObj = new JSONObject(payload.toJSONObject());
        String loginHint = payloadObj.get(AuthProxyConstants.LOGIN_HINT).toString();
        String scopeName = payloadObj.get(AuthProxyConstants.SCOPE).toString();
        String acrValue = payloadObj.get(AuthProxyConstants.ACR_VALUE).toString();
        String responseType = payloadObj.get(AuthProxyConstants.RESPONSE_TYPE).toString();
        String notificationUrl = payloadObj.get(AuthProxyConstants.NOTIFICATION_URI).toString();
        String state = payloadObj.get(AuthProxyConstants.STATE).toString();
        String nonce = payloadObj.get(AuthProxyConstants.NONCE).toString();
        String clientId = payloadObj.get(AuthProxyConstants.CLIENT_ID).toString();
        String clientNotificationToken = payloadObj.get(AuthProxyConstants.CLIENT_NOTIFICATION_TOKEN).toString();

        String sharedKey = getSharedKey(clientId);
        if (!isVerifiedSignature(jwsObject, sharedKey)) {
            log.error("Couldn't verify signature: " + jwsObject);
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(Response.Status.BAD_REQUEST
                    .toString()).build();
        }

        if (StringUtils.isEmpty(notificationUrl) || StringUtils.isEmpty(scopeName) || StringUtils.isEmpty
                (responseType)) {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).entity(Response.Status.BAD_REQUEST
                    .toString()).build();
        } else {

            String redirectUrl;
            try {
                redirectUrl = getPersistedCallbackUrl(clientId);
                if (StringUtils.isEmpty(redirectUrl)) {
                    throw new AuthenticatorException("Callback Url is empty for Client Id: " + clientId);
                }
            } catch (RemoteException | LoginAuthenticationExceptionException | IdentityOAuthAdminException |
                    AuthenticatorException e) {
                log.error("Error while retrieving Callback Url via Admin calls. ", e);
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).entity(Response.Status
                        .UNAUTHORIZED.toString())
                        .build();
            }

            StringBuilder codeUrlBuilder = new StringBuilder();
            BackChannelRequestDetails backChannelRequestDetails = new BackChannelRequestDetails();

            //todo : if there a correlationId in the request set it instead of creating new one.
            String correlationId = UUID.randomUUID().toString();

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
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).entity(Response.Status
                        .UNAUTHORIZED.toString())
                        .build();
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

            // Then there should be a update done in USSD/SMS etc authenticated when the SMS is initiated

            String code = getAuthCode(codeUrlBuilder.toString());
            log.info("code returned: " + code);

            if (code == null) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).entity(Response.Status
                        .UNAUTHORIZED.toString())
                        .build();
            } else {
                DataBaseConnectUtils.updateCodeInBackChannel(correlationId, code);
                return Response.status(Response.Status.OK.getStatusCode()).entity(Response.Status.OK.toString())
                        .build();
            }
        }
    }


    private boolean isValidNotificationUrl(String clientId, String notificationUrl) {
        List<String> allowedURLs;
        try {
            allowedURLs = DBUtils.getNotificationUrls(clientId);
        } catch (Exception ex) {
            log.error("Error while fetching Notification URL list. ", ex);
            return false;
        }

        if (!allowedURLs.contains(notificationUrl)) {
            log.error("Invalid Notification URL : " + notificationUrl);
            return false;
        }
        return true;
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

    private String getSharedKey(String clientId) {
        //todo retrieve from sp configurations
        return "a0a2abd8-6162-41c3-83d6-1cf559b46afc";
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

    private boolean isVerifiedSignature(JWSObject jwsObject, String sharedKey) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(sharedKey.getBytes());
        boolean verifiedSignature;
        try {
            verifiedSignature = jwsObject.verify(verifier);
        } catch (JOSEException e) {
            log.error("Couldn't verify signature: " + jwsObject);
            return false;
        }
        log.info("Verified JWS signature! " + verifiedSignature);
        return verifiedSignature;
    }
}

