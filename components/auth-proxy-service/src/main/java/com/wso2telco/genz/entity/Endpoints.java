/*
 *   To change this template, choose Tools | Templates
 *   and open the template in the editor.
 */
package com.wso2telco.genz.entity;

import com.google.gdata.util.common.util.Base64DecoderException;
import com.wso2telco.genz.MSISDNDecryption;
import com.wso2telco.genz.model.MSISDNHeader;
import com.wso2telco.genz.model.RedirectUrlQueryParams;
import com.wso2telco.genz.util.ConfigLoader;
import com.wso2telco.genz.util.AuthProxyConstants;
import com.wso2telco.genz.util.DBUtils;
import com.wso2telco.genz.util.DecryptAES;
import com.wso2telco.genz.util.EncryptAES;
import com.wso2telco.genz.util.MobileConnectConfig;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Path("/")
public class Endpoints {
    private static Log log = LogFactory.getLog(com.wso2telco.genz.entity.Endpoints.class);
    private static HashMap<String, MSISDNDecryption> msisdnDecryptors = null;
    private static MobileConnectConfig mobileConnectConfigs = null;
    private static Map<String, List<MSISDNHeader>> operatorsMSISDNHeadersList;

    static {
        try {
            //Load msisdn header properties.
            operatorsMSISDNHeadersList = DBUtils.getOperatorsMSISDNHeaderProperties();
            //Load mobile-connect.xml file.
            mobileConnectConfigs = ConfigLoader.getInstance().getMobileConnectConfig();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/oauth21/authorize1/operator/{operatorName}")
    public void RedirectToAuthorizeEndpoint(@Context HttpServletRequest httpServletRequest, @Context
            HttpServletResponse httpServletResponse, @Context HttpHeaders httpHeaders, @Context UriInfo uriInfo,
                                            @PathParam("operatorName") String operatorName, String jsonBody) throws
                                                                                                             Exception {
        operatorName = operatorName.toLowerCase();
        //Read query params from the header.
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String redirectURL = queryParams.get("redirect_uri").get(0);

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

        // not use this and add the validation.
        List<String> ipAHeaderAppProperty = null;
        String authorizeUrlProperty = null;
        //have to check whether mobile-conncect.xml exists or not.
        if (mobileConnectConfigs != null) {
            //Read Ipheader and authorize url of the operator from mobile-connect.xml
            //String ipHeaderAppProperty = mobileConnectConfigs.getAuthProxy().getIpHeader();
            authorizeUrlProperty = mobileConnectConfigs.getAuthProxy().getAuthorizeURL();
            //ipAHeaderAppProperty = httpHeaders.getRequestHeader(ipHeaderAppProperty);
        } else {
            throw new FileNotFoundException("mobile-connect.xml could not be found");
        }

        RedirectUrlQueryParams redirectUrlQueryParams = new RedirectUrlQueryParams();
        redirectUrlQueryParams.setAuthorizeUrl(authorizeUrlProperty);
        redirectUrlQueryParams.setOperatorName(operatorName);

        if (httpHeaders != null) {
            if (log.isDebugEnabled()) {
                for (String httpHeader : httpHeaders.getRequestHeaders().keySet()) {
                    log.debug("Header:" + httpHeader +
                                      "Value:" + httpHeaders.getRequestHeader(httpHeader));
                }
            }
        }

        msisdn = decryptMSISDN(httpHeaders, operatorName);

        //read msisdn and encrypt it based on operator requirement
        //read ipAddress based on operator requirement.

        Boolean isScopeExists = queryParams.containsKey(AuthProxyConstants.SCOPE);
        String operatorScope = null;

        if (isScopeExists) {
            operatorScope = queryParams.get(AuthProxyConstants.SCOPE).get(0);

            if (operatorScope.equals(AuthProxyConstants.SCOPE_CPI)) {
                boolean isUserExists = isUserExists(msisdn);
                if (!isUserExists) {
                    createUserProfile(msisdn, operatorName, AuthProxyConstants.SCOPE_CPI);
                    queryString = queryString + "isNew=true&";
                }

                // Replace query parameters based on the scope.
                queryParams.putSingle(AuthProxyConstants.SCOPE, AuthProxyConstants.SCOPE_OPENID);
                queryParams.putSingle(AuthProxyConstants.ACR, AuthProxyConstants.ACR_CPI_VALUE);
                queryString = processQueryString(queryParams, queryString);

            } else if (operatorScope.equals(AuthProxyConstants.SCOPE_MNV)) {
                String acr = queryParams.get(AuthProxyConstants.ACR).get(0);

                // Replace query parameters based on the scope.
                queryParams.putSingle(AuthProxyConstants.SCOPE, AuthProxyConstants.SCOPE_OPENID);
                queryString = processQueryString(queryParams, queryString);
                redirectUrlQueryParams.setQueryString(queryString);
                redirectUrlQueryParams.setIpAddress(ipAddress);

                if (authorizeUrlProperty != null) {
                    if (!StringUtils.isEmpty(msisdn)) {
                        if (msisdn.equals(loginHint)) {
                            if (acr.equals("2")) {
                                if (!isUserExists(msisdn)) {
                                    createUserProfile(msisdn, operatorName, AuthProxyConstants.SCOPE_MNV);
                                }
                            }
                            // Encrypt MSISDN
                            //have the opportunity to choose encrypt method.
                            msisdn = EncryptAES.encrypt(msisdn);
                            // URL encode
                            msisdn = URLEncoder.encode(msisdn, AuthProxyConstants.UTF_ENCODER);
                            redirectUrlQueryParams.setMsisdnHeader(msisdn);
                            redirectUrlQueryParams.setTelcoScope("mvn");
                            redirectURL = constructRedirectUrl(redirectUrlQueryParams);
                        } else {
                            if (acr.equals("2")) {
                                redirectUrlQueryParams.setMsisdnHeader(msisdn);
                                redirectUrlQueryParams.setTelcoScope("invalid");
                                redirectURL = constructRedirectUrl(redirectUrlQueryParams);
                            } else if (acr.equals("3")) {
                                redirectUrlQueryParams.setMsisdnHeader(null);
                                redirectUrlQueryParams.setTelcoScope("mvn");
                                redirectURL = constructRedirectUrl(redirectUrlQueryParams);
                            } else {
                                //nop
                            }
                        }
                    } else {
                        redirectUrlQueryParams.setMsisdnHeader(msisdn);
                        redirectUrlQueryParams.setTelcoScope("mvn");
                        redirectUrlQueryParams.setIpAddress(ipAddress);
                        redirectURL = constructRedirectUrl(redirectUrlQueryParams);
                    }
                } else {
                    throw new ConfigurationException("AuthorizeURL could not be found in mobile-connect.xml");
                }
            } else {
                if (!StringUtils.isEmpty(decryptedLoginHint)) {
                    queryParams.putSingle(AuthProxyConstants.LOGIN_HINT, decryptedLoginHint);
                }
                queryString = processQueryString(queryParams, queryString);
            }
        }

        //Reconstruct AuthURL
        if (!operatorScope.equals(AuthProxyConstants.SCOPE_MNV)) {
            // Encrypt MSISDN
            //have the opportunity to choose encrypt method.
            msisdn = EncryptAES.encrypt(msisdn);
            // URL encode
            if (msisdn != null) {
                msisdn = URLEncoder.encode(msisdn, AuthProxyConstants.UTF_ENCODER);
            } else {
                msisdn = "";
            }

            redirectUrlQueryParams.setMsisdnHeader(msisdn);
            redirectUrlQueryParams.setQueryString(queryString);
            redirectUrlQueryParams.setIpAddress(ipAddress);
            redirectUrlQueryParams.setTelcoScope(AuthProxyConstants.SCOPE_OPENID);
            redirectURL = constructRedirectUrl(redirectUrlQueryParams);
        }

        if (log.isDebugEnabled()) {
            log.debug("redirectURL : " + redirectURL);
        }
        httpServletResponse.sendRedirect(redirectURL + "&prompt=login");

    }

    private String decryptMSISDN(HttpHeaders httpHeaders, String operatorName)
            throws ClassNotFoundException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException,
                   IllegalBlockSizeException, Base64DecoderException, NoSuchAlgorithmException, InvalidKeyException,
                   IllegalAccessException, InstantiationException {
        String msisdn = null;
        List<MSISDNHeader> msisdnHeaderList = operatorsMSISDNHeadersList.get(operatorName);

        for (int id = 0; id < msisdnHeaderList.size(); id++) {
            MSISDNHeader msisdnHeader = msisdnHeaderList.get(id);
            String msisdnHeaderName = msisdnHeader.getMsisdnHeaderName();
            if (httpHeaders.getRequestHeader(msisdnHeaderName) != null) {
                msisdn = httpHeaders.getRequestHeader(msisdnHeaderName).get(0);
                boolean isHeaderEncrypted = msisdnHeader.isHeaderEncrypted();
                if (isHeaderEncrypted) {
                    String encryptionKey = msisdnHeader.getHeaderEncryptionKey();
                    String encryptionMethod = msisdnHeader.getHeaderEncryptionMethod();
                    if (!msisdnDecryptors.containsKey(encryptionMethod)) {
                        Class encryptionClass = Class.forName(encryptionMethod);
                        MSISDNDecryption clsInstance = (MSISDNDecryption) encryptionClass.newInstance();
                        msisdnDecryptors.put(encryptionMethod, clsInstance);
                    }
                    msisdn = msisdnDecryptors.get(encryptionMethod).decryptMsisdn(msisdn, encryptionKey);
                }
                break;
            }
        }
        return msisdn;
    }

    private String constructRedirectUrl(RedirectUrlQueryParams redirectUrlQueryParams) throws ConfigurationException {
        String redirectURL = null;
        String authorizeUrl = redirectUrlQueryParams.getAuthorizeUrl();
        String queryString = redirectUrlQueryParams.getQueryString();
        String msisdnHeader = redirectUrlQueryParams.getMsisdnHeader();
        String operatorName = redirectUrlQueryParams.getOperatorName();
        String telcoScope = redirectUrlQueryParams.getTelcoScope();
        String ipAddress = redirectUrlQueryParams.getIpAddress();
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

