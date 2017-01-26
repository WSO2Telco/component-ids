/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
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
package com.wso2telco.gsma.authenticators;

import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.LinkedHashSet;

;
 
// TODO: Auto-generated Javadoc
/**
 * The Class GSMAMSISDNAuthenticator.
 */
public class GSMAMSISDNAuthenticator extends AbstractApplicationAuthenticator implements
		LocalApplicationAuthenticator {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6817280268460894001L;
	
	/** The log. */
	private static Log log = LogFactory.getLog(GSMAMSISDNAuthenticator.class);

	/** The Configuration service */
	private static ConfigurationService configurationService = new ConfigurationServiceImpl();

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public boolean canHandle(HttpServletRequest request) {
		if (log.isDebugEnabled()) {
			log.debug("GSMA MSISDN Authenticator canHandle invoked");
		}

		if ((request.getParameter("msisdn") != null) || (getLoginHintValues(request) != null)) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
	 */
	@Override
	public AuthenticatorFlowStatus process(HttpServletRequest request,
			HttpServletResponse response, AuthenticationContext context)
			throws AuthenticationFailedException, LogoutFailedException {

		if (context.isLogoutRequest()) {
			return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
		} else {
			return super.process(request, response, context);
		}
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#initiateAuthenticationRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
	 */
	@Override
	protected void initiateAuthenticationRequest(HttpServletRequest request,
			HttpServletResponse response, AuthenticationContext context)
			throws AuthenticationFailedException {
        log.info("Initiating authentication request");

        // Retrieve entry LOA and set to authentication context
		LinkedHashSet<?> acrs = getACRValues(request);
		String selectedLOA = (String) acrs.iterator().next();
		context.setProperty("entryLOA", selectedLOA);

		String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();

		String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(
				context.getQueryParams(), context.getCallerSessionKey(),
				context.getContextIdentifier());

        if(log.isDebugEnabled()) {
            log.debug("Query parameters : " + queryParams);
        }

		try {

			String retryParam = "";

			if (context.isRetrying()) {
				retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
			}

			response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams))
					+ "&authenticators=" + getName() + ":" + "LOCAL" + retryParam);

		} catch (IOException e) {
			throw new AuthenticationFailedException(e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#processAuthenticationResponse(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
	 */
	@Override
	protected void processAuthenticationResponse(HttpServletRequest request,
			HttpServletResponse response, AuthenticationContext context)
			throws AuthenticationFailedException {
        log.info("Processing authentication response");

        String msisdn = request.getParameter("msisdn");

        if(log.isDebugEnabled()) {
            log.debug("MSISDN : " + msisdn);
        }

		boolean isAuthenticated = false;

		// Check the authentication by checking if username exists
		try {

			if (msisdn == null) {
				String loginHint = getLoginHintValues(request); // request.getParameter("login_hint");

				if (loginHint != null) {
					// String encryptappend =
					// DataHolder.getInstance().getMobileConnectConfig().getEncryptAppend();
					// encryptappend not used as per latest comment hence
					// subsstring for 11 digits
                	loginHint = loginHint.replace(" ", "+");
					msisdn = (decryptData(loginHint)).split("\\|")[0]; // .replace(encryptappend,
																		// "");
				}
			}

			// Accept any MSISDN and consider as authenticated.
			// Intended functionality for GSMA hub
			isAuthenticated = true;
		} catch (IOException e) {
			log.error("GSMA MSISDN Authentication failed while trying to obtain login hint value",
					e);
		}

		if (!isAuthenticated) {
			log.info("Authentication failed. MSISDN doesn't exist.");
			throw new AuthenticationFailedException("Authentication Failed");
		}
		log.info("Authentication success");

		context.setProperty("msisdn", msisdn);
		//context.setSubject(msisdn);
		/*AuthenticatedUser user=new AuthenticatedUser();
		context.setSubject(user);*/
        AuthenticationContextHelper.setSubject(context,msisdn);
		String rememberMe = request.getParameter("chkRemember");

		if (rememberMe != null && "on".equals(rememberMe)) {
			context.setRememberMe(true);
		}
	}

	/**
	 * Decrypt data.
	 *
	 * @param strData the str data
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String decryptData(String strData) throws IOException {

		//byte[] data = new BASE64Decoder().decodeBuffer(strData);
		byte[] data = Base64.decodeBase64(strData.getBytes());
		byte[] descryptedData = null;

		try {
            String filename = configurationService.getDataHolder().getMobileConnectConfig().getKeyfile();
            PrivateKey privateKey = readPrivateKeyFromFile(filename);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			descryptedData = cipher.doFinal(data);
			return new String(descryptedData);
		} catch (Exception e) {
			log.error("Error" + e );
		}

		log.info("login_hint decryption completed");
		return null;

	}

	 
	/**
	 * Read private key from file.
	 *
	 * @param fileName the file name
	 * @return the private key
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws InvalidKeySpecException the invalid key spec exception
	 */
	public PrivateKey readPrivateKeyFromFile(String fileName) throws IOException,
			NoSuchAlgorithmException, InvalidKeySpecException {

		String publicK = readStringKey(fileName);
		//byte[] keyBytes = new BASE64Decoder().decodeBuffer(publicK);
		byte[] keyBytes = Base64.decodeBase64(publicK.getBytes());
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory fact = KeyFactory.getInstance("RSA");
		return fact.generatePrivate(keySpec);
	}

	/**
	 * Read string key.
	 *
	 * @param fileName the file name
	 * @return the string
	 */
	public static String readStringKey(String fileName) {

		BufferedReader reader = null;
		StringBuffer fileData = null;
		try {

			fileData = new StringBuffer(2048);
			reader = new BufferedReader(new FileReader(fileName));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}

			reader.close();

		} catch (Exception e) {
			log.error("Error " + e);
		} finally {
			if (reader != null) {
				reader = null;
			}
		}
		return fileData.toString();

	}

	/**
	 * Validate msisdn.
	 *
	 * @param msisdn the msisdn
	 * @return true, if successful
	 */
	protected boolean validateMsisdn(String msisdn) {
		boolean isvalid = false;
		if (msisdn != null
				&& ((msisdn.length() == 11 && msisdn.indexOf('+') < 0) || (msisdn.length() == 12 && msisdn
						.matches("[0-9]+")))) {
			isvalid = true;
		}
		return isvalid;
	}

	/**
	 * Gets the login hint values.
	 *
	 * @param request the request
	 * @return the login hint values
	 */
	private String getLoginHintValues(HttpServletRequest request) {
		String loginHintValues = null;

		try {
			String sdk = request.getParameter(OAuthConstants.SESSION_DATA_KEY);
			CacheKey ck = new SessionDataCacheKey(sdk);
			SessionDataCacheKey sessionDataCacheKey=new SessionDataCacheKey(sdk);
			SessionDataCacheEntry sdce = (SessionDataCacheEntry) SessionDataCache.getInstance()
					.getValueFromCache(sessionDataCacheKey);
			loginHintValues = sdce.getoAuth2Parameters().getLoginHint();
		} catch (Exception e) {
			log.error("Error while getting login_hint values " + e);
		}

		return loginHintValues;
	}

	/**
	 * Gets the ACR values.
	 *
	 * @param request the request
	 * @return the ACR values
	 */
	private LinkedHashSet<?> getACRValues(HttpServletRequest request) {
		String sdk = request.getParameter(OAuthConstants.SESSION_DATA_KEY);
		CacheKey ck = new SessionDataCacheKey(sdk);
		SessionDataCacheKey sessionDataCacheKey=new SessionDataCacheKey(sdk);
		SessionDataCacheEntry sdce = (SessionDataCacheEntry) SessionDataCache.getInstance()
				.getValueFromCache(sessionDataCacheKey);
		LinkedHashSet<?> acrValues = sdce.getoAuth2Parameters().getACRValues();
		return acrValues;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#retryAuthenticationEnabled()
	 */
	@Override
	protected boolean retryAuthenticationEnabled() {
		// Setting retry to true as we need the correct MSISDN to continue
		return true;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getContextIdentifier(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public String getContextIdentifier(HttpServletRequest request) {
		return request.getParameter("sessionDataKey");
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getFriendlyName()
	 */
	@Override
	public String getFriendlyName() {
		return Constants.GSMA_MSISDN_AUTHENTICATOR_FRIENDLY_NAME;
	}

	/**
	 * Gets the private key file.
	 *
	 * @return the private key file
	 */
	private String getPrivateKeyFile() {
		return Constants.PRIVATE_KEYFILE;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
	 */
	@Override
	public String getName() {
		return Constants.GSMA_MSISDN_AUTHENTICATOR_NAME;
	}
}
