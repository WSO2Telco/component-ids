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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import com.wso2telco.gsma.authenticators.internal.CustomAuthenticatorServiceComponent;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
 
// TODO: Auto-generated Javadoc
/**
 * The Class MSISDNAuthenticator.
 */
public class MSISDNAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6817280268460894001L;
    
    /** The log. */
    private static Log log = LogFactory.getLog(MSISDNAuthenticator.class);

    /** The Constant LOGIN_HINT_ENCRYPTED_PREFIX. */
    private static final String LOGIN_HINT_ENCRYPTED_PREFIX = "ENCR_MSISDN:";
    
    /** The Constant LOGIN_HINT_NOENCRYPTED_PREFIX. */
    private static final String LOGIN_HINT_NOENCRYPTED_PREFIX = "MSISDN:";
    
    /** The Constant LOGIN_HINT_SEPARATOR. */
    private static final String LOGIN_HINT_SEPARATOR = "|";
    
    /** The Constant ENCRYPTION_ALGORITHM. */
    private static final String ENCRYPTION_ALGORITHM = "RSA";

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public boolean canHandle(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("MSISDN Authenticator canHandle invoked");
        }
        
        

         
        if ((request.getParameter("msisdn") != null) || (getLoginHintValues(request) != null) || ((request.getParameter("msisdn_header") != null) && (request.getParameter("msisdn_header") != ""))) {
            log.info("msisdn forwarding ");
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

        String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();

        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                context.getCallerSessionKey(),
                context.getContextIdentifier());

        try {

            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            }
            
            log.info("Query params: " + queryParams);
            
            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams))+ "&redirect_uri="+ request.getParameter("redirect_uri") + "&authenticators="
                    + getName() + ":" + "LOCAL" + retryParam);

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

    	String msisdn = request.getParameter("msisdn");
        //if((msisdn==null) & ((request.getParameter("msisdn_header") != null) && (request.getParameter("msisdn_header") != ""))){
        	//msisdn=context.getSubject();
        	//log.info("Reading header_msisdn from HeaderEnrichment and assigned to msisdn" +context.getSubject());
        	
        //}//COMMENTED  
     //   context.setProperty("redirectURI",request.getParameter("redirect_uri"));
        boolean isAuthenticated = false;

        // Check the authentication by checking if username exists
        try {

            if (msisdn == null) {
                String loginHint = getLoginHintValues(request); //request.getParameter("login_hint");

                if (loginHint != null) {
                    log.info("MSISDN by login hint: " + loginHint);
                    // String encryptappend = DataHolder.getInstance().getMobileConnectConfig().getEncryptAppend();
                    // encryptappend not used as per latest comment hence subsstring for 11 digits
                    if (loginHint.startsWith(LOGIN_HINT_ENCRYPTED_PREFIX)){
                        loginHint = loginHint.replace(LOGIN_HINT_ENCRYPTED_PREFIX, "");
                        String decrypted = decryptData(loginHint);
                        log.debug("Decrypted login hint: " + decrypted);
                        msisdn = decrypted.substring(0, decrypted.indexOf(LOGIN_HINT_SEPARATOR));
                        log.debug("MSISDN by encrypted login hint: " + msisdn);
                    } else if (loginHint.startsWith(LOGIN_HINT_NOENCRYPTED_PREFIX)){
                        msisdn = loginHint.replace(LOGIN_HINT_NOENCRYPTED_PREFIX, "");
                        log.debug("MSISDN by login hint: " + msisdn);
                    } else {
                        log.warn("No supported login hint format");
                }
                }
            } else {
            	log.info("MSISDN by request parameter or context parameter: " + msisdn);
            }
            context.setProperty("msisdn", msisdn);
            int tenantId = -1234;
            UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService()
                    .getTenantUserRealm(tenantId);

            if (userRealm != null) {
                UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();

                /*String userLocked = userStoreManager.getUserClaimValue(msisdn, "http://wso2.org/claims/identity/accountLocked", "default");
                if(userLocked != null && userLocked.equalsIgnoreCase("true")) {
                    log.info("MSISDN Authenticator authentication failed ");
                    if (log.isDebugEnabled()) {
                        log.debug("User authentication failed due to locked account.");
                    }
                    throw new AuthenticationFailedException("Authentication Failed");
                }*/

                isAuthenticated = userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(msisdn));
            } else {
                throw new AuthenticationFailedException("Cannot find the user realm for the given tenant: " + tenantId);
            }
        } catch (IdentityException e) {
            log.error("MSISDN Authentication failed while trying to get the tenant ID of the user", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("MSISDN Authentication failed while trying to authenticate", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("MSISDN Authentication failed while trying to obtain login hint value", e);
        }

        if (!isAuthenticated) {
            log.info("MSISDN Authenticator authentication failed ");
            context.setProperty("faileduser", msisdn);
            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to not existing user MSISDN.");
            }

            throw new AuthenticationFailedException("Authentication Failed");
        }
        log.info("MSISDN Authenticator authentication success for MSISDN - " + msisdn);
        
        context.setProperty("msisdn", msisdn);
        AuthenticationContextHelper.setSubject(context, msisdn);
        //context.setSubject(msisdn);
        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
    }

    /**
     * Decrypt data.
     *
     * @param data the data
     * @return the string
     * @throws Exception the exception
     */
    public String decryptData(String data) throws Exception{
        byte[] bytes = hexStringToByteArray(data);
            String filename = DataHolder.getInstance().getMobileConnectConfig().getKeyfile();
        PrivateKey key = getPrivateKey(filename);
        return decrypt(bytes, key);
        }

    /**
     * Decrypt.
     *
     * @param text the text
     * @param key the key
     * @return the string
     */
    public static String decrypt(byte[] text, PrivateKey key) {
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);

            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] dectyptedText = cipher.doFinal(text);
            
            return new String(dectyptedText);

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Exception encrypting data " + ex.getClass().getName() + ": "+ ex.getMessage());
        return null;
        }
    }

    /**
     * Gets the private key.
     *
     * @param filename the filename
     * @return the private key
     * @throws Exception the exception
     */
    public static PrivateKey getPrivateKey(String filename) throws Exception {

        try {

            String publicK = readStringKey(filename);
            //byte[] keyBytes = new BASE64Decoder().decodeBuffer(publicK);
            byte[] keyBytes = Base64.decodeBase64(publicK.getBytes());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory fact = KeyFactory.getInstance("RSA");

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(ENCRYPTION_ALGORITHM);
            return kf.generatePrivate(spec);

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Exception reading private key:" + ex.getMessage());
            return null;
    }

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
        } finally {
            if (reader != null) {
                reader = null;
            }
        }
        return fileData.toString();

    }



    /**
     * Hex string to byte array.
     *
     * @param s the s
     * @return the byte[]
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Validate msisdn.
     *
     * @param msisdn the msisdn
     * @return true, if successful
     */
    protected boolean validateMsisdn(String msisdn) {
        boolean isvalid = false;
        if (msisdn != null && ((msisdn.length() == 11 && msisdn.indexOf('+') < 0) || (msisdn.length() == 12 && msisdn.matches("[0-9]+")))) {
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
            SessionDataCacheEntry sdce =
                    (SessionDataCacheEntry) SessionDataCache.getInstance().getValueFromCache(sessionDataCacheKey);
            loginHintValues = sdce.getoAuth2Parameters().getLoginHint();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return loginHintValues;
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
        return Constants.MSISDN_AUTHENTICATOR_FRIENDLY_NAME;
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
        return Constants.MSISDN_AUTHENTICATOR_NAME;
    }
}
