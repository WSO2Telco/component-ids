package com.gsma.authenticators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import sun.misc.BASE64Decoder;
/**
 * GSMA MSISDN based Authenticator
 */
public class GSMAMSISDNAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private static final long serialVersionUID = 6817280268460894001L;
    private static Log log = LogFactory.getLog(GSMAMSISDNAuthenticator.class);

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
                    //String encryptappend = DataHolder.getInstance().getMobileConnectConfig().getEncryptAppend();
                    //encryptappend not used as per latest comment hence subsstring for 11 digits
                	// Replace space by + as this gets dropped.
                	loginHint = loginHint.replace(" ", "+");
            		// System.out.println("encryptedLoginHint_MSISDN: " + loginHint);
                    msisdn = (decryptData(loginHint)).split("\\|")[0]; //.replace(encryptappend, "");
                }
            }

            // Accept any MSISDN and consider as authenticated.
            // Intended functionality for GSMA hub
            isAuthenticated = true;
        } catch (IOException e) {
            log.error("GSMA MSISDN Authentication failed while trying to obtain login hint value", e);
        }

        if (!isAuthenticated) {
            log.info("GSMA MSISDN Authenticator authentication failed ");
            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to not existing user MSISDN.");
            }

            throw new AuthenticationFailedException("Authentication Failed");
        }
        log.info("GSMA MSISDN Authenticator authentication success for MSISDN - " + msisdn);

        context.setProperty("msisdn", msisdn);
        context.setSubject(msisdn);
        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
    }

    private String decryptData(String strData) throws IOException {

        byte[] data = new BASE64Decoder().decodeBuffer(strData);
        byte[] descryptedData = null;

        try {
            String filename = DataHolder.getInstance().getMobileConnectConfig().getKeyfile();
            PrivateKey privateKey = readPrivateKeyFromFile(filename);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            descryptedData = cipher.doFinal(data);
            return new String(descryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("login_hint decryption completed");
        return null;

    }

    /**
     * read Private Key From File
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public PrivateKey readPrivateKeyFromFile(String fileName) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        String publicK = readStringKey(fileName);
        byte[] keyBytes = new BASE64Decoder().decodeBuffer(publicK);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        return fact.generatePrivate(keySpec);
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

    protected boolean validateMsisdn(String msisdn) {
        boolean isvalid = false;
        if (msisdn != null && ((msisdn.length() == 11 && msisdn.indexOf('+') < 0) || (msisdn.length() == 12 && msisdn.matches("[0-9]+")))) {
            isvalid = true;
        }
        return isvalid;
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
        return Constants.GSMA_MSISDN_AUTHENTICATOR_FRIENDLY_NAME;
    }

    private String getPrivateKeyFile() {
        return Constants.PRIVATE_KEYFILE;
    }

    @Override
    public String getName() {
        return Constants.GSMA_MSISDN_AUTHENTICATOR_NAME;
    }
}
