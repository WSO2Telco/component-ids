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
import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.entity.ClientDetails;
import com.wso2telco.entity.Data;
import com.wso2telco.entity.Fcm;
import com.wso2telco.exception.EmptyResultSetException;
import com.wso2telco.util.DBConnection;
import com.wso2telco.util.SslSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.sql.SQLException;

@Path("/pushServiceAPI/")
public class PushServiceAPI {

    private Log log = LogFactory.getLog(PushServiceAPI.class);
    private DBConnection dbConnection = null;
    public static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    private int success = 0;
    private int failure = 1;

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
    public Response sendPushNotification(@PathParam("msisdn") String msisdn, String pushMessageData) {

        JSONObject pushMessageObj = new JSONObject(pushMessageData);
        JSONObject messageData = pushMessageObj.getJSONObject("data");
        String pushMessageResponse;

        try{
            dbConnection = DBConnection.getInstance();
            ClientDetails clientDetails = dbConnection.getClientDetails(msisdn);
            if (clientDetails != null) {
                String pushToken = clientDetails.getPushToken();
                String message = messageData.getString("message");
                String applicationName = messageData.getString("applicationName");
                String referenceId = messageData.getString("ref");
                String acr = messageData.getString("acr");
                String spImageUrl = messageData.getString("spImgUrl");

                Data data = new Data();
                data.setMsg(message);
                data.setSp(applicationName);
                data.setRef(referenceId);
                data.setAcr(acr);
                data.setSp_url(spImageUrl);

                Fcm fcm = new Fcm();
                fcm.setTo(pushToken);
                fcm.setData(data);

                HttpClient client = getNewHttpClient();
                HttpPost post = new HttpPost(FCM_URL);

                post.setHeader("Authorization", "key=AIzaSyCIqO7iVo2djUVRIKh-DUe1kn3zODTzcDg");
                post.setHeader("Content-Type", MediaType.APPLICATION_JSON);

                StringEntity requestEntity = new StringEntity(new Gson().toJson(fcm), ContentType.APPLICATION_JSON);
                post.setEntity(requestEntity);

                HttpResponse httpResponse = client.execute(post);
                getJsonObject(httpResponse);

                pushMessageResponse = "{\"success\" :\"" + success + "\",\"failure\" :\"" + failure + "\"}";
            } else
                pushMessageResponse = "{\"success\" :\"" + 0 + "\",\"failure\" :\"" + 1 + "\"}";
        }
        catch(SQLException | IOException | DBUtilException | EmptyResultSetException |ClassNotFoundException e){
            log.error("Exception " + e);
            pushMessageResponse = "{\"success\" :\"" + 0 + "\",\"failure\" :\"" + 1 + "\"}";
        }
        return Response.ok(pushMessageResponse, MediaType.APPLICATION_JSON).build();
    }

    private void getJsonObject(HttpResponse response) {
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        } catch (UnsupportedOperationException e) {
            log.error("UnsupportedOperationException " + e);
        } catch (IOException e) {
            log.error("IOException " + e);
        }

        StringBuffer result = new StringBuffer();
        String line = "";
        try {
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            log.error("IOException " + e);
        }

        try {
            JSONObject o = new JSONObject(result.toString());
            if (o.get("success") != null) {
                success = (int) o.get("success");
                failure = (int) o.get("failure");
            }
        } catch (JSONException e) {
            log.error("JSONException " + e);
        }
    }

    public HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SslSocketFactory sf = new SslSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams parameters = new BasicHttpParams();
            HttpProtocolParams.setVersion(parameters, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(parameters, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(parameters,
                    registry);

            return new DefaultHttpClient(ccm, parameters);
        } catch (Exception e) {
            log.error("Exception occured " + e);
            return new DefaultHttpClient();
        }
    }
}
