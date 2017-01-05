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
package com.wso2telco.gsma.authenticators.ussd;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wso2telco.Util;
import com.wso2telco.core.config.ReadMobileConnectConfig;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.model.OutboundUSSDMessageRequest;
import com.wso2telco.gsma.authenticators.model.ResponseRequest;
import com.wso2telco.gsma.authenticators.util.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

// TODO: Auto-generated Javadoc
/**
 * The Class SendUSSD.
 */
public class SendUSSD {

    /** The log. */
    private static Log log = LogFactory.getLog(SendUSSD.class);

    /** The Configuration service */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /** The ussd config. */
    private MobileConnectConfig.USSDConfig ussdConfig;
    
    /** The const mtinit. */
    private static String CONST_MTINIT = "mtinit";
    
    /** The application. */
    private Application application=new Application();

    /**
     * Send ussd.
     *
     * @param msisdn the msisdn
     * @param sessionID the session id
     * @param serviceProvider the service provider
     * @param operator the operator
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void sendUSSD(String msisdn, String sessionID, String serviceProvider,String operator) throws IOException {
        ussdConfig = configurationService.getDataHolder().getMobileConnectConfig().getUssdConfig();

        USSDRequest req = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(ussdConfig.getShortCode());
        outboundUSSDMessageRequest.setKeyword(ussdConfig.getKeyword());
        log.info("massage:" + ussdConfig.getMessage() + serviceProvider);
        outboundUSSDMessageRequest.setOutboundUSSDMessage(ussdConfig.getMessage() + application.changeApplicationName(serviceProvider) + "\n1. Yes\n2. No");
        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();
        responseRequest.setNotifyURL(configurationService.getDataHolder().getMobileConnectConfig().getUssdConfig().getUssdContextEndpoint());
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);
        outboundUSSDMessageRequest.setUssdAction(CONST_MTINIT);

        req.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);

        Gson gson = new GsonBuilder().serializeNulls().create();
        String reqString = gson.toJson(req);

        String endpoint = ussdConfig.getEndpoint();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1) + "/" + "tel:+" + msisdn;

        } else {
            endpoint = endpoint + "/tel:+" + msisdn;
        }

        if (log.isDebugEnabled()) {
            log.debug("endpoint :"+endpoint);
            log.debug("reqstr :"+reqString);
        }
        log.info("[sendUSSD][reqString] : " + reqString);
        postRequest(endpoint, reqString,operator);
    }
    
    /**
     * Send ussdpin.
     *
     * @param msisdn the msisdn
     * @param sessionID the session id
     * @param serviceProvider the service provider
     * @param operator the operator
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void sendUSSDPIN(String msisdn, String sessionID, String serviceProvider,String operator) throws IOException {
        ussdConfig = configurationService.getDataHolder().getMobileConnectConfig().getUssdConfig();
        Map<String, String> readMobileConnectConfigResult=null;
        ReadMobileConnectConfig readMobileConnectConfig = new ReadMobileConnectConfig();
        try {
            readMobileConnectConfigResult = readMobileConnectConfig.query("USSD");
        }catch (Exception ex){
            log.error("Exception :"+ex);
        }
        USSDRequest req = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(ussdConfig.getShortCode());
        outboundUSSDMessageRequest.setKeyword(ussdConfig.getKeyword());
        log.info("massage:" + readMobileConnectConfigResult.get("MessageContentPin")+serviceProvider);
        outboundUSSDMessageRequest.setOutboundUSSDMessage(readMobileConnectConfigResult.get("MessageContentPin")+application.changeApplicationName(serviceProvider)+"\n\nPIN");
        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();
        responseRequest.setNotifyURL(configurationService.getDataHolder().getMobileConnectConfig().getUssdConfig().getUssdPinContextEndpoint());
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);
        outboundUSSDMessageRequest.setUssdAction(CONST_MTINIT);

        req.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);

        Gson gson = new GsonBuilder().serializeNulls().create();
        String reqString = gson.toJson(req);

        String endpoint = ussdConfig.getEndpoint();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1) + "/" + "tel:+" + msisdn;

        } else {
            endpoint = endpoint + "/tel:+" + msisdn;
        }

        if (log.isDebugEnabled()) {
            log.debug("endpoint :"+endpoint);
            log.debug("reqstr :"+reqString);
        }
        
        log.info("[sendUSSDPIN][reqString] : " + reqString);
        postRequest(endpoint, reqString,operator);
    }
    

    /**
     * Post request.
     *
     * @param url the url
     * @param requestStr the request str
     * @param operator the operator
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    /*
    private String postRequest(String url, String requestStr,String operator) throws IOException {

//        HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(url);

        postRequest.addHeader("accept", "application/json");
        postRequest.addHeader("Authorization", "Bearer " + ussdConfig.getAuthToken());
        
        if (operator != null){
            postRequest.addHeader("operator",operator);
        }

        StringEntity input = new StringEntity(requestStr);
        input.setContentType("application/json");

        postRequest.setEntity(input);

        HttpResponse response = client.execute(postRequest);

        if ((response.getStatusLine().getStatusCode() != 201)) {
            log.error("Error occured while calling end points - " + response.getStatusLine().getStatusCode() + "-" +
                    response.getStatusLine().getReasonPhrase());
        } else {
            log.info("Success Request");
        }
        String responseStr = null;
        HttpEntity responseEntity = response.getEntity();
        if (responseEntity != null) {
            responseStr = EntityUtils.toString(responseEntity);
        }
        return responseStr;
    }
    */


    /**
     * Post request.
     *
     * @param url        the url
     * @param requestStr the request str
     * @param operator   the operator
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void postRequest(String url, String requestStr, String operator) throws IOException {
        final HttpPost postRequest = new HttpPost(url);
        postRequest.addHeader("accept", "application/json");
        postRequest.addHeader("Authorization", "Bearer " + ussdConfig.getAuthToken());

        if (operator != null) {
            postRequest.addHeader("operator", operator);
        }

        StringEntity input = new StringEntity(requestStr);
        input.setContentType("application/json");

        postRequest.setEntity(input);
        final CountDownLatch latch = new CountDownLatch(1);
        FutureCallback<HttpResponse> futureCallback = new FutureCallback<HttpResponse>() {
            @Override
            public void completed(final HttpResponse response) {
                latch.countDown();
                if ((response.getStatusLine().getStatusCode() != 201)) {
                    log.error("Error occurred while calling end point - " + response.getStatusLine().getStatusCode() +
                                      "; Error - " +
                                      response.getStatusLine().getReasonPhrase());
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Success Request - " + postRequest.getURI().getSchemeSpecificPart());
                    }
                }
            }

            @Override
            public void failed(final Exception ex) {
                latch.countDown();
                log.error("Error occurred while calling end point - " + postRequest.getURI().getSchemeSpecificPart() +
                                  "; Error - " + ex);
            }

            @Override
            public void cancelled() {
                latch.countDown();
                log.warn("Operation cancelled while calling end point - " +
                                 postRequest.getURI().getSchemeSpecificPart());
            }
        };
        Util.sendAsyncRequest(postRequest, futureCallback, latch);
    }

}
