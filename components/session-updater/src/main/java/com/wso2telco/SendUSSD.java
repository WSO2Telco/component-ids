/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
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
 * The Class SendUSSD.
 */
public class SendUSSD {

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(SendUSSD.class);

    /**
     * The const mtinit.
     */
    //private MobileConnectConfig.USSDConfig ussdConfig;
    private static String CONST_MTINIT = "mtinit";

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /**
     * Send ussd.
     *
     * @param msisdn       the msisdn
     * @param sessionID    the session id
     * @param noOfAttempts the no of attempts
     * @param action       the action
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected String sendUSSD(String msisdn, String sessionID, int noOfAttempts, String action) throws IOException {
        // load config values
        MobileConnectConfig.SessionUpdaterConfig sessionUpdaterConfig = configurationService.getDataHolder()
                .getMobileConnectConfig().getSessionUpdaterConfig();

        String reqString = getJsonPayload(msisdn, sessionID, noOfAttempts, action);

        String endpoint = sessionUpdaterConfig.getUssdsend();

        endpoint = endpoint + "/tel:+" + msisdn;

        String returnString = postRequest(endpoint, reqString);

        return returnString;
    }

    /**
     * Gets the json payload.
     *
     * @param msisdn       the msisdn
     * @param sessionID    the session id
     * @param noOfAttempts the no of attempts
     * @param action       the action
     * @return the json payload
     */
    public static String getJsonPayload(String msisdn, String sessionID, int noOfAttempts, String action) {
        return getJsonPayload(msisdn, sessionID, noOfAttempts, action, null);
    }

    /**
     * Gets the json payload.
     *
     * @param msisdn        the msisdn
     * @param sessionID     the session id
     * @param noOfAttempts  the no of attempts
     * @param action        the action
     * @param ussdSessionID the ussd session id
     * @return the json payload
     */
    public static String getJsonPayload(String msisdn, String sessionID, int noOfAttempts, String action, String
            ussdSessionID) {
        String reqString = null;

        // load config values
        MobileConnectConfig.SessionUpdaterConfig sessionUpdaterConfig = configurationService.getDataHolder()
                .getMobileConnectConfig().getSessionUpdaterConfig();

        USSDRequest req = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(sessionUpdaterConfig.getShortcode());
        outboundUSSDMessageRequest.setKeyword(sessionUpdaterConfig.getKeyword());

        if (noOfAttempts == 1) {
            outboundUSSDMessageRequest.setOutboundUSSDMessage(sessionUpdaterConfig.getMessage());
        } else if (noOfAttempts == 2) {
            outboundUSSDMessageRequest.setOutboundUSSDMessage(sessionUpdaterConfig.getRetry_message());
        } else {
            outboundUSSDMessageRequest.setOutboundUSSDMessage(sessionUpdaterConfig.getError_message());
        }

        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        if (ussdSessionID != null)
            outboundUSSDMessageRequest.setSessionID(ussdSessionID);

        ResponseRequest responseRequest = new ResponseRequest();

        responseRequest.setNotifyURL(sessionUpdaterConfig.getNotifyurl());
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);

        outboundUSSDMessageRequest.setUssdAction(action);

        req.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);

        Gson gson = new GsonBuilder().serializeNulls().create();
        reqString = gson.toJson(req);


        return reqString;
    }

    /**
     * Gets the USSD json payload.
     *
     * @param msisdn       the msisdn
     * @param sessionID    the session id
     * @param noOfAttempts the no of attempts
     * @param action       the action
     * @return the USSD json payload
     */
    public static String getUSSDJsonPayload(String msisdn, String sessionID, int noOfAttempts, String action) {
        return getUSSDJsonPayload(msisdn, sessionID, noOfAttempts, action, null);
    }

    /**
     * Gets the USSD json payload.
     *
     * @param msisdn        the msisdn
     * @param sessionID     the session id
     * @param noOfAttempts  the no of attempts
     * @param action        the action
     * @param ussdSessionID the ussd session id
     * @return the USSD json payload
     */
    public static String getUSSDJsonPayload(String msisdn, String sessionID, int noOfAttempts, String action, String
            ussdSessionID) {
        String reqString = null;

        // load config values
        MobileConnectConfig.SessionUpdaterConfig sessionUpdaterConfig = configurationService.getDataHolder()
                .getMobileConnectConfig().getSessionUpdaterConfig();

        USSDRequest req = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(sessionUpdaterConfig.getShortcode());
        outboundUSSDMessageRequest.setKeyword(sessionUpdaterConfig.getKeyword());

        if (ussdSessionID != null) {
            outboundUSSDMessageRequest.setSessionID(ussdSessionID);
        }

        if (noOfAttempts < 2) {
            outboundUSSDMessageRequest.setOutboundUSSDMessage(sessionUpdaterConfig.getRetry_message());
        } else if (noOfAttempts == 5) {
            outboundUSSDMessageRequest.setOutboundUSSDMessage("Thank You");
        } else if (noOfAttempts == 9) {
            if (log.isDebugEnabled())
                log.info("Reset PIN request sending....");
            outboundUSSDMessageRequest.setOutboundUSSDMessage("Please reset your PIN");
        } else {
            outboundUSSDMessageRequest.setOutboundUSSDMessage(sessionUpdaterConfig.getError_message());
        }

        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();

        responseRequest.setNotifyURL(sessionUpdaterConfig.getNotifyurl());
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);


        outboundUSSDMessageRequest.setUssdAction(action);

        req.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);

        Gson gson = new GsonBuilder().serializeNulls().create();
        reqString = gson.toJson(req);


        return reqString;
    }

    /**
     * Post request.
     *
     * @param url        the url
     * @param requestStr the request str
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private String postRequest(String url, String requestStr) throws IOException {

        // load config values
        MobileConnectConfig.SessionUpdaterConfig sessionUpdaterConfig = configurationService.getDataHolder()
                .getMobileConnectConfig().getSessionUpdaterConfig();

        HttpClient client = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(url);

        postRequest.addHeader("accept", "application/json");
        postRequest.addHeader("Authorization", "Bearer " + sessionUpdaterConfig.getAccesstoken());


        StringEntity input = new StringEntity(requestStr);
        input.setContentType("application/json");

        postRequest.setEntity(input);

        HttpResponse response = client.execute(postRequest);

        if ((response.getStatusLine().getStatusCode() != 201)) {
            log.error("Error occured while calling end points - " + response.getStatusLine().getStatusCode() + "-" +
                    response.getStatusLine().getReasonPhrase());
        } else {
            if (log.isDebugEnabled())
                log.debug("Success Request");
        }

        String responseStr = null;
        HttpEntity responseEntity = response.getEntity();
        if (responseEntity != null) {
            responseStr = EntityUtils.toString(responseEntity);
        }

        return responseStr;
    }
}
