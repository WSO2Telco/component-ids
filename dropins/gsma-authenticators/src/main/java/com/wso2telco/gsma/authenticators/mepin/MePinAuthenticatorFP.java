/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 * 
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.gsma.authenticators.mepin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

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

import com.google.gson.JsonObject;
import com.gsma.authenticators.mepin.MePinQuery;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.DataHolder;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;

// TODO: Auto-generated Javadoc
/**
 * The Class MePinAuthenticatorFP.
 */
public class MePinAuthenticatorFP extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8570406196896249057L;
    
    /** The log. */
    private static Log log = LogFactory.getLog(MePinAuthenticatorFP.class);
    
    /** The loa. */
    public static String LOA = "fp";


    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {
        if (log.isDebugEnabled()) {
            log.debug("MePIN Authenticator canHandle invoked");
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
     */
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

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#initiateAuthenticationRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
     */
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
                DBUtils.insertUserResponse(context.getContextIdentifier(), String.valueOf(MePinAuthenticatorFP
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
                    serviceProviderName, MePinAuthenticatorFP.LOA);
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

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#processAuthenticationResponse(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
     */
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
        
        AuthenticationContextHelper.setSubject(context, msisdn);

        log.info("MePIN Authenticator authentication success");

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
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
     */
    @Override
    public String getName() {
        return Constants.MEPIN_AUTHENTICATOR_FP_NAME;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getFriendlyName()
     */
    @Override
    public String getFriendlyName() {
        return Constants.MEPIN_AUTHENTICATOR_FRIENDLY_NAME;
    }

    /**
     * The Enum UserResponse.
     */
    private enum UserResponse {
        
        /** The pending. */
        PENDING,
        
        /** The approved. */
        APPROVED,
        
        /** The rejected. */
        REJECTED
    }
}
