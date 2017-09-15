/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
package com.wso2telco.user.impl;

import com.google.gson.Gson;
import com.wso2telco.config.*;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.dbutils.DbService;
import com.wso2telco.core.dbutils.model.FederatedIdpMappingDTO;
import com.wso2telco.claims.ClaimsRetrieverFactory;
import com.wso2telco.core.config.model.ScopeDetailsConfig;
import com.wso2telco.dao.DBConnection;
import com.wso2telco.dao.ScopeDetails;
import com.wso2telco.util.ClaimUtil;
import org.apache.amber.oauth2.common.utils.JSONUtils;
import com.wso2telco.util.ClaimsRetrieverType;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCache;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheEntry;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheKey;
import org.wso2.carbon.identity.oauth.user.UserInfoClaimRetriever;
import org.wso2.carbon.identity.oauth.user.UserInfoEndpointException;
import org.wso2.carbon.identity.oauth.user.UserInfoResponseBuilder;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

// TODO: Auto-generated Javadoc

/**
 * The Class ClaimInfoMultipleScopeResponseBuilder.
 */
public class ClaimInfoMultipleScopeResponseBuilder implements UserInfoResponseBuilder {

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(ClaimInfoMultipleScopeResponseBuilder.class);

    private final String hashPhoneScope = "mc_identity_phonenumber_hashed";

    private final String phone_number_claim = "phone_number";

    private final String operator = "operator1";

    private List<ScopeDetailsConfig.Scope> scopes;

    /**
     * The openid scopes.
     */
    List<String> openidScopes = Arrays.asList("profile", "email", "address", "phone", "openid","mc_identity_phonenumber_hashed");

    private static DbService dbConnection = new DbService();
    private static MobileConnectConfig mobileConnectConfig = null;
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    static {
        mobileConnectConfig = configurationService.getDataHolder().getMobileConnectConfig();
    }

    private static MobileConnectConfig.FederatedIdentityProviders federatedIdps = mobileConnectConfig
            .getFederatedIdentityProviders();

    private static HashMap<String, MobileConnectConfig.Provider> federatedIdpMap = new HashMap<>();

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.oauth.user.UserInfoResponseBuilder#getResponseString(org.wso2.carbon.identity
     * .oauth2.dto.OAuth2TokenValidationResponseDTO)
     */
    public String getResponseString(OAuth2TokenValidationResponseDTO tokenResponse) throws UserInfoEndpointException,
            OAuthSystemException {

        String tokenValue = tokenResponse.getAuthorizationContextToken()
                .getTokenString();

        org.apache.log4j.MDC.put("REF_ID", tokenValue);

        log.info("Start Generating User Claim Info for Access token : " + tokenValue);

        if (log.isDebugEnabled()) {
            log.debug("Generating Claim Info for Access token : " + tokenValue);
        }
        if (mobileConnectConfig.isFederatedDeployment()) {
            FederatedIdpMappingDTO fidpInstance = new FederatedIdpMappingDTO();
            fidpInstance = checkDbIfFederatedAccessToken(fidpInstance, tokenResponse);

            if (fidpInstance.getFidpAccessToken() != null) {

                log.info("Federated Identity User Info Flow initiated for " + fidpInstance.getOperator()
                        + "endpoint with access token: " + fidpInstance.getFidpAccessToken());

                readFederatedIdentityProvidersConfigurations();

                String userInfoJson = null;

                try {
                    userInfoJson = initiateFederatedUserInfoProcess(fidpInstance);
                } catch (Exception e) {
                    log.error("Error occurred when invoking federated userinfo endpoint " + e.getMessage());
                    throw new UserInfoEndpointException("Error occurred invoking federated the userinfo endpoint", e);
                }
                return userInfoJson;
            }
        }

        Map<ClaimMapping, String> userAttributes = getUserAttributesFromCache(tokenResponse);
        Map<String, Object> claims = null;
        //read claimValues per scope from scope-config.xml
        //   DataHolder.getInstance().setScopeConfigs(ConfigLoader.getInstance().getScopeConfigs());

        ScopeDetailsConfig scopeConfigs =com.wso2telco.core.config.ConfigLoader.getInstance().getScopeDetailsConfig();

        if (userAttributes != null) {
            UserInfoClaimRetriever retriever = UserInfoEndpointConfig.getInstance().getUserInfoClaimRetriever();
            claims = retriever.getClaimsMap(userAttributes);

            boolean hasAcr = claims.containsKey("acr");
            boolean hasAmr = claims.containsKey("amr");
            if (hasAcr && hasAmr && userAttributes.size() == 2) {
                //userattributes only contains only acr and amr values
                userAttributes = null;

            }
        }
        // Commenting out this since a null entry resides inside cache
//        if (userAttributes == null || userAttributes.isEmpty()) {
//            if (log.isDebugEnabled()) {
//                log.debug("User attributes not found in cache. Trying to retrieve from user store.");
//            }
        try {
            claims = ClaimUtil.getClaimsFromUserStore(tokenResponse);
        } catch (Exception e) {
            log.error("Error while retrieving claims from user store : "+e.getMessage());
            throw new UserInfoEndpointException("Error while retrieving claims from user store.");
        }
//        }

        String contextPath = System.getProperty("request.context.path");
        String[] requestedScopes = tokenResponse.getScope();
        scopes=scopeConfigs.getPremiumScopes();
        if (contextPath.equals("/oauth2")) {
            //oauth2/userinfo requests should serve scopes in openid connect onl
            requestedScopes = getValidScopes(requestedScopes);
            scopes=scopeConfigs.getUserInfoScope();

        }

        Map<String, Object> requestedClaims = null;
        try {
            DBConnection dbConnection = DBConnection.getInstance();
            ScopeDetails scopeDetails = dbConnection.getScopeFromAcessToken(tokenResponse
                    .getAuthorizationContextToken().getTokenString());

            List<MobileConnectConfig.OperatorData> operators= com.wso2telco.core.config.ConfigLoader.getInstance().getMobileConnectConfig().getOperatorsList().getOperatorData();
            String  operatorName =(String)claims.get(operator);
            String claimsRetrieverType= ClaimsRetrieverType.Local.toString();

            for (MobileConnectConfig.OperatorData operator : operators) {
                if(operator.getOperatorName().equalsIgnoreCase(operatorName)){
                    claimsRetrieverType=operator.getUserInfoEndPointType();
                    break;
                }
            }

            requestedClaims= ClaimsRetrieverFactory.getClaimsRetriever(claimsRetrieverType).getRequestedClaims(requestedScopes, scopes, claims);
            requestedClaims.put("sub", scopeDetails.getPcr());

        } catch (NoSuchAlgorithmException e) {
            log.error("User Info retrieval failed because of error while generating hashed claim values : "+e.getMessage());
            throw new UserInfoEndpointException("Error while generating hashed claim values.");
        } catch (Exception e) {
            log.error("User Info retrieval failed because of error while generating sub value : "+e.getMessage());
            throw new UserInfoEndpointException("Error while generating sub value");
        }

        Gson gson = new Gson();
        String userInfoJson = gson.toJson(requestedClaims);
        log.info("User Info retrieval Success");
        log.info("User data : "+userInfoJson);
        return userInfoJson;
    }

    /**
     * Gets the user attributes from cache.
     *
     * @param tokenResponse the token response
     * @return the user attributes from cache
     */
    private Map<ClaimMapping, String> getUserAttributesFromCache(OAuth2TokenValidationResponseDTO tokenResponse) {
        AuthorizationGrantCacheKey cacheKey = new AuthorizationGrantCacheKey(tokenResponse
                .getAuthorizationContextToken().getTokenString());
        AuthorizationGrantCacheEntry cacheEntry = (AuthorizationGrantCacheEntry) AuthorizationGrantCache.getInstance
                ().getValueFromCache(cacheKey);
        if (cacheEntry == null) {
            return new HashMap<ClaimMapping, String>();
        }
        return cacheEntry.getUserAttributes();
    }


    /**
     * Gets the valid scopes.
     *
     * @param requestedScopes the requested scopes
     * @return the valid scopes
     */
    private String[] getValidScopes(String[] requestedScopes) {
        List<String> validScopes = new ArrayList<String>();
        for (String scope : requestedScopes) {
            if (!openidScopes.contains(scope)) {
                continue;
            }
            validScopes.add(scope);
        }
        return validScopes.toArray(new String[validScopes.size()]);
    }


    private String getHashedClaimValue(String claimValue) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(claimValue.getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    /**
     * To verify whether this is federated Token request
     *
     * @param fidpInstance
     * @param tokenResponse
     * @return db instance
     */
    private FederatedIdpMappingDTO checkDbIfFederatedAccessToken(FederatedIdpMappingDTO fidpInstance,
            OAuth2TokenValidationResponseDTO tokenResponse) {

        try {
            fidpInstance = dbConnection.retrieveFederatedTokenMappings(tokenResponse.getAuthorizationContextToken()
                    .getTokenString());
        } catch (Exception e) {
            log.error("Error while retrieving federated token mapping information from DB, hence continuing regular flow : "
                    + tokenResponse.getAuthorizationContextToken().getTokenString());
        }
        return fidpInstance;

    }

    /**
     * Load Federated Identity Provider configurations
     */
    private void readFederatedIdentityProvidersConfigurations() {

        for (MobileConnectConfig.Provider prv : federatedIdps.getProvider()) {
            federatedIdpMap.put(prv.getOperator(), prv);
        }

    }

    private String initiateFederatedUserInfoProcess(FederatedIdpMappingDTO fidpInstance)
            throws UserInfoEndpointException {

        String userInfoJson;
        userInfoJson = getFederatedUserInfoResponse(fidpInstance);

        if (userInfoJson.contains("error")) {
            String errorMsg = userInfoJson + " prompted from federated IDP for provided access_token"
                    + fidpInstance.getAccessToken();
            log.error(errorMsg);
            throw new UserInfoEndpointException(errorMsg);
        }
        return userInfoJson;

    }

    private String getFederatedUserInfoResponse(FederatedIdpMappingDTO fidpInstance) throws UserInfoEndpointException {

        HttpResponse urlResponse;
        urlResponse = invokeFederatedUserInfoEndpoint(fidpInstance);

        String jsonString = null;
        try {
            jsonString = processFederatedUserInfoResponse(urlResponse);
        } catch (IOException e) {
            String errorMsg = "Error while getting response from Federated IDP for userinfo request using access_token : "
                    + fidpInstance.getAccessToken();
            log.error(errorMsg);
            throw new UserInfoEndpointException(errorMsg, e);
        }

        if (log.isDebugEnabled()) {
            log.debug("UserInfo response code from Federated IDP " + fidpInstance.getOperator() + ":"
                    + urlResponse.getStatusLine());
            log.debug("UserInfo response message from Federated IDP " + fidpInstance.getOperator() + ":" + jsonString);
        }
        return jsonString;

    }

    private HttpResponse invokeFederatedUserInfoEndpoint(FederatedIdpMappingDTO fidpInstance)
            throws UserInfoEndpointException {

        String url = federatedIdpMap.get(fidpInstance.getOperator()).getUserInfoEndpoint();
        String accessToken = null;

        try {
            accessToken = URLEncoder.encode(fidpInstance.getFidpAccessToken(), String.valueOf(StandardCharsets.UTF_8));
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Authorization", "Bearer " + accessToken);
            CloseableHttpClient client = HttpClientBuilder.create().disableRedirectHandling().build();
            return client.execute(httpGet);
        } catch (UnsupportedEncodingException e) {
            String errorMsg = "Error while encoding the URL for federated Callback : "
                    + mobileConnectConfig.getFederatedCallbackUrl() + " operator : " + fidpInstance.getOperator()
                    + " endpoint : " + url;;
            log.error(errorMsg);
            throw new UserInfoEndpointException(errorMsg, e);
        } catch (ClientProtocolException e) {
            String errorMsg = "Error while executing request for federated IDP userinfo endpoint using access_token : "
                    + accessToken + " operator : " + fidpInstance.getOperator() + " endpoint : " + url;
            log.error(errorMsg);
            throw new UserInfoEndpointException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Error while getting response from Federated IDP for userinfo request using access_token : "
                    + fidpInstance.getAccessToken()
                    + " operator : "
                    + fidpInstance.getOperator()
                    + " endpoint : "
                    + url;
            log.error(errorMsg);
            throw new UserInfoEndpointException(errorMsg, e);
        }

    }

    private String processFederatedUserInfoResponse(HttpResponse urlResponse) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlResponse.getEntity().getContent(),
                StandardCharsets.UTF_8));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

}