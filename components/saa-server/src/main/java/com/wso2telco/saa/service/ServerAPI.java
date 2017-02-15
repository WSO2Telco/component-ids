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
import com.wso2telco.exception.EmptyResultSetException;
import com.wso2telco.util.DBConnection;
import com.wso2telco.util.PropertyReader;
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
     * @throws ClassNotFoundException on error
     */
    @POST
    @Path("api/v1/clients")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerClient(@HeaderParam("msisdn") String msisdn, String clientData) throws ClassNotFoundException {
        log.info("Inside registerClient API");
        dbConnection = DBConnection.getInstance();
        final int UNREGISTERED_CLIENT = 1;
        final int REGISTRED_CLIENT = 0;
        int success = 0;
        int failure = 0;
        String responseMessage = null;
        int clientAvailability;
        String response = null;
        JSONObject requestInfoObj = new JSONObject(clientData);
        try {
            String clientDeviceID = requestInfoObj.getString("clientDeviceID");
            String platform = requestInfoObj.getString("platform");
            String pushToken = requestInfoObj.getString("pushToken");

            log.info("checking client exists");
            clientAvailability = dbConnection.isExist(msisdn);
            if (clientAvailability == UNREGISTERED_CLIENT) {
                dbConnection.addClient(clientDeviceID, platform, pushToken, msisdn);
                success = 1;
                responseMessage = "Device registered";
            } else if (clientAvailability == REGISTRED_CLIENT) {
                failure = 1;
                responseMessage = "Device Already registered";
            }
        } catch (Exception e) {
            log.info("error occurred while checking availability");
            log.info("Error :" + e.toString());
            failure = 1;
            responseMessage = "Error in Registration";
            log.info(responseMessage, e);
        }

        log.info("sending response back");
        response = "{\"success\" :\"" + success + "\",\"failure\" :\"" + failure + "\",\"result\" : {\"message\" :\"" + responseMessage + "\"}}";
        Response build = Response.ok(response, MediaType.APPLICATION_JSON).build();
        log.info("sent response");
        return build;
    }

    /**
     * InBound from SAA Adapter
     * Request comes from SAA Adapter to authenticate the Service Provider.
     *
     * @param messageDetails message details
     * @return success or failure indicating the client is authenticated or not to the SAA adapter.
     * @throws ClassNotFoundException on error
     */
    @POST
    @Path("api/v1/clients/{msisdn}/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateClient(@PathParam("msisdn") String msisdn, String messageDetails) throws ClassNotFoundException {
        log.info("Inside authenticate Client");
        dbConnection = DBConnection.getInstance();
        int success = 0;
        int failure = 0;
        String responseMessage = null;
        String response;
        boolean authenticationResponse;
        String clientDeviceId;
        String platform;
        String pushToken;
        JSONObject requestInfoObj = new JSONObject(messageDetails);
        JSONObject pushNotificationApiResponse;
        String refId = requestInfoObj.getString("ref");

        String clientDetailsArray[] = null;
        try {
            clientDetailsArray = dbConnection.getClientDetails(msisdn);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (EmptyResultSetException e) {
            e.printStackTrace();
        }
        String pushMessageDetails;

        if (clientDetailsArray != null) {
            clientDeviceId = clientDetailsArray[0];
            platform = clientDetailsArray[1];
            pushToken = clientDetailsArray[2];
            pushMessageDetails = "{\"platform\" :\"" + platform + "\",\"pushToken\" :\"" + pushToken + "\",\"data\" : " + messageDetails + "}";

            authenticationResponse = dbConnection.authenticateClient(refId, clientDeviceId, messageDetails);

            if (authenticationResponse) {

                try {
                    pushNotificationApiResponse = postRequest(msisdn, pushMessageDetails);

                    if (pushNotificationApiResponse.getInt("success") == 1) {
                        dbConnection.updateMessageTable(refId, 'A');
                        success = 1;
                        responseMessage = "Message Pushed";
                    } else {
                        failure = 1;
                        responseMessage = "Invalid Registration";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (authenticationResponse == false) {
                failure = 1;
                responseMessage = "Authentication Unsuccessful";
            }

        } else {
            failure = 1;
            responseMessage = "Authentication Unsuccessful";
        }

        response = "{\"success\" :\"" + success + "\",\"failure\" :\"" + failure + "\",\"result\" : {\"message\" :\"" + responseMessage + "\"}}";
        log.info(response.toString());
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }

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
        return pushNotificationApiResponse;
    }

    /**
     * InBound from SAA Client
     *
     * @param messageDetails message details
     * @return success or failure indicating the client is authenticated or not to the SAA Client.
     * @throws ClassNotFoundException on error
     */
    @POST
    @Path("api/v1/clients/{msisdn}/auth_response")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateClientBySAAClient(@PathParam("msisdn") String msisdn, String messageDetails) throws ClassNotFoundException {
        log.info("Inside auth_response API method");

        dbConnection = DBConnection.getInstance();
        JSONObject requestInfoObj = new JSONObject(messageDetails);
        int success = 0;
        int failure = 0;
        String responceMessage;
        String response;

        int authenticatedStatus = requestInfoObj.getInt("status");
        String refID = requestInfoObj.getString("refId");

        if (authenticatedStatus == 1) {
            log.info("authentication success");
            dbConnection.updateMessageTable(refID, 'S');
            success = 1;
            responceMessage = "Status Updated";

        } else {
            failure = 1;
            responceMessage = "Invalid messageID";
        }

        log.info("sending response back");
        response = "{\"success\" :\"" + success + "\",\"failure\" :\"" + failure + "\",\"result\" : {\"message\" :\"" + responceMessage + "\"}}";
        Response build = Response.ok(response, MediaType.APPLICATION_JSON).build();
        log.info("response sent");
        return build;
    }

    /**
     * InBound from SAA Client
     *
     * @param msisdn MSISDN of the client
     * @return registred or notRegistred indicating the client registred or not in the SAA Server database.
     * @throws ClassNotFoundException on error
     */
    @GET
    @Path("api/v1/clients/{msisdn}/is_registered")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isRegistered(@PathParam("msisdn") String msisdn) throws ClassNotFoundException {
        dbConnection = DBConnection.getInstance();
        int isExist = dbConnection.isExist(msisdn);
        boolean registered = false;
        String response;

        if (isExist == 1) {
            registered = false;
        } else if (isExist == 0) {
            registered = true;
        }

        response = "{\"registered\" :\"" + registered + "\"}";
        return Response.ok(response, MediaType.APPLICATION_JSON).build();
    }

    /**
     * InBound from Service Provider Customer Care
     *
     * @param msisdn MSISDN of the client
     * @return Client instance removed from  the SAA Server database.
     * @throws ClassNotFoundException on error
     */
    @DELETE
    @Path("api/v1/clients/{msisdn}/unregisterClient")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unregisterClient(@PathParam("msisdn") String msisdn) throws ClassNotFoundException {
        dbConnection = DBConnection.getInstance();
        int isExist = dbConnection.isExist(msisdn);
        String responseMessage = null;
        String apiResponse;
        boolean unregister = false;

        if (isExist == 1) {
            responseMessage = "msisdn is not registered in the database";
        } else if (isExist == 0) {
            unregister = dbConnection.removeClient(msisdn);
            if (unregister == true)
                responseMessage = "SUCCESS";
            else
                responseMessage = "FAILURE";
        }
        apiResponse = "{\"removeClient\" :\"" + responseMessage + "\"}";
        return Response.ok(apiResponse, MediaType.APPLICATION_JSON).build();
    }
}
