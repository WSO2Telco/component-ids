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

import com.google.gson.Gson;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.entity.AuthenticationMessageDetail;
import com.wso2telco.entity.ClientDetails;
import com.wso2telco.entity.FirebaseCloudMessageDetails;
import com.wso2telco.exception.EmptyResultSetException;
import com.wso2telco.util.DBConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.sql.SQLException;

@Path("/pushServiceAPI/")
public class PushServiceAPI {

    private static String fcmUrl = null;
    private static final String MSISDN = "msisdn";
    private static final String DATA = "data";
    private static final String MESSAGE = "message";
    private static final String APPLICATION_NAME = "applicationName";
    private static final String REFERENCE = "referenceID";
    private static final String ACR = "acr";
    private static final String SP_LOGO_URL = "spImgUrl";
    private static String fcmKey = null;
    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";
    private static MobileConnectConfig mobileConnectConfigs = null;
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    private static Log log = LogFactory.getLog(PushServiceAPI.class);
    private DBConnection dbConnection = null;

    static {
        //Load mobile-connect.xml file.
        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
        fcmUrl = mobileConnectConfigs.getSaaConfig().getFcmEndpoint();
        fcmKey = mobileConnectConfigs.getSaaConfig().getFcmKey();
    }

    /**
     * OutBound to Push Service*
     *
     * @param pushMessageData Message details to pass to the fcm
     * @param msisdn          msisdn of the client user
     * @return successful message
     */
    @POST
    @Path("client/{" + MSISDN + "}/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendPushNotification(@PathParam(MSISDN) String msisdn, String pushMessageData) {

        JSONObject pushMessageObj = new JSONObject(pushMessageData);
        JSONObject messageData = pushMessageObj.getJSONObject(DATA);
        JSONObject pushMessageResponse = new JSONObject();
        int[] success_failure;

        try {
            dbConnection = DBConnection.getInstance();
            ClientDetails clientDetails = dbConnection.getClientDetails(msisdn);
            if (clientDetails != null) {
                String pushToken = clientDetails.getPushToken();
                String message = messageData.getString(MESSAGE);
                String applicationName = messageData.getString(APPLICATION_NAME);
                String referenceId = messageData.getString(REFERENCE);
                String acr = messageData.getString(ACR);
                String spImageUrl = messageData.getString(SP_LOGO_URL);

                AuthenticationMessageDetail data = new AuthenticationMessageDetail();
                data.setMsg(message);
                data.setSp(applicationName);
                data.setRef(referenceId);
                data.setAcr(acr);
                data.setSpUrl(spImageUrl);

                FirebaseCloudMessageDetails firebaseCloudMessageDetails = new FirebaseCloudMessageDetails();
                firebaseCloudMessageDetails.setTo(pushToken);
                firebaseCloudMessageDetails.setData(data);

                DefaultHttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(fcmUrl);

                post.setHeader("Authorization", fcmKey);
                post.setHeader("Content-Type", MediaType.APPLICATION_JSON);

                StringEntity requestEntity = new StringEntity(new Gson().toJson(firebaseCloudMessageDetails),
                        ContentType.APPLICATION_JSON);
                post.setEntity(requestEntity);

                HttpResponse httpResponse = client.execute(post);
                success_failure = getJsonObject(httpResponse);

                pushMessageResponse.put(SUCCESS, success_failure[0]);
                pushMessageResponse.put(FAILURE, success_failure[1]);

            } else {
                pushMessageResponse.put(SUCCESS, 0);
                pushMessageResponse.put(FAILURE, 1);
            }
        } catch (SQLException | IOException | DBUtilException | EmptyResultSetException | ClassNotFoundException e) {
            log.error("Error occurred in sending message through Firebase Cloud Messaging Service for the client with" +
                    " MSISDN:" + msisdn + "error:" + e.getMessage());
            pushMessageResponse.put(SUCCESS, 0);
            pushMessageResponse.put(FAILURE, 1);
        }
        return Response.ok(pushMessageResponse.toString(), MediaType.APPLICATION_JSON).build();
    }

    private int[] getJsonObject(HttpResponse response) throws IOException {

        BufferedReader bufferedReader;
        StringBuffer result = new StringBuffer();
        String bufferReaderLine;
        int[] success_failure = new int[2];

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            while ((bufferReaderLine = bufferedReader.readLine()) != null) {
                result.append(bufferReaderLine);
            }

            JSONObject responseObject = new JSONObject(result.toString());
            if (responseObject.get(SUCCESS) != null) {
                success_failure[0] = (int) responseObject.get(SUCCESS);
                success_failure[1] = (int) responseObject.get(FAILURE);
            }
            return success_failure;
        } catch (UnsupportedOperationException | IOException | JSONException e) {
            log.error("Error in reading the FireBase Cloud Messaging Service Response.Error:" + e.getMessage());
            throw new UnsupportedOperationException(e.getMessage());
        }
    }
}


