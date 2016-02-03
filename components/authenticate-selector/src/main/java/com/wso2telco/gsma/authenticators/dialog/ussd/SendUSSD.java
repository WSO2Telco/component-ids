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
package com.wso2telco.gsma.authenticators.dialog.ussd;

import com.wso2telco.gsma.authendictorselector.AuthenticatorSelector;
import com.wso2telco.gsma.utils.ReadMobileConnectConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Map;

 
// TODO: Auto-generated Javadoc
/**
 * The Class SendUSSD.
 */
public class SendUSSD implements AuthenticatorSelector {

    /** The log. */
    private static Log log = LogFactory.getLog(SendUSSD.class);

/** The read mobile connect config. */
//    private MobileConnectConfig.USSDConfig ussdConfig;
    ReadMobileConnectConfig readMobileConnectConfig = new ReadMobileConnectConfig();
    
    /** The read mobile connect config result. */
    Map<String, String> readMobileConnectConfigResult;
    
    /** The const mtinit. */
    private static String CONST_MTINIT = "mtinit";

    /** The service provider name. */
    String msisdn, identifier, serviceProviderName;

    /**
     * Instantiates a new send ussd.
     *
     * @param msisdn the msisdn
     * @param identifier the identifier
     * @param serviceProviderName the service provider name
     */
    public SendUSSD(String msisdn, String identifier, String serviceProviderName) {
        this.msisdn = msisdn;
        this.identifier = identifier;
        this.serviceProviderName = serviceProviderName;
    }

    /**
     * Send ussd.
     *
     * @param msisdn the msisdn
     * @param sessionID the session id
     * @param serviceProvider the service provider
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws XPathExpressionException the x path expression exception
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     */
    protected String sendUSSD(String msisdn, String sessionID, String serviceProvider) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
//        ussdConfig = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig();

        USSDRequest req = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        readMobileConnectConfigResult = readMobileConnectConfig.query("dialog/USSD");
//        outboundUSSDMessageRequest.setShortCode(ussdConfig.getShortCode());
        outboundUSSDMessageRequest.setShortCode(readMobileConnectConfigResult.get("ShortCode"));
//        outboundUSSDMessageRequest.setKeyword(ussdConfig.getKeyword());
        outboundUSSDMessageRequest.setKeyword(readMobileConnectConfigResult.get("Keyword"));
//        outboundUSSDMessageRequest.setOutboundUSSDMessage(serviceProvider + ":" + ussdConfig.getMessage()+"\n1. OK\n2. Cancel");
        outboundUSSDMessageRequest.setOutboundUSSDMessage(serviceProvider + ":" + readMobileConnectConfigResult.get("MessageContent") +"\n1. OK\n2. Cancel");
        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();
//        responseRequest.setNotifyURL(DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getUssdContextEndpoint());
        responseRequest.setNotifyURL(readMobileConnectConfigResult.get("USSDContextEndpoint"));
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);
        outboundUSSDMessageRequest.setUssdAction(CONST_MTINIT);

        req.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);

        Gson gson = new GsonBuilder().serializeNulls().create();
        String reqString = gson.toJson(req);

//        String endpoint = ussdConfig.getEndpoint();
        String endpoint = readMobileConnectConfigResult.get("Endpoint");
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
        return postRequest(endpoint, reqString);
    }

    /**
     * Send ussdpin.
     *
     * @param msisdn the msisdn
     * @param sessionID the session id
     * @param serviceProvider the service provider
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws XPathExpressionException the x path expression exception
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     */
    protected String sendUSSDPIN(String msisdn, String sessionID, String serviceProvider) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
//        ussdConfig = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig();

        USSDRequest req = new USSDRequest();
        readMobileConnectConfigResult = readMobileConnectConfig.query("dialog/USSD");
        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
//        outboundUSSDMessageRequest.setShortCode(ussdConfig.getShortCode());
        outboundUSSDMessageRequest.setShortCode(readMobileConnectConfigResult.get("ShortCode"));
//        outboundUSSDMessageRequest.setKeyword(ussdConfig.getKeyword());
        outboundUSSDMessageRequest.setKeyword(readMobileConnectConfigResult.get("Keyword"));
//        outboundUSSDMessageRequest.setOutboundUSSDMessage(serviceProvider + ":" + ussdConfig.getMessage()+"\n\nPIN");
        outboundUSSDMessageRequest.setOutboundUSSDMessage(serviceProvider + ":" + readMobileConnectConfigResult.get("MessageContent") +"\n\nPIN");
        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();
//        responseRequest.setNotifyURL(DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getUssdPinContextEndpoint());
        responseRequest.setNotifyURL(readMobileConnectConfigResult.get("USSDPinContextEndpoint"));
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);
        outboundUSSDMessageRequest.setUssdAction(CONST_MTINIT);

        req.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);

        Gson gson = new GsonBuilder().serializeNulls().create();
        String reqString = gson.toJson(req);

//        String endpoint = ussdConfig.getEndpoint();
        String endpoint = readMobileConnectConfigResult.get("Endpoint");
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
        return postRequest(endpoint, reqString);
    }


    /**
     * Post request.
     *
     * @param url the url
     * @param requestStr the request str
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws XPathExpressionException the x path expression exception
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     */
    private String postRequest(String url, String requestStr) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {

//        HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(url);
        readMobileConnectConfigResult = readMobileConnectConfig.query("dialog/USSD");
        postRequest.addHeader("accept", "application/json");
//        postRequest.addHeader("Authorization", "Bearer " + ussdConfig.getAuthToken());
        postRequest.addHeader("Authorization", "Bearer " + readMobileConnectConfigResult.get("AuthToken"));


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

    /* (non-Javadoc)
     * @see com.wso2telco.gsma.authendictorselector.AuthenticatorSelector#invokeAuthendicator()
     */
    @Override
    public String invokeAuthendicator() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        return sendUSSD(msisdn, identifier, serviceProviderName);
    }

    /**
     * Invoke authendicator ussdpin.
     *
     * @return the string
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     * @throws XPathExpressionException the x path expression exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String invokeAuthendicatorUSSDPIN() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        return sendUSSDPIN(msisdn, identifier, serviceProviderName);
    }
}

