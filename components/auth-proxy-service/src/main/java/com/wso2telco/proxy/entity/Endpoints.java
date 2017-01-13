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
import com.wso2telco.openid.extension.scope.ScopeConstant;
import com.wso2telco.proxy.MSISDNDecryption;
import com.wso2telco.proxy.model.Operator;
import com.wso2telco.proxy.model.RedirectUrlInfo;
import com.wso2telco.proxy.util.*;
import com.wso2telco.proxy.model.MSISDNHeader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceStub;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceUserRegistrationException;
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
import java.util.Arrays;
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

    static {
        try {
            //Load mobile-connect.xml file.
            mobileConnectConfigs = ConfigLoader.getInstance().getMobileConnectConfig();
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
                                            @PathParam("operatorName") String operatorName, String jsonBody) throws
                                                                                                             Exception {
        operatorName = operatorName.toLowerCase();
        //Read query params from the header.
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String redirectURL = queryParams.get(AuthProxyConstants.REDIRECT_URI).get(0);

        String loginHint = null;
        String decryptedLoginHint = null;
        String ipAddress = null;
        String msisdn = null;
        String queryString = "";
        List<String> loginHintParameter = queryParams.get(AuthProxyConstants.LOGIN_HINT);
        if (loginHintParameter != null) {
            //Read login_hint value from the query params.
            loginHint = loginHintParameter.get(0);
            log.debug("Login Hint = " + loginHint);

            if (!StringUtils.isEmpty(loginHint)) {
                if ((loginHint.length() != 12) &&
                        !(loginHint.startsWith(AuthProxyConstants.LOGIN_HINT_NOENCRYPTED_PREFIX)) &&
                        !(loginHint.startsWith(AuthProxyConstants.LOGIN_HINT_ENCRYPTED_PREFIX))) {
                    String[] decryptedFullLoginHint = DecryptAES.decrypt(loginHint).split("\\+");
                    log.debug("Decrypted login hint = " + decryptedFullLoginHint);
                    decryptedLoginHint = decryptedFullLoginHint[1];
                }
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
                    log.debug("Header:" + httpHeader + "Value:" + httpHeaders.getRequestHeader(httpHeader));
                }
            }
        }

        msisdn = decryptMSISDN(httpHeaders, operatorName);
        ipAddress = getIpAddress(httpHeaders, operatorName);
        queryParams.putSingle(AuthProxyConstants.PROMPT, AuthProxyConstants.LOGIN);

        Boolean isScopeExists = queryParams.containsKey(AuthProxyConstants.SCOPE);
        String operatorScopeWithClaims;

        if (isScopeExists) {
            operatorScopeWithClaims = queryParams.get(AuthProxyConstants.SCOPE).get(0);
            //split form space or + sign  
            String[] scopeValues = operatorScopeWithClaims.split("\\s+|\\+");

            if (Arrays.asList(scopeValues).contains(ScopeConstant.OAUTH20_VALUE_SCOPE)) {

                queryString = processQueryString(queryParams, queryString);

                // Encrypt MSISDN
                msisdn = EncryptAES.encrypt(msisdn);
                // URL encode
                if (msisdn != null) {
                    msisdn = URLEncoder.encode(msisdn, AuthProxyConstants.UTF_ENCODER);
                } else {
                    msisdn = "";
                }
                redirectUrlInfo.setMsisdnHeader(msisdn);
                redirectUrlInfo.setQueryString(queryString);
                redirectUrlInfo.setIpAddress(ipAddress);
                redirectUrlInfo.setTelcoScope(operatorScopeWithClaims);
                redirectURL = constructRedirectUrl(redirectUrlInfo);
            }

//            if (operatorScope.equals(AuthProxyConstants.SCOPE_CPI)) {
//                boolean isUserExists = isUserExists(msisdn);
//                if (!isUserExists) {
//                    createUserProfile(msisdn, operatorName, AuthProxyConstants.SCOPE_CPI);
//                    queryParams.putSingle(AuthProxyConstants.IS_NEW, String.valueOf(true));
//                }
//
//                // Replace query parameters based on the scope.
//                queryParams.putSingle(AuthProxyConstants.SCOPE, AuthProxyConstants.SCOPE_OPENID);
//                queryParams.putSingle(AuthProxyConstants.ACR, "6");
//                queryString = processQueryString(queryParams, queryString);
//
//            } else if (operatorScope.equals(AuthProxyConstants.SCOPE_MNV)) {
//                String acr = queryParams.get(AuthProxyConstants.ACR).get(0);
//
//                // Replace query parameters based on the scope.
//                queryParams.putSingle(AuthProxyConstants.SCOPE, AuthProxyConstants.SCOPE_OPENID);
//                queryString = processQueryString(queryParams, queryString);
//                redirectUrlInfo.setQueryString(queryString);
//                redirectUrlInfo.setIpAddress(ipAddress);
//
//                if (authorizeUrlProperty != null) {
//                    if (!StringUtils.isEmpty(msisdn)) {
//                        if (msisdn.equals(loginHint)) {
//                            if (acr.equals("2")) {
//                                if (!isUserExists(msisdn)) {
//                                    createUserProfile(msisdn, operatorName, AuthProxyConstants.SCOPE_MNV);
//                                }
//                            }
//                            // Encrypt MSISDN
//                            //have the opportunity to choose encrypt method.
//                            msisdn = EncryptAES.encrypt(msisdn);
//                            // URL encode
//                            msisdn = URLEncoder.encode(msisdn, AuthProxyConstants.UTF_ENCODER);
//                            redirectUrlInfo.setMsisdnHeader(msisdn);
//                            redirectUrlInfo.setTelcoScope("mvn");
//                            redirectURL = constructRedirectUrl(redirectUrlInfo);
//                        } else {
//                            if (acr.equals("2")) {
//                                redirectUrlInfo.setMsisdnHeader(msisdn);
//                                redirectUrlInfo.setTelcoScope("invalid");
//                                redirectURL = constructRedirectUrl(redirectUrlInfo);
//                            } else if (acr.equals("3")) {
//                                redirectUrlInfo.setMsisdnHeader(null);
//                                redirectUrlInfo.setTelcoScope("mvn");
//                                redirectURL = constructRedirectUrl(redirectUrlInfo);
//                            } else {
//                               // do nothing.
//                            }
//                        }
//                    } else {
//                        redirectUrlInfo.setMsisdnHeader(msisdn);
//                        redirectUrlInfo.setTelcoScope("mvn");
//                        redirectUrlInfo.setIpAddress(ipAddress);
//                        redirectURL = constructRedirectUrl(redirectUrlInfo);
//                    }
//                } else {
//                    throw new ConfigurationException("AuthorizeURL could not be found in mobile-connect.xml");
//                }
//            } else {
//                if (!StringUtils.isEmpty(decryptedLoginHint)) {
//                    queryParams.putSingle(AuthProxyConstants.LOGIN_HINT, decryptedLoginHint);
//                }
//                queryString = processQueryString(queryParams, queryString);
//            }
        }

        //Reconstruct AuthURL
//        if (!operatorScope.equals(AuthProxyConstants.SCOPE_MNV)) {
//            // Encrypt MSISDN
//            msisdn = EncryptAES.encrypt(msisdn);
//            // URL encode
//            if (msisdn != null) {
//                msisdn = URLEncoder.encode(msisdn, AuthProxyConstants.UTF_ENCODER);
//            } else {
//                msisdn = "";
//            }
//            redirectUrlInfo.setMsisdnHeader(msisdn);
//            redirectUrlInfo.setQueryString(queryString);
//            redirectUrlInfo.setIpAddress(ipAddress);
//            redirectUrlInfo.setTelcoScope(AuthProxyConstants.SCOPE_OPENID);
//            redirectURL = constructRedirectUrl(redirectUrlInfo);
//        }

        if (log.isDebugEnabled()) {
            log.debug("redirectURL : " + redirectURL);
        }
        httpServletResponse.sendRedirect(redirectURL);
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
        String operatorName = redirectUrlInfo.getOperatorName();
        String telcoScope = redirectUrlInfo.getTelcoScope();
        String ipAddress = redirectUrlInfo.getIpAddress();
        if (authorizeUrl != null) {
            redirectURL = authorizeUrl + queryString + AuthProxyConstants.MSISDN_HEADER + "=" +
                    msisdnHeader + "&" + AuthProxyConstants.OPERATOR + "=" +
                    operatorName + "&" + AuthProxyConstants.TELCO_SCOPE + "=" + telcoScope;
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

