package com.wso2telco.grant.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCache;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheEntry;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheKey;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AuthorizationCodeGrantHandler;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.dbutils.DbService;
import com.wso2telco.core.dbutils.model.FederatedIdpMappingDTO;
import com.wso2telco.dao.FederatedTransactionDAO;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;

public class CustomAuthCodeGrant extends AuthorizationCodeGrantHandler {

    private static Log log = LogFactory.getLog(CustomAuthCodeGrant.class);

    private static final String TOKEN_AUTHORIZATION = "Authorization";
    private static final String TOKEN_CONTENT_TYPE = "Content-Type";
    private static final String TOKEN_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";
    private static final String TOKEN_CODE = "code";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String TOKEN_GRANT = "grant_type";
    private static final String TOKEN_GRANT_TYPE = "authorization_code";
    private static final String REDIRECT = "redirect_uri";
    private static final String TOKEN_TYPE = "Basic ";
    private static final long EXPIRY = 9223372036854775L;
    private static final long EXPIRY_MILLIS = 9223372036854775807L;
    private static boolean isdebug = log.isDebugEnabled();

    private static MobileConnectConfig mobileConnectConfig = null;
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    private static DbService dbConnection = new DbService();
    private static HashMap<String, MobileConnectConfig.Provider> federatedIdpMap = new HashMap<>();

    static {
        mobileConnectConfig = configurationService.getDataHolder().getMobileConnectConfig();
        for (MobileConnectConfig.Provider prv : mobileConnectConfig.getFederatedIdentityProviders().getProvider()) {
            federatedIdpMap.put(prv.getOperator(), prv);
        }
    }

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        log.info("AuthCode Validatation process triggered for authorize code : "
                + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());
        boolean isValidAuthCode;

        try {
            isValidAuthCode = super.validateGrant(tokReqMsgCtx);
        } catch (Exception ex) {
            if (mobileConnectConfig.getDataPublisher().isEnabled())
                publishForFailureData(tokReqMsgCtx);

            log.error(ex.getMessage());
            throw new IdentityOAuth2Exception(ex.getMessage(), ex);

        }

        if (!isValidAuthCode && mobileConnectConfig.getDataPublisher().isEnabled())
            publishForFailureData(tokReqMsgCtx);

        return isValidAuthCode;
    }

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        log.info("Access Token process triggered for authorize code : "
                + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());

        OAuth2AccessTokenRespDTO tokenRespDTO = null;

        try {
            if (!mobileConnectConfig.isFederatedDeployment())
                return issueTokenFromIS(tokReqMsgCtx);

            if (isdebug) {
                log.debug(" Identified as FederatedIDP integrated deployment, hence modified Token flow triggered for authcode : "
                        + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());
            }

            FederatedIdpMappingDTO fidpInstance = checkDbIfFederatedAuthToken(tokReqMsgCtx.getAuthorizedUser()
                    .getAuthenticatedSubjectIdentifier());
            tokenRespDTO = handleFederatedTokenFlow(tokReqMsgCtx, fidpInstance);
        } catch (Exception ex) {
            if (mobileConnectConfig.getDataPublisher().isEnabled())
                publishForFailureData(tokReqMsgCtx);

            log.error(ex.getMessage());
            throw new IdentityOAuth2Exception(ex.getMessage(), ex);
        }

        return tokenRespDTO;

    }

    private void publishForFailureData(OAuthTokenReqMessageContext tokReqMsgCtx) {

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

    private String getValuesFromCacheForFederatedIDP(OAuthTokenReqMessageContext request, String key) {

        AuthorizationGrantCacheKey authorizationGrantCacheKey = new AuthorizationGrantCacheKey(request
                .getOauth2AccessTokenReqDTO().getAuthorizationCode());
        AuthorizationGrantCacheEntry authorizationGrantCacheEntry = AuthorizationGrantCache.getInstance()
                .getValueFromCache(authorizationGrantCacheKey);

        if (authorizationGrantCacheEntry == null)
            return "";

        Map<ClaimMapping, String> userAttributes = authorizationGrantCacheEntry.getUserAttributes();
        ClaimMapping acrKey = null;
        for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()) {
            ClaimMapping mapping = entry.getKey();
            if (mapping.getLocalClaim() != null && mapping.getLocalClaim().getClaimUri().equals(key))
                acrKey = mapping;

        }

        if (acrKey != null)
            return authorizationGrantCacheEntry.getUserAttributes().get(acrKey);
        else {
            log.error("Error occured while retrieving " + key + " from cache");
            return "";
        }

    }

    private OAuth2AccessTokenRespDTO handleFederatedTokenFlow(OAuthTokenReqMessageContext tokReqMsgCtx,
            FederatedIdpMappingDTO fidpInstance) throws IdentityOAuth2Exception {

        OAuth2AccessTokenRespDTO tokenRespDTO = null;

        if (fidpInstance != null && fidpInstance.getFidpAuthCode() != null) {

            log.info("AuthCode mapping found in Database hence Federated Identity Access Token Flow initiated for AuthCode : "
                    + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());
            tokenRespDTO = initiateFederatedTokenProcess(tokReqMsgCtx, fidpInstance);

        } else {
            tokenRespDTO = issueTokenFromIS(tokReqMsgCtx);
        }

        return tokenRespDTO;
    }

    private OAuth2AccessTokenRespDTO initiateFederatedTokenProcess(OAuthTokenReqMessageContext tokReqMsgCtx,
            FederatedIdpMappingDTO fidpInstance) throws IdentityOAuth2Exception {

        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();
        JSONObject fidpTokenResponse = getFederatedTokenResponse(tokReqMsgCtx, fidpInstance);

        if (fidpTokenResponse != null && fidpTokenResponse.optString(ACCESS_TOKEN) != "") {

            FederatedIdpMappingDTO existingToken = null;

            try {
                existingToken = dbConnection.checkIfExistingFederatedToken(fidpTokenResponse.get(ACCESS_TOKEN)
                        .toString());
            } catch (JSONException e) {
                String errorMsg = "Error while getting the access token from federated token response for : "
                        + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode();
                log.error(errorMsg + " " + e.getMessage());
                throw new IdentityOAuth2Exception(errorMsg, e);
            } catch (Exception e) {
                String errorMsg = "Error while checking if the provided federated token already exist in database for : "
                        + fidpTokenResponse.get(ACCESS_TOKEN).toString();
                log.error(errorMsg + " " + e.getMessage());
                throw new IdentityOAuth2Exception(errorMsg, e);
            }

            tokenRespDTO = initiateISTokenProcess(existingToken, tokReqMsgCtx);
            tokenRespDTO = postFederatedTokenResponseHanlding(tokReqMsgCtx, tokenRespDTO, fidpTokenResponse);

        } else if (fidpTokenResponse != null) {
            log.error("Error response from Federated IDP : " + fidpTokenResponse.toString());
            throw new IdentityOAuth2Exception("Error response from Federated IDP : " + fidpTokenResponse.toString());
        }

        return tokenRespDTO;
    }

    private OAuth2AccessTokenRespDTO initiateISTokenProcess(FederatedIdpMappingDTO existingToken,
            OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        OAuth2AccessTokenRespDTO tokenRespDTO;

        if (existingToken.getAccessToken() != null) {

            AccessTokenDO existingAccessTokenDO = FederatedTransactionDAO.getExisingTokenFromIdentityDB(tokReqMsgCtx,
                    existingToken);

            if (isdebug) {
                log.debug("Retrieved latest access token : " + existingAccessTokenDO.getAccessToken()
                        + " for client Id " + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId()
                        + " from database");
            }

            invalidateAuthCode(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode(),
                    existingAccessTokenDO.getTokenId());
            clearCacheForAuthCode(tokReqMsgCtx);
            tokenRespDTO = prepareFromExistingToken(existingAccessTokenDO, tokReqMsgCtx);

        } else {
            tokenRespDTO = issueTokenFromIS(tokReqMsgCtx);

        }
        return tokenRespDTO;
    }

    private void invalidateAuthCode(String authCode, String tokenId) throws IdentityOAuth2Exception {

        this.tokenMgtDAO.deactivateAuthorizationCode(authCode, tokenId);

        if (isdebug) {
            log.debug(" Successfully deactivated the authcode used to generate access token, authCode : " + authCode);
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
                if (expireTime > 0L) {
                    log.debug("Access token " + existingAccessTokenDO.getAccessToken() + " is valid for another "
                            + expireTime + "ms");
                } else {
                    log.debug("Infinite lifetime Access Token " + existingAccessTokenDO.getAccessToken()
                            + " found in cache");
                }
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
        String responseString = null;
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

        post.setHeader(TOKEN_AUTHORIZATION, prepareAuthorizationHeader(tokReqMsgCtx));
        post.setHeader(TOKEN_CONTENT_TYPE, TOKEN_CONTENT_TYPE_VALUE);
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
        urlParameters.add(new BasicNameValuePair(TOKEN_GRANT, TOKEN_GRANT_TYPE));
        urlParameters.add(new BasicNameValuePair(TOKEN_CODE, fidpInstance.getFidpAuthCode()));
        urlParameters.add(new BasicNameValuePair(REDIRECT, mobileConnectConfig.getFederatedCallbackUrl()));
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
        return TOKEN_TYPE + Base64Utils.encode(userPass.getBytes(StandardCharsets.UTF_8));

    }
}
