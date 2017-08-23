package com.wso2telco.grantHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.openidconnect.as.OIDC;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
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
    private static final String TOKEN_GRANT_TYPE = "authorization_code";

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

        super.validateGrant(tokReqMsgCtx);

        OAuth2AccessTokenRespDTO tokenRespDTO = new OAuth2AccessTokenRespDTO();
        FederatedIdpMappingDTO fidpInstance = new FederatedIdpMappingDTO();

        fidpInstance = checkDbIfFederatedAuthToken(fidpInstance, tokReqMsgCtx.getAuthorizedUser()
                .getAuthenticatedSubjectIdentifier());

        if (fidpInstance.getFidpAuthCode() != null) {

            log.info("Federated Identity Access Token Flow initiated");
            readFederatedIdentityProvidersConfigurations();
            tokenRespDTO = initiateFederatedTokenProcess(tokReqMsgCtx, fidpInstance, tokenRespDTO);

        } else {
            tokenRespDTO = issueTokenFromIS(tokReqMsgCtx, tokenRespDTO);
        }

        return tokenRespDTO;

    }

    private OAuth2AccessTokenRespDTO initiateFederatedTokenProcess(OAuthTokenReqMessageContext tokReqMsgCtx,
            FederatedIdpMappingDTO fidpInstance, OAuth2AccessTokenRespDTO tokenRespDTO) throws IdentityOAuth2Exception {

        try {

            JSONObject fidpTokenResponse = getFederatedTokenResponse(tokReqMsgCtx, fidpInstance);

            if (fidpTokenResponse.optString("access_token") != "") {

                FederatedIdpMappingDTO existingToken = dbConnection.checkIfExistingFederatedToken(fidpTokenResponse
                        .get("access_token").toString());

                tokenRespDTO = initiateISTokenProcess(existingToken, tokenRespDTO, tokReqMsgCtx);
                tokenRespDTO = postFederatedTokenResponseHanlding(tokReqMsgCtx, tokenRespDTO, fidpTokenResponse);

            } else {
                log.error("Error response from Federated IDP : " + fidpTokenResponse.toString());
                throw new Exception("Federated IDP responded with error response : " + fidpTokenResponse.toString());
            }

        } catch (Exception e) {
            log.error("Error generating Federeated access token " + e.getMessage());
            throw new IdentityOAuth2Exception("Error generating Federeated access token", e);
        }
        return tokenRespDTO;
    }

    private OAuth2AccessTokenRespDTO initiateISTokenProcess(FederatedIdpMappingDTO existingToken,
            OAuth2AccessTokenRespDTO tokenRespDTO, OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        if (existingToken.getAccessToken() != null) {

            AccessTokenDO existingAccessTokenDO = FederatedTransactionDAO.getExisingTokenFromIdentityDB(tokReqMsgCtx,
                    existingToken);
            invalidateAuthCode(tokReqMsgCtx, existingAccessTokenDO);
            clearCacheForAuthCode(tokReqMsgCtx);
            tokenRespDTO = prepareFromExistingToken(existingAccessTokenDO, tokenRespDTO);

        } else {
            tokenRespDTO = issueTokenFromIS(tokReqMsgCtx, tokenRespDTO);

        }
        return tokenRespDTO;
    }

    private void invalidateAuthCode(OAuthTokenReqMessageContext tokReqMsgCtx, AccessTokenDO existingAccessTokenDO)
            throws IdentityOAuth2Exception {
        this.tokenMgtDAO.deactivateAuthorizationCode(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode(),
                existingAccessTokenDO.getTokenId());
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

    private OAuth2AccessTokenRespDTO prepareFromExistingToken(AccessTokenDO existingAccessTokenDO,
            OAuth2AccessTokenRespDTO tokenRespDTO) {

        long expireTime = OAuth2Util.getTokenExpireTimeMillis(existingAccessTokenDO);
        if (("ACTIVE".equals(existingAccessTokenDO.getTokenState())) && (((expireTime > 0L) || (expireTime < 0L)))) {
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
                tokenRespDTO.setExpiresIn(9223372036854775L);
                tokenRespDTO.setExpiresInMillis(9223372036854775807L);
            }
        }
        return tokenRespDTO;

    }

    private OAuth2AccessTokenRespDTO postFederatedTokenResponseHanlding(OAuthTokenReqMessageContext tokReqMsgCtx,
            OAuth2AccessTokenRespDTO tokenRespDTO, JSONObject fidpTokenResponse) throws Exception {

        dbConnection.insertFederatedTokenMappings(tokenRespDTO.getAccessToken(), tokReqMsgCtx
                .getOauth2AccessTokenReqDTO().getAuthorizationCode(), fidpTokenResponse.get("access_token").toString());
        tokenRespDTO.setIDToken(fidpTokenResponse.get(OIDC.Response.ID_TOKEN).toString());
        return tokenRespDTO;
    }

    private OAuth2AccessTokenRespDTO issueTokenFromIS(OAuthTokenReqMessageContext tokReqMsgCtx,
            OAuth2AccessTokenRespDTO tokenRespDTO) throws IdentityOAuth2Exception {
        tokenRespDTO = super.issue(tokReqMsgCtx);
        return tokenRespDTO;

    }

    private FederatedIdpMappingDTO checkDbIfFederatedAuthToken(FederatedIdpMappingDTO fidpInstance, String fidpAuthCode) {

        try {
            fidpInstance = dbConnection.retrieveFederatedAuthCodeMappings(fidpAuthCode);
        } catch (Exception e) {
            log.error("Error while retrieving federated mapping information hence continuing regular flow"
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
            FederatedIdpMappingDTO fidpInstance) throws Exception {

        HttpURLConnection connection = null;
        InputStream is = null;

        URL obj = new URL(prepareFederatedTokenEndpoint(fidpInstance));

        connection = getFederatedTokenEndpointConnection(obj, tokReqMsgCtx, connection);

        if (connection.getResponseCode() == 200) {
            is = connection.getInputStream();
        } else {
            is = connection.getErrorStream();
        }

        String responseString = readFederatedTokenResponse(is);

        if (connection != null) {
            connection.disconnect();

        }

        JSONObject jsonObject = new JSONObject(responseString);

        if (log.isDebugEnabled()) {
            log.debug("Token response code from Federated IDP " + fidpInstance.getOperator() + ":"
                    + connection.getResponseCode());
            log.debug("Token response message from Federated IDP :  " + fidpInstance.getOperator() + ":"
                    + responseString);
        }

        return jsonObject;

    }

    private HttpURLConnection getFederatedTokenEndpointConnection(URL obj, OAuthTokenReqMessageContext tokReqMsgCtx,
            HttpURLConnection connection) throws IOException {

        connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty(TOKEN_AUTHORIZATION, prepareAuthorizationHeader(tokReqMsgCtx));
        connection.setRequestProperty(TOKEN_CONTENT_TYPE, TOKEN_CONTENT_TYPE_VALUE);
        connection.setRequestProperty("charset", StandardCharsets.UTF_8.toString());
        return connection;

    }

    private String prepareFederatedTokenEndpoint(FederatedIdpMappingDTO fidpInstance) throws Exception {

        String redirectURL = URLEncoder.encode(mobileConnectConfig.getFederatedCallbackUrl(),
                String.valueOf(StandardCharsets.UTF_8));
        String tokenURL = federatedIdpMap.get(fidpInstance.getOperator()).getTokenEndpoint();
        String queryParameters = "grant_type=" + TOKEN_GRANT_TYPE + "&redirect_uri=" + redirectURL;
        String url = tokenURL + "?" + TOKEN_CODE + "=" + fidpInstance.getFidpAuthCode() + "&" + queryParameters;
        return url;

    }

    private String readFederatedTokenResponse(InputStream is) throws IOException {

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        String line;

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();

        return stringBuilder.toString();

    }

    private String prepareAuthorizationHeader(OAuthTokenReqMessageContext tokReqMsgCtx) {

        String userPass = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId() + ":"
                + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientSecret();
        String authorizationHeader = "Basic " + Base64Utils.encode(userPass.getBytes(StandardCharsets.UTF_8));

        return authorizationHeader;

    }
}
