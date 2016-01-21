package com.gsma.authenticators.android;

import com.gsma.authenticators.AuthenticatorException;
import com.gsma.authenticators.Constants;
import com.gsma.authenticators.DBUtils;
import com.gsma.authenticators.DataHolder;
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
import org.wso2.carbon.identity.core.model.OAuthAppDO;
import org.wso2.carbon.identity.oauth.cache.BaseCache;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.wso2.carbon.identity.core.dao.OAuthAppDAO;

/**
 * USSD Authenticator for waiting on USSD response
 */
public class AndroidAuthenticatorOK extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private boolean initiated = false;

    private static final long serialVersionUID = 7785133722588291677L;
    private static Log log = LogFactory.getLog(AndroidAuthenticatorOK.class);
    private static final String PIN_CLAIM = "http://wso2.org/claims/pin";

    private static String LOA = "loa2";
    
    @Override
    public boolean canHandle(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("Android Authenticator canHandle invoked");
        }

//        if (request.getParameter("msisdn") != null) {
//            return true;
//        }


//        if (initiated) {
//            return true;
//        } else {
//            log.info("Android Initiated");
//            initiated = true;
//            return false;
//
//        }
        return true;
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

        log.info("initiateAuthenticationRequest : Android");
        String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                        context.getCallerSessionKey(),
                        context.getContextIdentifier());

        try {
            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            } else {
                // Insert entry to DB only if this is not a retry
                log.info("Android Inserting session : " + context.getContextIdentifier());
                DBUtils.insertUserResponse(context.getContextIdentifier(), String.valueOf(UserResponse.AUTHENTICATION_WAITING));
            }

            //MSISDN will be saved in the context in the MSISDNAuthenticator
            String msisdn = (String) context.getProperty("msisdn");
            log.info("Android : MSISDN " + msisdn);

            //String pinEnabled = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getPinauth();
            String androidResponse = null;


            /*   try {
             log.info("Service provider Name: " + USSDAuthenticator.getAppInformation(context.getRelyingParty()).getApplicationName());
             } catch (IdentityOAuth2Exception ex) {
             Logger.getLogger(USSDAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
             } catch (InvalidOAuthClientException ex) {
             Logger.getLogger(USSDAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
             }*/


            //Changing SP dashboard Name
            String serviceProviderName = null;

            try {
                serviceProviderName = AndroidAuthenticatorOK.getAppInformation(context.getRelyingParty()).getApplicationName();
            } catch (IdentityOAuth2Exception ex) {
                Logger.getLogger(AndroidAuthenticatorOK.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidOAuthClientException ex) {
                Logger.getLogger(AndroidAuthenticatorOK.class.getName()).log(Level.SEVERE, null, ex);
            }


            log.info("Service Provider Name = " + serviceProviderName);
            if (serviceProviderName.equals("wso2_sp_dashboard")) {
                serviceProviderName = DataHolder.getInstance().getMobileConnectConfig().getAndroidConfig().getDashBoard();

            }

            androidResponse = new SendAndroid().sendAndroid(msisdn, context.getContextIdentifier(), serviceProviderName,LOA);

//          if(androidResponse.compareToIgnoreCase())


            log.info("Redirecting " + response.encodeRedirectURL(loginPage + ("?" + queryParams)) + "&authenticators="
                    + getName() + ":" + "LOCAL" + retryParam);
            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams)) + "&authenticators="
                    + getName() + ":" + "LOCAL" + retryParam);

        } catch (IOException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (AuthenticatorException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        String sessionDataKey = request.getParameter("sessionDataKey");

        log.info("Android processAuthenticationResponse SessionDataKey " + sessionDataKey.toString());
        boolean isAuthenticated = false;

        // Check if the user has provided consent
        try {

            //String pinEnabled = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getPinauth();

            String responseStatus = DBUtils.getUserResponse(sessionDataKey);
            log.info("Android processAuthenticationResponse responseStatus " + responseStatus == null ? "null" : responseStatus);

            if (responseStatus != null && responseStatus.equalsIgnoreCase(UserResponse.AUTHENTICATED.toString())) {
                isAuthenticated = true;
            }

        } catch (AuthenticatorException e) {
            log.error("Android Authentication failed while trying to authenticate", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }

        if (!isAuthenticated) {
            log.info("Android Authenticator authentication failed ");
            context.setProperty("faileduser", (String) context.getProperty("msisdn"));

            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to user not providing consent.");
            }

            throw new AuthenticationFailedException("Authentication Failed");
        }


        String msisdn = (String) context.getProperty("msisdn");
        context.setSubject(msisdn);

        log.info("Android Authenticator authentication success");

//        context.setSubject(msisdn);
        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
    }

    /**
     * @param clientID
     * @return
     * @throws org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception
     * @throws org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException
     */
    private static OAuthAppDO getAppInformation(String clientID)
            throws IdentityOAuth2Exception, InvalidOAuthClientException {
        BaseCache<String, OAuthAppDO> appInfoCache = new BaseCache<String, OAuthAppDO>(
                "AppInfoCache"); //$NON-NLS-1$
        if (null != appInfoCache) {
            if (log.isDebugEnabled()) {
                log.debug("Successfully created AppInfoCache under " //$NON-NLS-1$
                        + OAuthConstants.OAUTH_CACHE_MANAGER);
            }
        }

        OAuthAppDO oAuthAppDO = appInfoCache.getValueFromCache(clientID);
        if (oAuthAppDO != null) {
            return oAuthAppDO;
        } else {
            oAuthAppDO = new OAuthAppDAO().getAppInformation(clientID);
            appInfoCache.addToCache(clientID, oAuthAppDO);
            return oAuthAppDO;
        }
    }

    @Override
    protected boolean retryAuthenticationEnabled() {
        return false;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        return request.getParameter("sessionDataKey");
    }

    @Override
    public String getFriendlyName() {
        return Constants.ANDROID_AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {
        return Constants.ANDROID_AUTHENTICATOR_OK_NAME;
    }

    private enum UserResponse {

        AUTHENTICATED,
        AUTHENTICATION_FAILED,
        AUTHENTICATION_WAITING,
        INVALID_USER,
        USER_BLOCKED
    }
}
