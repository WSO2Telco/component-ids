package com.gsma.authenticators;

import com.gsma.authenticators.internal.CustomAuthenticatorServiceComponent;
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
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * This Authenticator authenticate users if MSISDN is whitelisted
 */
public class WhiteListMSISDNAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private static final long serialVersionUID = 2702390106128891592L;
    private static Log log = LogFactory.getLog(WhiteListMSISDNAuthenticator.class);
    private static final String LOGIN_HINT_ENCRYPTED_PREFIX = "ENCR_MSISDN:";
    private static final String LOGIN_HINT_NOENCRYPTED_PREFIX = "MSISDN:";
    private static final String LOGIN_HINT_SEPARATOR = "|";
    private static final String ENCRYPTION_ALGORITHM = "RSA";

    @Override
    public boolean canHandle(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("WhiteListMSISDNAuthenticator canHandle invoked");
        }

        if ((request.getParameter("msisdn") != null) || (getLoginHintValues(request) != null)) {
            return true;
        }
        return false;
    }

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

            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams)) + "&authenticators="
                    + getName() + ":" + "LOCAL" + retryParam);

        } catch (IOException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }


    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        String msisdn = request.getParameter("msisdn");

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
                log.info("MSISDN by request parameter: " + msisdn);
            }

            int tenantId = IdentityUtil.getTenantIdOFUser(msisdn);
            UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService()
                    .getTenantUserRealm(tenantId);

            if (userRealm != null) {
                UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();

                String userLocked = userStoreManager.getUserClaimValue(msisdn, "http://wso2.org/claims/identity/accountLocked", "default");
                if (userLocked != null && userLocked.equalsIgnoreCase("true")) {
                    log.info("WHITELIST MSISDN Authenticator authentication failed ");
                    if (log.isDebugEnabled()) {
                        log.debug("User authentication failed due to locked account.");
                    }
                    throw new AuthenticationFailedException("Authentication Failed");
                }

                isAuthenticated = userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(msisdn));
            } else {
                throw new AuthenticationFailedException("Cannot find the user realm for the given tenant: " + tenantId);
            }
        } catch (IdentityException e) {
            log.error("WHITELIST MSISDN Authentication failed while trying to get the tenant ID of the user", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("WHITELIST MSISDN Authentication failed while trying to authenticate", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (IOException e) {
            log.error("WHITELIST MSISDN Authentication failed while trying to obtain login hint value", e);
        }  catch (Exception e) {
            log.error("WHITELIST MSISDN Authentication failed while trying to obtain login hint value", e);
        }

        if (!isAuthenticated) {
            log.info("WHITELIST MSISDN Authenticator authentication failed ");
            context.setProperty("faileduser", msisdn);
            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to not existing user MSISDN.");
            }

            throw new AuthenticationFailedException("Authentication Failed");
        }

        if (!isWhiteListedNumber(msisdn)) {
            log.info("WHITELIST MSISDN Authenticator authentication failed ");
            context.setProperty("faileduser", msisdn);
            if (log.isDebugEnabled()) {
                log.debug("User authentication failed as user MSISDN is not whitelisted.");
            }

            throw new AuthenticationFailedException("Authentication Failed");
        }

        log.info("WHITELIST MSISDN Authenticator authentication success for MSISDN - " + msisdn);

        context.setProperty("msisdn", msisdn);
        context.setSubject(msisdn);
        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
    }


    private String decryptData(String data) throws Exception{
        byte[] bytes = hexStringToByteArray(data);
        String filename = DataHolder.getInstance().getMobileConnectConfig().getKeyfile();
        PrivateKey key = getPrivateKey(filename);
        return decrypt(bytes, key);
    }

    public static PrivateKey getPrivateKey(String filename) throws Exception {

        try {

            String publicK = readStringKey(filename);
            byte[] keyBytes = new BASE64Decoder().decodeBuffer(publicK);
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


    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private String getLoginHintValues(HttpServletRequest request) {
        String loginHintValues = null;

        try {
            String sdk = request.getParameter(OAuthConstants.SESSION_DATA_KEY);
            CacheKey ck = new SessionDataCacheKey(sdk);
            SessionDataCacheEntry sdce =
                    (SessionDataCacheEntry) SessionDataCache.getInstance().getValueFromCache(ck);
            loginHintValues = sdce.getoAuth2Parameters().getLoginHint();
        } catch (Exception e) {
        }

        return loginHintValues;
    }

    private boolean isWhiteListedNumber(String msisdn) {
        boolean isWhiteListed = false;
        try {
            String api_id = DBUtils.getWhiteListedNumbers(msisdn);
            if (api_id != null) {
                isWhiteListed = true;
            }
        } catch (AuthenticatorException e) {
            log.error(" Error occurred while checking MSISDN : " + msisdn + " is whitelisted.");
            e.printStackTrace();
        }
        return isWhiteListed;
    }


    @Override
    protected boolean retryAuthenticationEnabled() {
        // Setting retry to true as we need the correct MSISDN to continue
        return true;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        return request.getParameter("sessionDataKey");
    }

    @Override
    public String getFriendlyName() {
        return Constants.MSISDN_AUTHENTICATOR_FRIENDLY_NAME;
    }

    private String getPrivateKeyFile() {
        return Constants.PRIVATE_KEYFILE;
    }

    @Override
    public String getName() {
        return Constants.WHITELIST_MSISDN_AUTHENTICATOR_NAME;
    }
}
