package com.wso2telco.grant.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.openidconnect.as.OIDC;
import org.json.JSONException;
import org.json.JSONObject;
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

public class CustomAuthCodeGrant extends AuthorizationCodeGrantHandler {

    private static Log log = LogFactory.getLog(CustomAuthCodeGrant.class);

    private static final String TOKEN_AUTHORIZATION = "Authorization";
    private static final String TOKEN_CONTENT_TYPE = "Content-Type";
    private static final String TOKEN_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";
    private static final String TOKEN_CODE = "code";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String TOKEN_GRANT = "grant_type=";
    private static final String TOKEN_GRANT_TYPE = "authorization_code";
    private static final String REDIRECT = "redirect_uri";
    private static final String TOKEN_TYPE = "Basic ";
    private static final String QUESTION_MARK = "?";
    private static final String EQUAL = "=";
    private static final String AMPERSAND = "&";
    private static final long EXPIRY = 9223372036854775L;
    private static final long EXPIRY_MILLIS = 9223372036854775807L;

    private static MobileConnectConfig mobileConnectConfig = null;
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    private static DbService dbConnection = new DbService();

    static {
        mobileConnectConfig = configurationService.getDataHolder().getMobileConnectConfig();
    }

    private static MobileConnectConfig.FederatedIdentityProviders federatedIdps = mobileConnectConfig
            .getFederatedIdentityProviders();
    private static HashMap<String, MobileConnectConfig.Provider> federatedIdpMap = new HashMap<>();

    @Override
    public OAuth2AccessTokenRespDTO issue(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {

        OAuth2AccessTokenRespDTO tokenRespDTO = null;

        if (!mobileConnectConfig.isFederatedDeployment())
            return issueTokenFromIS(tokReqMsgCtx);

        if (log.isDebugEnabled()) {
            log.debug(" Identified as FederatedIDP integrated deployment, hence modified Token flow triggered for authcode : "
                    + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());
        }

        super.validateGrant(tokReqMsgCtx);

        FederatedIdpMappingDTO fidpInstance = checkDbIfFederatedAuthToken(tokReqMsgCtx.getAuthorizedUser()
                .getAuthenticatedSubjectIdentifier());
        tokenRespDTO = handleFederatedTokenFlow(tokReqMsgCtx, fidpInstance);

        return tokenRespDTO;

    }

    private OAuth2AccessTokenRespDTO handleFederatedTokenFlow(OAuthTokenReqMessageContext tokReqMsgCtx,
            FederatedIdpMappingDTO fidpInstance) throws IdentityOAuth2Exception {

        OAuth2AccessTokenRespDTO tokenRespDTO = null;

        if (fidpInstance != null && fidpInstance.getFidpAuthCode() != null) {

            log.info("AuthCode mapping found in Database hence Federated Identity Access Token Flow initiated for AuthCode : "
                    + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode());
            readFederatedIdentityProvidersConfigurations();
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
                log.error(errorMsg);
                throw new IdentityOAuth2Exception(errorMsg, e);
            } catch (Exception e) {
                String errorMsg = "Error while checking if the provided federated token already exist in database for : "
                        + fidpTokenResponse.get(ACCESS_TOKEN).toString();
                log.error(errorMsg);
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

            if (log.isDebugEnabled()) {
                log.debug("Retrieved latest access token : " + existingAccessTokenDO.getAccessToken()
                        + " for client Id " + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId()
                        + " from database");
            }

            invalidateAuthCode(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode(),
                    existingAccessTokenDO.getTokenId());
            clearCacheForAuthCode(tokReqMsgCtx);
            tokenRespDTO = prepareFromExistingToken(existingAccessTokenDO);

        } else {
            tokenRespDTO = issueTokenFromIS(tokReqMsgCtx);

        }
        return tokenRespDTO;
    }

    private void invalidateAuthCode(String authCode, String tokenId) throws IdentityOAuth2Exception {

        this.tokenMgtDAO.deactivateAuthorizationCode(authCode, tokenId);

        if (log.isDebugEnabled()) {
            log.debug(" Successfully deactivated the authcode used to generate access token, authCode : " + authCode);
        }
    }

    private void clearCacheForAuthCode(OAuthTokenReqMessageContext tokReqMsgCtx) {

        if (this.cacheEnabled) {
            String authzCode = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode();
            String clientId = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
            OAuthCacheKey cacheKey = new OAuthCacheKey(OAuth2Util.buildCacheKeyStringForAuthzCode(clientId, authzCode));
            this.oauthCache.clearCacheEntry(cacheKey);

            if (log.isDebugEnabled()) {
                log.debug("Cache was cleared for authorization code info for client id : " + clientId);
            }
        }
    }

    private OAuth2AccessTokenRespDTO prepareFromExistingToken(AccessTokenDO existingAccessTokenDO) {

        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();
        long expireTime = OAuth2Util.getTokenExpireTimeMillis(existingAccessTokenDO);

        if ("ACTIVE".equals(existingAccessTokenDO.getTokenState()) && ((expireTime > 0L) || (expireTime < 0L))) {
            if (log.isDebugEnabled()) {
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
        }
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
            log.error(errorMsg);
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

    private void readFederatedIdentityProvidersConfigurations() {

        for (MobileConnectConfig.Provider prv : federatedIdps.getProvider()) {
            federatedIdpMap.put(prv.getOperator(), prv);
        }

    }

    private JSONObject getFederatedTokenResponse(OAuthTokenReqMessageContext tokReqMsgCtx,
            FederatedIdpMappingDTO fidpInstance) throws IdentityOAuth2Exception {

        HttpURLConnection connection = null;
        InputStream is = null;
        JSONObject jsonObject = null;
        String responseString = null;

        URL obj;
        try {
            obj = new URL(prepareFederatedTokenEndpoint(fidpInstance));
            connection = getFederatedTokenEndpointConnection(obj, tokReqMsgCtx);
            is = responseCodeProcess(connection);
            responseString = readFederatedTokenResponse(is);
            if (responseString != null)
                jsonObject = new JSONObject(responseString);
            if (log.isDebugEnabled()) {
                log.debug("Token response code from Federated IDP " + fidpInstance.getOperator() + ":"
                        + connection.getResponseCode());
                log.debug("Token response message from Federated IDP :  " + fidpInstance.getOperator() + ":"
                        + responseString);
            }
        } catch (Exception e) {
            String errorMsg = "Error while invoking the Federated token endpoint for : " + fidpInstance.getOperator();
            log.error(errorMsg);
            throw new IdentityOAuth2Exception(errorMsg, e);
        }

        connection.disconnect();

        return jsonObject;
    }

    private InputStream responseCodeProcess(HttpURLConnection connection) throws IOException {
        return (connection.getResponseCode() == 200) ? connection.getInputStream() : connection.getErrorStream();
    }

    private HttpURLConnection getFederatedTokenEndpointConnection(URL obj, OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IOException {
        
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty(TOKEN_AUTHORIZATION, prepareAuthorizationHeader(tokReqMsgCtx));
        connection.setRequestProperty(TOKEN_CONTENT_TYPE, TOKEN_CONTENT_TYPE_VALUE);
        connection.setRequestProperty("charset", StandardCharsets.UTF_8.toString());
        return connection;
    }

    private String prepareFederatedTokenEndpoint(FederatedIdpMappingDTO fidpInstance) throws IdentityOAuth2Exception {

        String redirectURL;

        try {
            redirectURL = URLEncoder.encode(mobileConnectConfig.getFederatedCallbackUrl(),
                    String.valueOf(StandardCharsets.UTF_8));
        } catch (UnsupportedEncodingException e) {
            String errorMsg = "Error while encoding the URL for federated Callback : "
                    + mobileConnectConfig.getFederatedCallbackUrl();
            log.error(errorMsg);
            throw new IdentityOAuth2Exception(errorMsg, e);

        }
        String tokenURL = federatedIdpMap.get(fidpInstance.getOperator()).getTokenEndpoint();
        String queryParameters = TOKEN_GRANT + TOKEN_GRANT_TYPE + AMPERSAND + REDIRECT + EQUAL + redirectURL;
        return tokenURL + QUESTION_MARK + TOKEN_CODE + EQUAL + fidpInstance.getFidpAuthCode() + AMPERSAND
                + queryParameters;

    }

    private String readFederatedTokenResponse(InputStream is) throws IOException {

        String line = null;
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
