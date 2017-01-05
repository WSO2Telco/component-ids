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
package com.wso2telco;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wso2telco.core.config.model.PinConfig;
import com.wso2telco.cryptosystem.AESencrp;
import com.wso2telco.entity.LoginHistory;
import com.wso2telco.exception.AuthenticatorException;
import com.wso2telco.util.Constants;
import com.wso2telco.util.DbUtil;
import com.wso2telco.util.ReadMobileConnectConfig;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.mgt.stub.UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.utils.DBUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
//import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
//import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
//import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;


// TODO: Auto-generated Javadoc

/**
 * The Class Endpoints.
 */
@Path("/endpoint")
public class Endpoints {

    private static boolean temp = false;

    /**
     * The context.
     */
    //private static final Logger LOG = Logger.getLogger(Endpoints.class.getName());
    @Context
    private UriInfo context;

    /**
     * The success response.
     */
    String successResponse = "\"" + "amountTransaction" + "\"";

    /**
     * The service exception.
     */
    String serviceException = "\"" + "serviceException" + "\"";

    /**
     * The policy exception.
     */
    String policyException = "\"" + "policyException" + "\"";

    /**
     * The error return.
     */
    String errorReturn = "\"" + "errorreturn" + "\"";

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(Endpoints.class);

    /**
     * The ussd no of attempts.
     */
    private static Map<String, Integer> ussdNoOfAttempts = new HashMap<String, Integer>();

    /**
     * constant for the first attempt in LOA3 flow
     */
    private static final int FIRST_ATTEMPT = 1;

    /**
     * Instantiates a new endpoints.
     */
    public Endpoints() {


    }

    /**
     * Ussd receive.
     *
     * @param jsonBody the json body
     * @return the response
     * @throws SQLException  the SQL exception
     * @throws JSONException the JSON exception
     * @throws IOException   Signals that an I/O exception has occurred.
     */
    @POST
    @Path("/ussd/receive")
    @Consumes("application/json")
    @Produces("application/json")
    public Response ussdReceive(String jsonBody) throws SQLException, JSONException, IOException {

        log.info("Json Body" + jsonBody);
        Gson gson = new GsonBuilder().serializeNulls().create();
        org.json.JSONObject jsonObj = new org.json.JSONObject(jsonBody);
        String message = jsonObj.getJSONObject("inboundUSSDMessageRequest").getString("inboundUSSDMessage");
        String sessionID = jsonObj.getJSONObject("inboundUSSDMessageRequest").getString("clientCorrelator");
        String msisdn = extractMsisdn(jsonObj);

        int responseCode = 400;
        String responseString = null;

        String status = null;


        String ussdSessionID = null;
        if (jsonObj.getJSONObject("inboundUSSDMessageRequest").has("sessionID") && !jsonObj.getJSONObject("inboundUSSDMessageRequest").isNull("sessionID")) {
            ussdSessionID = jsonObj.getJSONObject("inboundUSSDMessageRequest").getString("sessionID");
            log.info("####### LOGS  ussdSessionID 01 : " + ussdSessionID);
        }
        ussdSessionID = ((ussdSessionID != null) ? ussdSessionID : "");
        log.info("####### LOGS  ussdSessionID 02 : " + ussdSessionID);

        //USSD 1 = YES
        //USSD 2 = NO
        if (message.equals("1")) {
            status = "Approved";
            responseCode = Response.Status.CREATED.getStatusCode();
            DatabaseUtils.updateStatus(sessionID, status);
        } else {
            status = "Rejected";
            responseCode = Response.Status.BAD_REQUEST.getStatusCode();
            DatabaseUtils.updateStatus(sessionID, status);
        }

        if (responseCode == Response.Status.BAD_REQUEST.getStatusCode()) {
            responseString = "{" + "\"requestError\":" + "{"
                    + "\"serviceException\":" + "{" + "\"messageId\":\"" + "SVC0275" + "\"" + "," + "\"text\":\"" + "Internal server Error" + "\"" + "}"
                    + "}}";
        } else {
            responseString = SendUSSD.getUSSDJsonPayload(msisdn, sessionID, 5, "mtfin", ussdSessionID);
        }

        return Response.status(responseCode).entity(responseString).build();
    }

    @POST
    @Path("/registration/pin/ussd")
    @Consumes("application/json")
    @Produces("application/json")
    public Response registrationPinUssd(String jsonBody) {
        String response = "";

        Gson gson = new GsonBuilder().serializeNulls().create();
        org.json.JSONObject jsonObj = new org.json.JSONObject(jsonBody);
        String pin = jsonObj.getJSONObject("inboundUSSDMessageRequest").getString("inboundUSSDMessage");
        String sessionID = jsonObj.getJSONObject("inboundUSSDMessageRequest").getString("clientCorrelator");

        AuthenticationContext authenticationContext = getAuthenticationContext(sessionID);

        PinConfig pinConfig = (PinConfig) authenticationContext.getProperty(Constants.PIN_CONFIG_OBJECT);

        boolean isValidFormatPin = isValidPinFormat(pin);
        String msisdn = extractMsisdn(jsonObj);

        String ussdSessionId = getUssdSessionId(jsonObj);

        try {
            if (pinConfig.getCurrentStep() == PinConfig.CurrentStep.REGISTRATION) {

                if (!isValidFormatPin) {
                    response = handleInvalidFormat(gson, sessionID, pinConfig, msisdn, ussdSessionId);
                    return Response.status(Response.Status.CREATED).entity(response).build();
                } else {
                    response = getPinConfirmResponse(gson, pin, sessionID, pinConfig, msisdn, ussdSessionId);
                    return Response.status(Response.Status.CREATED).entity(response).build();
                }
            } else {
                pinConfig.setConfirmedPin(pin);

                if (!isValidFormatPin) {
                    response = handleInvalidFormat(gson, sessionID, pinConfig, msisdn, ussdSessionId);
                    return Response.status(Response.Status.CREATED).entity(response).build();
                } else if (!pinConfig.isPinsMatched()) {
                    response = handlePinMismatches(gson, sessionID, pinConfig, msisdn, ussdSessionId);
                    return Response.status(Response.Status.CREATED).entity(response).build();
                } else {
                    response = getPinMatchedResponse(gson, sessionID, msisdn, ussdSessionId);
                    DbUtil.updateRegistrationStatus(sessionID, Constants.STATUS_APPROVED);
                    return Response.status(Response.Status.CREATED).entity(response).build();
                }
            }
        } catch (SQLException e) {
           log.error("Error occurred while updating registration status");
        } catch (AuthenticatorException e) {
            log.error("Error occurred while inserting to the database");
        }
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    private String getPinMatchedResponse(Gson gson, String sessionID, String msisdn, String ussdSessionId) {
        log.info("Pins are matched");

        USSDRequest ussdRequest;
        String response;
        String message = ReadMobileConnectConfig.readUssdConfig(Constants.PIN_REG_SUCCESS);
        ussdRequest = getUssdRequest(msisdn, sessionID, ussdSessionId, Constants.MTFIN, message);
        response = gson.toJson(ussdRequest);

        return response;
    }

    private String getPinConfirmResponse(Gson gson, String pin, String sessionID, PinConfig pinConfig, String msisdn, String ussdSessionId) {
        log.info("Valid pin received");

        USSDRequest ussdRequest;
        String response;
        String ussdMessage = ReadMobileConnectConfig.readUssdConfig(Constants.PIN_CONFIRM_MSG);
        ussdRequest = getUssdRequest(msisdn, sessionID, ussdSessionId, Constants.MTCONT, ussdMessage);

        pinConfig.setCurrentStep(PinConfig.CurrentStep.CONFIRMATION);
        pinConfig.setRegisteredPin(pin);

        response = gson.toJson(ussdRequest);
        return response;
    }

    private String handlePinMismatches(Gson gson, String sessionID, PinConfig pinConfig, String msisdn, String ussdSessionId)
            throws SQLException, AuthenticatorException {
        USSDRequest ussdRequest;
        String response;

        if (pinConfig.getPinMismatchAttempts() < Integer.parseInt(ReadMobileConnectConfig.readUssdConfig("PinMismatchAttempts"))) {
            String ussdMessage = ReadMobileConnectConfig.readUssdConfig(Constants.PIN_MISMATCH);
            ussdRequest = getUssdRequest(msisdn, sessionID, ussdSessionId, Constants.MTCONT, ussdMessage);
            response = gson.toJson(ussdRequest);

            log.info("Pin mismatch detected. Sending retry pin message [ " + ussdMessage + " ]");
        } else {
            String ussdMessage = ReadMobileConnectConfig.readUssdConfig(Constants.ERROR_PIN_MISMATCH_EXCEED);
            ussdRequest = getUssdRequest(msisdn, sessionID, ussdSessionId, Constants.MTFIN, ussdMessage);
            response = gson.toJson(ussdRequest);

            DbUtil.updateRegistrationStatus(sessionID, Constants.STATUS_REJECTED);

            log.info("Maximum attempts reached. Sending access denied message [ " + ussdMessage + " ]");
        }

        pinConfig.incrementPinMistmachAttempts();

        return response;
    }

    private String handleInvalidFormat(Gson gson, String sessionID, PinConfig pinConfig, String msisdn, String ussdSessionId)
            throws SQLException, AuthenticatorException {
        USSDRequest ussdRequest;
        String response;

        if (pinConfig.getInvalidFormatAttempts() < Integer.parseInt(ReadMobileConnectConfig
                .readUssdConfig("InvalidFormatPinAttempts"))) {
            String message = ReadMobileConnectConfig.readUssdConfig(Constants.PIN_INVALID_FORMAT);
            ussdRequest = getUssdRequest(msisdn, sessionID, ussdSessionId, Constants.MTCONT, message);

            log.info("Invalid pin. Sending retry pin message [ " + message + " ]");
        } else {
            String ussdMessage = ReadMobileConnectConfig.readUssdConfig(Constants.ERROR_PIN_FORMAT_EXCEED);
            ussdRequest = getUssdRequest(msisdn, sessionID, ussdSessionId, Constants.MTFIN, ussdMessage);

            DbUtil.updateRegistrationStatus(sessionID, Constants.STATUS_REJECTED);

            log.info("Invalid pin maximum attempts reached. Sending access denied message [ " + ussdMessage + " ]");
        }
        pinConfig.incrementInvalidFormatAttempts();

        response = gson.toJson(ussdRequest);
        return response;
    }

    private String getUssdSessionId(JSONObject jsonObj) {
        String ussdSessionId = null;

        if (jsonObj.getJSONObject("inboundUSSDMessageRequest").has("sessionID")
                && !jsonObj.getJSONObject("inboundUSSDMessageRequest").isNull("sessionID")) {
            ussdSessionId = jsonObj.getJSONObject("inboundUSSDMessageRequest").getString("sessionID");

            log.debug("Ussd session id retrieved [ " + ussdSessionId + " ] ");
        }
        return ussdSessionId;
    }

    private AuthenticationContext getAuthenticationContext(String sessionID) {
        AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(sessionID);
        Object cacheEntryObj = AuthenticationContextCache.getInstance().getValueFromCache(cacheKey);
        return ((AuthenticationContextCacheEntry) cacheEntryObj).getContext();
    }

    private boolean isValidPinFormat(String pin) {

        if (pin.matches("[0-9]+") && pin.length() <= Integer.parseInt(ReadMobileConnectConfig.readUssdConfig(Constants.KEY_MAX_LENGTH))) {
            return true;
        } else {
            return false;
        }
    }

    @POST
    @Path("/registration/ussd")
    @Consumes("application/json")
    @Produces("application/json")
    public Response registrationUssd(String jsonBody) throws SQLException, JSONException, IOException {

        log.info("Json Body" + jsonBody);
        Gson gson = new GsonBuilder().serializeNulls().create();
        org.json.JSONObject jsonObj = new org.json.JSONObject(jsonBody);
        String message = jsonObj.getJSONObject("inboundUSSDMessageRequest").getString("inboundUSSDMessage");
        String sessionID = jsonObj.getJSONObject("inboundUSSDMessageRequest").getString("clientCorrelator");
        String msisdn = extractMsisdn(jsonObj);

        int responseCode = 400;
        String responseString = null;

        String status;

        //USSD 1 = YES
        //USSD 2 = NO
        if (message.equals("1")) {
            log.info("Updating registration status as success");

            status = "Approved";
            responseCode = Response.Status.CREATED.getStatusCode();
            DatabaseUtils.updateRegistrationStatus(sessionID, status);
        } else {
            log.info("Updating registration status as rejected");
            status = "Rejected";
            responseCode = Response.Status.BAD_REQUEST.getStatusCode();
            DatabaseUtils.updateRegistrationStatus(sessionID, status);
        }

        if (responseCode == Response.Status.BAD_REQUEST.getStatusCode()) {
            responseString = "{" + "\"requestError\":" + "{"
                    + "\"serviceException\":" + "{" + "\"messageId\":\"" + "SVC0275" + "\"" + "," + "\"text\":\"" + "Internal server Error" + "\"" + "}"
                    + "}}";
        }/* else {
            responseString = SendUSSD.getUSSDJsonPayload(msisdn, sessionID, 5, "mtfin",ussdSessionID);
        }*/

        return Response.status(responseCode).entity(responseString).build();
    }

    @GET
    @Path("/registration/ussd/status")
    // @Consumes("application/json")
    @Produces("application/json")
    public Response registrationUserStatus(@QueryParam("sessionId") String sessionId, String jsonBody) throws SQLException {

        log.info("Checking user status for session id [ " + sessionId + " ] ");

        String userStatus;
        String responseString;

        userStatus = DatabaseUtils.getUSerStatus(sessionId);

        responseString = "{" + "\"sessionId\":\"" + sessionId + "\"," + "\"status\":\"" + userStatus + "\"" + "}";

        return Response.status(200).entity(responseString).build();
    }

    /**
     * Validate ussd response.
     *
     * @param message       the message
     * @param msisdn        the msisdn
     * @param sessionID     the session id
     * @param ussdSessionID the ussd session id
     * @return the string
     */
    private String validateUSSDResponse(String message, String msisdn, String sessionID, String ussdSessionID) {

        log.info("message : " + message);
        log.info("msisdn : " + msisdn);
        log.info("sessionID : " + sessionID);
        log.info("ussdSessionID : " + ussdSessionID);


        String responseString = null;
        Integer noOfAttempts = ussdNoOfAttempts.get(msisdn);
        if (noOfAttempts == null) {
            ussdNoOfAttempts.put(msisdn, 1);
            noOfAttempts = 0;
        }
        if (noOfAttempts < 2) {//resend USSD request
            responseString = SendUSSD.getUSSDJsonPayload(msisdn, sessionID, noOfAttempts, "mtcont", ussdSessionID);//
            ussdNoOfAttempts.put(msisdn, noOfAttempts + 1);
        }
        return responseString;
    }

    /**
     * Validate pin.
     *
     * @param pin       the pin
     * @param sessionID the session id
     * @param msisdn    the msisdn
     * @return the string
     */
    private String validatePIN(String pin, String sessionID, String msisdn) {

        log.info("pin : " + pin);
        log.info("sessionID : " + sessionID);
        log.info("msisdn : " + msisdn);

        String responseString = null;
        try {
            LoginAdminServiceClient lAdmin = new LoginAdminServiceClient(FileUtil.getApplicationProperty("admin_url"));
            String sessionCookie = lAdmin.authenticate(FileUtil.getApplicationProperty("adminusername"), FileUtil.getApplicationProperty("adminpassword"));
            ClaimManagementClient claimManager = new ClaimManagementClient(FileUtil.getApplicationProperty("admin_url"), sessionCookie);
            String profilePin = claimManager.getPIN(msisdn);
            String hashedUserPin = getHashedPin(pin);
            if (hashedUserPin != null && profilePin != null && profilePin.equals(hashedUserPin)) {
                //success
                return null;
            } else {
                Integer noOfAttempts = DatabaseUtils.readMultiplePasswordNoOfAttempts(sessionID);
                if (noOfAttempts < 2) {//resend USSD
                    responseString = SendUSSD.getJsonPayload(msisdn, sessionID, 2, "mtcont");//send 2 to show retry_message
                    log.info("responseString 01: " + responseString);
                    DatabaseUtils.updateMultiplePasswordNoOfAttempts(sessionID, noOfAttempts + 1);
                } else {//lock user
                    UserIdentityManagementClient identityClient = new UserIdentityManagementClient(FileUtil.getApplicationProperty("admin_url"), sessionCookie);
                    identityClient.lockUser(msisdn);
                    DatabaseUtils.deleteUser(sessionID);
                }
            }
        } catch (AxisFault e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (LoginAuthenticationExceptionException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(Endpoints.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Endpoints.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UserIdentityManagementAdminServiceIdentityMgtServiceExceptionException ex) {
            Logger.getLogger(Endpoints.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException ex) {
            Logger.getLogger(Endpoints.class.getName()).log(Level.SEVERE, null, ex);
        }
        return responseString;
    }


    /**
     * Validate pin.
     *
     * @param pin           the pin
     * @param sessionID     the session id
     * @param msisdn        the msisdn
     * @param ussdSessionID the ussd session id
     * @return the string
     */
    private String validatePIN(String pin, String sessionID, String msisdn, String ussdSessionID) {
        String responseString = null;
        try {
            log.info("####### validatePIN  : ");
            LoginAdminServiceClient lAdmin = new LoginAdminServiceClient(FileUtil.getApplicationProperty("admin_url"));
            String sessionCookie = lAdmin.authenticate(FileUtil.getApplicationProperty("adminusername"), FileUtil.getApplicationProperty("adminpassword"));
            ClaimManagementClient claimManager = new ClaimManagementClient(FileUtil.getApplicationProperty("admin_url"), sessionCookie);
            String profilePin = claimManager.getPIN(msisdn);

            String hashedUserPin = getHashedPin(pin);


            if (hashedUserPin != null && profilePin != null && profilePin.equals(hashedUserPin)) {
                log.info("####### profilePin status : success");
                //success
                return null;
            } else {
                log.info("####### profilePin status : fail");
                Integer noOfAttempts = DatabaseUtils.readMultiplePasswordNoOfAttempts(sessionID);
                if (noOfAttempts < 2) {//resend USSD
                    responseString = SendUSSD.getJsonPayload(msisdn, sessionID, 2, "mtcont", ussdSessionID);//send 2 to show retry_message
                    log.info("####### retry request  : " + responseString);
                    DatabaseUtils.updateMultiplePasswordNoOfAttempts(sessionID, noOfAttempts + 1);
                } else {//lock user
                    //log.info("####### locked user  : ");
                    //UserIdentityManagementClient identityClient = new UserIdentityManagementClient(FileUtil.getApplicationProperty("admin_url"), sessionCookie);


                    String failedStatus = "FAILED_ATTEMPTS";
                    log.info("Updating the databse with session:" + sessionID + " and status: " + failedStatus);
                    DatabaseUtils.updateStatus(sessionID, failedStatus);
                    //DatabaseUtils.updateUSerStatus(sessionID, "FAILED_ATTEMPTS");

                    // identityClient.lockUser(msisdn);
                    DatabaseUtils.deleteUser(sessionID);

                    responseString = SendUSSD.getUSSDJsonPayload(msisdn, sessionID, 9, "mtfin", ussdSessionID);
                }
            }
        } catch (AxisFault e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (LoginAuthenticationExceptionException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(Endpoints.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Endpoints.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException ex) {
            Logger.getLogger(Endpoints.class.getName()).log(Level.SEVERE, null, ex);
        }
        return responseString;
    }

    /**
     * Ussd pin receive.
     *
     * @param jsonBody the json body
     * @return the response
     * @throws SQLException  the SQL exception
     * @throws JSONException the JSON exception
     */
    @POST
    @Path("/ussd/pin")
    @Consumes("application/json")
    @Produces("application/json")
    public Response ussdPinReceive(String jsonBody) throws SQLException, JSONException {
        Gson gson = new GsonBuilder().serializeNulls().create();
        log.info("Json Body pin" + jsonBody);

        org.json.JSONObject jsonObj = new org.json.JSONObject(jsonBody);
        String message = jsonObj.getJSONObject("inboundUSSDMessageRequest").getString("inboundUSSDMessage");
        String sessionID = jsonObj.getJSONObject("inboundUSSDMessageRequest").getString("clientCorrelator");
        String msisdn = extractMsisdn(jsonObj);

        String ussdSessionID = null;

        if (jsonObj.getJSONObject("inboundUSSDMessageRequest").has("sessionID") && !jsonObj.getJSONObject("inboundUSSDMessageRequest").isNull("sessionID")) {

            ussdSessionID = jsonObj.getJSONObject("inboundUSSDMessageRequest").getString("sessionID");

            log.info("####### LOGS  ussdSessionID 01 : " + ussdSessionID);

        }

        ussdSessionID = ((ussdSessionID != null) ? ussdSessionID : "");

        log.info("####### LOGS  ussdSessionID 02 : " + ussdSessionID);

        log.info("message>" + message);
        log.info("sessionID>" + sessionID);


        int responseCode = 400;
//        String responseString = null;
        //validatePIN returns non null value if USSD push should be done again(in case of incorrect PIN)
        String responseString = validatePIN(message, sessionID, msisdn, ussdSessionID);
        if (responseString != null) {
            return Response.status(201).entity(responseString).build();
        }
        String status = null;

        //USSD 1 = YES
        //USSD 2 = NO
        if ((message != null) && (!message.isEmpty())) {
            status = "Approved";
            responseCode = 201;
            DatabaseUtils.updatePinStatus(sessionID, status, message, ussdSessionID);
        } else {
            responseCode = 400;
            status = "Status not updated";
            //nop
        }

        if (responseCode == 400) {
            responseString = "{" + "\"requestError\":" + "{"
                    + "\"serviceException\":" + "{" + "\"messageId\":\"" + "SVC0275" + "\"" + "," + "\"text\":\"" + "Internal server Error" + "\"" + "}"
                    + "}}";
        } else {
            //responseString = "{" + "\"sessionID\":\"" + sessionID + "\","+ "\"status\":\"" + status + "\"" + "}";
            responseString = SendUSSD.getUSSDJsonPayload(msisdn, sessionID, 5, "mtfin", ussdSessionID);
        }
        return Response.status(responseCode).entity(responseString).build();
    }


    /**
     * User status.
     *
     * @param sessionID the session id
     * @param jsonBody  the json body
     * @return the response
     * @throws SQLException the SQL exception
     */
    @GET
    @Path("/ussd/status")
    // @Consumes("application/json")
    @Produces("application/json")
    public Response userStatus(@QueryParam("sessionID") String sessionID, String jsonBody) throws SQLException {

        String userStatus = null;
        String responseString = null;

        userStatus = DatabaseUtils.getUSerStatus(sessionID);

        responseString = "{" + "\"sessionID\":\"" + sessionID + "\","
                + "\"status\":\"" + userStatus + "\"" + "}";


        return Response.status(200).entity(responseString).build();
    }

    /**
     * Extract msisdn.
     *
     * @param jsonObj the json obj
     * @return the string
     * @throws JSONException the JSON exception
     */
    private String extractMsisdn(JSONObject jsonObj) throws JSONException {
        String address = jsonObj.getJSONObject("inboundUSSDMessageRequest").getString("address");
        if (address != null) {
            return address.split(":\\+")[1];
        }
        return null;
    }

    /**
     * Gets the hashed pin.
     *
     * @param pinvalue the pinvalue
     * @return the hashed pin
     */
    private String getHashedPin(String pinvalue) {
        String hashString = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pinvalue.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            hashString = hexString.toString();

        } catch (UnsupportedEncodingException ex) {
            log.info("Error getHashValue");
        } catch (NoSuchAlgorithmException ex) {
            log.info("Error getHashValue");
        }

        return hashString;

    }

    /**
     * Login history.
     *
     * @param userID      the user id
     * @param appID       the app id
     * @param strfromDate the strfrom date
     * @param strtoDate   the strto date
     * @return the response
     * @throws SQLException   the SQL exception
     * @throws ParseException the parse exception
     */
    @GET
    @Path("/login/history")
    // @Consumes("application/json")
    @Produces("application/json")
    public Response loginHistory(@QueryParam("userID") String userID, @QueryParam("appID") String appID, @QueryParam("fromDate") String strfromDate,
                                 @QueryParam("toDate") String strtoDate) throws SQLException, ParseException {

        String userStatus = null;
        String responseString = null;
        Date fromDate = new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse(strfromDate).getTime());
        Date toDate = new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse(strtoDate).getTime());
        List<LoginHistory> lsthistory = DatabaseUtils.getLoginHistory(userID, appID, fromDate, toDate);
        responseString = new Gson().toJson(lsthistory);
        return Response.status(200).entity(responseString).build();
    }

    /**
     * Login apps.
     *
     * @param userID the user id
     * @return the response
     * @throws SQLException   the SQL exception
     * @throws ParseException the parse exception
     */
    @GET
    @Path("/login/applications")
    // @Consumes("application/json")
    @Produces("application/json")
    public Response loginApps(@QueryParam("userID") String userID) throws SQLException, ParseException {

        List<String> lsthistory = DatabaseUtils.getLoginApps(userID);
        String responseString = new Gson().toJson(lsthistory);
        return Response.status(200).entity(responseString).build();
    }

    /**
     * Sms confirm.
     *
     * @param sessionID the session id
     * @return the response
     * @throws SQLException the SQL exception
     */
    @GET
    @Path("/sms/response")
    // @Consumes("application/json")
    @Produces("text/plain")
    public Response smsConfirm(@QueryParam("sessionID") String sessionID) throws SQLException {
        log.info("sessionID 01: " + sessionID);
        String responseString = null;
        String status = null;
        try {
            sessionID = AESencrp.decrypt(sessionID.replaceAll(" ", "+"));
        } catch (Exception e) {
            e.printStackTrace();
            responseString = e.getLocalizedMessage();
            return Response.status(500).entity(responseString).build();
        }
        String userStatus = DatabaseUtils.getUSerStatus(sessionID);
        if (userStatus.equalsIgnoreCase("PENDING")) {
            DatabaseUtils.updateStatus(sessionID, "APPROVED");
            status = "APPROVED";
            responseString = " You are successfully authenticated via mobile-connect";
        } else if (userStatus.equalsIgnoreCase("EXPIRED")) {
            status = "EXPIRED";
            responseString = " You are token expired";
        } else {
            status = "EXPIRED";
            responseString = " You are token already approved";
        }

        responseString = "{" + "\"status\":\"" + status + "\","
                + "\"text\":\"" + responseString + "\"" + "}";

        return Response.status(200).entity(responseString).build();
    }

    /**
     * Mepin confirm.
     *
     * @param identifier        the identifier
     * @param transactionId     the transaction id
     * @param allow             the allow
     * @param transactionStatus the transaction status
     * @return the response
     * @throws SQLException the SQL exception
     */
    @POST
    @Path("/mepin/response")
    @Consumes("application/x-www-form-urlencoded")
    public Response mepinConfirm(@FormParam("identifier") String identifier, @FormParam("transaction_id") String
            transactionId, @FormParam("allow") String allow, @FormParam("transaction_status") String
                                         transactionStatus) throws SQLException {

        log.info("MePIN transactionID: " + transactionId);
        log.info("MePIN identifier: " + identifier);
        log.info("MePIN transactionStatus: " + transactionStatus);

        MePinStatusRequest mePinStatus = new MePinStatusRequest(transactionId);
        FutureTask<String> futureTask = new FutureTask<String>(mePinStatus);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(futureTask);

        return Response.status(200).build();
    }

    private static USSDRequest getUssdContinueRequest(String msisdn, String sessionID, String ussdSessionID, String action) {

        USSDRequest req = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(ReadMobileConnectConfig.readUssdConfig(Constants.SHORT_CODE));
        outboundUSSDMessageRequest.setKeyword(ReadMobileConnectConfig.readUssdConfig(Constants.KEY_WORD));
        outboundUSSDMessageRequest.setSessionID(ussdSessionID);

        outboundUSSDMessageRequest.setOutboundUSSDMessage(ReadMobileConnectConfig.readUssdConfig(Constants.PIN_CONFIRM_MSG));

        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();

        responseRequest.setNotifyURL(ReadMobileConnectConfig.readUssdConfig(Constants.PIN_REG_NOTIFY_URL));
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);


        outboundUSSDMessageRequest.setUssdAction(action);
        req.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);
        return req;
    }

    private static USSDRequest getUssdRequest(String msisdn, String sessionID, String ussdSessionID,
                                              String action, String message) {

        USSDRequest req = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(ReadMobileConnectConfig.readUssdConfig(Constants.SHORT_CODE));
        outboundUSSDMessageRequest.setKeyword(ReadMobileConnectConfig.readUssdConfig(Constants.KEY_WORD));
        outboundUSSDMessageRequest.setSessionID(ussdSessionID);

        outboundUSSDMessageRequest.setOutboundUSSDMessage(ReadMobileConnectConfig.readUssdConfig(message));

        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();

        responseRequest.setNotifyURL(ReadMobileConnectConfig.readUssdConfig(Constants.PIN_REG_NOTIFY_URL));
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);


        outboundUSSDMessageRequest.setUssdAction(action);
        req.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);
        return req;
    }

    private static USSDRequest getUssdInvalidFormatRequest(String msisdn, String sessionID, String ussdSessionID, String action) {

        USSDRequest req = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(ReadMobileConnectConfig.readUssdConfig(Constants.SHORT_CODE));
        outboundUSSDMessageRequest.setKeyword(ReadMobileConnectConfig.readUssdConfig(Constants.KEY_WORD));
        outboundUSSDMessageRequest.setSessionID(ussdSessionID);

        outboundUSSDMessageRequest.setOutboundUSSDMessage(ReadMobileConnectConfig.readUssdConfig(Constants.PIN_INVALID_FORMAT));

        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();

        responseRequest.setNotifyURL(ReadMobileConnectConfig.readUssdConfig(Constants.PIN_REG_NOTIFY_URL));
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);


        outboundUSSDMessageRequest.setUssdAction(action);
        req.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);
        return req;
    }
}
