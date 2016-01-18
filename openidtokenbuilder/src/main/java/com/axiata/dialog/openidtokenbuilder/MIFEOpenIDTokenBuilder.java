package com.axiata.dialog.openidtokenbuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.*;

import com.nimbusds.jwt.PlainJWT;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.openidconnect.as.messages.IDTokenBuilder;
import org.apache.oltu.openidconnect.as.messages.IDTokenException;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.core.model.OAuthAppDO;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCache;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheEntry;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheKey;
import org.wso2.carbon.identity.oauth.cache.BaseCache;
import org.wso2.carbon.identity.oauth.cache.CacheEntry;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.OAuthCache;
import org.wso2.carbon.identity.oauth.cache.OAuthCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.TokenMgtDAO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.openidconnect.CustomClaimsCallbackHandler;

import com.gsma.authenticators.config.Authenticator;
import com.gsma.authenticators.config.ConfigLoader;
import com.gsma.authenticators.config.LOAConfig;
import com.jayway.jsonpath.JsonPath;

public class MIFEOpenIDTokenBuilder implements
		org.wso2.carbon.identity.openidconnect.IDTokenBuilder {

	private static final String ACR_HOST_URI = Messages
			.getString("MIFEOpenIDTokenBuilder.acrHostUri"); //$NON-NLS-1$
	private static final String RETRIEVE_SERVICE = Messages
			.getString("MIFEOpenIDTokenBuilder.retrieveService"); //$NON-NLS-1$
	private static final String CREATE_SERVICE = Messages
			.getString("MIFEOpenIDTokenBuilder.createService"); //$NON-NLS-1$
	private static final String APP_PROV_SERVICE = Messages
			.getString("MIFEOpenIDTokenBuilder.appProvService"); //$NON-NLS-1$
	private static final String SERVICE_PROVIDER = Messages
			.getString("MIFEOpenIDTokenBuilder.serviceProvider"); //$NON-NLS-1$
	private static final String SERVICE_KEY = Messages
			.getString("MIFEOpenIDTokenBuilder.serviceKey"); //$NON-NLS-1$
	private static final String ACR_ACCESS_TOKEN = Messages
			.getString("MIFEOpenIDTokenBuilder.acrAccessToken"); //$NON-NLS-1$

	private static Log log = LogFactory.getLog(MIFEOpenIDTokenBuilder.class);
	private static boolean DEBUG = log.isDebugEnabled();
	private HttpClient httpClient;
	private String acrAppID;

	public String buildIDToken(OAuthTokenReqMessageContext request,
			OAuth2AccessTokenRespDTO tokenRespDTO) throws IdentityOAuth2Exception {

		OAuthServerConfiguration config = OAuthServerConfiguration.getInstance();
		String issuer = config.getOpenIDConnectIDTokenIssuerIdentifier();
		long lifetime = Integer.parseInt(config.getOpenIDConnectIDTokenExpiration());
		long curTime = Calendar.getInstance().getTimeInMillis() / 1000;

		String msisdn = request.getAuthorizedUser().replaceAll("@.*", ""); //$NON-NLS-1$ //$NON-NLS-2$
		msisdn = "tel:+".concat(msisdn); //$NON-NLS-1$

		// Loading respective application data
		OAuthAppDO oAuthAppDO;
		try {
			oAuthAppDO = getAppInformation(request.getOauth2AccessTokenReqDTO());
		} catch (InvalidOAuthClientException e) {
			throw new IdentityOAuth2Exception(
					"Error occured while retrieving application information", e); //$NON-NLS-1$
		}
		String applicationName = oAuthAppDO.getApplicationName();

		// Get authenticators used
		String amr = getValuesFromCache(request, "amr");

		// Retrieve or create an ACR app
		acrAppID = getACRAppID(applicationName);

		// Set ACR (PCR) to sub
		String subject = getACR(msisdn);

		// Get access token issued time
		long accessTokenIssuedTime = getAccessTokenIssuedTime(tokenRespDTO.getAccessToken());

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

		try {
			request.addProperty("accessToken", tokenRespDTO.getAccessToken());
			IDTokenBuilder builder = new IDTokenBuilder().setIssuer(issuer).setSubject(subject)
					.setAudience(request.getOauth2AccessTokenReqDTO().getClientId())
					.setAuthorizedParty(request.getOauth2AccessTokenReqDTO().getClientId())
					.setExpiration(curTime + lifetime).setIssuedAt(curTime)
					.setAuthTime(accessTokenIssuedTime).setAtHash(atHash)
					.setClaim("acr", acr).setClaim("amr", amr); //$NON-NLS-1$ //$NON-NLS-2$
			// setting up custom claims
			CustomClaimsCallbackHandler claimsCallBackHandler = OAuthServerConfiguration
					.getInstance().getOpenIDConnectCustomClaimsCallbackHandler();
			claimsCallBackHandler.handleCustomClaims(builder, request);
            String plainIDToken = builder.buildIDToken();
            return new PlainJWT((com.nimbusds.jwt.JWTClaimsSet) PlainJWT.parse(plainIDToken).getJWTClaimsSet()).serialize();
		} catch (IDTokenException e) {
			throw new IdentityOAuth2Exception("Error occured while generating the IDToken", e); //$NON-NLS-1$
        } catch (ParseException e) {
            log.error("Error while parsing the IDToken", e);
            throw new IdentityOAuth2Exception("Error while parsing the IDToken", e);
        }
	}

	private String getValuesFromCache(OAuthTokenReqMessageContext request, String key)
			throws IdentityOAuth2Exception {
        String keyValue = null;
		AuthorizationGrantCacheKey authorizationGrantCacheKey = new AuthorizationGrantCacheKey(
				request.getOauth2AccessTokenReqDTO().getAuthorizationCode());
		AuthorizationGrantCacheEntry authorizationGrantCacheEntry = (AuthorizationGrantCacheEntry) AuthorizationGrantCache
				.getInstance().getValueFromCache(authorizationGrantCacheKey);

		Iterator<ClaimMapping> userAttributes = authorizationGrantCacheEntry.getUserAttributes()
				.keySet().iterator();

		ClaimMapping acrKey = null;
		while (userAttributes.hasNext()) {
			ClaimMapping mapping = userAttributes.next();

          	if (mapping.getLocalClaim().getClaimUri().equals(key)) {

				acrKey = mapping;
                keyValue = authorizationGrantCacheEntry.getUserAttributes().get(acrKey);
                //remove values from the cache after usages
                authorizationGrantCacheEntry.getUserAttributes().remove(acrKey);
            }

		}


        if (null != acrKey) {
			return keyValue;
		} else {
			throw new IdentityOAuth2Exception("Error occured while retrieving " + key
					+ " from cache");
		}
	}

	/**
	 * @param tokenReqDTO
	 * @return
	 * @throws IdentityOAuth2Exception
	 * @throws InvalidOAuthClientException
	 */
	public OAuthAppDO getAppInformation(OAuth2AccessTokenReqDTO tokenReqDTO)
			throws IdentityOAuth2Exception, InvalidOAuthClientException {
		BaseCache<String, OAuthAppDO> appInfoCache = new BaseCache<String, OAuthAppDO>(
				"AppInfoCache"); //$NON-NLS-1$
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
			oAuthAppDO = new OAuthAppDAO().getAppInformation(tokenReqDTO.getClientId());
			appInfoCache.addToCache(tokenReqDTO.getClientId(), oAuthAppDO);
			return oAuthAppDO;
		}
	}

	/**
	 * A new ACR app is provisioned and returned.
	 * 
	 * @param applicationName
	 * @throws IdentityOAuth2Exception
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
			StringRequestEntity requestEntity = new StringRequestEntity(createRequest.toString(),
					"application/json", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
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

			return strACR; //$NON-NLS-1$;

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

	/**
	 * @param accessToken
	 * @return
	 * @throws IdentityOAuth2Exception
	 */
	public long getAccessTokenIssuedTime(String accessToken) throws IdentityOAuth2Exception {
		AccessTokenDO accessTokenDO = null;
		TokenMgtDAO tokenMgtDAO = new TokenMgtDAO();

		OAuthCache oauthCache = OAuthCache.getInstance();
		CacheKey cacheKey = new OAuthCacheKey(accessToken);
		CacheEntry result = oauthCache.getValueFromCache(cacheKey);

		// cache hit, do the type check.
		if (result instanceof AccessTokenDO) {
			accessTokenDO = (AccessTokenDO) result;
		}

		// Cache miss, load the access token info from the database.
		if (null == accessTokenDO) {
			accessTokenDO = tokenMgtDAO.retrieveAccessToken(accessToken, false);
		}

		// if the access token or client id is not valid
		if (null == accessTokenDO) {
			log.error("Error occured while getting access token based information"); //$NON-NLS-1$
			throw new IdentityOAuth2Exception(
					"Error occured while getting access token based information"); //$NON-NLS-1$
		}

		long timeIndMilliSeconds = accessTokenDO.getIssuedTime().getTime();

		return timeIndMilliSeconds / 1000;
	}

	/**
	 * @param acr
	 * @return
	 * @throws IdentityOAuth2Exception
	 */
	public String getAMRFromACR(String acr) throws IdentityOAuth2Exception {
		List<Authenticator> authenticatorsList = null;

		LOAConfig config = ConfigLoader.getInstance().getLoaConfig();
		if (null != acr && !"".equals(acr) && !" ".equals(acr)) { //$NON-NLS-1$ //$NON-NLS-2$
			authenticatorsList = config.getLOA(acr).getAuthentication().getAuthenticatorList()
					.get(0).getAuthenticators();
		}

		if (null != authenticatorsList) {
			List<String> authenticatorNames = new ArrayList<String>();
			for (Authenticator auth : authenticatorsList) {
				authenticatorNames.add(auth.getAuthenticatorName());
			}
			return StringUtils.join(authenticatorNames, ',');
		} else {
			log.error("Error occured while getting AMR from ACR"); //$NON-NLS-1$
			throw new IdentityOAuth2Exception("Error occured while getting AMR from ACR"); //$NON-NLS-1$
		}

	}

	/**
	 * This method uses the ACR API specified in properties
	 * 
	 * @param msisdn
	 * @return
	 * @throws IdentityOAuth2Exception
	 */
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
					throw new IdentityOAuth2Exception(
							"Retrieving/creating processes of ACR failed", e); //$NON-NLS-1$
				}
			}
		}

		return strACR;
	}

	/**
	 * If an ACR is already created for the particular MSISDN, it's returned
	 * 
	 * @return
	 * @throws IdentityOAuth2Exception
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
			StringRequestEntity requestEntity = new StringRequestEntity(retrieveRequest.toString(),
					"application/json", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
			PostMethod postMethod = new PostMethod(requestURL);
			postMethod
					.addRequestHeader(new Header("Authorization-ACR", "ServiceKey " + SERVICE_KEY)); //$NON-NLS-1$ //$NON-NLS-2$
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
	 * Create a new ACR
	 * 
	 * @param msisdn
	 * @return
	 * @throws IdentityOAuth2Exception
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
			StringRequestEntity requestEntity = new StringRequestEntity(createRequest.toString(),
					"application/json", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
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

}
