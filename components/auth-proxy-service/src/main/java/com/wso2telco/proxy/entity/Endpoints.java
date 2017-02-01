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
import com.wso2telco.core.config.model.LoginHintFormatDetails;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.proxy.MSISDNDecryption;
import com.wso2telco.proxy.model.AuthenticatorException;
import com.wso2telco.proxy.model.MSISDNHeader;
import com.wso2telco.proxy.model.Operator;
import com.wso2telco.proxy.model.RedirectUrlInfo;
import com.wso2telco.proxy.util.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Path("/")
public class Endpoints {
    private static Log log = LogFactory.getLog(Endpoints.class);
    private static HashMap<String, MSISDNDecryption> msisdnDecryptorsClassObjectMap = null;
    private static MobileConnectConfig mobileConnectConfigs = null;
    private static Map<String, List<MSISDNHeader>> operatorsMSISDNHeadersMap;
    private static Map<String, Operator> operatorPropertiesMap = null;

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

    /**
     * The Constant LOGIN_HINT_SEPARATOR.
     */
    private static final String LOGIN_HINT_SEPARATOR = "|";

    static {
        try {
            //Load mobile-connect.xml file.
            mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
            //Load msisdn header properties.
            operatorsMSISDNHeadersMap = DBUtils.getOperatorsMSISDNHeaderProperties();
            //Load operator properties.
            operatorPropertiesMap = DBUtils.getOperatorProperties();
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
                                            @PathParam("operatorName") String operatorName, String jsonBody) throws Exception {

        log.info("Request processing started from proxy");

        operatorName = operatorName.toLowerCase();
        //Read query params from the header.
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String clientId = queryParams.getFirst("client_id");
        String redirectURL = queryParams.get(AuthProxyConstants.REDIRECT_URI).get(0);
        String scopeName = queryParams.get(AuthProxyConstants.SCOPE).get(0);

        if (!configurationService.getDataHolder().getMobileConnectConfig().isSpValidationDisabled() && !isValidScope(scopeName, clientId)) {
            log.error("Scope [ " + scopeName + " ] is not allowed for client [ " + clientId + " ]");
            redirectURL = redirectURL + "?error=access_denied";
        } else {
            String loginHint = null;
            String decryptedLoginHint = null;
            String ipAddress = null;
            String msisdn = null;
            String queryString = "";

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
                throw new FileNotFoundException("mobile-connect.xml could not be found");
            }

            RedirectUrlInfo redirectUrlInfo = new RedirectUrlInfo();
            redirectUrlInfo.setAuthorizeUrl(authorizeUrlProperty);
            redirectUrlInfo.setOperatorName(operatorName);

            if (httpHeaders != null) {
                if (log.isDebugEnabled()) {
                    for (String httpHeader : httpHeaders.getRequestHeaders().keySet()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Header : " + httpHeader + " Value: " + httpHeaders.getRequestHeader(httpHeader));
                        }
                    }
                }
            }

            msisdn = decryptMSISDN(httpHeaders, operatorName);
            ipAddress = getIpAddress(httpHeaders, operatorName);
            queryParams.putSingle(AuthProxyConstants.PROMPT, AuthProxyConstants.LOGIN);

            //Validate with Scope wise parameters
            validateAndSetScopeParameters(loginHint, msisdn, scopeName, redirectUrlInfo);

            String loginhint_msisdn = null;
            try{
                loginhint_msisdn = retreiveLoginHintMsisdn(loginHint, scopeName);
            } catch(Exception e){
                log.debug("Error retrieving loginhint msisdn : " + e);
            }

            Boolean isScopeExists = queryParams.containsKey(AuthProxyConstants.SCOPE);
            String operatorScopeWithClaims;

            if (isScopeExists) {
                operatorScopeWithClaims = queryParams.get(AuthProxyConstants.SCOPE).get(0);
                //split form space or + sign
                String[] scopeValues = operatorScopeWithClaims.split("\\s+|\\+");

                //  if (Arrays.asList(scopeValues).contains(ScopeConstant.OAUTH20_VALUE_SCOPE)) {

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

                redirectUrlInfo.setMsisdnHeader(msisdn);
                redirectUrlInfo.setLoginhintMsisdn(loginhint_msisdn);
                redirectUrlInfo.setQueryString(queryString);
                redirectUrlInfo.setIpAddress(ipAddress);
                redirectUrlInfo.setTelcoScope(operatorScopeWithClaims);
                redirectURL = constructRedirectUrl(redirectUrlInfo);
            }

            if (log.isDebugEnabled()) {
                log.debug("redirectURL : " + redirectURL);
            }
        }

        httpServletResponse.sendRedirect(redirectURL);
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
     * @throws AuthenticationFailedException
     * @throws ConfigurationException
     * @return MSISDN extracted from login-hint
     */
    private void validateAndSetScopeParameters(String loginHint, String msisdnHeader, String scope,
                                               RedirectUrlInfo redirectUrlInfo)
            throws AuthenticationFailedException, ConfigurationException {
        //TODO: get all scope related params. This should be move to a initialization method or add to cache later
        ScopeParam scopeParam = getScopeParam(scope);
        redirectUrlInfo.setLoginhintMandatory(scopeParam.isLoginHintMandatory());
        redirectUrlInfo.setShowTnc(scopeParam.isTncVisible());
        redirectUrlInfo.setHeaderMismatchResult(scopeParam.getMsisdnMismatchResult());
        redirectUrlInfo.setHeFailureResult(scopeParam.getHeFailureResult());

        if (scopeParam != null) {
            //check login hit existance validation
            if (scopeParam.isLoginHintMandatory()) {
                if (StringUtils.isEmpty(loginHint)) {
                    throw new AuthenticationFailedException("Login Hint parameter cannot be empty");
                }

                if (StringUtils.isNotEmpty(msisdnHeader)) {
                    // check if decryption possible
                    if (log.isDebugEnabled()) {
                        log.debug("Set msisdn from header msisdn_header : " + msisdnHeader);
                    }
                    if (!validateMsisdnFormat(msisdnHeader)) {
                        throw new AuthenticationFailedException(
                                "Invalid msisdn format - " + msisdnHeader);
                    }

                    //validate login hint format
                    validateFormatAndMatchLoginHintWithHeaderMsisdn(loginHint, scopeParam.getLoginHintFormat(),
                            msisdnHeader, scopeParam.getMsisdnMismatchResult());
                }
            }
        }
    }

    /**
     * Get the scope param object
     * @param scope
     * @return
     * @throws AuthenticationFailedException
     * @throws ConfigurationException
     */
    private ScopeParam getScopeParam(String scope) throws AuthenticationFailedException, ConfigurationException {
        Map scopeDetail;
        try {
            scopeDetail = DBUtils.getScopeParams(scope);
        } catch (AuthenticatorException e) {
            throw new AuthenticationFailedException(
                    "Error occurred while getting scope parameters from the database for the scope - " + scope, e);
        }
        if (scopeDetail == null || scopeDetail.isEmpty()) {
            throw new ConfigurationException(
                    "Please configure Scope related Parameters properly in scope_parameter table");
        }

        //set the scope specific params
        return (ScopeParam) scopeDetail.get("params");
    }

    /**
     * Validate msisdn format and matches with the login hint
     *
     * @param loginHint
     * @param loginHintAllowedFormatDetailsList
     * @param plainTextMsisdnHeader
     * @param headerMismatchResult
     * @throws AuthenticationFailedException
     * @return MSISDN extracted from login-hint
     */
    private void validateFormatAndMatchLoginHintWithHeaderMsisdn(String loginHint,
                                                                 List<LoginHintFormatDetails>
                                                                         loginHintAllowedFormatDetailsList,
                                                                 String plainTextMsisdnHeader,
                                                                 ScopeParam.msisdnMismatchResultTypes
                                                                         headerMismatchResult)
            throws AuthenticationFailedException {
        boolean isValidFormatType = false; //msisdn/loginhint should be a either of defined formats

        for (LoginHintFormatDetails loginHintFormatDetails : loginHintAllowedFormatDetailsList) {
            String msisdn = null;
            switch (loginHintFormatDetails.getFormatType()) {
                case PLAINTEXT:
                    if (log.isDebugEnabled()) {
                        log.debug("Plain text login hint : " + msisdn);
                    }
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
                                if (log.isDebugEnabled()) {
                                    log.debug("Decrypted login hint : " + decrypted);
                                }
                                msisdn = decrypted.substring(0, decrypted.indexOf(LOGIN_HINT_SEPARATOR));
                                if (log.isDebugEnabled()) {
                                    log.debug("MSISDN by encrypted login hint : " + msisdn);
                                }
                                isValidFormatType = true;
                                break;
                            } catch (Exception e) {
                                log.error("Error while decrypting login hint : " + loginHint, e);
                            }
                        }
                    } else {
                        isValidFormatType = true;
                        break;
                    }
                case MSISDN:
                    if (StringUtils.isNotEmpty(loginHint)) {
                        if (loginHint.startsWith(LOGIN_HINT_NOENCRYPTED_PREFIX)) {
                            msisdn = loginHint.replace(LOGIN_HINT_NOENCRYPTED_PREFIX, "");
                            if (log.isDebugEnabled()) {
                                log.debug("MSISDN by login hint: " + msisdn);
                            }
                            isValidFormatType = true;
                            break;
                        }
                    } else {
                        isValidFormatType = true;
                        break;
                    }
                default:
                    log.warn("Invalid Login Hint format - " + loginHintFormatDetails.getFormatType());
            }

            //msisdn/loginhint should be a either of defined formats
            if (!isValidFormatType) {
                throw new AuthenticationFailedException(
                        "login hint is malformat");
            }

            if (StringUtils.isNotEmpty(msisdn)) {
                //validate format
                if (validateMsisdnFormat(msisdn)) {
                    if (ScopeParam.msisdnMismatchResultTypes.ERROR_RETURN.equals(headerMismatchResult)) {
                        if (!plainTextMsisdnHeader.equals(msisdn)) {
                            throw new AuthenticationFailedException(
                                    "login hint is not matching with the header msisdn");
                        }
                    }
                } else {
                    throw new AuthenticationFailedException(
                            "login hint is malformat");
                }
            }
        }
    }


    private String retreiveLoginHintMsisdn(String loginHint, String scope)
            throws AuthenticationFailedException, ConfigurationException {
        boolean isValidFormatType = false; //msisdn/loginhint should be a either of defined formats
        String msisdn = null;

        ScopeParam scopeParam = getScopeParam(scope);

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
                                break;
                            } catch (Exception e) {
                                log.error("Error while decrypting login hint : " + loginHint, e);
                            }
                        }
                    } else {
                        isValidFormatType = true;
                        break;
                    }
                case MSISDN:
                    if (StringUtils.isNotEmpty(loginHint)) {
                        if (loginHint.startsWith(LOGIN_HINT_NOENCRYPTED_PREFIX)) {
                            msisdn = loginHint.replace(LOGIN_HINT_NOENCRYPTED_PREFIX, "");
                            isValidFormatType = true;
                            break;
                        }
                    } else {
                        isValidFormatType = true;
                        break;
                    }
                default:
                    log.warn("Invalid Login Hint format - " + loginHintFormatDetails.getFormatType());
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
            String plaintextMsisdnRegex =
                    configurationService.getDataHolder().getMobileConnectConfig().getMsisdn().getValidationRegex();
            return msisdn.matches(plaintextMsisdnRegex);
        }
        return true;
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

    private String getIpAddress(HttpHeaders httpHeaders, String operatorName) {
        String ipAddress = null;
        Operator operatorProperties = operatorPropertiesMap.get(operatorName);
        if (operatorProperties != null) {
            boolean isRequiredIpValidation = operatorProperties.isRequiredIpValidation();
            if (isRequiredIpValidation) {
                String ipHeader = operatorProperties.getIpHeader();
                if (!StringUtils.isEmpty(ipHeader)) {
                    if (httpHeaders.getRequestHeader(ipAddress) != null) {
                        ipAddress = httpHeaders.getRequestHeader(ipAddress).get(0);
                    }
                }
            }
        }
        return ipAddress;
    }

    private String constructRedirectUrl(RedirectUrlInfo redirectUrlInfo) throws ConfigurationException {
        String redirectURL = null;
        String authorizeUrl = redirectUrlInfo.getAuthorizeUrl();
        String queryString = redirectUrlInfo.getQueryString();
        String msisdnHeader = redirectUrlInfo.getMsisdnHeader();
        String loginHintMsisdn = redirectUrlInfo.getLoginhintMsisdn();
        String operatorName = redirectUrlInfo.getOperatorName();
        String telcoScope = redirectUrlInfo.getTelcoScope();
        String ipAddress = redirectUrlInfo.getIpAddress();
        boolean isLoginhintMandatory = redirectUrlInfo.isLoginhintMandatory();
        boolean isShowTnc = redirectUrlInfo.isShowTnc();
        ScopeParam.msisdnMismatchResultTypes headerMismatchResult = redirectUrlInfo.getHeaderMismatchResult();
        ScopeParam.heFailureResults heFailureResult = redirectUrlInfo.getHeFailureResult();

        if (authorizeUrl != null) {
            redirectURL = authorizeUrl + queryString + AuthProxyConstants.MSISDN_HEADER + "=" +
                    msisdnHeader + "&" + AuthProxyConstants.OPERATOR + "=" +
                    operatorName + "&" + AuthProxyConstants.TELCO_SCOPE + "=" + telcoScope + "&" +
                    AuthProxyConstants.LOGIN_HINT_MANDATORY + "=" + isLoginhintMandatory + "&" +
                    AuthProxyConstants.SHOW_TNC + "=" + isShowTnc + "&" + AuthProxyConstants.HEADER_MISMATCH_RESULT +
                    "=" + headerMismatchResult + "&" + AuthProxyConstants.HE_FAILURE_RESULT +
                    "=" + heFailureResult;

            if(loginHintMsisdn != null && !StringUtils.isEmpty(loginHintMsisdn)){
                redirectURL = redirectURL + "&" + AuthProxyConstants.LOGIN_HINT_MSISDN +
                        "=" + loginHintMsisdn;
            }

            // Reconstruct Authorize url with ip address.
            if (ipAddress != null) {
                redirectURL += "&" + AuthProxyConstants.IP_ADDRESS + "=" + ipAddress;
            }
        } else {
            throw new ConfigurationException("AuthorizeURL could not be found in mobile-connect.xml");
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

