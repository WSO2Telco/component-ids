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
package org.wso2telco.pushservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2telco.saaserver.DBConnection.DBConnection;

import javax.ws.rs.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PushServiceAPI {

    private Log log = LogFactory.getLog(PushServiceAPI.class);
    private DBConnection dbConnection = null;

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
        String clientDetails[] = dbConnection.getClientDetails(msisdn);
        String pushToken = clientDetails[2];
        String message = messageData.getString("message");
        String applicationName = messageData.getString("applicationName");
        String referenceId = messageData.getString("ref");
        String acr = messageData.getString("acr");
        String spImageUrl = messageData.getString("spImgUrl");
        String pushMessageResponse;
        String response = null;

        if (platform.equalsIgnoreCase("android")) {
            Client client = ClientBuilder.newClient();
            WebTarget resource = client.target("https://fcm.googleapis.com/fcm/send");
            Invocation.Builder request = resource.request(MediaType.APPLICATION_JSON);
            request.header("Authorization", "key=AIzaSyCIqO7iVo2djUVRIKh-DUe1kn3zODTzcDg");
            request.header("Content-Type", MediaType.APPLICATION_JSON);
            response = request.post(Entity.json("{\"to\": \""+pushToken+"\",\"data\": {\"msg\": \""+message+"\",\"sp\": \""+applicationName+"\",\"ref\":\""+referenceId+"\",\"acr\":\""+acr+"\",\"sp_url\":\""+spImageUrl+"\"}}"), String.class);

        } else if (platform.equalsIgnoreCase("ios")) {
            Client client = ClientBuilder.newClient();
            WebTarget resource = client.target("https://fcm.googleapis.com/fcm/send");
            Invocation.Builder request = resource.request(MediaType.APPLICATION_JSON);
            request.header("Authorization", "key=AIzaSyCIqO7iVo2djUVRIKh-DUe1kn3zODTzcDg");
            request.header("Content-Type", MediaType.APPLICATION_JSON);
            response = request.post(Entity.json("{\"to\": \""+pushToken+"\",\"data\": {\"msg\": \""+message+"\",\"sp\": \""+applicationName+"\",\"ref\":\""+referenceId+"\",\"acr\":\""+acr+"\",\"sp_url\":\""+spImageUrl+"\"}}"), String.class);
        }

        fcmMessageObj = new JSONObject(response);
        success = fcmMessageObj.getInt("success");
        failure = fcmMessageObj.getInt("failure");
        pushMessageResponse = "{\"success\" :\"" + success + "\",\"failure\" :\"" + failure + "\"}";
        return Response.ok(pushMessageResponse, MediaType.APPLICATION_JSON).build();
    }
}
