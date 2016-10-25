/*
 *   To change this template, choose Tools | Templates
 *   and open the template in the editor.
 */
package com.wso2telco.entity;

import com.wso2telco.MSISDNDecription;
import com.wso2telco.model.MSISDNHeader;
import com.wso2telco.util.AuthProxyConstants;
import com.wso2telco.util.ConfigLoader;
import com.wso2telco.util.DBUtils;
import com.wso2telco.util.DecryptAES;
import com.wso2telco.util.EncryptAES;
import com.wso2telco.util.FileUtil;
import com.wso2telco.util.MobileConnectConfig;
import org.apache.axis2.AxisFault;
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
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.wso2telco.util.AuthProxyConstants.CLAIM;
import static com.wso2telco.util.AuthProxyConstants.LOGIN_HINT_NOENCRYPTED_PREFIX;

@Path("/")
public class Endpoints {
    private static Log log = LogFactory.getLog(Endpoints.class);
    private static HashMap<String, MSISDNDecription> msisdnDecryptors = null;
   // DBUtils dbUtils = null;
    private static Map<String, List<MSISDNHeader>> operatorsMSISDNHeadersList;
    public Endpoints() throws SQLException, NamingException {
       // dbUtils = new DBUtils();
        //operatorsMSISDNHeadersList = dbUtils.getOperatorsMSISDNHeaderProperties();
    }

    static {
        try {
            operatorsMSISDNHeadersList = DBUtils
                    .getOperatorsMSISDNHeaderProperties();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/oauth2/authorize/operator/{operatorName}/")
    public void RedirectToAuthorizeEndpoint(@Context HttpServletRequest httpServletRequest, @Context
            HttpServletResponse httpServletResponse, @Context HttpHeaders httpHeaders, @Context UriInfo uriInfo,
                                            @PathParam("operatorName") String operator, String jsonBody) throws
                                                                                                         Exception {
        //Read query params from the header.
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        String redirectURL = queryParams.get("redirect_uri").get(0);

        String loginHint = null;
        String decryptedLoginHint = null;
        String ipAddress = null;
        String msisdn = null;
        String queryString = null;
        List<String> loginHintParameter = queryParams.get(AuthProxyConstants.LOGIN_HINT);
        if (loginHintParameter != null) {
            //Read login_hint value from the query params.
            loginHint = loginHintParameter.get(0);
            log.debug("Login Hint = " + loginHint);
        }

        List<String> ipAHeaderAppProperty = null;
        String authorizeUrlProperty = null;

        //Load mobile-connect.xml file.
        MobileConnectConfig mobileConnectConfigs = ConfigLoader.getInstance().getMobileConnectConfig();
        if (mobileConnectConfigs != null) {
            //Read Ipheader and authorize url of the operator from mobile-connect.xml
            String ipHeaderAppProperty = mobileConnectConfigs.getAuthProxy().getIpHeader();
            authorizeUrlProperty = mobileConnectConfigs.getAuthProxy().getAuthorizeURL();
            ipAHeaderAppProperty = httpHeaders.getRequestHeader(ipHeaderAppProperty);
        } else {
            throw new FileNotFoundException("mobile-connect.xml could not be found");
        }

        if (!StringUtils.isEmpty(loginHint)) {
            //12 ??? should be taken from a database or common space
            if (( loginHint.length() != 12) &&
                    !(loginHint.startsWith(AuthProxyConstants.LOGIN_HINT_NOENCRYPTED_PREFIX)) &&
                    !(loginHint.startsWith(AuthProxyConstants.LOGIN_HINT_ENCRYPTED_PREFIX))  ){
                String[] decryptedFullLoginHint = DecryptAES.decrypt(loginHint).split("\\+");
                log.debug("Decrypted login hint = " + decryptedFullLoginHint);
                decryptedLoginHint = decryptedFullLoginHint[1];

            }
        }

        if (httpHeaders != null) {
            if (log.isDebugEnabled()) {
                for (String httpHeader : httpHeaders.getRequestHeaders().keySet()) {
                    log.debug("Header:" + httpHeader +
                                      "Value:" + httpHeaders.getRequestHeader(httpHeader));
                }
            }
        }

        List<MSISDNHeader> msisdnHeaderList = operatorsMSISDNHeadersList.get(operator.toLowerCase());

        for(MSISDNHeader msisdnHeader: msisdnHeaderList) {
            String msisdnHeaderName = msisdnHeader.getMsisdnHeaderName();
            if (httpHeaders.getRequestHeader(msisdnHeaderName) != null) {
                msisdn = httpHeaders.getRequestHeader(msisdnHeaderName).get(0);
                boolean isHeaderEncrypted = msisdnHeader.isHeaderEncrypted();
                if (isHeaderEncrypted) {
                    String encryptionKey = msisdnHeader.getHeaderEncryptionKey();
                    String encryptionMethod= msisdnHeader.getHeaderEncryptionMethod();
                    if (!msisdnDecryptors.containsKey(encryptionMethod)) {
                        Class encryptionClass = Class.forName(encryptionMethod);
                        MSISDNDecription clsInstance = (MSISDNDecription) encryptionClass.newInstance();
                        msisdnDecryptors.put(encryptionMethod, clsInstance);
                    }
                    msisdn = msisdnDecryptors.get(encryptionMethod).decryptMsisdn(msisdn, encryptionKey);
                }
                break;
            }
        }



        //read msisdn and encrypt it based on operator requirement
        //read ipAddress based on operator requirement.

        Boolean isScopeExists = queryParams.containsKey(AuthProxyConstants.SCOPE);
        String operatorScope = null;

        if (isScopeExists) {
            operatorScope = queryParams.get(AuthProxyConstants.SCOPE).get(0);

            if (operatorScope.equals(AuthProxyConstants.SCOPE_CPI)) {
                boolean isUserExists = isUserExists(msisdn);
                if (!isUserExists) {
                    createUserProfile(msisdn, operator, AuthProxyConstants.SCOPE_CPI);
                    queryString = queryString + "isNew=true&";
                }

                // Replace query parameters based on the scope.
                queryParams.putSingle(AuthProxyConstants.SCOPE, AuthProxyConstants.SCOPE_OPENID);
                queryParams.putSingle(AuthProxyConstants.ACR, AuthProxyConstants.ACR_CPI_VALUE);
                queryString = processQueryString(queryParams, queryString);

            } else if (operatorScope.equals(AuthProxyConstants.SCOPE_MNV)){
                String acr = queryParams.get(AuthProxyConstants.ACR).get(0);

                // Replace query parameters based on the scope.
                queryParams.putSingle(AuthProxyConstants.SCOPE, AuthProxyConstants.SCOPE_OPENID);
                queryString = processQueryString(queryParams, queryString);

                if (authorizeUrlProperty != null) {
                    redirectURL = authorizeUrlProperty + queryString + AuthProxyConstants.IP_ADDRESS + "=" + ipAddress + "&" + AuthProxyConstants
                            .OPERATOR + "=" +
                            operator;
                    if (!StringUtils.isEmpty(msisdn)){
                        if (msisdn.equals(loginHint)) {
                            if (acr.equals("2")) { // 2 should taken from a conf file
                                if (!isUserExists(msisdn)) {
                                    createUserProfile(msisdn, operator, AuthProxyConstants.SCOPE_MNV);
                                }
                            }
                            // Encrypt MSISDN
                            //have the opportunity to choose encrypt method.
                            msisdn = EncryptAES.encrypt(msisdn);
                            // URL encode
                            msisdn = URLEncoder.encode(msisdn, AuthProxyConstants.UTF_ENCODER);

                            redirectURL = redirectURL + "&" + AuthProxyConstants.MSISDN_HEADER + "=" +
                                    msisdn +  "&telco_scope=mnv";
                        } else {
                            if (acr.equals("2")){
                                redirectURL = redirectURL + "&" + AuthProxyConstants.MSISDN_HEADER + "=" +
                                        msisdn + "&telco_scope=invalid";
                            }else if (acr.equals("3")){
                                redirectURL = redirectURL + "&" + AuthProxyConstants.MSISDN_HEADER + "=" +
                                        "&telco_scope=mnv";
                            }
                            else{
                                //nop
                            }
                        }
                    } else {
                        redirectURL = redirectURL + "&" + AuthProxyConstants.MSISDN_HEADER + "=" +
                                "&telco_scope=mnv";
                    }
                } else {
                    throw new ConfigurationException("AuthorizeURL could not be found in mobile-connect.xml");
                }
            } else {
                queryParams.putSingle(AuthProxyConstants.LOGIN_HINT, decryptedLoginHint);
                queryString = processQueryString(queryParams, queryString);
            }
        }

        //Reconstruct AuthURL
        if (!operatorScope.equals(AuthProxyConstants.SCOPE_MNV)) {
            // Encrypt MSISDN
            //have the opportunity to choose encrypt method.
            msisdn = EncryptAES.encrypt(msisdn);
            // URL encode
            msisdn = URLEncoder.encode(msisdn, AuthProxyConstants.UTF_ENCODER);
            //Have to check whether authorize url exists or not in mobile-connect.xml
            if (authorizeUrlProperty != null) {
                redirectURL = authorizeUrlProperty + queryString + AuthProxyConstants.MSISDN_HEADER + "=" +
                        msisdn + "&" +AuthProxyConstants.OPERATOR + "=" +
                        operator + "&" + AuthProxyConstants.TELCO_SCOPE + "=" + AuthProxyConstants.SCOPE_OPENID;
                // Reconstruct Authorize url with ip address.
                if (ipAddress != null) {
                    redirectURL += "&" + AuthProxyConstants.IP_ADDRESS + "=" + ipAddress;
                }
            } else {
                throw new ConfigurationException("AuthorizeURL could not be found in mobile-connect.xml");
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("redirectURL : " + redirectURL);
        }
        httpServletResponse.sendRedirect(redirectURL);

    }

    private String processQueryString(MultivaluedMap<String, String> queryParams, String queryString) {
        for (Entry<String, List<String>> entry : queryParams.entrySet()) {
            queryString = queryString + entry.getKey().toString() + "=" + entry.getValue().get(0) + "&";
        }
        return queryString;
    }

    public boolean isUserExists(String userName)
            throws RemoteException, UserRegistrationAdminServiceUserRegistrationException {
        UserRegistrationAdminService userRegistrationAdminService = new UserRegistrationAdminServiceStub();
        boolean isUserExists = userRegistrationAdminService.isUserExist(userName);
        return isUserExists;
    }
    public void createUserProfile(String username, String operator,String scope)
            throws RemoteException, UserRegistrationAdminServiceUserRegistrationException,
                   UserRegistrationAdminServiceIdentityException, UserRegistrationAdminServiceException {
        UserRegistrationAdminService userRegistrationAdminService = new UserRegistrationAdminServiceStub();
        /* by sending the claim dialects, gets existing claims list */
        //this is not a my comment.
        UserFieldDTO[] userFieldDTOs = userRegistrationAdminService.readUserFieldsForUserRegistration
                (CLAIM);

        for (int count = 0; count < userFieldDTOs.length; count++) {
            if (AuthProxyConstants.OPERATOR_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                userFieldDTOs[count].setFieldValue(operator);
            } else if (AuthProxyConstants.LOA_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                if(scope.equals(AuthProxyConstants.SCOPE_CPI)){
                    userFieldDTOs[count].setFieldValue("1");
                }
                else if(scope.equals(AuthProxyConstants.SCOPE_MNV)){
                    userFieldDTOs[count].setFieldValue("2");
                }

            } else if (AuthProxyConstants.MOBILE_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                userFieldDTOs[count].setFieldValue(username);
            } else {
                userFieldDTOs[count].setFieldValue("");
            }

            if (log.isDebugEnabled()) {
                log.debug("User Fields Value :" + userFieldDTOs[count].getFieldValue() + " : Claim " +
                                  userFieldDTOs[count].getClaimUri() + " : Name " + userFieldDTOs[count].getFieldName());
            }
        }

        // setting properties of user DTO
        UserDTO userDTO = new UserDTO();
        userDTO.setOpenID(AuthProxyConstants.SCOPE_OPENID);
        userDTO.setPassword(null);
        userDTO.setUserFields(userFieldDTOs);
        userDTO.setUserName(username);
        // add user DTO to the user registration admin service client

        userRegistrationAdminService.addUser(userDTO);
    }


}

