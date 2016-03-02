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
package com.wso2telco.gsma.authenticators.dialog.sms;

import com.wso2telco.gsma.authendictorselector.AuthenticatorSelector;
import com.wso2telco.gsma.utils.ReadMobileConnectConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.SAXException;
//import org.apache.log4j.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

 
// TODO: Auto-generated Javadoc
/**
 * The Class SendSMS.
 */
public class SendSMS implements AuthenticatorSelector{
	
    /** The log. */
    private static Log log = LogFactory.getLog(SendSMS.class); 

//    private static final Logger LOG = Logger.getLogger(SendSMS.class.getName());
/** The read mobile connect config. */
//    private MobileConnectConfig.SMSConfig smsConfig;
    ReadMobileConnectConfig readMobileConnectConfig = new ReadMobileConnectConfig();
    
    /** The read mobile connect config result. */
    Map<String, String> readMobileConnectConfigResult;
//    public SendSMS(String operator) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
//        readMobileConnectConfigResult = readMobileConnectConfig.query("SMS/" + operator);
/** The msisdn. */
//    }
    String msisdn;
    
    /** The message. */
    String message;

    /**
     * Instantiates a new send sms.
     *
     * @param msisdn the msisdn
     * @param message the message
     */
    public SendSMS(String msisdn, String message) {
        this.msisdn = msisdn;
        this.message = message;
    }

    /**
     * Send sms.
     *
     * @param msisdn the msisdn
     * @param message the message
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws XPathExpressionException the x path expression exception
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     */
    public String sendSMS(String msisdn, String message) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        String returnString = null;

        List<String> address = new ArrayList<String>();
        address.add("tel:+" + msisdn);


        OutboundSMSTextMessage messageObj = new OutboundSMSTextMessage();
        ReceiptRequest receipt = new ReceiptRequest();
        
        receipt.setNotifyURL("http://application.example.com/notifications/DeliveryInfoNotification");
        receipt.setcallbackData("some-data-useful-to-the-requester");
        
        messageObj.setMessage(message);

        OutboundSMSMessageRequest outbound = new OutboundSMSMessageRequest();

        outbound.setOutboundTextMessage(messageObj);
        outbound.setAddress(address);
        outbound.setSenderAddress("tel:26451");
        
        outbound.setReceiptRequest(receipt);

        SendSMSRequest req = new SendSMSRequest();

        req.setOutboundSMSMessageRequest(outbound);

        Gson gson = new GsonBuilder().serializeNulls().create();

        returnString = gson.toJson(req);

//        smsConfig = DataHolder.getInstance().getMobileConnectConfig().getSmsConfig();
//        postRequest(smsConfig.getEndpoint(),returnString);
        readMobileConnectConfigResult = readMobileConnectConfig.query("dialog/SMS");

//        postRequest("http://ideabiz.lk/apicall/smsmessaging/v1/outbound/26451/requests",returnString);
        postRequest(readMobileConnectConfigResult.get("Endpoint"),returnString);

        return returnString;

    }

    /**
     * Post request.
     *
     * @param url the url
     * @param requestStr the request str
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws XPathExpressionException the x path expression exception
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     */
    protected void postRequest(String url, String requestStr) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {


//        HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(url);

        postRequest.addHeader("accept", "application/json");
//        postRequest.addHeader("Authorization", "Bearer " + smsConfig.getAuthToken());
        readMobileConnectConfigResult = readMobileConnectConfig.query("dialog/SMS");
        postRequest.addHeader("Authorization", "Bearer " + readMobileConnectConfigResult.get("AuthToken"));


        StringEntity input = new StringEntity(requestStr);
        input.setContentType("application/json");

        postRequest.setEntity(input);

        HttpResponse response = client.execute(postRequest);

        if ( (response.getStatusLine().getStatusCode() != 201)){
           log.error("Error occured while calling end points");
        }
        else{
			if (log.isDebugEnabled()) {
				log.debug("Success");
			}
        }
    }

    /* (non-Javadoc)
     * @see com.wso2telco.gsma.authendictorselector.AuthenticatorSelector#invokeAuthendicator()
     */
    @Override
    public String invokeAuthendicator() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        return sendSMS(msisdn,message);
    }
}

