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
package com.wso2telco.gsma.authenticators.headerenrich;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.core.config.MobileConnectConfig;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.IPRangeChecker;
import com.wso2telco.gsma.authenticators.internal.CustomAuthenticatorServiceComponent;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.authenticators.util.DecryptionAES;
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
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
 
// TODO: Auto-generated Javadoc
/**
 * The Class HeaderEnrichmentAuthenticator.
 */
public class HeaderEnrichmentAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4438354156955225674L;
    
    /** The operatorips. */
    static List<String> operatorips = null;
    
    /** The operators. */
    static List<MobileConnectConfig.OPERATOR> operators = null;
    
    /** The log. */
    private static Log log = LogFactory.getLog(HeaderEnrichmentAuthenticator.class);

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
            msisdn = request.getParameter("msisdn_header");
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
        String operator = request.getParameter("operator");
        

        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                context.getCallerSessionKey(),
                context.getContextIdentifier());

        try {

            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            }

            String ipAddress = request.getParameter("ipAddress");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }

            if (ipAddress == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Header IpAddress not found.");
                }
            }

            if (!validateOperator(operator,ipAddress)) {
                throw new AuthenticationFailedException("Authentication Failed");
            }

            msisdn = request.getParameter("msisdn_header");
            
            
            try {
                 msisdn = DecryptionAES.decrypt(msisdn);
            } catch (Exception ex) {
                Logger.getLogger(HeaderEnrichmentAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            log.info("msisdn after decryption=" + msisdn);
			
	    log.info("MSISDN@initiate= " + msisdn);
            if ((msisdn == null) && (DataHolder.getInstance().getMobileConnectConfig().getHEADERENRICH().getEnrichflg().equalsIgnoreCase("true"))) {
//            if ((msisdn == null)) {
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
        String operator = null;
        Boolean ipValidation = false;

        operator = request.getParameter("operator");

        operators = DataHolder.getInstance().getMobileConnectConfig().getHEADERENRICH().getOperators();

        for (MobileConnectConfig.OPERATOR op : operators){
            if(operator.equalsIgnoreCase(op.getOperatorName())){
                ipValidation = Boolean.valueOf(op.getIpValidation());
            }
        }

        //check to msisdn header

         
        
        try {
            msisdn = request.getParameter("msisdn_header");
            operator = request.getParameter("operator");
            
            log.info("HeaderEnrichment redirect URI = " + request.getParameter("redirect_uri"));
            context.setProperty("operator",operator);
            context.setProperty("redirectURI",request.getParameter("redirect_uri"));
            //Set operator name to context
            
            //log.info(op);
            
            try {
                 msisdn = DecryptionAES.decrypt(msisdn);
            } catch (Exception ex) {
                Logger.getLogger(HeaderEnrichmentAuthenticator.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            log.info("msisdn after decryption=" + msisdn);
            
            log.info("MSISDN@Process= " + msisdn);
        } catch (NullPointerException e) {
            log.info("Header MSISDN Not Found");
        }
        
        String ipAddress = retriveIPAddress(request);
        log.info("ipAddress = " + ipAddress);
        if (ipAddress == null) {
            if (log.isDebugEnabled()) {
                log.debug("Header IpAddress not found.");
            }
            if(ipValidation){
                throw new AuthenticationFailedException("Authentication Failed");
            }
        }
        
       boolean validOperator = true;

        if (ipAddress != null && ipValidation){
            validOperator = validateOperator(operator,ipAddress);
            
        }else{
            //nop
        }
        if (validOperator) {

            //msisdn = request.getHeader("msisdn");
            //temporary to check parameter
            //msisdn = request.getParameter("msisdn");

            if ( msisdn != null && msisdn.length() > 1 && (!msisdn.isEmpty()) ) {
                // Check the authentication by checking if username exists
				log.info("Check whether user account exists");
                try {
                	int tenantId = -1234;
                    UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService()
                            .getTenantUserRealm(tenantId);

                    if (userRealm != null) {
                        UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
                        isAuthenticated = userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(msisdn.replace("+", "").trim()));
                    } else {
                        throw new AuthenticationFailedException("Cannot find the user realm for the given tenant: " + tenantId);
                    }
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    log.error("HeaderEnrichment Authentication failed while trying to authenticate", e);
                    throw new AuthenticationFailedException(e.getMessage(), e);
                }

//                AuthenticatedUser user=new AuthenticatedUser();
//                context.setSubject(user);
                  AuthenticationContextHelper.setSubject(context,msisdn);
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
        
        context.setProperty("msisdn", msisdn);
       
        AuthenticationContextHelper.setSubject(context,msisdn);
       
        
        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
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
            ipAddress = request.getParameter("ipAddress");
        } catch (Exception e) {
        	log.error("Error occured Retriving ip address " + e);
        }

        if (ipAddress == null) {
          //  ipAddress = request.getRemoteAddr();
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
     * @param operator the operator
     * @param strip the strip
     * @return true, if successful
     */
    protected boolean validateOperator(String operator, String strip) {
        boolean isvalid = false;
 

        log.info("Operator name  " + operator);

        //        operatorips = DataHolder.getInstance().getMobileConnectConfig().getHEADERENRICH().getMobileIPRanges();
        operators = DataHolder.getInstance().getMobileConnectConfig().getHEADERENRICH().getOperators();

        for (MobileConnectConfig.OPERATOR op : operators){
            if(operator.equalsIgnoreCase(op.getOperatorName())){
                    for (String ids : op.getMobileIPRanges()) {
                        if (ids != null) {
                            String[] iprange = ids.split(":");
                            isvalid = IPRangeChecker.isValidRange(iprange[0], iprange[1], strip);
                            if (isvalid) {
                                break;
                            }
                        }
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
