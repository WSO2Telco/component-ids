package com.wso2telco.grant.handler;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.dbutils.DbService;
import com.wso2telco.core.dbutils.model.FederatedIdpMappingDTO;
import com.wso2telco.dao.FederatedTransactionDAO;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.oltu.openidconnect.as.OIDC;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCache;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheEntry;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheKey;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.OAuthTokenPersistenceFactory;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.AuthzCodeDO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AuthorizationCodeGrantHandler;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import static org.wso2.carbon.identity.openidconnect.OIDCConstants.CODE_ID;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CustomAuthCodeGrant extends AuthorizationCodeGrantHandler {

    private static Log log = LogFactory.getLog(CustomAuthCodeGrant.class);

    private static final String ACCESS_TOKEN = "access_token";
    private static final long EXPIRY = 9223372036854775L;
    private static final long EXPIRY_MILLIS = 9223372036854775807L;
    private static boolean isdebug = log.isDebugEnabled();
    private static boolean isPublishingEnabled = false;

    private static MobileConnectConfig mobileConnectConfig = null;
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    private static DbService dbConnection = new DbService();
    private static HashMap<String, MobileConnectConfig.Provider> federatedIdpMap = new HashMap<>();

    static {
        mobileConnectConfig = configurationService.getDataHolder().getMobileConnectConfig();
        isPublishingEnabled = mobileConnectConfig.getDataPublisher().isEnabled();
        for (MobileConnectConfig.Provider prv : mobileConnectConfig.getFederatedIdentityProviders().getProvider()) {
            federatedIdpMap.put(prv.getOperator(), prv);
        }
    }

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        org.apache.log4j.MDC.put("REF_ID", tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());
        log.info("AuthCode Validatation process triggered for authorize code : "
                + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());
        boolean isValidAuthCode;

        try {
            isValidAuthCode = super.validateGrant(tokReqMsgCtx);
        } catch (Exception ex) {
            publishForFailureData(tokReqMsgCtx, isPublishingEnabled);
            log.error("Error occurred while validate the authcode" + ex);
            throw new IdentityOAuth2Exception(ex.getMessage(), ex);
        }

        if (!isValidAuthCode)
            publishForFailureData(tokReqMsgCtx, isPublishingEnabled);

        return isValidAuthCode;
    }

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        log.info("Access Token process triggered for authorize code : "
                + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());

        OAuth2AccessTokenRespDTO tokenRespDTO;

        try {
            if (!mobileConnectConfig.isFederatedDeployment()) {
                tokenRespDTO = issueTokenFromIS(tokReqMsgCtx);
                log.info("Issued token from IS" + tokenRespDTO.getAccessToken());
            } else{
                tokenRespDTO = issueTokenForFederatedIds(tokReqMsgCtx);
                log.info("Issued token from FederatedIds" + tokenRespDTO.getAccessToken());
            }

        } catch (Exception ex) {
            publishForFailureData(tokReqMsgCtx, isPublishingEnabled);
            log.error("error occurred whle issuing token" + ex);
            throw new IdentityOAuth2Exception(ex.getMessage(), ex);
        }

        return tokenRespDTO;

    }

    private OAuth2AccessTokenRespDTO issueTokenForFederatedIds(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        OAuth2AccessTokenRespDTO tokenRespDTO;
        if (isdebug) {
            log.debug(" Identified as FederatedIDP integrated deployment, hence modified Token flow triggered for authcode : "
                    + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());
        }

        FederatedIdpMappingDTO fidpInstance = checkDbIfFederatedAuthToken(tokReqMsgCtx.getAuthorizedUser()
                .getAuthenticatedSubjectIdentifier());
        tokenRespDTO = handleFederatedTokenFlow(tokReqMsgCtx, fidpInstance);
        return tokenRespDTO;

    }

    private void publishForFailureData(OAuthTokenReqMessageContext tokReqMsgCtx, boolean isPublishingEnabled) {

        if (isPublishingEnabled) {
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("Timestamp", String.valueOf(new Date().getTime()));
            tokenMap.put("ClientId", tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId());
            tokenMap.put("ContentType", "application/x-www-form-urlencoded");
            tokenMap.put("sessionId", getValuesFromCacheForFederatedIDP(tokReqMsgCtx, "sessionId"));
            tokenMap.put("State", getValuesFromCacheForFederatedIDP(tokReqMsgCtx, "state"));
            tokenMap.put("Nonce", getValuesFromCacheForFederatedIDP(tokReqMsgCtx, "nonce"));
            tokenMap.put("StatusCode", "400");
            if (isdebug) {
                for (Map.Entry<String, String> entry : tokenMap.entrySet()) {
                    log.debug(entry.getKey() + " : " + entry.getValue());
                }
            }
            DataPublisherUtil.publishTokenEndpointData(tokenMap);
        }

    }

    private String getValuesFromCacheForFederatedIDP(OAuthTokenReqMessageContext request, String key) {

        String cacheResponse = "";
        AuthorizationGrantCacheKey authorizationGrantCacheKey = new AuthorizationGrantCacheKey(request
                .getOauth2AccessTokenReqDTO().getAuthorizationCode());
        AuthorizationGrantCacheEntry authorizationGrantCacheEntry = AuthorizationGrantCache.getInstance()
                .getValueFromCache(authorizationGrantCacheKey);
        if (authorizationGrantCacheEntry != null)
            cacheResponse = getValueFromCacheClaims(authorizationGrantCacheEntry, key);
        return cacheResponse;

    }

    private String getValueFromCacheClaims(AuthorizationGrantCacheEntry authorizationGrantCacheEntry, String key) {
        String cacheClaim = "";
        ClaimMapping acrKey = null;
        Map<ClaimMapping, String> userAttributes = authorizationGrantCacheEntry.getUserAttributes();
        for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()) {
            ClaimMapping mapping = entry.getKey();
            if (mapping.getLocalClaim() != null && mapping.getLocalClaim().getClaimUri().equals(key))
                acrKey = mapping;

        }
        if (acrKey != null)
            cacheClaim = authorizationGrantCacheEntry.getUserAttributes().get(acrKey);
        return cacheClaim;

    }

    private OAuth2AccessTokenRespDTO handleFederatedTokenFlow(OAuthTokenReqMessageContext tokReqMsgCtx,
            FederatedIdpMappingDTO fidpInstance) throws IdentityOAuth2Exception {

        OAuth2AccessTokenRespDTO tokenRespDTO;

        if (fidpInstance != null && fidpInstance.getFidpAuthCode() != null) {

            log.info("AuthCode mapping found in Database hence Federated Identity Access Token Flow initiated for AuthCode : "
                    + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());
            tokenRespDTO = initiateFederatedTokenProcess(tokReqMsgCtx, fidpInstance);

        } else
            tokenRespDTO = issueTokenFromIS(tokReqMsgCtx);

        return tokenRespDTO;
    }

    private OAuth2AccessTokenRespDTO initiateFederatedTokenProcess(OAuthTokenReqMessageContext tokReqMsgCtx,
            FederatedIdpMappingDTO fidpInstance) throws IdentityOAuth2Exception {

        OAuth2AccessTokenRespDTO tokenRespDTO;
        JSONObject fidpTokenResponse = getFederatedTokenResponse(tokReqMsgCtx, fidpInstance);

        tokenRespDTO = processFederatedIDPResponseBeforeSend(fidpTokenResponse, tokReqMsgCtx);

        return tokenRespDTO;
    }

    private OAuth2AccessTokenRespDTO processFederatedIDPResponseBeforeSend(JSONObject fidpTokenResponse,
            OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();

        if (fidpTokenResponse != null && fidpTokenResponse.optString(ACCESS_TOKEN) != "")
            tokenRespDTO = processForSuccessFederatedResponse(fidpTokenResponse, tokReqMsgCtx);

        else if (fidpTokenResponse != null) {
            log.error("Error response from Federated IDP : " + fidpTokenResponse.toString());
            throw new IdentityOAuth2Exception("Error response from Federated IDP : " + fidpTokenResponse.toString());
        }
        return tokenRespDTO;
    }

    private OAuth2AccessTokenRespDTO processForSuccessFederatedResponse(JSONObject fidpTokenResponse,
            OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        FederatedIdpMappingDTO existingToken = null;
        OAuth2AccessTokenRespDTO tokenRespDTO;

        try {
            existingToken = dbConnection.checkIfExistingFederatedToken(fidpTokenResponse.get(ACCESS_TOKEN).toString());
        } catch (Exception e) {
            String errorMsg = "Error while checking if the provided federated token already exist in database for : "
                    + fidpTokenResponse.get(ACCESS_TOKEN).toString();
            log.error(errorMsg + " " + e.getMessage());
        }

        tokenRespDTO = initiateISTokenProcess(existingToken, tokReqMsgCtx);
        tokenRespDTO = postFederatedTokenResponseHanlding(tokReqMsgCtx, tokenRespDTO, fidpTokenResponse);
        return tokenRespDTO;
    }

    private OAuth2AccessTokenRespDTO initiateISTokenProcess(FederatedIdpMappingDTO existingToken,
            OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        OAuth2AccessTokenRespDTO tokenRespDTO;

        if (existingToken != null && existingToken.getAccessToken() != null)
            tokenRespDTO = provideAlreadyISIssedToken(existingToken, tokReqMsgCtx);
        else
            tokenRespDTO = issueTokenFromIS(tokReqMsgCtx);

        return tokenRespDTO;
    }

    private OAuth2AccessTokenRespDTO provideAlreadyISIssedToken(FederatedIdpMappingDTO existingToken,
            OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        OAuth2AccessTokenRespDTO tokenRespDTO;
        AccessTokenDO existingAccessTokenDO = FederatedTransactionDAO.getExisingTokenFromIdentityDB(tokReqMsgCtx,
                existingToken);

        if (isdebug) {
            log.debug("Retrieved latest access token : " + existingAccessTokenDO.getAccessToken() + " for client Id "
                    + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId() + " from database");
        }

        deactivateAuthzCode(tokReqMsgCtx, existingAccessTokenDO.getTokenId(),
                tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());
        clearCacheForAuthCode(tokReqMsgCtx);
        tokenRespDTO = prepareFromExistingToken(existingAccessTokenDO, tokReqMsgCtx);
        return tokenRespDTO;
    }

    private void deactivateAuthzCode(OAuthTokenReqMessageContext tokReqMsgCtx, String tokenId,
                                     String authzCode) throws IdentityOAuth2Exception {
        try {
            // Here we deactivate the authorization and in the process update the tokenId against the authorization
            // code so that we can correlate the current access token that is valid against the authorization code.
            AuthzCodeDO authzCodeDO = new AuthzCodeDO();
            authzCodeDO.setAuthorizationCode(authzCode);
            authzCodeDO.setOauthTokenId(tokenId);
            authzCodeDO.setAuthzCodeId(tokReqMsgCtx.getProperty(CODE_ID).toString());
            OAuthTokenPersistenceFactory.getInstance().getAuthorizationCodeDAO()
                    .deactivateAuthorizationCode(authzCodeDO);
            if (log.isDebugEnabled()
                    && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.AUTHORIZATION_CODE)) {
                log.debug("Deactivated authorization code : " + authzCode);
            }
        } catch (IdentityException e) {
            throw new IdentityOAuth2Exception("Error occurred while deactivating authorization code", e);
        }
    }

    private void clearCacheForAuthCode(OAuthTokenReqMessageContext tokReqMsgCtx) {

        if (this.cacheEnabled) {
            String authzCode = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode();
            String clientId = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
            OAuthCacheKey cacheKey = new OAuthCacheKey(OAuth2Util.buildCacheKeyStringForAuthzCode(clientId, authzCode));
            this.oauthCache.clearCacheEntry(cacheKey);

            if (isdebug) {
                log.debug("Cache was cleared for authorization code info for client id : " + clientId);
            }
        }
    }

    private OAuth2AccessTokenRespDTO prepareFromExistingToken(AccessTokenDO existingAccessTokenDO,
            OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();
        long expireTime = OAuth2Util.getTokenExpireTimeMillis(existingAccessTokenDO);

        if ("ACTIVE".equals(existingAccessTokenDO.getTokenState()) && ((expireTime > 0L) || (expireTime < 0L))) {
            if (isdebug) {
                if (expireTime > 0L)
                    log.debug("Access token " + existingAccessTokenDO.getAccessToken() + " is valid for another "
                            + expireTime + "ms");
                else
                    log.debug("Infinite lifetime Access Token " + existingAccessTokenDO.getAccessToken()
                            + " found in cache");

            }
            tokenRespDTO.setAccessToken(existingAccessTokenDO.getAccessToken());
            tokenRespDTO.setTokenId(existingAccessTokenDO.getTokenId());
            tokenRespDTO.setRefreshToken(existingAccessTokenDO.getRefreshToken());
            if (expireTime > 0L) {
                tokenRespDTO.setExpiresIn(expireTime / 1000L);
                tokenRespDTO.setExpiresInMillis(expireTime);
            } else {
                tokenRespDTO.setExpiresIn(EXPIRY);
                tokenRespDTO.setExpiresInMillis(EXPIRY_MILLIS);
            }
        } else
            tokenRespDTO = issueTokenFromIS(tokReqMsgCtx);

        return tokenRespDTO;

    }

    private OAuth2AccessTokenRespDTO postFederatedTokenResponseHanlding(OAuthTokenReqMessageContext tokReqMsgCtx,
            OAuth2AccessTokenRespDTO tokenRespDTO, JSONObject fidpTokenResponse) throws IdentityOAuth2Exception {

        try {
            dbConnection.insertFederatedTokenMappings(tokenRespDTO.getAccessToken(), tokReqMsgCtx.getAuthorizedUser()
                    .getAuthenticatedSubjectIdentifier(), fidpTokenResponse.get(ACCESS_TOKEN).toString());
        } catch (JSONException e) {
            String errorMsg = "Error while getting the access token from federated token response for : "
                    + tokenRespDTO.getAccessToken();
            log.error(errorMsg);
            throw new IdentityOAuth2Exception(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Database error while inserting the authToken mappings for federated provider, access_token : "
                    + tokenRespDTO.getAccessToken();
            log.error(errorMsg + " " + e.getMessage());
            throw new IdentityOAuth2Exception(errorMsg, e);
        }
        tokenRespDTO.setIDToken(fidpTokenResponse.get(OIDC.Response.ID_TOKEN).toString());
        return tokenRespDTO;
    }

    private OAuth2AccessTokenRespDTO issueTokenFromIS(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        return super.issue(tokReqMsgCtx);

    }

    private FederatedIdpMappingDTO checkDbIfFederatedAuthToken(String fidpAuthCode) {

        FederatedIdpMappingDTO fidpInstance = null;

        try {
            fidpInstance = dbConnection.retrieveFederatedAuthCodeMappings(fidpAuthCode);
        } catch (Exception e) {
            log.error("Error while retrieving federated mapping information hence continuing default flow"
                    + fidpAuthCode);
        }

        return fidpInstance;

    }

    private JSONObject getFederatedTokenResponse(OAuthTokenReqMessageContext tokReqMsgCtx,
            FederatedIdpMappingDTO fidpInstance) throws IdentityOAuth2Exception {

        JSONObject jsonObject = null;
        String responseString;
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = null;
        HttpResponse response = null;

        try {

            post = prepareTokenBodyParameters(fidpInstance);
            post = prepareTokenHeaderParameters(post, tokReqMsgCtx);
            response = client.execute(post);
            responseString = readFederatedTokenResponse(response.getEntity().getContent());

            if (responseString != null)
                jsonObject = new JSONObject(responseString);
            if (isdebug) {
                log.debug("Token response code from Federated IDP " + fidpInstance.getOperator() + ":"
                        + response.getStatusLine().getStatusCode());
                log.debug("Token response message from Federated IDP :  " + fidpInstance.getOperator() + ":"
                        + responseString);
            }
        } catch (Exception e) {
            String errorMsg = "Error while invoking the Federated token endpoint for : " + fidpInstance.getOperator()
                    + " using auth code : " + fidpInstance.getFidpAuthCode();
            log.error(errorMsg + " " + e.getMessage());
            throw new IdentityOAuth2Exception(errorMsg, e);
        } finally {
            if (post != null)
                post.releaseConnection();
        }
        return jsonObject;
    }

    private HttpPost prepareTokenHeaderParameters(HttpPost post, OAuthTokenReqMessageContext tokReqMsgCtx) {

        post.setHeader("Authorization", prepareAuthorizationHeader(tokReqMsgCtx));
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("charset", StandardCharsets.UTF_8.toString());

        if (isdebug) {
            Header[] headers = post.getAllHeaders();
            for (Header eachHeader : headers)
                log.debug("Federated Token request Header Key: " + eachHeader.getName() + " Value: "
                        + eachHeader.getValue());

        }

        return post;
    }

    private HttpPost prepareTokenBodyParameters(FederatedIdpMappingDTO fidpInstance)
            throws UnsupportedEncodingException {

        HttpPost post = new HttpPost(federatedIdpMap.get(fidpInstance.getOperator()).getTokenEndpoint());
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        urlParameters.add(new BasicNameValuePair("code", fidpInstance.getFidpAuthCode()));
        urlParameters.add(new BasicNameValuePair("redirect_uri", mobileConnectConfig.getFederatedCallbackUrl()));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        if (isdebug) {
            log.debug("Federated Token request body parameters : " + post.getEntity());

        }
        return post;

    }

    private String readFederatedTokenResponse(InputStream is) throws IOException {

        String line;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();
        return stringBuilder.toString();

    }

    private String prepareAuthorizationHeader(OAuthTokenReqMessageContext tokReqMsgCtx) {

        String userPass = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId() + ":"
                + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientSecret();
        return "Basic " + Base64Utils.encode(userPass.getBytes(StandardCharsets.UTF_8));

    }
}
