package com.gsma.authenticators.sms;

import com.gsma.authendictorselector.DialogAuthenticatorSelectorImpl;
import com.gsma.authenticators.AuthenticatorException;
import com.gsma.authenticators.Constants;
import com.gsma.authenticators.DBUtils;
import com.gsma.authenticators.FindOperator;
import com.gsma.authenticators.cryptosystem.AESencrp;
import com.gsma.shorten.SelectShortUrl;
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
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Map;

/**
 * SMS Authenticator for sending SMS and waiting on user response
 */
public class SMSAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private static final long serialVersionUID = -1189332409518227376L;
    private static Log log = LogFactory.getLog(SMSAuthenticator.class);

    @Override
    public boolean canHandle(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            log.debug("SMS Authenticator canHandle invoked");
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
                //Insert Encription method hear
                DBUtils.insertUserResponse(context.getContextIdentifier(), String.valueOf(UserResponse.PENDING));
            }

            //MSISDN will be saved in the context in the MSISDNAuthenticator
            String msisdn = (String) context.getProperty("msisdn");

            FindOperator findOperator = new FindOperator();
            String operatorName = findOperator.getOperatorName(msisdn);
            if(operatorName.equalsIgnoreCase("dialog")){
                com.gsma.utils.ReadMobileConnectConfig readMobileConnectConfig = new com.gsma.utils.ReadMobileConnectConfig();
                Map<String, String> readMobileConnectConfigResult;
                readMobileConnectConfigResult = readMobileConnectConfig.query("dialog/SMS");

                String messageText = readMobileConnectConfigResult.get("MessageContent") + " " + context.getSequenceConfig()
                        .getApplicationConfig().getApplicationName();

                String listenerWebappHost = readMobileConnectConfigResult.get("ListenerWebappHost");
                if (listenerWebappHost == null || listenerWebappHost.isEmpty()) {
                    listenerWebappHost = "http://" + System.getProperty("carbon.local.ip") + ":9764";
                }
                String messageURL ="";
                try {
                    messageURL = listenerWebappHost + Constants.LISTNER_WEBAPP_SMS_CONTEXT +
                            response.encodeURL(AESencrp.encrypt(context.getContextIdentifier()));
                    if(readMobileConnectConfigResult.get("IsShortUrl").equalsIgnoreCase("true")){
                    SelectShortUrl selectShortUrl=new SelectShortUrl();
                    messageURL=selectShortUrl.getShortUrl(readMobileConnectConfigResult.get("ShortUrlClass"),messageURL,readMobileConnectConfigResult.get("AccessToken"),readMobileConnectConfigResult.get("ShortUrlService"));
                    }

                }catch(Exception e){
                    throw new AuthenticationFailedException(e.getMessage(), e);
                }
//            String smsResponse = new SendSMS().sendSMS(msisdn, messageText + "\n" + messageURL);
                DialogAuthenticatorSelectorImpl  dialogAuthenticatorSelector = new DialogAuthenticatorSelectorImpl();
                String smsResponse = dialogAuthenticatorSelector.getSMS(msisdn, messageText + "\n" + messageURL);
            }
//            AuthenticatorSelectorFactory authenticatorSelectorFactory = new AuthenticatorSelectorFactory();
//            AuthenticatorSelector authenticatorSelector = authenticatorSelectorFactory.Authenticator("dialog");

//            String smsResponse = new SendSMS().sendSMS(msisdn, messageText + "\n" + messageURL);

            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams)) + "&authenticators=" +
                    getName() + ":" + "LOCAL" + retryParam);

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
            String responseStatus = DBUtils.getUserResponse(sessionDataKey);

            if (responseStatus.equalsIgnoreCase(UserResponse.APPROVED.toString())) {
                isAuthenticated = true;
            }

        } catch (AuthenticatorException e) {
            log.error("SMS Authentication failed while trying to authenticate", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }

        if (!isAuthenticated) {
            log.info("SMS Authenticator authentication failed ");
            context.setProperty("faileduser", (String) context.getProperty("msisdn"));
            
            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to user not providing consent.");
            }

            throw new AuthenticationFailedException("Authentication Failed");
        }
      
        
        String msisdn = (String) context.getProperty("msisdn");
        context.setSubject(msisdn);
        
        log.info("SMS Authenticator authentication success");

//        context.setSubject(msisdn);
        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
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
        return Constants.SMS_AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {
        return Constants.SMS_AUTHENTICATOR_NAME;
    }

    private enum UserResponse {
        PENDING,
        APPROVED,
        REJECTED
    }

}
