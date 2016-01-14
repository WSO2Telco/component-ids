package com.gsma.authenticators.ussd;

import com.gsma.authendictorselector.DialogAuthenticatorSelectorImpl;
import com.gsma.authenticators.*;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wso2.carbon.identity.base.IdentityException;
//import org.wso2.carbon.identity.core.dao.OAuthAppDAO;
import org.wso2.carbon.identity.core.model.OAuthAppDO;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.BaseCache;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.SAXException;

/**
 * USSD Authenticator for waiting on USSD response
 */
public class USSDAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private static final long serialVersionUID = 7785133722588291677L;
    private static Log log = LogFactory.getLog(USSDAuthenticator.class);
    private static final String PIN_CLAIM = "http://wso2.org/claims/pin";

    @Override
    public boolean canHandle(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("USSD Authenticator canHandle invoked");
        }

//        if (request.getParameter("msisdn") != null) {
//            return true;
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
                DBUtils.insertUserResponse(context.getContextIdentifier(), String.valueOf(UserResponse.PENDING));
            }

            //MSISDN will be saved in the context in the MSISDNAuthenticator
            String msisdn = (String) context.getProperty("msisdn");

            //String pinEnabled = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getPinauth();
            String ussdResponse = null;


            /*   try {
             log.info("Service provider Name: " + USSDAuthenticator.getAppInformation(context.getRelyingParty()).getApplicationName());
             } catch (IdentityOAuth2Exception ex) {
             Logger.getLogger(USSDAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
             } catch (InvalidOAuthClientException ex) {
             Logger.getLogger(USSDAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
             }*/


            //Changing SP dashboard Name
            String serviceProviderName = null;

            serviceProviderName = context.getSequenceConfig().getApplicationConfig().getApplicationName();


            log.info("Service Provider Name = " + serviceProviderName);
            if (serviceProviderName.equals("wso2_sp_dashboard")) {
                com.gsma.utils.ReadMobileConnectConfig readMobileConnectConfig = new com.gsma.utils.ReadMobileConnectConfig();
                Map<String, String> readMobileConnectConfigResult;
                readMobileConnectConfigResult = readMobileConnectConfig.query("dialog/USSD");

                serviceProviderName = readMobileConnectConfigResult.get("DashBoard");
//                serviceProviderName = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getDashBoard();
            }

            FindOperator findOperator = new FindOperator();
            String operatorName = findOperator.getOperatorName(msisdn);
            if(operatorName.equalsIgnoreCase("dialog")){
                DialogAuthenticatorSelectorImpl dialogAuthenticatorSelector = new DialogAuthenticatorSelectorImpl();
                ussdResponse = dialogAuthenticatorSelector.getUSSD(msisdn, context.getContextIdentifier(), serviceProviderName);
            }
//            ussdResponse = new SendUSSD().sendUSSD(msisdn, context.getContextIdentifier(), serviceProviderName);
            
            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams)) + "&authenticators="
                    + getName() + ":" + "LOCAL" + retryParam);

        } catch (IOException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (AuthenticatorException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
            HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        String sessionDataKey = request.getParameter("sessionDataKey");

        boolean isAuthenticated = false;

        // Check if the user has provided consent
        try {

            //String pinEnabled = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getPinauth();

            String responseStatus = DBUtils.getUserResponse(sessionDataKey);

            if (responseStatus.equalsIgnoreCase(UserResponse.APPROVED.toString())) {
                isAuthenticated = true;
            }

        } catch (AuthenticatorException e) {
            log.error("USSD Authentication failed while trying to authenticate", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }

        if (!isAuthenticated) {
            log.info("USSD Authenticator authentication failed ");
            context.setProperty("faileduser", (String) context.getProperty("msisdn"));
            
            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to user not providing consent.");
            }

            throw new AuthenticationFailedException("Authentication Failed");
        }
        

        String msisdn = (String) context.getProperty("msisdn");
        context.setSubject(msisdn);
        
        log.info("USSD Authenticator authentication success");

//        context.setSubject(msisdn);
        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
    }

    /**
     * @param clientID
     * @return
     * @throws IdentityOAuth2Exception
     * @throws InvalidOAuthClientException
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
        return Constants.USSD_AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {
        return Constants.USSD_AUTHENTICATOR_NAME;
    }

    private enum UserResponse {

        PENDING,
        APPROVED,
        REJECTED
    }
}
