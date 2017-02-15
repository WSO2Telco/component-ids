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

import com.google.gson.Gson;
import com.wso2telco.entity.Data;
import com.wso2telco.entity.Fcm;
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

@Path("/pushServiceAPI/")
public class PushServiceAPI {

    private Log log = LogFactory.getLog(PushServiceAPI.class);
    private DBConnection dbConnection = null;
    public static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

    /**
     * OutBound to Push Service*
     *
     * @param pushMessageData
     * @return successful message
     * @throws ClassNotFoundException
     */
    @POST
    @Path("client/{msisdn}/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendPushNotification(@PathParam("msisdn") String msisdn, String pushMessageData) throws ClassNotFoundException {

        dbConnection = DBConnection.getInstance();
        JSONObject pushMessageObj = new JSONObject(pushMessageData);
        JSONObject fcmMessageObj;
        int success = 0;
        int failure = 0;
        String platform = pushMessageObj.getString("platform");
        JSONObject messageData = pushMessageObj.getJSONObject("data");
        String clientDetails[] = new String[0];
        String pushMessageResponse;
        try {
            clientDetails = dbConnection.getClientDetails(msisdn);
            String pushToken = clientDetails[2];
            String message = messageData.getString("message");
            String applicationName = messageData.getString("applicationName");
            String referenceId = messageData.getString("ref");
            String acr = messageData.getString("acr");
            String spImageUrl = messageData.getString("spImgUrl");
            String response = null;

            Data data = new Data();
            data.setMsg(message);
            data.setSp(applicationName);
            data.setRef(referenceId);
            data.setAcr(acr);
            data.setSp_url(spImageUrl);

            Fcm fcm = new Fcm();
            fcm.setTo(pushToken);
            fcm.setData(data);

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(FCM_URL);

            StringEntity requestEntity = new StringEntity(new Gson().toJson(fcm), ContentType.APPLICATION_JSON);
            post.setEntity(requestEntity);


            HttpResponse httpResponse = client.execute(post);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((httpResponse.getEntity().getContent())));

            String output;
            StringBuilder stringBuilder = new StringBuilder();
            while ((output = br.readLine()) != null) {
                stringBuilder.append(output);
            }
            pushMessageResponse = "{\"success\" :\"" + success + "\",\"failure\" :\"" + failure + "\"}";
            return Response.ok(pushMessageResponse, MediaType.APPLICATION_JSON).build();
        } catch (IOException e) {
            e.printStackTrace();
            pushMessageResponse = "{\"success\" :\"" + 0 + "\",\"failure\" :\"" + 1 + "\"}";
            return Response.ok(pushMessageResponse, MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            pushMessageResponse = "{\"success\" :\"" + 0 + "\",\"failure\" :\"" + 1 + "\"}";
            return Response.ok(pushMessageResponse, MediaType.APPLICATION_JSON).build();
        } catch (EmptyResultSetException e) {
            pushMessageResponse = "{\"success\" :\"" + 0 + "\",\"failure\" :\"" + 1 + "\"}";
            return Response.ok(pushMessageResponse, MediaType.APPLICATION_JSON).build();
        }
    }
}
