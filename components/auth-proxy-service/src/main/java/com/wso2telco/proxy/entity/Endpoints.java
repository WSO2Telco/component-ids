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
package com.wso2telco.proxy.entity;

import com.google.gdata.util.common.util.Base64DecoderException;
import com.sun.jersey.spi.container.ContainerRequest;
import com.wso2telco.core.config.model.LoginHintFormatDetails;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.model.ScopeDetailsConfig;
import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.pcrservice.exception.PCRException;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import com.wso2telco.openidtokenbuilder.MIFEOpenIDTokenBuilder;
import com.wso2telco.proxy.MSISDNDecryption;
import com.wso2telco.proxy.attributeshare.AttributeShare;
import com.wso2telco.proxy.model.AuthenticatorException;
import com.wso2telco.proxy.model.MSISDNHeader;
import com.wso2telco.proxy.model.RedirectUrlInfo;
import com.wso2telco.proxy.util.AuthProxyConstants;
import com.wso2telco.proxy.util.DBUtils;
import com.wso2telco.proxy.util.Decrypt;
import com.wso2telco.proxy.util.EncryptAES;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.user.registration.stub.*;
import org.wso2.carbon.identity.user.registration.stub.dto.UserDTO;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.ConfigurationException;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;

@Path("/")
public class Endpoints {

    private static Log log = LogFactory.getLog(Endpoints.class);
    private static HashMap<String, MSISDNDecryption> msisdnDecryptorsClassObjectMap = null;
    private static MobileConnectConfig mobileConnectConfigs = null;
    private static ScopeDetailsConfig scopeDetailsConfigs = null;
    private static Map<String, List<MSISDNHeader>> operatorsMSISDNHeadersMap;
    private static Map<String, MobileConnectConfig.OPERATOR> operatorPropertiesMap = null;
    private static Map<String, ScopeDetailsConfig.Scope> scopeMap = null;

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /**
     * The Constant LOGIN_HINT_ENCRYPTED_PREFIX.
     */
    private static final String LOGIN_HINT_ENCRYPTED_PREFIX = "ENCR_MSISDN:";

    /**
     * The Constant LOGIN_HINT_NOENCRYPTED_PREFIX.
     */
    private static final String LOGIN_HINT_NOENCRYPTED_PREFIX = "MSISDN:";

    private static final String LOGIN_HINT_PCR = "PCR:";

    /**
     * The Constant LOGIN_HINT_SEPARATOR.
     */
    private static final String LOGIN_HINT_SEPARATOR = "|";

    static {
        try {

            //Load mobile-connect.xml file.
            mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
            //Load scope-config.xml file.
            scopeDetailsConfigs = configurationService.getDataHolder().getScopeDetailsConfig();
            //Load msisdn header properties.
            operatorsMSISDNHeadersMap = DBUtils.getOperatorsMSISDNHeaderProperties();

            //Load operator properties.
            operatorPropertiesMap = new HashMap<String, MobileConnectConfig.OPERATOR>();
            List<MobileConnectConfig.OPERATOR> operators = mobileConnectConfigs.getHEADERENRICH().getOperators();
            for (MobileConnectConfig.OPERATOR op : operators) {
                operatorPropertiesMap.put(op.getOperatorName(), op);
            }

            //Load scope related request optional parameters.
            scopeMap = new HashMap<>();
            List<ScopeDetailsConfig.Scope> scopes = scopeDetailsConfigs.getPremiumScopes();

            for (ScopeDetailsConfig.Scope sc : scopes) {
                scopeMap.put(sc.getName(), sc);
            }
        } catch (SQLException e) {
            log.error("Error occurred while retrieving operator MSISDN properties of operators.");
        } catch (NamingException e) {
            log.error("DataSource could not be found in mobile-connect.xml.");
        }
    }

    @GET
    @Path("/oauth2/authorize/operator/{operatorName}")
    public void RedirectToAuthorizeEndpoint(@Context HttpServletRequest httpServletRequest, @Context
            HttpServletResponse httpServletResponse, @Context HttpHeaders httpHeaders, @Context UriInfo uriInfo,
                                            @PathParam("operatorName") String operatorName, String jsonBody) throws
            Exception {

        operatorName = operatorName.toLowerCase();
        //Read query params from the header.
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String clientId = queryParams.getFirst("client_id");
        String state = queryParams.getFirst("state");
        String redirectURL = null;
        String scopeName = null;
        String responseType = null;
        boolean isBackChannelAllowed = false;

        org.apache.log4j.MDC.put("REF_ID", state);
        log.info("Request processing started from proxy");

        boolean invalid = false;
        if (queryParams.get(AuthProxyConstants.REDIRECT_URI) != null) {
            redirectURL = queryParams.get(AuthProxyConstants.REDIRECT_URI).get(0);
            invalid = !DBUtils.isValidCallback(redirectURL, clientId);
        } else {
            invalid = true;
        }

        if (queryParams.get(AuthProxyConstants.SCOPE) != null) {
            scopeName = queryParams.get(AuthProxyConstants.SCOPE).get(0);
        } else if (!invalid) {
            invalid = true;
        }

        if (queryParams.get(AuthProxyConstants.RESPONSE_TYPE) != null) {
            responseType = queryParams.get(AuthProxyConstants.RESPONSE_TYPE).get(0);
        } else if (!invalid) {
            invalid = true;
        }

        if (!invalid) {

            //maintain userstatus related to request for data publishing purpose
            UserStatus userStatus = DataPublisherUtil.buildUserStatusFromRequest(httpServletRequest, null);
            //check for forwarded trn Id
            String transactionId = DataPublisherUtil.getSessionID(httpServletRequest, null);
            if (StringUtils.isEmpty(transactionId)) {
                //generate new trn id
                transactionId = UUID.randomUUID().toString();
            }
            userStatus.setTransactionId(transactionId);
            userStatus.setConsumerKey(((ContainerRequest) httpHeaders).getQueryParameters().getFirst(
                    AuthProxyConstants.CLIENT_ID));

            userStatus.setStatus(DataPublisherUtil.UserState.PROXY_PROCESSING.name());
            DataPublisherUtil.publishUserStatusMetaData(userStatus);
            DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.PROXY_PROCESSING,
                    null);


            if (!configurationService.getDataHolder().getMobileConnectConfig().isSpValidationDisabled() && !isValidScope
                    (scopeName, clientId)) {
                String errMsg = "Scope [ " + scopeName + " ] is not allowed for client [ " + clientId + " ]";
                log.error(errMsg);
                DataPublisherUtil.updateAndPublishUserStatus(
                        userStatus, DataPublisherUtil.UserState.INVALID_REQUEST, errMsg);

                redirectURL = redirectURL + "?error=access_denied";
                invalid = true;
            } else {
                String loginHint = null;
                String ipAddress = null;
                String msisdn = null;
                String queryString = "";
                String correlationId = null;
                String redirectUrl = null;

                try {

                    msisdn = decryptMSISDN(httpHeaders, operatorName);

                    List<String> loginHintParameter = queryParams.get(AuthProxyConstants.LOGIN_HINT);
                    if (loginHintParameter != null) {
                        //Read login_hint value from the query params.
                        loginHint = loginHintParameter.get(0);
                        if (log.isDebugEnabled()) {
                            log.debug("Login Hint : " + loginHint);
                        }
                    }

                    String authorizeUrlProperty = null;
                    //have to check whether mobile-connect.xml exists or not.
                    if (mobileConnectConfigs != null) {
                        authorizeUrlProperty = mobileConnectConfigs.getAuthProxy().getAuthorizeURL();
                    } else {
                        String errMsg = "mobile-connect.xml could not be found";
                        DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState
                                .CONFIGURATION_ERROR, errMsg);

                        throw new FileNotFoundException(errMsg);
                    }

                    RedirectUrlInfo redirectUrlInfo = new RedirectUrlInfo();
                    redirectUrlInfo.setAuthorizeUrl(authorizeUrlProperty);
                    redirectUrlInfo.setOperatorName(operatorName);

                    if (null != (queryParams.get(AuthProxyConstants.IS_BACKCHANNEL_ALLOWED)) &&
                            (isBackChannelAllowed = Boolean.parseBoolean(queryParams.get(AuthProxyConstants
                                    .IS_BACKCHANNEL_ALLOWED).get(0).toString()))) {
                        correlationId = queryParams.get(AuthProxyConstants.CORRELATION_ID).get(0);
                        redirectUrl = queryParams.get(AuthProxyConstants.REDIRECT_URL).get(0);
                        redirectUrlInfo.setBackChannelAllowed(isBackChannelAllowed);
                        redirectUrlInfo.setCorrelationId(correlationId);
                        redirectUrlInfo.setRedirectUrl(redirectUrl);
                    }

                    if (httpHeaders != null) {
                        if (log.isDebugEnabled()) {
                            for (String httpHeader : httpHeaders.getRequestHeaders().keySet()) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Header : " + httpHeader + " Value: " + httpHeaders.getRequestHeader
                                            (httpHeader));
                                }
                            }
                        }
                    }

                    ipAddress = getIpAddress(httpHeaders, httpServletRequest, operatorName);

                    //Validate with Scope wise parameters and throw exceptions
                    ScopeParam scopeParam = validateAndSetScopeParameters(loginHint, msisdn, scopeName, redirectUrlInfo,
                            userStatus, redirectURL);

                    String loginhint_msisdn = null;
                    try {
                        loginhint_msisdn = retreiveLoginHintMsisdn(loginHint, scopeParam, redirectURL);
                    } catch (Exception e) {
                        log.debug("Error retrieving loginhint msisdn : " + e);
                    }

                    Boolean isScopeExists = queryParams.containsKey(AuthProxyConstants.SCOPE);
                    String operatorScopeWithClaims;

                    if (isScopeExists) {
                        operatorScopeWithClaims = queryParams.get(AuthProxyConstants.SCOPE).get(0);

                        // Check if scope list contains openid scope, and append if it does not contain
                        if (queryParams.containsKey(AuthProxyConstants.SCOPE) && queryParams.get(AuthProxyConstants
                                .SCOPE).get(0) != null) {
                            List<String> scopes = new ArrayList<>(Arrays.asList(queryParams.get(AuthProxyConstants
                                    .SCOPE)
                                    .get(0).split(" ")));
                            if (!scopes.contains(AuthProxyConstants.SCOPE_OPENID)) {
                                queryParams.get(AuthProxyConstants.SCOPE)
                                        .set(0, scopeName + " " + AuthProxyConstants.SCOPE_OPENID);
                            }
                            for(int i = 0; i < queryParams.get(AuthProxyConstants.SCOPE).size(); i++)
                                log.info(queryParams.get(AuthProxyConstants.SCOPE).get(i));

                            log.info(queryParams.get(AuthProxyConstants.SCOPE).remove("charge"));
                        }

                        List<String> promptValues = queryParams.get(AuthProxyConstants.PROMPT);
                        if (promptValues != null && !promptValues.isEmpty()) {
                            redirectUrlInfo.setPrompt(promptValues.get(0));
                            queryParams.remove(AuthProxyConstants.PROMPT);
                        }

                        queryString = processQueryString(queryParams, queryString);

                        // Encrypt MSISDN
                        msisdn = EncryptAES.encrypt(msisdn);

                        // Encrypt login-hint msisdn
                        loginhint_msisdn = EncryptAES.encrypt(loginhint_msisdn);

                        // URL encode
                        if (msisdn != null) {
                            msisdn = URLEncoder.encode(msisdn, AuthProxyConstants.UTF_ENCODER);
                        } else {
                            msisdn = "";
                        }

                        // URL encode login hint msisdn
                        if (loginhint_msisdn != null) {
                            loginhint_msisdn = URLEncoder.encode(loginhint_msisdn, AuthProxyConstants.UTF_ENCODER);
                        } else {
                            loginhint_msisdn = "";
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("redirectURL : " + redirectURL);
                        }

                        Map<String, Object> attShareDetails = AttributeShare.attributeShareScopesValidation(scopeName,
                                operatorName, clientId, loginhint_msisdn, msisdn);

                        redirectUrlInfo.setMsisdnHeader(msisdn);
                        redirectUrlInfo.setLoginhintMsisdn(loginhint_msisdn);
                        redirectUrlInfo.setQueryString(queryString);
                        redirectUrlInfo.setIpAddress(ipAddress);
                        redirectUrlInfo.setTelcoScope(operatorScopeWithClaims);
                        redirectUrlInfo.setTransactionId(userStatus.getTransactionId());

                        redirectUrlInfo.setAttributeSharingScope(Boolean.parseBoolean(attShareDetails.get
                                (AuthProxyConstants.ATTR_SHARE_SCOPE).toString()));

                        if(attShareDetails.get(AuthProxyConstants.TRUSTED_STATUS) != null)
                            redirectUrlInfo.setTrustedStatus(attShareDetails.get(AuthProxyConstants.TRUSTED_STATUS).toString());
                        else
                            redirectUrlInfo.setTrustedStatus(null);

                        if(attShareDetails.get(AuthProxyConstants.ATTR_SHARE_SCOPE_TYPE) != null){
                            redirectUrlInfo.setAttributeSharingScopeType(attShareDetails.get(AuthProxyConstants
                                    .ATTR_SHARE_SCOPE_TYPE).toString());
                        }else{
                            redirectUrlInfo.setAttributeSharingScopeType(null);
                        }

                        redirectUrlInfo.setAPIConsent(Boolean.parseBoolean(attShareDetails.get(AuthProxyConstants.IS_API_CONSENT).toString()));

                        if(scopeParam.isConsentPage()){
                            redirectUrlInfo.setShowConsent(true);
                        }
                        redirectUrlInfo.setScopeTypesList(scopeParam.getScopeTypesList());
                       redirectURL = constructRedirectUrl(redirectUrlInfo, userStatus, isBackChannelAllowed);
 

                        DataPublisherUtil.updateAndPublishUserStatus(
                                userStatus, DataPublisherUtil.UserState.PROXY_REQUEST_FORWARDED_TO_IS, "Redirect URL : "
                                        + redirectURL);
                    }

                } catch (Exception e) {
                    log.error("Exception : " + e.getMessage());
                    //todo: dynamically set error description depending on scope parameters
                    redirectURL = redirectURL + "?error=access_denied&error_description=" + e.getMessage();
                    DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState
                                    .OTHER_ERROR,
                            e.getMessage());
                }

 
                if (log.isDebugEnabled()) {
                    log.debug("redirectURL : " + redirectURL);
                }
 
            }

        }

        if (invalid) {
            if (redirectURL == null) {
                throw new Exception("Invalid Request- Redirect URL not found");
            }
            httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Request");
        } else {
            log.info(String.format("Redirecting to : %s", redirectURL));
            httpServletResponse.sendRedirect(redirectURL);
        }
    }


    /**
     * Check the remaining account locked time
     *
     * @param userName
     * @return true if scope is allowed, else false
     */
    @GET
    @Path("/accountLockRemainingTime/{userName}")
    public String getRemainingTime(@Context HttpServletRequest httpServletRequest, @Context
            HttpServletResponse httpServletResponse, @PathParam("userName") String userName) throws
            Exception {

        String remainingTimeInMins = "";
        long accountUnlockTime = DBUtils.getAccountUnlockTime(userName);

        if (0 != accountUnlockTime) {
            Instant instant = Instant.now();
            long timeStampSeconds = instant.toEpochMilli();
            remainingTimeInMins = Integer.toString((int)Math.ceil((accountUnlockTime - timeStampSeconds) / 60000));
        }
        return remainingTimeInMins;
    }

    /**
     * Check if the Scope is allowed for SP
     *
     * @param scopeName
     * @param clientId
     * @return true if scope is allowed, else false
     */
    private boolean isValidScope(String scopeName, String clientId)
            throws AuthenticatorException, ConfigurationException {
        return DBUtils.isSPAllowedScope(scopeName, clientId);
    }


    /**
     * Validate and set scope related parameters to RedirectUrlInfo
     *
     * @param loginHint
     * @param msisdnHeader
     * @param scope
     * @param redirectUrlInfo
     * @return MSISDN extracted from login-hint
     * @throws AuthenticationFailedException
     * @throws ConfigurationException
     */
    private ScopeParam validateAndSetScopeParameters(String loginHint, String msisdnHeader, String scope,
                                                     RedirectUrlInfo redirectUrlInfo, UserStatus userStatus, String
                                                             redirectURL)
            throws AuthenticationFailedException, ConfigurationException {
        //TODO: get all scope related params. This should be move to a initialization method or add to cache later
        ScopeParam scopeParam = getScopeParam(scope, userStatus);
        // show t&c page on authenticators depending on this scope specific variable

        if (scopeParam == null) {
            String errMsg = "Invalid scope config - " + scope;

            DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.CONFIGURATION_ERROR,
                    errMsg);

            throw new AuthenticationFailedException(errMsg);
        }


        redirectUrlInfo.setShowTnc(scopeParam.isTncVisible());
        redirectUrlInfo.setHeaderMismatchResult(scopeParam.getMsisdnMismatchResult());
        redirectUrlInfo.setHeFailureResult(scopeParam.getHeFailureResult());

        String verifiedLoginHint = null;

        if (scopeParam.isLoginHintMandatory() && StringUtils.isEmpty(loginHint)) {
            String errMsg = "Login Hint parameter cannot be empty for scope : " + scope;
            DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                    DataPublisherUtil.UserState.LOGIN_HINT_INVALID, errMsg);

            throw new AuthenticationFailedException(errMsg);
        }

        if (scopeParam.isHeaderMsisdnMandatory() && StringUtils.isEmpty(msisdnHeader)) {
            String errMsg = "MSISDN header not found for scope : " + scope;

            DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.MSISDN_INVALID,
                    errMsg);

            throw new AuthenticationFailedException(errMsg);
        }

        if (StringUtils.isNotEmpty(loginHint)) {
            verifiedLoginHint = retunFormatVerfiedPlainTextLoginHint(loginHint, scopeParam.getLoginHintFormat(),
                    userStatus, redirectURL);
        }

        if (StringUtils.isNotEmpty(msisdnHeader)) {
            if (!validateMsisdnFormat(msisdnHeader)) {
                String errMsg = "Invalid msisdn header format - " + msisdnHeader;

                DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.MSISDN_INVALID,
                        errMsg);

                throw new AuthenticationFailedException(errMsg);
            }

            if (StringUtils.isNotEmpty(verifiedLoginHint)) {
                if (ScopeParam.MsisdnMismatchResultTypes.ERROR_RETURN.equals(scopeParam.getMsisdnMismatchResult())) {
                    if (!verifiedLoginHint.equals(msisdnHeader)) {
                        DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                                DataPublisherUtil.UserState.LOGIN_HINT_MISMATCH,
                                null);

                        throw new AuthenticationFailedException(
                                "login hint is not matching with the header msisdn");
                    }
                }
            }
        }

        return scopeParam;
    }

    /**
     * Get the scope param object
     *
     * @param scope
     * @return
     * @throws AuthenticationFailedException
     * @throws ConfigurationException
     */
    private ScopeParam getScopeParam(String scope, UserStatus userStatus) throws AuthenticationFailedException,
            ConfigurationException {
        Map scopeDetail;
        try {
            scopeDetail = DBUtils.getScopeParams(scope);
        } catch (AuthenticatorException e) {
            String errMsg = "Error occurred while getting scope parameters from the database for the scope - " + scope;

            DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.OTHER_ERROR, errMsg);

            throw new AuthenticationFailedException(errMsg);
        }
        if (scopeDetail == null || scopeDetail.isEmpty()) {
            String errMsg = "Please configure Scope related Parameters properly in scope_parameter table";
            DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.CONFIGURATION_ERROR,
                    errMsg);

            throw new ConfigurationException(errMsg);
        }

        //set the scope specific params
        return (ScopeParam) scopeDetail.get("params");
    }

    /**
     * Validate msisdn format and matches with the login hint
     *
     * @param loginHint
     * @param loginHintAllowedFormatDetailsList
     * @return MSISDN extracted from login-hint
     * @throws AuthenticationFailedException
     */
    private String retunFormatVerfiedPlainTextLoginHint(String loginHint,
                                                        List<LoginHintFormatDetails>
                                                                loginHintAllowedFormatDetailsList, UserStatus
                                                                userStatus, String redirectURL)
            throws AuthenticationFailedException {
        boolean isValidFormatType = false; //msisdn/loginhint should be a either of defined formats

        String plainTextLoginHint = null;
        String pcrValue = null;
        for (LoginHintFormatDetails loginHintFormatDetails : loginHintAllowedFormatDetailsList) {

            switch (loginHintFormatDetails.getFormatType()) {
                case PLAINTEXT:
                    if (log.isDebugEnabled()) {
                        log.debug("Plain text login hint : " + plainTextLoginHint);
                    }
                    if (StringUtils.isNotEmpty(loginHint) && (!loginHint.startsWith(LOGIN_HINT_ENCRYPTED_PREFIX) &&
                            !loginHint.startsWith(LOGIN_HINT_NOENCRYPTED_PREFIX) && !loginHint.startsWith
                            (LOGIN_HINT_PCR))) {
                        plainTextLoginHint = loginHint;
                        isValidFormatType = true;
                    }
                    break;
                case ENCRYPTED:
                    String decryptAlgorithm = loginHintFormatDetails.getDecryptAlgorithm();
                    if (StringUtils.isNotEmpty(loginHint)) {
                        if (loginHint.startsWith(LOGIN_HINT_ENCRYPTED_PREFIX)) {
                            String decrypted = null;
                            try {
                                //decrypt msisdn using given algorithm
                                decrypted = Decrypt.decryptData(loginHint.replace(LOGIN_HINT_ENCRYPTED_PREFIX, ""),
                                        decryptAlgorithm);
                                if (log.isDebugEnabled()) {
                                    log.debug("Decrypted login hint : " + decrypted);
                                }
                                plainTextLoginHint = decrypted.substring(0, decrypted.indexOf(LOGIN_HINT_SEPARATOR));
                                if (log.isDebugEnabled()) {
                                    log.debug("MSISDN by encrypted login hint : " + plainTextLoginHint);
                                }
                                isValidFormatType = true;
                            } catch (Exception e) {
                                log.error("Error while decrypting login hint : " + loginHint, e);
                            }
                        }
                    }
                    break;
                case MSISDN:
                    if (StringUtils.isNotEmpty(loginHint)) {
                        if (loginHint.startsWith(LOGIN_HINT_NOENCRYPTED_PREFIX)) {
                            plainTextLoginHint = loginHint.replace(LOGIN_HINT_NOENCRYPTED_PREFIX, "");
                            if (log.isDebugEnabled()) {
                                log.debug("MSISDN by login hint: " + plainTextLoginHint);
                            }
                            isValidFormatType = true;
                        }
                    }
                    break;
                case PCR:
                    if (StringUtils.isNotEmpty(loginHint) && loginHint.startsWith(LOGIN_HINT_PCR)) {

                        pcrValue = loginHint.replace(LOGIN_HINT_PCR, "");
                        isValidFormatType = true;
                        try {
                            String retreivedMsisdn = getMSISDNbyPcr(redirectURL, pcrValue);
                            if (StringUtils.isNotEmpty(retreivedMsisdn)) {
                                plainTextLoginHint = retreivedMsisdn;
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("PCR by login hint: " + plainTextLoginHint);
                            }

                        } catch (Exception e) {
                            log.error("Given pcr in the login hint cannot be accepted");
                            throw new AuthenticationFailedException("Given pcr in the login hint cannot be accepted:"
                                    + e);
                        }

                    }
                    break;
                default:
                    log.warn("Invalid Login Hint format - " + loginHintFormatDetails.getFormatType());
                    break;
            }

            if (isValidFormatType) {
                break;
            }

        }

        //msisdn/loginhint should be a either of defined formats
        if (!isValidFormatType || !validateMsisdnFormat(plainTextLoginHint)) {
            DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.LOGIN_HINT_INVALID,
                    null);

            throw new AuthenticationFailedException(
                    "login hint is malformat");
        }

        return plainTextLoginHint;
    }


    private String retreiveLoginHintMsisdn(String loginHint, ScopeParam scopeParam, String callbackurl)
            throws AuthenticationFailedException, ConfigurationException {
        boolean isValidFormatType = false; //msisdn/loginhint should be a either of defined formats
        String msisdn = null;

        for (LoginHintFormatDetails loginHintFormatDetails : scopeParam.getLoginHintFormat()) {
            switch (loginHintFormatDetails.getFormatType()) {
                case PLAINTEXT:
                    if (StringUtils.isNotEmpty(loginHint)) {
                        msisdn = loginHint;
                    }
                    isValidFormatType = true;
                    break;
                case ENCRYPTED:
                    String decryptAlgorithm = loginHintFormatDetails.getDecryptAlgorithm();
                    if (StringUtils.isNotEmpty(loginHint)) {
                        if (loginHint.startsWith(LOGIN_HINT_ENCRYPTED_PREFIX)) {
                            String decrypted = null;
                            try {
                                //decrypt msisdn using given algorithm
                                decrypted = Decrypt.decryptData(loginHint.replace(LOGIN_HINT_ENCRYPTED_PREFIX, ""),
                                        decryptAlgorithm);
                                msisdn = decrypted.substring(0, decrypted.indexOf(LOGIN_HINT_SEPARATOR));
                                isValidFormatType = true;
                            } catch (Exception e) {
                                log.error("Error while decrypting login hint : " + loginHint, e);
                                throw new AuthenticationFailedException("Error while decrypting login hint :" + e);
                            }
                        }
                    } else {
                        isValidFormatType = true;
                    }
                    break;
                case MSISDN:
                    if (StringUtils.isNotEmpty(loginHint) && loginHint.startsWith(LOGIN_HINT_NOENCRYPTED_PREFIX)) {
                        msisdn = loginHint.replace(LOGIN_HINT_NOENCRYPTED_PREFIX, "");
                        isValidFormatType = true;
                        break;
                    } else {
                        isValidFormatType = true;
                    }
                    break;
                case PCR:
                    if (StringUtils.isNotEmpty(loginHint)) {
                        if (loginHint.startsWith(LOGIN_HINT_PCR)) {
                            try {
                                String retreivedMsisdn = getMSISDNbyPcr(callbackurl, loginHint.replace
                                        (LOGIN_HINT_PCR, ""));
                                if (StringUtils.isNotEmpty(retreivedMsisdn)) {
                                    msisdn = retreivedMsisdn;
                                } else {
                                    log.error("No MSISDN for the given PCR");
                                    throw new AuthenticationFailedException("Cannot find MSISDN from pcr in the login" +
                                            " hint");
                                }

                            } catch (Exception e) {
                                log.error("pcr in the login hint cannot be accepted");
                                throw new AuthenticationFailedException("pcr in the login hint cannot be accepted: "
                                        + e);
                            }
                            isValidFormatType = true;
                        }
                    } else {
                        isValidFormatType = true;
                    }
                    break;
                default:
                    log.warn("Invalid Login Hint format - " + loginHintFormatDetails.getFormatType());
                    break;
            }

            //msisdn/loginhint should be a either of defined formats
            if (isValidFormatType && validateMsisdnFormat(msisdn)) {
                return msisdn;
            }
        }

        return null;
    }

    /**
     * Check if the msisdn is in correct format. Validate the format with prefedeined/configurable regex
     *
     * @param msisdn
     * @return
     */
    private boolean validateMsisdnFormat(String msisdn) {
        if (StringUtils.isNotEmpty(msisdn)) {
            String regex = configurationService.getDataHolder().getMobileConnectConfig().getMsisdn()
                    .getValidationRegex();
            if (StringUtils.isNotEmpty(regex)) {
                return msisdn.matches(regex);
            }
        }
        return true;
    }

    private String getMSISDNbyPcr(String callbackUrl, String pcr) throws PCRException {
        String retrievedMsisdn = "";
        if (StringUtils.isNotEmpty(pcr)) {
            MIFEOpenIDTokenBuilder mifeOpenIDTokenBuilder = new MIFEOpenIDTokenBuilder();
            retrievedMsisdn = mifeOpenIDTokenBuilder.getMSISDNbyPcr(callbackUrl, pcr);
        }
        return retrievedMsisdn;
    }

    private String decryptMSISDN(HttpHeaders httpHeaders, String operatorName)
            throws ClassNotFoundException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException,
            IllegalBlockSizeException, Base64DecoderException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalAccessException, InstantiationException {
        String msisdn = null;
        List<MSISDNHeader> msisdnHeaderList = operatorsMSISDNHeadersMap.get(operatorName);

        for (int id = 0; id < msisdnHeaderList.size(); id++) {
            MSISDNHeader msisdnHeader = msisdnHeaderList.get(id);
            String msisdnHeaderName = msisdnHeader.getMsisdnHeaderName();
            if (httpHeaders.getRequestHeader(msisdnHeaderName) != null) {
                msisdn = httpHeaders.getRequestHeader(msisdnHeaderName).get(0);
                boolean isHeaderEncrypted = msisdnHeader.isHeaderEncrypted();
                if (isHeaderEncrypted) {
                    String encryptionKey = msisdnHeader.getHeaderEncryptionKey();
                    String encryptionMethod = msisdnHeader.getHeaderEncryptionMethod();
                    if (!msisdnDecryptorsClassObjectMap.containsKey(encryptionMethod)) {
                        Class encryptionClass = Class.forName(encryptionMethod);
                        MSISDNDecryption clsInstance = (MSISDNDecryption) encryptionClass.newInstance();
                        msisdnDecryptorsClassObjectMap.put(encryptionMethod, clsInstance);
                    }
                    msisdn = msisdnDecryptorsClassObjectMap.get(encryptionMethod).decryptMsisdn(msisdn, encryptionKey);
                }
                break;
            }
        }
        return msisdn;
    }

    private String getIpAddress(HttpHeaders httpHeaders, HttpServletRequest httpServletRequest, String operatorName) {
        String ipAddress = null;
        boolean isOverrideIpHeader = mobileConnectConfigs.getHEADERENRICH().isOverrideIpheader();
        MobileConnectConfig.OPERATOR operatorProperties = operatorPropertiesMap.get(operatorName);
        String ipHeaderName = mobileConnectConfigs.getHEADERENRICH().getIPHeaderName();
        if (StringUtils.isNotEmpty(ipHeaderName) && operatorProperties != null) {
            boolean isRequiredIpValidation = "true".equalsIgnoreCase(operatorProperties.getIpValidation());
            if (isRequiredIpValidation) {
                if (httpHeaders.getRequestHeader(ipHeaderName) != null) {
                    ipAddress = httpHeaders.getRequestHeader(ipHeaderName).get(0);
                }
                ipAddress = (((ipAddress == null) || isOverrideIpHeader) ? captureIpFallbackRemoteHost
                        (httpServletRequest) : ipAddress);
            }
        }
        return ipAddress;
    }

    private String captureIpFallbackRemoteHost(HttpServletRequest httpServletRequest) {
        String remoteIpAddress = null;
        remoteIpAddress = httpServletRequest.getRemoteAddr();
        return remoteIpAddress;
    }

    private String constructRedirectUrl(RedirectUrlInfo redirectUrlInfo, UserStatus userStatus, boolean isBackChannelAllowed) throws
            ConfigurationException {
        String redirectURL = null;
        String authorizeUrl = redirectUrlInfo.getAuthorizeUrl();
        String queryString = redirectUrlInfo.getQueryString();
        String msisdnHeader = redirectUrlInfo.getMsisdnHeader();
        String loginHintMsisdn = redirectUrlInfo.getLoginhintMsisdn();
        String operatorName = redirectUrlInfo.getOperatorName();
        String telcoScope = redirectUrlInfo.getTelcoScope();
        String ipAddress = redirectUrlInfo.getIpAddress();
        String prompt = redirectUrlInfo.getPrompt();
        String validationRegex = configurationService.getDataHolder().getMobileConnectConfig().getMsisdn()
                .getValidationRegex();
        boolean isAttrScope = redirectUrlInfo.isAttributeSharingScope();
        boolean isShowTnc = redirectUrlInfo.isShowTnc();

        ScopeParam.MsisdnMismatchResultTypes headerMismatchResult = redirectUrlInfo.getHeaderMismatchResult();
        ScopeParam.HeFailureResults heFailureResult = redirectUrlInfo.getHeFailureResult();
        boolean isShowConsent = redirectUrlInfo.isShowConsent();
        String spType = redirectUrlInfo.getTrustedStatus();
        String attrShareScopeType = redirectUrlInfo.getAttributeSharingScopeType();
        EnumSet<ScopeParam.scopeTypes> scopeTypesList = redirectUrlInfo.getScopeTypesList();

        String transactionId = redirectUrlInfo.getTransactionId();
        boolean isAPIConsent = redirectUrlInfo.isAPIConsent();

        if (authorizeUrl != null) {
            redirectURL = authorizeUrl + queryString + "&" +
                    AuthProxyConstants.OPERATOR + "=" + operatorName + "&" +
                    AuthProxyConstants.TELCO_SCOPE + "=" + telcoScope + "&" +
                    AuthProxyConstants.SHOW_TNC + "=" + isShowTnc + "&" +
                    AuthProxyConstants.HEADER_MISMATCH_RESULT + "=" + headerMismatchResult + "&" +
                    AuthProxyConstants.HE_FAILURE_RESULT + "=" + heFailureResult + "&" +
                    AuthProxyConstants.ATTR_SHARE_SCOPE + "=" + isAttrScope + "&" +
                    AuthProxyConstants.SHOW_CONSENT + "=" + isShowConsent + "&" +
                    AuthProxyConstants.TRUSTED_STATUS + "=" + spType + "&" +
                    AuthProxyConstants.ATTR_SHARE_SCOPE_TYPE + "=" + attrShareScopeType;

            if (msisdnHeader != null && StringUtils.isNotEmpty(msisdnHeader)) {
                redirectURL += "&" + AuthProxyConstants.MSISDN_HEADER + "=" + msisdnHeader;
            }

            if (loginHintMsisdn != null && !StringUtils.isEmpty(loginHintMsisdn)) {
                redirectURL += "&" + AuthProxyConstants.LOGIN_HINT_MSISDN +
                        "=" + loginHintMsisdn;
            }

            // Reconstruct Authorize url with ip address.
            if (ipAddress != null) {
                redirectURL += "&" + AuthProxyConstants.IP_ADDRESS + "=" + ipAddress;
            }

            if (StringUtils.isNotEmpty(transactionId)) {
                redirectURL += "&" + AuthProxyConstants.TRANSACTION_ID +
                        "=" + transactionId;
            }

            if(StringUtils.isNotEmpty(prompt)){
                redirectURL += "&" + AuthProxyConstants.TELCO_PROMPT +
                        "=" + prompt;
            }

            if (StringUtils.isNotEmpty(validationRegex) && !isBackChannelAllowed) {
                redirectURL = redirectURL + "&" + AuthProxyConstants.MSISDN_VALIDATION_REGEX +
                        "=" + validationRegex;
            }

            if(scopeTypesList!=null && !scopeTypesList.isEmpty()){
                redirectURL += "&" + AuthProxyConstants.SCOPE_TYPES + "=";
                boolean init=false;
                for(ScopeParam.scopeTypes scopetype:scopeTypesList){
                    if(init){
                        redirectURL+="-";
                    }
                    redirectURL+=scopetype.name();
                    init=true;
                }
            }

            if(isAPIConsent){
//                log.info("=====================///////////start///////////////==================");
                redirectURL += "&" + AuthProxyConstants.IS_API_CONSENT + "=" + isAPIConsent;
//                boolean init=false;
//                List<String> approvedScopes = redirectUrlInfo.getApprovedScopes();
//                Map<String, String> approveNeededScopes = redirectUrlInfo.getApproveNeededScopes();
//                Iterator it = approveNeededScopes.entrySet().iterator();
//                redirectURL += "&" + AuthProxyConstants.APPROVED_SCOPES + "=";
//                for(String scopes:approvedScopes){
//                    if(init){
//                        redirectURL+="-";
//                    }
//                    redirectURL+=scopes;
//                    init = true;
//                }
//                init = false;
//                redirectURL += "&" + AuthProxyConstants.APPROVE_NEEDED_SCOPES + "=";
//                while (it.hasNext()) {
//                    Map.Entry pair = (Map.Entry)it.next();
//                    System.out.println(pair.getKey() + " = " + pair.getValue().toString().replaceAll("%", "%25").replaceAll(" ", "%20"));
//                    if(init){
//                        redirectURL+="---";
//                    }
//                    ;
//                    redirectURL+=pair.getKey() + "--" + pair.getValue().toString().replaceAll("%", "%25").replaceAll(" ", "%20");
//                    init = true;
//                    it.remove(); // avoids a ConcurrentModificationException
//                }
//                redirectURL += "&" + AuthProxyConstants.APPROVE_ALL_ENABLE + "=" + redirectUrlInfo.isEnableapproveall();
//                log.info("===================//////////stop///////////==================");
            }

        } else {
            String errMsg = "AuthorizeURL could not be found in mobile-connect.xml";
            DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.CONFIGURATION_ERROR,
                    errMsg);

            throw new ConfigurationException(errMsg);
        }
        return redirectURL;
    }

    private String processQueryString(MultivaluedMap<String, String> queryParams, String queryString) {
        for (Entry<String, List<String>> entry : queryParams.entrySet()) {
            queryString = queryString + entry.getKey().toString() + "=" + entry.getValue().get(0) + "&";
        }
        return queryString;
    }

    private boolean isUserExists(String userName) throws RemoteException,
            UserRegistrationAdminServiceUserRegistrationException {
        UserRegistrationAdminService userRegistrationAdminService = new UserRegistrationAdminServiceStub();
        boolean isUserExists = userRegistrationAdminService.isUserExist(userName);
        return isUserExists;
    }

    private void createUserProfile(String username, String operator, String scope)
            throws RemoteException, UserRegistrationAdminServiceUserRegistrationException,
            UserRegistrationAdminServiceIdentityException, UserRegistrationAdminServiceException {
        UserRegistrationAdminService userRegistrationAdminService = new UserRegistrationAdminServiceStub();

        UserFieldDTO[] userFieldDTOs = userRegistrationAdminService.readUserFieldsForUserRegistration
                (AuthProxyConstants.CLAIM);

        for (int count = 0; count < userFieldDTOs.length; count++) {
            if (AuthProxyConstants.OPERATOR_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                userFieldDTOs[count].setFieldValue(operator);
            } else if (AuthProxyConstants.LOA_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                if (scope.equals(AuthProxyConstants.SCOPE_CPI)) {
                    userFieldDTOs[count].setFieldValue("1");
                } else if (scope.equals(AuthProxyConstants.SCOPE_MNV)) {
                    userFieldDTOs[count].setFieldValue("2");
                }

            } else if (AuthProxyConstants.MOBILE_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                userFieldDTOs[count].setFieldValue(username);
            } else {
                userFieldDTOs[count].setFieldValue("");
            }

            if (log.isDebugEnabled()) {
                log.debug("User Fields Value :" + userFieldDTOs[count].getFieldValue() + " : Claim " +
                        userFieldDTOs[count].getClaimUri() + " : Name " +
                        userFieldDTOs[count].getFieldName());
            }
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setOpenID(AuthProxyConstants.SCOPE_OPENID);
        userDTO.setPassword(null);
        userDTO.setUserFields(userFieldDTOs);
        userDTO.setUserName(username);
        userRegistrationAdminService.addUser(userDTO);
    }
}

