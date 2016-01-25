/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
 ******************************************************************************/
package com.wso2telco.gsma.authenticators.android;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wso2telco.gsma.authenticators.DataHolder;
import com.wso2telco.gsma.authenticators.config.MobileConnectConfig;
import com.wso2telco.gsma.authenticators.model.AuthenticateUserRequest;
import com.wso2telco.gsma.authenticators.model.ServiceProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Class SendAndroid.
 */
public class SendAndroid {

    /** The log. */
    private static Log log = LogFactory.getLog(SendAndroid.class);
    
    /** The android config. */
    private MobileConnectConfig.AndroidConfig androidConfig;

    /**
     * Send android.
     *
     * @param msisdn the msisdn
     * @param sessionID the session id
     * @param serviceProvider the service provider
     * @param sLOA the s loa
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected String sendAndroid(String msisdn, String sessionID, String serviceProvider,String sLOA) throws IOException {

       log.info("Sending android request");
        androidConfig = DataHolder.getInstance().getMobileConnectConfig().getAndroidConfig();

        AndroidRequest req = new AndroidRequest();

        req.setAuthenticateUserRequest(new AuthenticateUserRequest());
        req.getAuthenticateUserRequest().setServiceProvider(new ServiceProvider());

        req.getAuthenticateUserRequest().setAddress("tel:+" + msisdn);
        req.getAuthenticateUserRequest().setClientCorrelator(sessionID);
        req.getAuthenticateUserRequest().setRequestedLOA(sLOA);
        req.getAuthenticateUserRequest().getServiceProvider().setServiceProviderName(serviceProvider);


        
        Gson gson = new GsonBuilder().serializeNulls().create();
        String reqString = gson.toJson(req);

        String endpoint = androidConfig.getEndpoint();
//        if (endpoint.endsWith("/")) {
//            endpoint = endpoint.substring(0, endpoint.length() - 1) + "/" + "tel:+" + msisdn;
//
//        } else {
//            endpoint = endpoint + "/tel:+" + msisdn;
//        }

        String APIKey = androidConfig.getAPIKey();

        if (log.isDebugEnabled()) {
            log.debug("endpoint :"+endpoint);
            log.debug("request :"+reqString);
            log.debug("APIKEY :"+APIKey);
        }
        log.info("[sendAndroid][reqString] : " + reqString);

        log.info("Endpoint : " + endpoint  );
        log.info("Request" + reqString  );

        return postRequest(endpoint, reqString,APIKey);
    }


    /**
     * Post request.
     *
     * @param url the url
     * @param requestStr the request str
     * @param APIKey the API key
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private String postRequest(String url, String requestStr,String APIKey) throws IOException {

//        HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(url);

        postRequest.addHeader("accept", "application/json");
        postRequest.addHeader("ApiKey", APIKey);
//        postRequest.addHeader("Authorization", "Bearer " + ussdConfig.getAuthToken());


        StringEntity input = new StringEntity(requestStr);
        input.setContentType("application/json");

        postRequest.setEntity(input);

        HttpResponse response = client.execute(postRequest);

        String responseStr = null;
        HttpEntity responseEntity = response.getEntity();

        if (responseEntity != null) {
            responseStr = EntityUtils.toString(responseEntity);
        }

        log.info("Android Response : " + responseStr);

        if ((response.getStatusLine().getStatusCode() != 201)) {
            log.error("Error occured while calling end points - " + response.getStatusLine().getStatusCode() + "-" +
                    response.getStatusLine().getReasonPhrase());
            responseStr = null;
        } else {
            log.info("Success Request");
        }

        return responseStr;
    }
}
