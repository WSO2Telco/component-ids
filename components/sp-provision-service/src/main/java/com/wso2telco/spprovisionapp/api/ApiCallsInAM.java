/**
 * ****************************************************************************
 * Copyright  (c) 2015-2018, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 * <p>
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package com.wso2telco.spprovisionapp.api;

import com.google.gson.Gson;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.spprovisionapp.exception.HttpResponseIsEmptyException;
import com.wso2telco.spprovisionapp.model.TokenResponse;
import com.wso2telco.spprovisionapp.utils.HttpClientUtil;
import net.iharder.Base64;
import okhttp3.*;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.List;

public class ApiCallsInAM {
    static final Logger logInstance = Logger.getLogger(ApiCallsInAM.class);

    private String amUserCreationAPI, amLoginAPI, amAppCreationAPI, addSubscriptionAPI, password, appName, tokenUrlAm,
            tokenEndpoint;
    private List<Cookie> cookies;
    private CookieStore cookieStore;
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    private static MobileConnectConfig mobileConnectConfigs;

    static {
        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
    }


    public ApiCallsInAM() {
        String host;
        host = mobileConnectConfigs.getSpProvisionConfig().getApiManagerUrl();
        tokenUrlAm = mobileConnectConfigs.getSpProvisionConfig().getAmTokenUrl();
        tokenEndpoint = mobileConnectConfigs.getSpProvisionConfig().getTokenUrl();

        amUserCreationAPI = host + "/store/site/blocks/user/sign-up/ajax/user-add.jag";
        amLoginAPI = host + "/store/site/blocks/user/login/ajax/login.jag";
        amAppCreationAPI = host + "/store/site/blocks/application/application-add/ajax/application-add.jag";
        addSubscriptionAPI = host + "/store/site/blocks/subscription/subscription-add/ajax/subscription-add.jag";
        password = mobileConnectConfigs.getSpProvisionConfig().getDefaultUserPassword();
    }

    private static String postToURLjson(String url, HttpClient httpClient) {
        try {
            HttpPost postRequest = new HttpPost(url);

            postRequest.addHeader("Content-Type", "application/json");
            HttpResponse response = httpClient.execute(postRequest);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            String output;
            StringBuffer totalOutput = new StringBuffer();
            String postResponse;

            while ((output = br.readLine()) != null) {
                totalOutput.append(output);
            }

            postResponse = "{\"httpCode\": " + response.getStatusLine().getStatusCode() + ",\"output\": "
                    + totalOutput.toString() + "}";
            return postResponse;
        } catch (IOException ex) {
            logInstance.error("IOException occured in reading responce from AM APIs:" + ex.getMessage(), ex);
            String postResponse = "{error: true, message: \"" + ex.getMessage() + "\"}";
            return postResponse;
        }
    }

    /*
     * Create new User in AM
     */
    public String createNewUserInAm(String appName, String firstName, String lastName, String devEmail) throws
            IOException {

        String url = amUserCreationAPI + "?allFieldsValues=" + firstName + "%7C" + lastName + "%7C" + devEmail
                + "&username=" + appName + "&password=" + password + "&action=addUser";
        HttpClient httpClient = getNewHttpClient();
        String response = postToURLjson(url, httpClient);
        httpClient.getConnectionManager().shutdown();
        return response;
    }

    /*
     * Login the new User to API
     */
    public String loginToAm(String appName) throws IOException {
        cookieStore = null;


        String url = amLoginAPI + "?username=" + appName + "&password="
                + URLEncoder.encode(password, "UTF-8") + "&action=login";
        DefaultHttpClient httpClient = getNewHttpClient();
        String response = postToURLjson(url, httpClient);
        httpClient.getConnectionManager().shutdown();
        cookieStore = httpClient.getCookieStore();
        return response;
    }

    /*
     * add subscriptions to the newly created app
     */
    /*
     * Service provider get scope
     */

    /*
     * Create application in AM
     */
    public String createApplication(String appName, String description, String callback, String tier)
            throws IOException {

        String url = amAppCreationAPI + "?action=addApplication&application=" + appName + "&tier=" + tier
                + "&description=" + description + "&callbackUrl=" + callback + "";
        DefaultHttpClient httpClient = getNewHttpClient();
        httpClient.setCookieStore(cookieStore);
        String response = postToURLjson(url, httpClient);
        httpClient.getConnectionManager().shutdown();
        return response;
    }

    public String addSubscritions(String userName, String appName, String apiName, String apiVersion,
                                  String apiProvider, String tier) {
        String response = null;
        try {
            response = loginToAm(userName);
            try {
                JSONObject responseJson = new JSONObject(response);
                if (responseJson.has("output")) {
                    JSONObject output = responseJson.getJSONObject("output");
                    if (output.has("error")) {
                        if (output.getBoolean("error")) {
                            return response;
                        }
                    }
                }
            } catch (JSONException e) {
                logInstance.error("Error parsing Add Subscriptions JSON response", e);
            }
        } catch (IOException ex) {
            logInstance.error("IO Exception occurred when trying to login to the user in Add subscription " +
                    "process" + ex.getMessage(), ex);
        }
        String url = addSubscriptionAPI + "?action=addAPISubscription&name=" + apiName + "&version=" + apiVersion
                + "&provider=" + apiProvider + "&tier=" + tier + "&applicationName=" + appName + "";
        DefaultHttpClient httpClient = getNewHttpClient();
        httpClient.setCookieStore(cookieStore);
        response = postToURLjson(url, httpClient);
        httpClient.getConnectionManager().shutdown();
        return response;
    }

    public String getAccessToken(String consumerKey, String consumerSecret) throws IOException {

        return getTokenWithOkHttpClient(consumerKey, consumerSecret);
    }

    private String getTokenWithOkHttpClient(String consumerKey, String consumerSecret) throws IOException {
        OkHttpClient client = HttpClientUtil.getUnsafeOkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials");
        String encoding = Base64.encodeBytes((consumerKey + ":" + consumerSecret).getBytes());
        Request request = new Request.Builder()
                .url(tokenEndpoint)
                .post(body)
                .addHeader("authorization", "Basic " + encoding)
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build();

        Response response = client.newCall(request).execute();
        if (response != null && response.body() != null) {
            TokenResponse tokenResponse = new Gson().fromJson(response.body().string(), TokenResponse.class);
            return tokenResponse.getAccessToken();
        } else {
            throw new HttpResponseIsEmptyException("Response is empty");
        }
    }


    public DefaultHttpClient getNewHttpClient() {
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
        } catch (Exception exception) {
            logInstance.error("Exception" + exception.getMessage(), exception);
            return new DefaultHttpClient();
        }
    }

    private String convertStringToJson(String value) throws JSONException {
        JSONObject jObject = new JSONObject(value);
        String accessToken = jObject.getString("access_token");
        return accessToken;
    }
}
