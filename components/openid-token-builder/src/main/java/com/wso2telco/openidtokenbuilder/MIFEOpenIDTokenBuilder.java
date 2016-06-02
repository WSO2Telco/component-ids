/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 * 
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.openidtokenbuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Iterator;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.openidconnect.as.messages.IDTokenBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.common.cache.BaseCache;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCache;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheEntry;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheKey;
import org.wso2.carbon.identity.oauth.cache.CacheEntry;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.openidconnect.CustomClaimsCallbackHandler;

import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jwt.PlainJWT;
import com.wso2telco.util.AuthenticationHealper;

// TODO: Auto-generated Javadoc
/**
 * The Class MIFEOpenIDTokenBuilder.
 */
public class MIFEOpenIDTokenBuilder implements org.wso2.carbon.identity.openidconnect.IDTokenBuilder {

	Log LOG = LogFactory.getLog(MIFEOpenIDTokenBuilder.class);

	/** The Constant ACR_HOST_URI. */
	private static final String ACR_HOST_URI = Messages.getString("MIFEOpenIDTokenBuilder.acrHostUri"); //$NON-NLS-1$

	/** The Constant RETRIEVE_SERVICE. */
	private static final String RETRIEVE_SERVICE = Messages.getString("MIFEOpenIDTokenBuilder.retrieveService"); //$NON-NLS-1$

	/** The Constant CREATE_SERVICE. */
	private static final String CREATE_SERVICE = Messages.getString("MIFEOpenIDTokenBuilder.createService"); //$NON-NLS-1$

	/** The Constant APP_PROV_SERVICE. */
	private static final String APP_PROV_SERVICE = Messages.getString("MIFEOpenIDTokenBuilder.appProvService"); //$NON-NLS-1$

	/** The Constant SERVICE_PROVIDER. */
	private static final String SERVICE_PROVIDER = Messages.getString("MIFEOpenIDTokenBuilder.serviceProvider"); //$NON-NLS-1$

	/** The Constant SERVICE_KEY. */
	private static final String SERVICE_KEY = Messages.getString("MIFEOpenIDTokenBuilder.serviceKey"); //$NON-NLS-1$

	/** The Constant ACR_ACCESS_TOKEN. */
	private static final String ACR_ACCESS_TOKEN = Messages.getString("MIFEOpenIDTokenBuilder.acrAccessToken"); //$NON-NLS-1$

	/** The log. */
	private static Log log = LogFactory.getLog(MIFEOpenIDTokenBuilder.class);

	/** The debug. */
	private static boolean DEBUG = log.isDebugEnabled();

	/** The http client. */
	private HttpClient httpClient;

	/** The acr app id. */
	private String acrAppID;


	public String buildIDToken(OAuthTokenReqMessageContext request, OAuth2AccessTokenRespDTO tokenRespDTO) 	throws IdentityOAuth2Exception {
		try {
			OAuthServerConfiguration config = OAuthServerConfiguration.getInstance();
			String issuer = config.getOpenIDConnectIDTokenIssuerIdentifier();
			int lifetime = Integer.parseInt(config.getOpenIDConnectIDTokenExpiration()) * 1000;
			int curTime = (int) Calendar.getInstance().getTimeInMillis();

			// String msisdn = request.getAuthorizedUser().replaceAll("@.*",
			// ""); //$NON-NLS-1$ //$NON-NLS-2$
			String msisdn = AuthenticationHealper.getUser(request).replaceAll("@.*", ""); //$NON-NLS-1$ //$NON-NLS-2$

			msisdn = "tel:+".concat(msisdn); //$NON-NLS-1$

			// Loading respective application data
			OAuthAppDO oAuthAppDO;
			oAuthAppDO = getAppInformation(request.getOauth2AccessTokenReqDTO());
			String applicationName = oAuthAppDO.getApplicationName();

			// Get authenticators used
			String amr = getValuesFromCache(request, "amr");

			// Retrieve or create an ACR app
			acrAppID = getACRAppID(applicationName);

			// Set ACR (PCR) to sub
			String subject = null;
			subject = createLocalACR(msisdn, applicationName);
			subject = URLEncoder.encode(subject, "UTF-8");

			// Get access token issued time
			String accessTokenIssuedTime = getAccessTokenIssuedTime(tokenRespDTO.getAccessToken(), request);

			// Set base64 encoded value of the access token to atHash
			String atHash = new String(Base64.encodeBase64(tokenRespDTO.getAccessToken().getBytes()));

			// Get LOA used
			String acr = getValuesFromCache(request, "acr");

			if (DEBUG) {
				log.debug("Using issuer " + issuer); //$NON-NLS-1$
				log.debug("Subject " + subject); //$NON-NLS-1$
				log.debug("ID Token expiration seconds" + lifetime); //$NON-NLS-1$
				log.debug("Current time " + curTime); //$NON-NLS-1$
			}

			IDTokenBuilder builder = new IDTokenBuilder().setIssuer(issuer).setSubject(subject)
					.setAudience(request.getOauth2AccessTokenReqDTO().getClientId())
					.setAuthorizedParty(request.getOauth2AccessTokenReqDTO().getClientId())
					.setExpiration(curTime + lifetime).setIssuedAt(curTime).setAuthTime(accessTokenIssuedTime)
					.setAtHash(atHash).setClaim("acr", acr).setClaim("amr", amr); //$NON-NLS-1$ //$NON-NLS-2$
			// setting up custom claims
			CustomClaimsCallbackHandler claimsCallBackHandler = OAuthServerConfiguration.getInstance()
					.getOpenIDConnectCustomClaimsCallbackHandler();
			// TODO CODE COMMENTED#
			// claimsCallBackHandler.handleCustomClaims(builder, request);

			String plainIDToken = builder.buildIDToken();
			return new PlainJWT((com.nimbusds.jwt.JWTClaimsSet) PlainJWT.parse(plainIDToken).getJWTClaimsSet())
					.serialize();
		} catch (Exception e) {
			LOG.error("",e);
			throw new IdentityOAuth2Exception("Error occured while generating the IDToken", e); //$NON-NLS-1$
		} 
	}

	/**
	 * Gets the values from cache.
	 *
	 * @param request
	 *            the request
	 * @param key
	 *            the key
	 * @return the values from cache
	 * @throws IdentityOAuth2Exception
	 *             the identity o auth2 exception
	 */
	private String getValuesFromCache(OAuthTokenReqMessageContext request, String key) throws IdentityOAuth2Exception {
		String keyValue = null;
		AuthorizationGrantCacheKey authorizationGrantCacheKey = new AuthorizationGrantCacheKey(
				request.getOauth2AccessTokenReqDTO().getAuthorizationCode());
		AuthorizationGrantCacheEntry authorizationGrantCacheEntry = (AuthorizationGrantCacheEntry) AuthorizationGrantCache
				.getInstance().getValueFromCache(authorizationGrantCacheKey);

		Iterator<ClaimMapping> userAttributes = authorizationGrantCacheEntry.getUserAttributes().keySet().iterator();

		ClaimMapping acrKey = null;
		while (userAttributes.hasNext()) {
			ClaimMapping mapping = userAttributes.next();
			if (mapping.getLocalClaim() != null) {
				if (mapping.getLocalClaim().getClaimUri().equals(key)) {
					acrKey = mapping;
				}
			}

		}
		// TODO Code Diff
		if (null != acrKey) {
			return authorizationGrantCacheEntry.getUserAttributes().get(acrKey);
		} else {
			throw new IdentityOAuth2Exception("Error occured while retrieving " + key + " from cache");
		}
	}

	private String getAccessTokenIssuedTime(String accessToken, OAuthTokenReqMessageContext request)
			throws IdentityOAuth2Exception {

		AccessTokenDO accessTokenDO = null;
		TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();

		OAuthCache oauthCache = OAuthCache.getInstance();
		String authorizedUser = request.getAuthorizedUser().toString();
		boolean isUsernameCaseSensitive = IdentityUtil.isUserStoreInUsernameCaseSensitive(authorizedUser);
		if (!isUsernameCaseSensitive) {
			authorizedUser = authorizedUser.toLowerCase();
		}

		OAuthCacheKey cacheKey = new OAuthCacheKey(request.getOauth2AccessTokenReqDTO().getClientId() + ":"
				+ authorizedUser + ":" + OAuth2Util.buildScopeString(request.getScope()));
		CacheEntry result = oauthCache.getValueFromCache(cacheKey);

		// cache hit, do the type check.
		if (result instanceof AccessTokenDO) {
			accessTokenDO = (AccessTokenDO) result;
		}

		// Cache miss, load the access token info from the database.
		if (accessTokenDO == null) {
			accessTokenDO = tokenMgtDAO.retrieveAccessToken(accessToken, false);
		}

		// if the access token or client id is not valid
		if (accessTokenDO == null) {
			throw new IdentityOAuth2Exception("Access token based information is not available in cache or database");
		}

		return Long.toString(accessTokenDO.getIssuedTime().getTime() / 1000);
	}

	/**
	 * Gets the app information.
	 *
	 * @param tokenReqDTO
	 *            the token req dto
	 * @return the app information
	 * @throws IdentityOAuth2Exception
	 *             the identity o auth2 exception
	 * @throws InvalidOAuthClientException
	 *             the invalid o auth client exception
	 */
	public OAuthAppDO getAppInformation(OAuth2AccessTokenReqDTO tokenReqDTO)
			throws IdentityOAuth2Exception, InvalidOAuthClientException {
		BaseCache<String, OAuthAppDO> appInfoCache = new BaseCache<String, OAuthAppDO>("AppInfoCache"); //$NON-NLS-1$
		if (null != appInfoCache) {
			if (log.isDebugEnabled()) {
				log.debug("Successfully created AppInfoCache under " //$NON-NLS-1$
						+ OAuthConstants.OAUTH_CACHE_MANAGER);
			}
		}

		OAuthAppDO oAuthAppDO = appInfoCache.getValueFromCache(tokenReqDTO.getClientId());
		if (oAuthAppDO != null) {
			return oAuthAppDO;
		} else {
			// TODO CODE COMMENTED#
			// oAuthAppDO = new
			// OAuthAppDAO().getAppInformation(tokenReqDTO.getClientId());
			appInfoCache.addToCache(tokenReqDTO.getClientId(), oAuthAppDO);
			return oAuthAppDO;
		}
	}

	/**
	 * Gets the ACR app id.
	 *
	 * @param applicationName
	 *            the application name
	 * @return the ACR app id
	 * @throws IdentityOAuth2Exception
	 *             the identity o auth2 exception
	 */
	public String getACRAppID(String applicationName) throws IdentityOAuth2Exception {
		StringBuilder requestURLBuilder = new StringBuilder();
		requestURLBuilder.append(ACR_HOST_URI);
		requestURLBuilder.append("/"); //$NON-NLS-1$
		requestURLBuilder.append(APP_PROV_SERVICE);
		requestURLBuilder.append("/"); //$NON-NLS-1$
		requestURLBuilder.append(SERVICE_PROVIDER);
		String requestURL = requestURLBuilder.toString();

		JSONObject requestBody = new JSONObject();
		JSONObject createRequest = null;

		try {
			requestBody.put("appName", applicationName); //$NON-NLS-1$
			requestBody.put("serviceProviderAppId", applicationName); //$NON-NLS-1$
			requestBody.put("description", applicationName + " description"); //$NON-NLS-1$ //$NON-NLS-2$
			createRequest = new JSONObject().put("provisionAppRequest", requestBody); //$NON-NLS-1$
			StringRequestEntity requestEntity = new StringRequestEntity(createRequest.toString(), "application/json", //$NON-NLS-1$
					"UTF-8"); //$NON-NLS-1$
			PostMethod postMethod = new PostMethod(requestURL);
			postMethod.addRequestHeader("Authorization-ACR", "ServiceKey " + SERVICE_KEY); //$NON-NLS-1$ //$NON-NLS-2$
			postMethod.addRequestHeader("Authorization", "Bearer " + ACR_ACCESS_TOKEN); //$NON-NLS-1$ //$NON-NLS-2$
			postMethod.setRequestEntity(requestEntity);

			if (DEBUG) {
				log.debug("Connecting to ACR engine @ " + ACR_HOST_URI); //$NON-NLS-1$
				log.debug("Service name = " + APP_PROV_SERVICE); //$NON-NLS-1$
				log.debug("Service provider = " + SERVICE_PROVIDER); //$NON-NLS-1$
				log.debug("Request - ACR app = " + requestEntity.getContent()); //$NON-NLS-1$
			}

			httpClient = new HttpClient();
			httpClient.executeMethod(postMethod);

			String responseACRApp = new String(postMethod.getResponseBody(), "UTF-8"); //$NON-NLS-1$
			String strACR = ""; //$NON-NLS-1$
			if (null != responseACRApp && !"".equals(responseACRApp)) { //$NON-NLS-1$
				try {
					if (DEBUG) {
						log.debug("Response - ACR app = " + responseACRApp); //$NON-NLS-1$
					}
					strACR = JsonPath.read(responseACRApp, "$.provisionAppResponse.appID"); //$NON-NLS-1$
				} catch (Exception e) {
					log.error("Provisioning of ACR app failed", e); //$NON-NLS-1$
					throw new IdentityOAuth2Exception("Provisioning of ACR app failed", e); //$NON-NLS-1$
				}
			}

			return strACR; // $NON-NLS-1$;

		} catch (UnsupportedEncodingException e) {
			log.error("Error occured while creating request", e); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while creating request", e); //$NON-NLS-1$
		} catch (JSONException e) {
			log.error("Error occured while creating request", e); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while creating request", e); //$NON-NLS-1$
		} catch (HttpException e) {
			log.error("Error occured while creating ACR app", e); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while creating ACR app", e); //$NON-NLS-1$
		} catch (IOException e) {
			log.error("Error occured while creating ACR app", e); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while creating ACR app", e); //$NON-NLS-1$
		}
	}

	public String getACR(String msisdn) throws IdentityOAuth2Exception {

		String strACR = ""; //$NON-NLS-1$
		boolean isRetrievedACR = false;

		// First check if an ACR already exists
		String responseACR = retrieveACR(msisdn);
		if (null != responseACR && !"".equals(responseACR)) { //$NON-NLS-1$
			try {
				if (DEBUG) {
					log.debug("Response - retrieve ACR = " + responseACR); //$NON-NLS-1$
				}
				strACR = JsonPath.read(responseACR, "$.retriveAcrResponse.acr"); //$NON-NLS-1$
				isRetrievedACR = true;
			} catch (Exception e) {
				log.debug("No existing ACR found."); //$NON-NLS-1$
			}
		}

		// If ACR is not retrieved, create a new one
		if (!isRetrievedACR) {
			String responseNewACR = createACR(msisdn);
			if (null != responseNewACR && !"".equals(responseNewACR)) { //$NON-NLS-1$
				try {
					if (DEBUG) {
						log.debug("Response - create ACR = " + responseNewACR); //$NON-NLS-1$
					}
					strACR = JsonPath.read(responseNewACR, "$.createAcrResponse.acrInfo[0].acr"); //$NON-NLS-1$
				} catch (Exception e) {
					log.error("Retrieving/creating processes of ACR failed", e); //$NON-NLS-1$
					throw new IdentityOAuth2Exception("Retrieving/creating processes of ACR failed", e); //$NON-NLS-1$
				}
			}
		}

		return strACR;
	}

	/**
	 * Retrieve acr.
	 *
	 * @param msisdn
	 *            the msisdn
	 * @return the string
	 * @throws IdentityOAuth2Exception
	 *             the identity o auth2 exception
	 */
	public String retrieveACR(String msisdn) throws IdentityOAuth2Exception {

		StringBuilder requestURLBuilder = new StringBuilder();
		requestURLBuilder.append(ACR_HOST_URI);
		requestURLBuilder.append("/"); //$NON-NLS-1$
		requestURLBuilder.append(RETRIEVE_SERVICE);
		requestURLBuilder.append("/"); //$NON-NLS-1$
		requestURLBuilder.append(SERVICE_PROVIDER);
		requestURLBuilder.append("/"); //$NON-NLS-1$
		requestURLBuilder.append(acrAppID);
		String requestURL = requestURLBuilder.toString();

		JSONObject requestBody = new JSONObject();
		JSONObject retrieveRequest = null;

		try {
			requestBody.put("MSISDN", msisdn); //$NON-NLS-1$
			retrieveRequest = new JSONObject().put("retriveAcrRequest", requestBody); //$NON-NLS-1$
			StringRequestEntity requestEntity = new StringRequestEntity(retrieveRequest.toString(), "application/json", //$NON-NLS-1$
					"UTF-8"); //$NON-NLS-1$
			PostMethod postMethod = new PostMethod(requestURL);
			postMethod.addRequestHeader(new Header("Authorization-ACR", "ServiceKey " + SERVICE_KEY)); //$NON-NLS-1$ //$NON-NLS-2$
			postMethod.addRequestHeader("Authorization", "Bearer " + ACR_ACCESS_TOKEN); //$NON-NLS-1$ //$NON-NLS-2$
			postMethod.setRequestEntity(requestEntity);

			if (DEBUG) {
				log.debug("Connecting to ACR engine @ " + ACR_HOST_URI); //$NON-NLS-1$
				log.debug("Service name = " + RETRIEVE_SERVICE); //$NON-NLS-1$
				log.debug("Service provider = " + SERVICE_PROVIDER); //$NON-NLS-1$
				log.debug("App key = " + acrAppID); //$NON-NLS-1$
				log.debug("Request - retrieve ACR = " + requestEntity.getContent()); //$NON-NLS-1$
			}

			httpClient = new HttpClient();
			httpClient.executeMethod(postMethod);

			return new String(postMethod.getResponseBody(), "UTF-8"); //$NON-NLS-1$

		} catch (UnsupportedEncodingException e) {
			log.error("Error occured while creating request", e); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while creating request", e); //$NON-NLS-1$
		} catch (JSONException e) {
			log.error("Error occured while creating request", e); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while creating request", e); //$NON-NLS-1$
		} catch (HttpException e) {
			log.error("Error occured while retrieving ACR", e); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while retrieving ACR", e); //$NON-NLS-1$
		} catch (IOException e) {
			log.error("Error occured while retrieving ACR", e); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while retrieving ACR", e); //$NON-NLS-1$
		}
	}

	/**
	 * Creates the acr.
	 *
	 * @param msisdn
	 *            the msisdn
	 * @return the string
	 * @throws IdentityOAuth2Exception
	 *             the identity o auth2 exception
	 */
	public String createACR(String msisdn) throws IdentityOAuth2Exception {

		StringBuilder requestURLBuilder = new StringBuilder();
		requestURLBuilder.append(ACR_HOST_URI);
		requestURLBuilder.append("/"); //$NON-NLS-1$
		requestURLBuilder.append(CREATE_SERVICE);
		requestURLBuilder.append("/"); //$NON-NLS-1$
		requestURLBuilder.append(SERVICE_PROVIDER);
		requestURLBuilder.append("/"); //$NON-NLS-1$
		requestURLBuilder.append(acrAppID);
		String requestURL = requestURLBuilder.toString();

		JSONObject requestBody = new JSONObject();
		JSONObject createRequest = null;

		try {
			requestBody.put("MSISDN", (Object) new JSONArray().put(msisdn)); //$NON-NLS-1$
			createRequest = new JSONObject().put("createAcrRequest", requestBody); //$NON-NLS-1$
			StringRequestEntity requestEntity = new StringRequestEntity(createRequest.toString(), "application/json", //$NON-NLS-1$
					"UTF-8"); //$NON-NLS-1$
			PostMethod postMethod = new PostMethod(requestURL);
			postMethod.addRequestHeader("Authorization-ACR", "ServiceKey " + SERVICE_KEY); //$NON-NLS-1$ //$NON-NLS-2$
			postMethod.addRequestHeader("Authorization", "Bearer " + ACR_ACCESS_TOKEN); //$NON-NLS-1$ //$NON-NLS-2$
			postMethod.setRequestEntity(requestEntity);

			if (DEBUG) {
				log.debug("Connecting to ACR engine @ " + ACR_HOST_URI); //$NON-NLS-1$
				log.debug("Service name = " + CREATE_SERVICE); //$NON-NLS-1$
				log.debug("Service provider = " + SERVICE_PROVIDER); //$NON-NLS-1$
				log.debug("App key = " + acrAppID); //$NON-NLS-1$
				log.debug("Request - create ACR = " + requestEntity.getContent()); //$NON-NLS-1$
			}

			httpClient = new HttpClient();
			httpClient.executeMethod(postMethod);

			return new String(postMethod.getResponseBody(), "UTF-8"); //$NON-NLS-1$

		} catch (UnsupportedEncodingException e) {
			log.error("Error occured while creating request", e); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while creating request", e); //$NON-NLS-1$
		} catch (JSONException e) {
			log.error("Error occured while creating request", e); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while creating request", e); //$NON-NLS-1$
		} catch (HttpException e) {
			log.error("Error occured while creating ACR", e); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while creating ACR", e); //$NON-NLS-1$
		} catch (IOException e) {
			log.error("Error occured while creating ACR", e); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while creating ACR", e); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.wso2.carbon.identity.openidconnect.IDTokenBuilder#buildIDToken(org.
	 * wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext,
	 * org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO)
	 */
	public String buildIDToken(OAuthAuthzReqMessageContext tokReqMsgCtx, OAuth2AuthorizeRespDTO tokenRespDTO)
			throws IdentityOAuth2Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private String createLocalACR(String msisdn, String serviceProvider) throws Exception {
		String acr = null;
		String keyText = "cY4L3dBf@mifenew";

		byte[] keyValue = keyText.getBytes();

		SecretKey key = new SecretKeySpec(keyValue, "AES");
		String encryptedText = "";
		Cipher cipher = null;

		cipher = Cipher.getInstance("AES");

		if (serviceProvider != null) {
			byte[] plainTextByte = (serviceProvider + msisdn).getBytes();
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encryptedByte = null;

			encryptedByte = cipher.doFinal(plainTextByte);
			encryptedText = Base64.encodeBase64String(encryptedByte);
		} else {
			/// nop
		}

		log.info("Encrypted Text:" + encryptedText);
		return encryptedText;

	}
}
