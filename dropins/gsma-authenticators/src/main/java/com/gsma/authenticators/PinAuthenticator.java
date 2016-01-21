package com.gsma.authenticators;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

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

/**
 * Pin based Authenticator
 * 
 */
public class PinAuthenticator extends AbstractApplicationAuthenticator
		implements LocalApplicationAuthenticator {

	private static final long serialVersionUID = 4438354156955223778L;
    private static SecureRandom random = new SecureRandom();
    private static Log log = LogFactory.getLog(PinAuthenticator.class);

	@Override
	public boolean canHandle(HttpServletRequest request) {
        log.info("Pin Authenticator canhandle invoked");
        
        String pin = request.getParameter("pin");
        if(pin != null) {
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

        String pinNo = generatePassword();
        log.info("=========== Generated PIN# = " + pinNo + " ===========");
        if (log.isDebugEnabled()) {
            log.debug("=========== Generated PIN# = " + pinNo + " ===========");
        }

        context.setProperty("pin", pinNo);
        try {


            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            }

            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams)) + "&authenticators=" + getName() + ":" + "LOCAL" + retryParam);

        } catch (IOException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
	}

	@Override
	protected void processAuthenticationResponse(HttpServletRequest request,
			HttpServletResponse response, AuthenticationContext context)
			throws AuthenticationFailedException {

        String pin = request.getParameter("pin");

        boolean isAuthenticated = false;

        // Check the authentication
        isAuthenticated = context.getProperty("pin").equals(pin);

        if (!isAuthenticated) {
            log.info("Pin Authenticator authentication failed ");
            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to invalid pin.");
            }

            throw new AuthenticationFailedException("Authentication Failed");
        }
        log.info("Pin Authenticator authentication success");
        //context.setSubject((String)context.getProperty("BasicAuthSubject"));
        context.setSubject(pin);

        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
	}
	
	@Override
	protected boolean retryAuthenticationEnabled() {
		// Deliberately set false
        return false;
	}
	
	@Override
	public String getContextIdentifier(HttpServletRequest request) {
		return request.getParameter("sessionDataKey");
	}

	@Override
	public String getFriendlyName() {
		return Constants.PIN_AUTHENTICATOR_FRIENDLY_NAME;
	}

	@Override
	public String getName() {
		return Constants.PIN_AUTHENTICATOR_NAME;
	}

    /**
     * Generates (random) password for user to be provisioned
     *
     * @return
     */
    protected String generatePassword() {
        return new BigInteger(130, random).toString(32);
    }
}
