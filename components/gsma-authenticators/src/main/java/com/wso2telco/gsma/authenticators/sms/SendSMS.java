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
package com.wso2telco.gsma.authenticators.sms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wso2telco.core.config.ReadMobileConnectConfig;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.model.ReceiptRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class SendSMS.
 */
public class SendSMS {
    
    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(SendSMS.class.getName());

    /** The Configuration service */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /** The sms config. */
    private MobileConnectConfig.SMSConfig smsConfig;

    /**
     * Send sms.
     *
     * @param msisdn the msisdn
     * @param message the message
     * @param operator the operator
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected String sendSMS(String msisdn, String message,String operator) throws IOException {
        String returnString = null;
        
        List<String> address = new ArrayList<String>();
        address.add("tel:+" + msisdn);


        OutboundSMSTextMessage messageObj = new OutboundSMSTextMessage();
        messageObj.setMessage(message);
        
        OutboundSMSMessageRequest outbound = new OutboundSMSMessageRequest();
        
        
        ReceiptRequest receipt = new ReceiptRequest();
        
	//FIXME: Set from configs
        receipt.setCallbackData("");
        receipt.setNotifyURL("");
        outbound.setReceiptRequest(receipt);
        
      
        outbound.setOutboundTextMessage(messageObj);
        outbound.setAddress(address);

        Map<String, String> readMobileConnectConfigResult= null;

        readMobileConnectConfigResult = ReadMobileConnectConfig.query("SMS");

        String senderAddress = readMobileConnectConfigResult.get("SenderAddres");
        senderAddress =senderAddress.trim()==null?"26451":senderAddress.trim();
        
        outbound.setSenderAddress(senderAddress);
        
        SendSMSRequest req = new SendSMSRequest();
        
        req.setOutboundSMSMessageRequest(outbound);
        
        Gson gson = new GsonBuilder().serializeNulls().create();
        
        returnString = gson.toJson(req);

        smsConfig = configurationService.getDataHolder().getMobileConnectConfig().getSmsConfig();
        postRequest(smsConfig.getEndpoint(),returnString,operator);
        
        return returnString;
        
    }
    
    /**
     * Post request.
     *
     * @param url the url
     * @param requestStr the request str
     * @param operator the operator
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void postRequest(String url, String requestStr,String operator) throws IOException {


//        HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(url);
        
        postRequest.addHeader("accept", "application/json");
        postRequest.addHeader("Authorization", "Bearer " + smsConfig.getAuthToken());
       
        if (operator != null){
            postRequest.addHeader("operator",operator);
        }
        
        StringEntity input = new StringEntity(requestStr);
        input.setContentType("application/json");
        
        postRequest.setEntity(input);

        HttpResponse response = client.execute(postRequest);
        
        if ( (response.getStatusLine().getStatusCode() != 201)){
            LOG.error("Error occured while calling end points");
        }
        else{
            LOG.info("Success Request");
        }
        
    }
}
