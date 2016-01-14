package com.gsma.authenticators;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SelfAuthenticator after success registration
 */
public class SelfAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {
    private static final long serialVersionUID = 6817280268460894001L;
    private static Log log = LogFactory.getLog(MSSAuthenticator.class);

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
    protected void processAuthenticationResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationContext context) throws AuthenticationFailedException {
        log.info("Self Authenticator authentication Start ");
        String msisdn = (String) context.getProperty("msisdn");
        context.setProperty("msisdn", msisdn);
        context.setSubject(msisdn);
        String rememberMe = httpServletRequest.getParameter("chkRemember");
        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
    }

    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {
        if (log.isDebugEnabled()) {
            log.debug("Self Authenticator canHandle invoked");
        }
        return true;

    }

    @Override
    public String getContextIdentifier(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getParameter("sessionDataKey");
    }

    @Override
    public String getName() {
        return Constants.SELF_AUTHENTICATOR_NAME;
    }

    @Override
    public String getFriendlyName() {
        return Constants.SELF_AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    protected boolean retryAuthenticationEnabled() {
        // Setting retry to true as we need the correct continue
        return false;
    }


}