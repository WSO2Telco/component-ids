package com.gsma.authenticators.mepin;

import com.google.gson.JsonObject;
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
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class MePinAuthenticatorSWIPE extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private static final long serialVersionUID = -8570406196896249057L;
    private static Log log = LogFactory.getLog(MePinAuthenticatorSWIPE.class);
    private static String LOA = "swipe";


    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {
        if (log.isDebugEnabled()) {
            log.debug("MePIN Authenticator canHandle invoked");
        }
        return true;
    }

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
                                           AuthenticationContext context) throws AuthenticationFailedException,
            LogoutFailedException {

        if (context.isLogoutRequest()) {
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        } else {
            return super.process(request, response, context);
        }
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {
        String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(), context
                .getCallerSessionKey(), context.getContextIdentifier());

        try {
            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            } else {
                // Insert entry to DB only if this is not a retry
                DBUtils.insertUserResponse(context.getContextIdentifier(), String.valueOf(MePinAuthenticatorSWIPE
                        .UserResponse.PENDING));
            }

            //MSISDN will be saved in the context in the MSISDNAuthenticator
            String msisdn = (String) context.getProperty("msisdn");
            String mePinId = DBUtils.getMePinId(msisdn);

            String serviceProviderName = context.getSequenceConfig().getApplicationConfig().getApplicationName();
            log.debug("Service Provider Name = " + serviceProviderName);
            if (serviceProviderName.equals("wso2_sp_dashboard")) {
                serviceProviderName = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getDashBoard();
            }

            JsonObject transactionRes = new MePinQuery().createTransaction(mePinId, context.getContextIdentifier(),
                    serviceProviderName, MePinAuthenticatorSWIPE.LOA);
            String transaction_id = transactionRes.getAsJsonPrimitive("transaction_id").getAsString();
            String status = transactionRes.getAsJsonPrimitive("status").getAsString();

            if (!status.equalsIgnoreCase("ok")) {
                String statusText = transactionRes.getAsJsonPrimitive("status_text").getAsString();
                throw new AuthenticationFailedException("Error in MePIN transaction creation: " + statusText);
            }
            DBUtils.insertMePinTransaction(context.getContextIdentifier(), transaction_id, mePinId);

            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams))
                    + "&authenticators=" + getName() + ":" + "LOCAL" + retryParam);

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
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse
            response, AuthenticationContext context) throws AuthenticationFailedException {

        String sessionDataKey = request.getParameter("sessionDataKey");
        boolean isAuthenticated = false;

        // Check if the user has provided consent
        try {
            String responseStatus = DBUtils.getUserResponse(sessionDataKey);

            if (responseStatus.equalsIgnoreCase(UserResponse.APPROVED.toString())) {
                isAuthenticated = true;
            }

        } catch (AuthenticatorException e) {
            log.error("MePIN Authentication failed while trying to authenticate", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }

        if (!isAuthenticated) {
            log.info("MePIN Authenticator authentication failed ");
            context.setProperty("faileduser", context.getProperty("msisdn"));

            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to user not providing consent.");
            }
            throw new AuthenticationFailedException("Authentication Failed");
        }

        String msisdn = (String) context.getProperty("msisdn");
        context.setSubject(msisdn);

        log.info("MePIN Authenticator authentication success");

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
    public String getName() {
        return Constants.MEPIN_AUTHENTICATOR_SWIPE_NAME;
    }

    @Override
    public String getFriendlyName() {
        return Constants.MEPIN_AUTHENTICATOR_FRIENDLY_NAME;
    }

    private enum UserResponse {
        PENDING,
        APPROVED,
        REJECTED
    }
}
