/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.saa.service;

import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.entity.ClientDetails;
import com.wso2telco.exception.EmptyResultSetException;
import com.wso2telco.util.DBConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

@Path("/serverAPI/")
public class ServerAPI {

    private Log log = LogFactory.getLog(ServerAPI.class);
    private DBConnection dbConnection = null;
    private ConfigurationService configurationService = new ConfigurationServiceImpl();

    /**
     * InBound from SAA Client*
     *
     * @param clientData client data
     * @param msisdn     mobile number
     * @return int indicating the transaction is success ,failure or error
     */
    @POST
    @Path("api/v1/clients")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerClient(@HeaderParam("msisdn") String msisdn, String clientData) {
        log.info("Calling registerClient API by client with MSISDN: "+msisdn);

        int success = 0;
        int failure = 0;
        String responseMessage = null;
        boolean isAvailable;
        String response = null;
        JSONObject requestInfoObj = new JSONObject(clientData);

        try {
            dbConnection = DBConnection.getInstance();
            String clientDeviceID = requestInfoObj.getString("clientDeviceID");
            String platform = requestInfoObj.getString("platform");
            String pushToken = requestInfoObj.getString("pushToken");

            log.info("Check the client exist for client with msisdn:"+msisdn);
            isAvailable = dbConnection.isExist(msisdn);
            if (isAvailable) {
                dbConnection.addClient(clientDeviceID, platform, pushToken, msisdn);
                success = 1;
                responseMessage = "Device registered";
            } else {
                failure = 1;
                responseMessage = "Device Already registered";
            }
        } catch (Exception e) {
            log.info("Error occurred while checking availability for the client with MSISDN:"+msisdn);
            log.error("Exception Occurred " + e);
            failure = 1;
            responseMessage = "Error in Registration";
        }

        log.info("sending response back");
        response = "{\"success\" :\"" + success + "\",\"failure\" :\"" + failure + "\",\"result\" : {\"message\" :\"" + responseMessage + "\"}}";
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }

    /**
     * InBound from SAA Adapter
     * Request comes from SAA Adapter to authenticate the Service Provider.
     *
     * @param messageDetails message details
     * @param msisdn msisdn of the client
     * @return success or failure indicating the client is authenticated or not to the SAA adapter.
     */
    @POST
    @Path("api/v1/clients/{msisdn}/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateClient(@PathParam("msisdn") String msisdn, String messageDetails) {
        log.info("Calling authenticateClient API by client with MSISDN:"+msisdn);

        int success = 0;
        int failure = 0;
        String responseMessage = null;
        String response;
        JSONObject requestInfoObj = new JSONObject(messageDetails);
        JSONObject pushNotificationApiResponse;
        String refId = requestInfoObj.getString("ref");
        ClientDetails clientDetailsArray = new ClientDetails();
        String pushMessageDetails;

        try {
            dbConnection = DBConnection.getInstance();
            clientDetailsArray = dbConnection.getClientDetails(msisdn);

            if (clientDetailsArray != null) {
                pushMessageDetails = "{\"platform\" :\"" + clientDetailsArray.getPlatform() + "\",\"pushToken\" :\"" + clientDetailsArray.getPushToken() + "\",\"data\" : " + messageDetails + "}";

                dbConnection.authenticateClient(refId, clientDetailsArray.getDeviceId(), messageDetails);

                try {
                    pushNotificationApiResponse = postRequest(msisdn, pushMessageDetails);
                    log.info("pushNotificationAPI " + pushNotificationApiResponse);

                    if (pushNotificationApiResponse.getInt("success") == 1) {
                        dbConnection.updateMessageTable(refId, 'A');
                        success = 1;
                        responseMessage = "Message Pushed";
                    } else {
                        failure = 1;
                        responseMessage = "Invalid Registration";
                    }
                } catch (IOException e) {
                    log.error("IOException Occurred " + e);
                    failure = 1;
                    responseMessage = "Authentication Unsuccessful";
                }
            } else {
                log.info("clientDetailsArray is null");
                failure = 1;
                responseMessage = "Authentication Unsuccessful";
            }

        } catch (ClassNotFoundException | SQLException | DBUtilException | EmptyResultSetException e) {
            log.error("Exception Occurred " + e);
            failure = 1;
            responseMessage = "Authentication Unsuccessful";
        }

        response = "{\"success\" :\"" + success + "\",\"failure\" :\"" + failure + "\",\"result\" : {\"message\" :\"" + responseMessage + "\"}}";
        log.info(response);
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }

    /**
     * Method for post requests
     * Request comes from SAA Adapter to authenticate the Service Provider.
     *
     * @param msisdn             msisdn number of the user
     * @param pushMessageDetails info to pass to FCM
     * @return pushNotificationApiResponse JSON Object including response from FCM
     * @throws IOException on error
     */
    private JSONObject postRequest(String msisdn, String pushMessageDetails) throws IOException {

        String url = configurationService.getDataHolder().getMobileConnectConfig().getSaaConfig()
                .getPushServiceEndpoint().replace("{msisdn}", msisdn);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);

        StringEntity requestEntity = new StringEntity(pushMessageDetails, ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);

        JSONObject pushNotificationApiResponse;

        HttpResponse httpResponse = client.execute(httpPost);
        BufferedReader br = new BufferedReader(
                new InputStreamReader((httpResponse.getEntity().getContent())));

        String output;
        StringBuilder stringBuilder = new StringBuilder();

        while ((output = br.readLine()) != null) {
            stringBuilder.append(output);
        }
        pushNotificationApiResponse = new JSONObject(stringBuilder.toString());
        log.info("pushNotificationApiResponse is " + pushNotificationApiResponse.toString());
        return pushNotificationApiResponse;
    }

    /**
     * InBound from SAA Client
     *
     * @param messageDetails message details
     * @param msisdn msisdn of the client
     * @return success or failure indicating the client is authenticated or not to the SAA Client.
     * @throws ClassNotFoundException on error
     */
    @POST
    @Path("api/v1/clients/{msisdn}/auth_response")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateClientBySAAClient(@PathParam("msisdn") String msisdn, String messageDetails) throws ClassNotFoundException {

        log.info("Calling authenticateClientBySAAClient API by client with MSISDN:"+msisdn);
        JSONObject requestInfoObj = new JSONObject(messageDetails);

        int success = 0;
        int failure = 0;
        String responseMessage;
        String response;

        try {
            dbConnection = DBConnection.getInstance();
            if (requestInfoObj != null) {
                log.info("requestInfoObj is not null");
                int authenticatedStatus = requestInfoObj.getInt("status");
                String refID = requestInfoObj.getString("refId");

                if (authenticatedStatus == 1) {
                    log.info("authentication success");
                    dbConnection.updateMessageTable(refID, 'S');
                    success = 1;
                    responseMessage = "Status Updated";

                } else {
                    log.info("Invalid messageID");
                    failure = 1;
                    responseMessage = "Invalid messageID";
                }

            } else {
                log.info("Error in sending authorization response");
                failure = 1;
                responseMessage = "Error in sending authorization response";
            }
        } catch (SQLException | DBUtilException e) {
            log.info("Error in sending authorization response");
            failure = 1;
            responseMessage = "Error in sending authorization response";
        }

        response = "{\"success\" :\"" + success + "\",\"failure\" :\"" + failure + "\",\"result\" : {\"message\" :\"" + responseMessage + "\"}}";
        log.info("response sent");
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }


    /**
     * InBound from SAA Client
     *
     * @param msisdn MSISDN of the client
     * @return registred or notRegistred indicating the client registred or not in the SAA Server database.
     * @throws ClassNotFoundException ClassNotFound Exception
     * @throws SQLException SQLException
     * @throws DBUtilException DBUtilException
     */
    @GET
    @Path("api/v1/clients/{msisdn}/is_registered")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isRegistered(@PathParam("msisdn") String msisdn) throws ClassNotFoundException, SQLException, DBUtilException {
        dbConnection = DBConnection.getInstance();
        String response = "{\"registered\" :\"" + !dbConnection.isExist(msisdn) + "\"}";
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }

    /**
     * InBound from Service Provider Customer Care
     *
     * @param msisdn MSISDN of the client
     * @return Client instance removed from  the SAA Server database.
     */
    @DELETE
    @Path("api/v1/clients/{msisdn}/unregisterClient")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unregisterClient(@PathParam("msisdn") String msisdn) {

        String responseMessage = null;
        String apiResponse;

        try {
            dbConnection = DBConnection.getInstance();
            boolean isExist = dbConnection.isExist(msisdn);

            if (isExist) {
                responseMessage = "msisdn is not registered in the database";
            } else {
                dbConnection.removeClient(msisdn);
                responseMessage = "SUCCESS";
            }
            apiResponse = "{\"removeClient\" :\"" + responseMessage + "\"}";
        } catch (ClassNotFoundException | SQLException | DBUtilException e) {
            apiResponse = "{\"removeClient\" :\"" + "Error occurred while processing request" + "\"}";
        }
        return Response.ok(apiResponse, MediaType.APPLICATION_JSON).build();
    }
}
