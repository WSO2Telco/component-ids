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

import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
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
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
 
// TODO: Auto-generated Javadoc
/**
 * The Class PinAuthenticator.
 */
public class PinAuthenticator extends AbstractApplicationAuthenticator
		implements LocalApplicationAuthenticator {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4438354156955223778L;
    
    /** The random. */
    private static SecureRandom random = new SecureRandom();
    
    /** The log. */
    private static Log log = LogFactory.getLog(PinAuthenticator.class);

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public boolean canHandle(HttpServletRequest request) {
        log.info("Pin Authenticator canhandle invoked");
        
        String pin = request.getParameter("pin");
        if(pin != null) {
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

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#processAuthenticationResponse(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
	 */
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
//        AuthenticatedUser user=new AuthenticatedUser();
//        context.setSubject(user);

        AuthenticationContextHelper.setSubject(context, pin);

        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }
	}
	
	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#retryAuthenticationEnabled()
	 */
	@Override
	protected boolean retryAuthenticationEnabled() {
		// Deliberately set false
        return false;
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
		return Constants.PIN_AUTHENTICATOR_FRIENDLY_NAME;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
	 */
	@Override
	public String getName() {
		return Constants.PIN_AUTHENTICATOR_NAME;
	}

     
    /**
     * Generate password.
     *
     * @return the string
     */
    protected String generatePassword() {
        return new BigInteger(130, random).toString(32);
    }
}
