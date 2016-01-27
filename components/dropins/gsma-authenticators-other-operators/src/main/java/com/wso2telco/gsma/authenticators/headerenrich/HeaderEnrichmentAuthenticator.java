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
package com.wso2telco.gsma.authenticators.headerenrich;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.InvalidCredentialsException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.DataHolder;
import com.wso2telco.gsma.authenticators.IPRangeChecker;
import com.wso2telco.gsma.authenticators.config.MobileConnectConfig;
import com.wso2telco.gsma.authenticators.internal.CustomAuthenticatorServiceComponent;

 
// TODO: Auto-generated Javadoc
/**
 * The Class HeaderEnrichmentAuthenticator.
 */
public class HeaderEnrichmentAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4438354156955225674L;
    
    /** The log. */
    private static Log log = LogFactory.getLog(HeaderEnrichmentAuthenticator.class);
    
    /** The operatorips. */
    static List<String> operatorips = null;

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public boolean canHandle(HttpServletRequest request) {

        if (log.isDebugEnabled()) {
            log.debug("Header Enrich Authenticator canHandle invoked");
        }

         
        String msisdn;
        try {
            msisdn = request.getHeader("msisdn");
        } catch (NullPointerException e) {
            // Deliberately return false since headers are null
            return false;
        }
        //Temporary check parameter
        //String msisdn = request.getParameter("msisdn");

        if (msisdn == null) {
            return false;
        } else {
            return true;
        }
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
//        return AuthenticatorFlowStatus.INCOMPLETE;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#initiateAuthenticationRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
     */
    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
            HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        //String enrichpage = DataHolder.getInstance().getMobileConnectConfig().getHEADERENRICH().getEndpoint();
        String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        String loginprefix = DataHolder.getInstance().getMobileConnectConfig().getListenerWebappHost();
        String msisdn = null;

        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                context.getCallerSessionKey(),
                context.getContextIdentifier());

        try {

            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            }

            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }

            if (ipAddress == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Header IpAddress not found.");
                }
            }

            if (!validateOperator(ipAddress)) {
                throw new AuthenticationFailedException("Authentication Failed");
            }

            msisdn = request.getHeader("msisdn");

            if ((msisdn == null) && (DataHolder.getInstance().getMobileConnectConfig().getHEADERENRICH().getEnrichflg().equalsIgnoreCase("true"))) {

                if (context.isRetrying()) {
                    retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
                } else {
                    // Insert entry to DB only if this is not a retry
                    //DBUtils.insertUserResponse(context.getContextIdentifier(), String.valueOf(HeaderEnrichmentAuthenticator.UserResponse.PENDING));
                }
                 

                response.sendRedirect(response.encodeRedirectURL(loginprefix + loginPage + ("?" + queryParams)) + "&authenticators="
                        + getName() + ":" + "LOCAL" + retryParam);
            }

        } catch (IOException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
            // } catch (AuthenticatorException e) {
            // throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
            // } catch (AuthenticatorException e) {
            // throw new AuthenticationFailedException(e.getMessage(), e);
        }

    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator#processAuthenticationResponse(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
     */
    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
            HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        boolean isAuthenticated = false;
        String msisdn = null;

        //check to msisdn header

         
        
        try {
            msisdn = request.getHeader("msisdn");
        } catch (NullPointerException e) {
            log.info("Header MSISDN Not Found");
        }
        
        String ipAddress = retriveIPAddress(request);
        if (ipAddress == null) {
            if (log.isDebugEnabled()) {
                log.debug("Header IpAddress not found.");
            }
        }       
        
        if (validateOperator(ipAddress)) {

            //msisdn = request.getHeader("msisdn");
            //temporary to check parameter
            //msisdn = request.getParameter("msisdn");

            if (msisdn != null && validateMsisdn(msisdn)) {
                // Check the authentication by checking if username exists
                try {
                    int tenantId = IdentityUtil.getTenantIdOFUser(msisdn);
                    UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService()
                            .getTenantUserRealm(tenantId);

                    if (userRealm != null) {
                        UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
                        isAuthenticated = userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(msisdn.replace("+", "").trim()));
                    } else {
                        throw new AuthenticationFailedException("Cannot find the user realm for the given tenant: " + tenantId);
                    }
                } catch (IdentityException e) {
                    log.error("HeaderEnrichment Authentication failed while trying to get the tenant ID of the user", e);
                    throw new AuthenticationFailedException(e.getMessage(), e);
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    log.error("HeaderEnrichment Authentication failed while trying to authenticate", e);
                    throw new AuthenticationFailedException(e.getMessage(), e);
                }

                context.setSubject(msisdn);
            }

        }
        if (!isAuthenticated) {
            log.info("HeaderEnrichment Authenticator authentication failed ");
            context.setProperty("faileduser", msisdn);
            
            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to not existing user MSISDN.");
            }

            throw new AuthenticationFailedException("Authentication Failed");
        }
        log.info("HeaderEnrichment Authenticator authentication success for MSISDN - " + msisdn);
    }

    /**
     * Retrive ip address.
     *
     * @param request the request
     * @return the string
     */
    public String retriveIPAddress(HttpServletRequest request) {

        String ipAddress = null;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
        } catch (Exception e) {
        }

        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress;
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
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getFriendlyName()
     */
    @Override
    public String getFriendlyName() {
        return Constants.HE_AUTHENTICATOR_FRIENDLY_NAME;
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
     */
    @Override
    public String getName() {
        return Constants.HE_AUTHENTICATOR_NAME;
    }

    /**
     * Validate msisdn.
     *
     * @param msisdn the msisdn
     * @return true, if successful
     */
    protected boolean validateMsisdn(String msisdn) {
        boolean isvalid = false;
        if (msisdn != null && ((msisdn.length() == 11 && msisdn.indexOf('+') < 0) || (msisdn.length() == 12 && msisdn.matches("[0-9]+")))) {
            isvalid = true;
        }
        return isvalid;
    }

    /**
     * Validate operator.
     *
     * @param strip the strip
     * @return true, if successful
     */
    protected boolean validateOperator(String strip) {
        boolean isvalid = false;

        operatorips = DataHolder.getInstance().getMobileConnectConfig().getHEADERENRICH().getMobileIPRanges();

        for (String ids : operatorips) {
            if (ids != null) {
                String[] iprange = ids.split(":");
                isvalid = IPRangeChecker.isValidRange(iprange[0], iprange[1], strip);
                if (isvalid) {
                    break;
                }
            }
        }
        return isvalid;
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
