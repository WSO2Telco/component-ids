/** *****************************************************************************
 * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
 ***************************************************************************** */
package com.wso2telco.serviceprovider.provision.api;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.serviceprovider.provision.util.SpProvisionUtils;
import net.iharder.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class AmApiCalls {
    static final Logger logInstance = Logger.getLogger(AmApiCalls.class);
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    private static MobileConnectConfig mobileConnectConfigs = null;

    private String amUserCreationAPI, amLoginAPI, amAppCreationAPI, addSubscriptionAPI, password, appName, tokenUrlAm;
    private List<Cookie> cookies;
    private CookieStore cookieStore;
    //private Properties popertiesFromPropertyFile;

    static {
        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
    }

    public AmApiCalls(String environment) {
        //this.mobileConnectConfigs = mobileConnectConfig;
        String host = "https://localhost:9444";

        //popertiesFromPropertyFile = propertyFileHandler.popertiesFromPropertyFile();
        if (environment.equalsIgnoreCase("preprod")) {
            //host = popertiesFromPropertyFile.getProperty("host_preprod_AM");
            tokenUrlAm = "https://localhost:9444/token";
                    //popertiesFromPropertyFile.getProperty("token_url_am_preprod");
        } else {
            //host = popertiesFromPropertyFile.getProperty("host_prod_AM");
            //tokenUrlAm = popertiesFromPropertyFile.getProperty("token_url_am_prod");
        }

        amUserCreationAPI = host + "/store/site/blocks/user/sign-up/ajax/user-add.jag";
        amLoginAPI = host + "/store/site/blocks/user/login/ajax/login.jag";
        amAppCreationAPI = host + "/store/site/blocks/application/application-add/ajax/application-add.jag";
        addSubscriptionAPI = host + "/store/site/blocks/subscription/subscription-add/ajax/subscription-add.jag";
        password = "admin";
                //popertiesFromPropertyFile.getProperty("default_password");
    }

    /*
     * Create new User in AM
     */
    public String createNewUserInAm(String appName, String firstName, String lastName, String devEmail) throws IOException {

        String url = amUserCreationAPI + "?allFieldsValues=" + firstName + "%7C" + lastName + "%7C" + devEmail + "&username=" + appName + "&password=" + password + "&action=addUser";
        HttpClient httpClient = SpProvisionUtils.getNewHttpClient();
        String response = postToURLjson(url, httpClient);
        httpClient.getConnectionManager().shutdown();
        return response;
    }

    /*
     * Login the new User to API
     */
    public String loginToAm(String appName) throws IOException {
        cookieStore = null;
        String url = amLoginAPI + "?username=" + appName + "&password=" + password + "&action=login";
        DefaultHttpClient httpClient = SpProvisionUtils.getNewHttpClient();
        String response = postToURLjson(url, httpClient);
        httpClient.getConnectionManager().shutdown();
        cookieStore = httpClient.getCookieStore();
        return response;
    }

    /*
     * Create application in AM
     */
    public String createApplication(String appName, String description, String callback, String tier) throws IOException {

        String url = amAppCreationAPI + "?action=addApplication&application=" + appName + "&tier=" + tier + "&description=" + description + "&callbackUrl=" + callback + "";
        DefaultHttpClient httpClient = SpProvisionUtils.getNewHttpClient();
        httpClient.setCookieStore(cookieStore);
        String response = postToURLjson(url, httpClient);
        httpClient.getConnectionManager().shutdown();
        return response;
    }

    /*
     * add subscriptions to the newly created app
     */
    public String addSubscritions(String userName, String appName, String apiName, String apiVersion, String apiProvider, String tier) {

        try {
            loginToAm(userName);
        } catch (IOException ex) {
            logInstance.error("IO Exception occured when trying to login to the user in Add subscription process" + ex.toString(),ex);
        }
        String url = addSubscriptionAPI + "?action=addAPISubscription&name=" + apiName + "&version=" + apiVersion + "&provider=" + apiProvider + "&tier=" + tier + "&applicationName=" + appName + "";
        DefaultHttpClient httpClient = SpProvisionUtils.getNewHttpClient();
        httpClient.setCookieStore(cookieStore);
        String response = postToURLjson(url, httpClient);
        httpClient.getConnectionManager().shutdown();
        return response;
    }

    /*
     * Service provider get scope
     */
    public String getAccessToken(String consumerKey, String consumerSecret) {

        String accessToken;
        String url = tokenUrlAm + "?grant_type=client_credentials";
        DefaultHttpClient httpClient = SpProvisionUtils.getNewHttpClient();
        httpClient.setCookieStore(cookieStore);
        accessToken = postToURLurlEncoded(url, httpClient, consumerKey, consumerSecret);
        httpClient.getConnectionManager().shutdown();
        return accessToken;
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

            postResponse = "{\"httpCode\": " + response.getStatusLine().getStatusCode() + ",\"output\": " + totalOutput.toString() + "}";
            return postResponse;
        } catch (IOException ex) {
            logInstance.error("IOException occured in reading responce from AM APIs:" + ex.toString(),ex);
            String postResponse = "{\"exception\": " + ex.toString() + "\"}";
            return postResponse;
        }
    }

    private String postToURLurlEncoded(String url, HttpClient httpClient, String consumerKey, String consumerSecret) {
        String postResponse;
        try {

            DefaultHttpClient Client = new DefaultHttpClient();

            HttpPost postRequest = new HttpPost(url);
            String encoding = Base64.encodeBytes((consumerKey + ":" + consumerSecret).getBytes());
            postRequest.addHeader("Authorization", "Basic " + encoding);
            postRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
            HttpResponse response = httpClient.execute(postRequest);

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            String output;
            StringBuffer totalOutput = new StringBuffer();

            while ((output = br.readLine()) != null) {
                totalOutput.append(output);
            }

            postResponse = convertStringToJson(totalOutput.toString());

        } catch (IOException ex) {
            logInstance.error("IOException occured in reading responce from AM APIs:" + ex.getMessage(), ex);
            postResponse = "{\"exception\": " + ex.toString() + "\"}";
        } catch (JSONException ex) {
            logInstance.error("JSONException occured in reading responce from AM APIs:" + ex.getMessage(),ex);
            postResponse = "{\"exception\": " + ex.toString() + "\"}";
        }
        return postResponse;
    }

//    public DefaultHttpClient getNewHttpClient() {
//        try {
//            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            trustStore.load(null, null);
//
//            SslSocketFactory sf = new SslSocketFactory(trustStore);
//            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//
//            HttpParams parameters = new BasicHttpParams();
//            HttpProtocolParams.setVersion(parameters, HttpVersion.HTTP_1_1);
//            HttpProtocolParams.setContentCharset(parameters, HTTP.UTF_8);
//
//            SchemeRegistry registry = new SchemeRegistry();
//            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//            registry.register(new Scheme("https", sf, 443));
//
//            ClientConnectionManager ccm = new ThreadSafeClientConnManager(parameters,
//                    registry);
//
//            return new DefaultHttpClient(ccm, parameters);
//        } catch (Exception exception) {
//              logInstance.error("Exception" + exception.getMessage(),exception);
//            return new DefaultHttpClient();
//        }
//    }

    private String convertStringToJson(String value) throws JSONException {
        JSONObject jObject = new JSONObject(value);
        String accessToken = jObject.getString("access_token");
        return accessToken;
    }
}
