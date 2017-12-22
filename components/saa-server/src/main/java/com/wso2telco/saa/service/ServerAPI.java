/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
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
        import com.wso2telco.saaEnums.*;
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

    private static final String MSISDN = "msisdn";
    private static final String CLIENT_DEVICE_ID = "clientDeviceID";
    private static final String PLATFORM = "platform";
    private static final String PUSHTOKEN = "pushToken";
    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";
    private static final String RESULT = "result";
    private static final String MESSAGE = "message";
    private static final String DATA = "data";
    private static final String REFERENCEID = "referenceID";
    private static final String STATUS = "status";
    private static final String REGISTERED = "registered";
    private static final String REMOVE_CLIENT = "removeClient";

    private static Log log = LogFactory.getLog(ServerAPI.class);
    private DBConnection dbConnection = null;
    private ConfigurationService configurationService = new ConfigurationServiceImpl();

    /**
     * InBound from SAA Client
     *
     * @param clientData client data
     * @param msisdn     mobile number
     * @return int indicating the transaction is success ,failure or error
     */
    @POST
    @Path("api/v1/clients")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerClient(@HeaderParam(MSISDN) String msisdn, String clientData) {

        //// TODO: 3/6/17 Remove Success or Failure variables and keep one variable- status
        int success = 0;
        int failure = 0;
        String responseMessage;
        boolean isClientAvailable;
        JSONObject response = new JSONObject();

        //The message passing with the response back to client
        JSONObject messageFromAPI = new JSONObject();
        JSONObject requestInfo = new JSONObject(clientData);

        try {
            dbConnection = DBConnection.getInstance();
            String clientDeviceID = requestInfo.getString(CLIENT_DEVICE_ID);
            String platform = requestInfo.getString(PLATFORM);
            String pushToken = requestInfo.getString(PUSHTOKEN);

            isClientAvailable = dbConnection.isClientExist(msisdn);
            if (!isClientAvailable) {
                dbConnection.addClient(clientDeviceID, platform, pushToken, msisdn);
                success = 1;
                responseMessage = RegisterClientStatus.DEVICE_REGISTERED.toString();
            } else {
                failure = 1;
                responseMessage = RegisterClientStatus.DEVICE_ALREADY_REGISTERED.toString();
            }
        } catch (Exception e) {
            log.error("Error occurred while checking availability for the client with " + msisdn + ": " + e.getMessage());
            failure = 1;
            responseMessage = RegisterClientStatus.ERROR_IN_REGISTRATION.toString();
        }

        messageFromAPI.put(MESSAGE, responseMessage);
        response.put(SUCCESS, success);
        response.put(FAILURE, failure);
        response.put(RESULT, messageFromAPI);

        return Response.ok(response.toString(), MediaType.APPLICATION_JSON).build();

    }

    /**
     * InBound from SAA Adapter
     * Request comes from SAA Adapter to authenticate the Service Provider.
     *
     * @param messageDetails message details
     * @param msisdn         msisdn of the client
     * @return success or failure indicating the client is authenticated or not to the SAA adapter.
     */
    @POST
    @Path("api/v1/clients/{" + MSISDN + "}/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateClient(@PathParam(MSISDN) String msisdn, String messageDetails) {

        //// TODO: 3/6/17 Remove Success or Failure variables and keep one variable- status
        int success = 0;
        int failure = 0;
        String responseMessage;
        JSONObject response = new JSONObject();

        //The message passing with the response back to client
        JSONObject messageFromAPI = new JSONObject();
        JSONObject requestInfo = new JSONObject(messageDetails);
        JSONObject pushNotificationApiResponse;
        String referenceId = requestInfo.getString(REFERENCEID);
        ClientDetails clientDetails;
        JSONObject pushMessageDetails = new JSONObject();

        try {
            dbConnection = DBConnection.getInstance();
            clientDetails = dbConnection.getClientDetails(msisdn);

            if (clientDetails != null) {
                pushMessageDetails.put(PLATFORM, clientDetails.getPlatform());
                pushMessageDetails.put(PUSHTOKEN, clientDetails.getPushToken());
                pushMessageDetails.put(DATA, requestInfo);

                dbConnection.authenticateClient(referenceId, clientDetails.getDeviceId(), messageDetails);

                try {
                    pushNotificationApiResponse = postRequest(msisdn, pushMessageDetails.toString());

                    if (pushNotificationApiResponse.getInt(SUCCESS) == 1) {
                        dbConnection.updateMessageTable(referenceId, 'A');
                        success = 1;
                        responseMessage = AuthenticateClientStatus.MESSAGE_PUSHED.toString();
                    } else {
                        failure = 1;
                        responseMessage = AuthenticateClientStatus.INVALID_REGISTRATION.toString();
                    }
                } catch (IOException e) {
                    log.error("Error occurred while send Authentication Request to client with MSISDN :" + msisdn + ".Error is :" + e.getMessage());
                    failure = 1;
                    responseMessage = AuthenticateClientStatus.AUTHENTICATION_NOT_SUCCESSFULL.toString();
                }
            } else {
                failure = 1;
                responseMessage = AuthenticateClientStatus.AUTHENTICATION_NOT_SUCCESSFULL.toString();
            }

        } catch (ClassNotFoundException | SQLException | DBUtilException | EmptyResultSetException e) {
            log.error("Error occurred while send Authentication Request to client with MSISDN :" + msisdn + ".Error is :" + e.getMessage());
            failure = 1;
            responseMessage = AuthenticateClientStatus.AUTHENTICATION_NOT_SUCCESSFULL.toString();
        }

        messageFromAPI.put(MESSAGE, responseMessage);
        response.put(SUCCESS, success);
        response.put(FAILURE, failure);
        response.put(RESULT, messageFromAPI);

        return Response.ok(response.toString(), MediaType.APPLICATION_JSON).build();
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
                .getPushServiceEndpoint().replace("{" + MSISDN + "}", msisdn);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);

        StringEntity requestEntity = new StringEntity(pushMessageDetails, ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);

        JSONObject pushNotificationApiResponse;

        HttpResponse httpResponse = client.execute(httpPost);
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader((httpResponse.getEntity().getContent())));

        String output;
        StringBuilder stringBuilder = new StringBuilder();

        while ((output = bufferedReader.readLine()) != null) {
            stringBuilder.append(output);
        }
        pushNotificationApiResponse = new JSONObject(stringBuilder.toString());
        return pushNotificationApiResponse;
    }

    /**
     * InBound from SAA Client
     *
     * @param messageDetails message details
     * @param msisdn         msisdn of the client
     * @return success or failure indicating the client is authenticated or not to the SAA Client.
     */
    @POST
    @Path("api/v1/clients/{" + MSISDN + "}/auth_response")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticateClientBySAAClient(@PathParam(MSISDN) String msisdn, String messageDetails) {

        //// TODO: 3/6/17 Remove Success or Failure variables and keep one variable- status
        int success = 0;
        int failure = 0;
        String responseMessage;
        JSONObject response = new JSONObject();
        JSONObject requestInfo = new JSONObject(messageDetails);
        //The message passing with the response back to client
        JSONObject messageFromAPI = new JSONObject();

        try {
            dbConnection = DBConnection.getInstance();
            if (requestInfo != null) {

                int authenticatedStatus = requestInfo.getInt(STATUS);
                String refID = requestInfo.getString(REFERENCEID);

                if (authenticatedStatus == 1) {
                    dbConnection.updateMessageTable(refID, 'S');
                } else {
                    dbConnection.updateMessageTable(refID, 'C');
                }
                success = 1;
                responseMessage = AuthenticateClientBySAAClientStatus.STATUS_UPDATED.toString();

            } else {
                failure = 1;
                responseMessage = AuthenticateClientBySAAClientStatus.ERROR_IN_SENDING_AUTHORIZATION_RESPONSE.toString();
            }
        } catch (SQLException | DBUtilException | ClassNotFoundException e) {
            log.error("Error in sending authorization response for the client with MSISDN:" + msisdn + ".Error:" + e.getMessage());
            failure = 1;
            responseMessage = AuthenticateClientBySAAClientStatus.ERROR_IN_SENDING_AUTHORIZATION_RESPONSE.toString();
        }

        messageFromAPI.put(MESSAGE, responseMessage);
        response.put(SUCCESS, success);
        response.put(FAILURE, failure);
        response.put(RESULT, messageFromAPI);

        return Response.ok(response.toString(), MediaType.APPLICATION_JSON).build();
    }


    /**
     * InBound from SAA Client
     *
     * @param msisdn MSISDN of the client
     * @return registered or not Registered indicating the client registered or not in the SAA Server database.
     */
    @GET
    @Path("api/v1/clients/{" + MSISDN + "}/is_registered")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isClientRegistered(@PathParam(MSISDN) String msisdn) {

        JSONObject response = new JSONObject();
        String responseMessage;

        try {
            dbConnection = DBConnection.getInstance();
            responseMessage = String.valueOf(dbConnection.isClientExist(msisdn));
        } catch (ClassNotFoundException | SQLException | DBUtilException e) {
            log.error("Error in checking Client registration for client with MSISDN:" + msisdn);
            responseMessage = CheckClientRegisteredStatus.ERROR_IN_AUTHORIZING_BY_CLIENT.toString();
        }

        response.put(REGISTERED, responseMessage);
        return Response.ok(response.toString(), MediaType.APPLICATION_JSON).build();
    }

    /**
     * InBound from Service Provider Customer Care
     *
     * @param msisdn MSISDN of the client
     * @return Client instance removed from  the SAA Server database.
     */
    @DELETE
    @Path("api/v1/clients/{" + MSISDN + "}/unregisterClient")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unregisterClient(@PathParam(MSISDN) String msisdn) {

        String responseMessage;
        JSONObject response = new JSONObject();

        try {
            dbConnection = DBConnection.getInstance();
            boolean isClientExist = dbConnection.isClientExist(msisdn);

            if (!isClientExist) {
                responseMessage = UnregisterClientStatus.CLIENT_NOT_REGISTERED_IN_THE_DATABASE.toString();
            } else {
                dbConnection.removeClient(msisdn);
                responseMessage = UnregisterClientStatus.SUCCESSFULLY_REMOVED.toString();
            }

        } catch (ClassNotFoundException | SQLException | DBUtilException e) {
            log.error("Error in removing the client with MSISDN:" + msisdn);
            responseMessage = UnregisterClientStatus.ERROR_IN_REMOVING_CLIENT.toString();
        }

        response.put(REMOVE_CLIENT, responseMessage);
        return Response.ok(response.toString(), MediaType.APPLICATION_JSON).build();
    }
}
