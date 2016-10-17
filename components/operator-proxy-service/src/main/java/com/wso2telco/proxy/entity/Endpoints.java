/*
 *   To change this template, choose Tools | Templates
 *   and open the template in the editor.
 */
package com.wso2telco.proxy.entity;

import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;
import com.wso2telco.proxy.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;
import org.wso2.carbon.identity.user.registration.stub.dto.UserDTO;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;


/**
 *  REST Web Service
 *  Wso2Telco
 *
 *
 */
@Path("/")
public class Endpoints {


    private static Log log = LogFactory.getLog(Endpoints.class);

    private static final String OPERATOR_CLAIM_NAME = "http://wso2.org/claims/operator";
    private static final String MOBILE_CLAIM_NAME = "http://wso2.org/claims/mobile";
    private static final String LOA_CLAIM_NAME = "http://wso2.org/claims/loa";
    private static final String CLAIM = "http://wso2.org/claims";
    private static final String SCOPE = "scope";
    private static final String LOGIN_HINT = "login_hint";
    private static final String SCOPE_OPENID = "openid";
    private static final String ACR = "acr_values";
    private static final String ACR_CPI_VALUE = "6";
    private static final String SCOPE_CPI = "cpi";
    private static final String SCOPE_MNV = "mnv";
    private static final String LOA_CPI_VALUE = "1";
    private static final String LOA_MNV_VALUE = "2";
    private static final String OPERATOR_AIRTEL = "airtel";
    private static final String STATE = "state";
    private static final String LOGIN_HINT_ENCRYPTED_PREFIX = "ENCR_MSISDN:";
    private static final String LOGIN_HINT_NOENCRYPTED_PREFIX = "MSISDN:";
    private static final String OIDC_PROMPT_LOGIN = "&prompt=login";



    @GET
    @Path("/oauth2/authorize")
    //@Produces("application/json")
    public void RedirectToAuthorizeEndpoint(@Context HttpServletRequest hsr,
                                            @Context HttpServletResponse response,
                                            @Context HttpHeaders headers,
                                            @Context UriInfo ui,
                                            String jsonBody) throws SQLException, RemoteException, Exception {


        MultivaluedMap<String, String> queryParams = ui.getQueryParameters();

        String queryString = "";
        String redirectURL = queryParams.get("redirect_uri").get(0);


        String msisdn = null;
        String ipAddress = null;
        String wifiLoginHint = "";
        String decryptedLoginHint = null;
        
        if (queryParams.get("login_hint") != null){
                wifiLoginHint = queryParams.get("login_hint").get(0);
        }
        
        if( wifiLoginHint != null ){
                if ((!wifiLoginHint.isEmpty()) && 
                    ( wifiLoginHint.length() != 12) && 
                     !(wifiLoginHint.startsWith(LOGIN_HINT_NOENCRYPTED_PREFIX)) && 
                     !(wifiLoginHint.startsWith(LOGIN_HINT_ENCRYPTED_PREFIX))  ){
                    log.debug("Login Hint..............." + wifiLoginHint);
                    
                    

                    DecryptAES aes = new DecryptAES();

                    String decrptedLoginHint = aes.decrypt(wifiLoginHint);

                    log.debug("decrypted login hint = " + decrptedLoginHint);

                    String[] split = decrptedLoginHint.split("\\+");
                    wifiLoginHint =  split[1];
                    decryptedLoginHint = wifiLoginHint;
                   
                }
        }


        if (headers != null) {
            if (log.isDebugEnabled()) {
                log.debug("----------------------------------------------------------");

            }
            for (String header : headers.getRequestHeaders().keySet()) {
                if (log.isDebugEnabled()) {

                    log.debug("Header:" + header +
                            "Value:" + headers.getRequestHeader(header));

                }
            }

            if (log.isDebugEnabled()) {
                log.debug("----------------------------------------------------------");

            }
        }

        //Read the operator from config
        String operator = FileUtil.getApplicationProperty("operator");

        if (log.isDebugEnabled()) {
            log.debug("Operator : " + operator);
        }

        if(OPERATOR_AIRTEL.equalsIgnoreCase(operator)){

            if (headers.getRequestHeader(FileUtil.getApplicationProperty((String)"msisdn_header_1")) != null) {
                msisdn = (String)headers.getRequestHeader(FileUtil.getApplicationProperty((String)"msisdn_header_1")).get(0);

                if (msisdn != null) {
                    msisdn = this.decryptRadiusOne(msisdn);
                }
                
            } else {
                if (headers.getRequestHeader(FileUtil.getApplicationProperty((String)"msisdn_header_2")) != null) {
                    msisdn = (String)headers.getRequestHeader(FileUtil.getApplicationProperty((String)"msisdn_header_2")).get(0);
                    if (log.isDebugEnabled()) {
                        log.debug("msisdn : " + msisdn );
                    }
                }

                if (msisdn != null) {
                    msisdn = Endpoints.DecryptMsisdnWithRC4(msisdn, FileUtil.getApplicationProperty((String)"key_radius_2"));
                    if (log.isDebugEnabled()) {
                        log.debug("msisdn : " + msisdn );
                    }
                }
            }
        }
        else {

            if (headers.getRequestHeader(FileUtil.getApplicationProperty("msisdn_header")) != null) {
                msisdn = headers.getRequestHeader(FileUtil.getApplicationProperty("msisdn_header")).get(0);
                if (log.isDebugEnabled()) {
                    log.debug("msisdn : " + msisdn );
                }
            } else if (headers.getRequestHeader(FileUtil.getApplicationProperty((String)"msisdn_header_1")) != null) {
                msisdn = (String)headers.getRequestHeader(FileUtil.getApplicationProperty((String)"msisdn_header_1")).get(0);
                msisdn = Endpoints.DecryptMsisdnWithRC4(msisdn, FileUtil.getApplicationProperty((String)"key_radius_2"));

            }

            if (headers.getRequestHeader(FileUtil.getApplicationProperty("ip_header")) != null) {
                ipAddress = headers.getRequestHeader(FileUtil.getApplicationProperty("ip_header")).get(0);
            }
        }
        

        if (queryParams.containsKey(SCOPE) && queryParams.get(SCOPE).get(0).equals(SCOPE_CPI)) {


            boolean isNewUser = createUserProfile(msisdn, operator,SCOPE_CPI);

            for (Entry<String, List<String>> entry : queryParams.entrySet()) {
                if (SCOPE.equalsIgnoreCase(entry.getKey().toString())) {
                    queryString = queryString + entry.getKey().toString() + "=" + SCOPE_OPENID + "&";
                } else if (ACR.equalsIgnoreCase(entry.getKey().toString())) {
                    queryString = queryString + entry.getKey().toString() + "=" + ACR_CPI_VALUE + "&";
                } else {
                    queryString = queryString + entry.getKey().toString() + "=" + entry.getValue().get(0) + "&";
                }
            }

            if(isNewUser ){
                queryString = queryString + "isNew=true&";
            }

        } 
        
        
        else if (queryParams.containsKey(SCOPE) && queryParams.get(SCOPE).get(0).toLowerCase().equals(SCOPE_MNV)){
            
            log.debug("inside MNV");
            
            String loginHint = "";
           
            if (queryParams.get("login_hint") != null){
                loginHint = queryParams.get("login_hint").get(0);
            }
            
            
            String state = queryParams.get(STATE).get(0);
            String acr = queryParams.get(ACR).get(0);
            
            for (Entry<String, List<String>> entry : queryParams.entrySet()) {
                    
                    if (SCOPE.equalsIgnoreCase(entry.getKey().toString())) {
                        queryString = queryString + entry.getKey().toString() + "=" + SCOPE_OPENID + "&";
                    }
                    else {
                        queryString = queryString + entry.getKey().toString() + "=" + entry.getValue().get(0) + "&";
                    }
            }
            
            if(msisdn != null && !msisdn.isEmpty()){
                
                
                if (msisdn.equals(loginHint)){
                    
                   if(acr.equals("2")){
                        boolean isNewUser =  createUserProfile(msisdn, operator,SCOPE_MNV);
                   }
                    
                    msisdn = encrytMSISDN(msisdn);
                    
                    redirectURL = FileUtil.getApplicationProperty("authorizeURL") +
                    queryString +
                    "msisdn_header=" + msisdn + "&" +
                    "ipAddress=" + ipAddress + "&" + "operator=" + operator + "&telco_scope=mnv";

                }
                
                
                else{
                    if (acr.equals("2")){
                    //redirectURL = redirectURL + "?" + "error=" + "access_denied" ;
                    log.debug("mnv failed");
                    redirectURL = FileUtil.getApplicationProperty("authorizeURL") +
                            queryString +
                            "msisdn_header=" + msisdn + "&" +"ipAddress=" + ipAddress +
                            "&" + "operator=" + operator + "&telco_scope=invalid";
                    
                    }else if (acr.equals("3")){
                        log.debug("mnv PIN");
                        //boolean isNewUser =  createUserProfile(loginHint, operator,SCOPE_MNV);
                        
                       
                        redirectURL = FileUtil.getApplicationProperty("authorizeURL") +
                                      queryString +
                                      "msisdn_header=" + "&" +
                                      "ipAddress=" + ipAddress + "&" + "operator=" + operator + "&telco_scope=mnv";
                        
                        log.debug("Redirect URI MNV LOA3= " + redirectURL);
                    }
                    else{
                        //nop
                    }
                }
            }
            else{
                log.debug("offnet scenario");
                msisdn = encrytMSISDN(msisdn);
                redirectURL = FileUtil.getApplicationProperty("authorizeURL") +
                    queryString +
                    "msisdn_header=" + msisdn + "&" +
                    "ipAddress=" + ipAddress + "&" + "operator=" + operator + "&telco_scope=mnv";
            }
            
            
        }
        else {

            for (Entry<String, List<String>> entry : queryParams.entrySet()) {
                
                if (LOGIN_HINT.equalsIgnoreCase(entry.getKey().toString()) && decryptedLoginHint != null) {
                    queryString = queryString + entry.getKey().toString() + "=" + decryptedLoginHint + "&";
                }
                else{
                    queryString = queryString + entry.getKey().toString() + "=" + entry.getValue().get(0) + "&";
                 }
            }
        }


        

        //Reconstruct AuthURL
        if (ipAddress != null && (!queryParams.get(SCOPE).get(0).equals(SCOPE_MNV)) ) {
            msisdn = encrytMSISDN(msisdn);
            redirectURL = FileUtil.getApplicationProperty("authorizeURL") +
                    queryString +
                    "msisdn_header=" + msisdn + "&" +
                    "ipAddress=" + ipAddress + "&" + "operator=" + operator + "&telco_scope=openid";
        } else if (ipAddress == null && (!queryParams.get(SCOPE).get(0).equals(SCOPE_MNV)) ) {
            msisdn = encrytMSISDN(msisdn);
            redirectURL = FileUtil.getApplicationProperty("authorizeURL") +
                    queryString +
                    "msisdn_header=" + msisdn + "&" +
                    "operator=" + operator + "&telco_scope=openid";
        }else{
            //nop
        }



        if (log.isDebugEnabled()) {
            log.debug("redirectURL : " + redirectURL);
        }


        response.sendRedirect(redirectURL + OIDC_PROMPT_LOGIN);

    }
    
 
    public boolean createUserProfile(String username, String operator,String scope)

    {
        boolean isNewUser= false;

		/* reading admin url from application properties */
        String adminURL = FileUtil.getApplicationProperty("isadminurl")
                + UserProfileClaimsConstant.SERVICE_URL;
        if (log.isDebugEnabled()) {
            log.debug(adminURL);
        }
        /* getting user registration admin service */
        UserRegistrationAdminServiceClient userRegistrationAdminServiceClient = new UserRegistrationAdminServiceClient(
                adminURL);


		/* by sending the claim dialects, gets existing claims list */
        UserFieldDTO[] userFieldDTOs = new UserFieldDTO[0];
        try {
            userFieldDTOs = userRegistrationAdminServiceClient
                    .readUserFieldsForUserRegistration(CLAIM);
        } catch (UserRegistrationAdminServiceIdentityException e) {
            log.error("UserRegistrationAdminServiceIdentityException : " + e.getMessage());
        } catch (RemoteException e) {
            log.error("RemoteException : " + e.getMessage());
        }


        for (int count = 0; count < userFieldDTOs.length; count++) {

            if (OPERATOR_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                userFieldDTOs[count].setFieldValue(operator);
            } else if (LOA_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                
                if(scope.equals(SCOPE_CPI)){
                    userFieldDTOs[count].setFieldValue(LOA_CPI_VALUE);
                }
                else if(scope.equals(SCOPE_MNV)){
                    userFieldDTOs[count].setFieldValue(LOA_MNV_VALUE);
                }
                else{
                    //nop
                }
                
            } else if (MOBILE_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                userFieldDTOs[count].setFieldValue(username);
            } else {
                userFieldDTOs[count].setFieldValue("");
            }

            if (log.isDebugEnabled()) {
                log.debug("Value :" + userFieldDTOs[count].getFieldValue() + " : Claim " + userFieldDTOs[count].getClaimUri() + " : Name " + userFieldDTOs[count].getFieldName());

            }
        }


        // setting properties of user DTO
        UserDTO userDTO = new UserDTO();
        userDTO.setOpenID(SCOPE_OPENID);
        userDTO.setPassword(generateRandomPassword());
        userDTO.setUserFields(userFieldDTOs);
        userDTO.setUserName(username);

        // add user DTO to the user registration admin service client
        try {
            userRegistrationAdminServiceClient.addUser(userDTO);

            log.info("user registration successful - " + username);

            isNewUser = true;
        } catch (Exception e) {

            log.error("Error in adding User :" + e.getMessage());


        }

        return isNewUser;


    }

    private String generateRandomPassword() {
        //to be implemented random password later

        return "cY4L3dBf@";
    }


    private String decryptRadiusOne(String ciphertext)  {
        String header = ciphertext;
        byte[] decoded = new byte[0];

        String actualMSISDN ="";
        try {
            decoded = Base64.decode((String) header);

        String keyvalue = FileUtil.getApplicationProperty((String)"key_radius_1");
        byte[] bytesofkey = keyvalue.getBytes("UTF-8");
        MessageDigest md5digest = MessageDigest.getInstance("MD5");
        byte[] hashval = md5digest.digest(bytesofkey);
        SecretKeySpec rc4Key = new SecretKeySpec(hashval, "RC4");
        Cipher rc4Decrypt = Cipher.getInstance("RC4");
        rc4Decrypt.init(2, rc4Key);
        byte[] decryptedText = rc4Decrypt.doFinal(decoded);
        actualMSISDN = new String(decryptedText, "UTF-8");
        } catch (Base64DecoderException e) {
           log.error("decryptR1 Base64DecoderException : " +e.getMessage());
        } catch (NoSuchPaddingException e) {
            log.error("decryptR1 NoSuchPaddingException: " + e.getMessage());
        } catch (BadPaddingException e) {
            log.error("decryptR1 BadPaddingException : " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            log.error("decryptR1 NoSuchAlgorithmException : " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            log.error("decryptR1 IllegalBlockSizeException : " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            log.error("decryptR1 UnsupportedEncodingException : " + e.getMessage());
        } catch (InvalidKeyException e) {
            log.error("decryptR1 InvalidKeyException : " + e.getMessage());
        }
        return actualMSISDN;
    }

    public static String DecryptMsisdnWithRC4(String EncMsisdn, String encryptionKey) {
        String RawText = "";
        byte[] keyArray = new byte[256];
        String key = null;

        try {
            key = encryptionKey;
            keyArray = DatatypeConverter.parseHexBinary(EncMsisdn);

            Cipher c = Cipher.getInstance("RC4");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey.getBytes(), "RC4"));
            byte[] decrypted = c.doFinal(keyArray);
            RawText = new String(decrypted);
            System.out.println("this is decript hexa "+RawText);
        }
        catch (Exception e) {
            log.error("Decrypting with RC4 error : " +e.getMessage());
        }
        return RawText;
    }

    public static String DecryptMsisdnWithRC4(byte[] EncMsisdn, String encryptionKey) {
        String RawText = "";
        byte[] keyArray = new byte[256];
        String key = null;

        try {
            key = encryptionKey;
            Cipher c = Cipher.getInstance("RC4");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey.getBytes(), "RC4"));
            byte[] decrypted = c.doFinal(EncMsisdn);
            RawText = new String(decrypted);
            System.out.println("this is decript "+RawText);
        }
        catch (Exception e) {
          log.error("Decrypting with RC4 error : " +e.getMessage());
        }
        return RawText;
    }

    public static String EncriptMsisdnWithRC4(String EncMsisdn, String encryptionKey) {
        String RawText = "";

        try {

            Cipher c = Cipher.getInstance("RC4");
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptionKey.getBytes(), "RC4"));
            byte[] decrypted = c.doFinal(EncMsisdn.getBytes());
            String a=bytesToHex(decrypted);
            System.out.println("encript hexa"+a);
            RawText = new String(decrypted);
            System.out.println("This is a text"+decrypted.toString());
            DecryptMsisdnWithRC4(decrypted,"passward1234");
        }
        catch (Exception e) {
             log.error("Encripting with RC4 error : " +e.getMessage());
        }
        return RawText;
    }

    private String encrytMSISDN(String msisdn) throws Exception{
        //Encrypt MSISDN
        EncryptAES aes = new EncryptAES();
        msisdn = aes.encrypt(msisdn);

        //URL encode
        msisdn = URLEncoder.encode(msisdn, "UTF-8");
        return msisdn;
    }


    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }



}

