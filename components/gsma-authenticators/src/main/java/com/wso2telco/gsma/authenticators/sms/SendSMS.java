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
import com.wso2telco.Util;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.model.OperatorMapping;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.model.ReceiptRequest;
import com.wso2telco.gsma.authenticators.sms.message.OutboundSMSTextMessage;
import com.wso2telco.gsma.authenticators.sms.message.v1.OutboundSMSMessageRequest;
import com.wso2telco.gsma.authenticators.sms.message.v2.SenderAddress;
import com.wso2telco.gsma.authenticators.util.BasicFutureCallback;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc

/**
 * The Class SendSMS.
 */
public class SendSMS {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = Logger.getLogger(SendSMS.class.getName());

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /**
     * The sms config.
     */
    private MobileConnectConfig.SMSConfig smsConfig;

    /**
     * Send sms.
     *
     * @param msisdn   the msisdn
     * @param message  the message
     * @param operator the operator
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String sendSMS(String msisdn, String message, String operator, BasicFutureCallback futureCallback)
            throws IOException, AuthenticationFailedException {
        smsConfig = configurationService.getDataHolder().getMobileConnectConfig().getSmsConfig();
        String returnString = null;

        List<String> address = new ArrayList<String>();
        address.add("tel:+" + msisdn);


        OutboundSMSTextMessage messageObj = new OutboundSMSTextMessage();
        messageObj.setMessage(message);

        ReceiptRequest receipt = new ReceiptRequest();
        receipt.setCallbackData("");
        receipt.setNotifyURL("");

        OutboundSMSMessageRequest outbound = new OutboundSMSMessageRequest();
        String smsversion=smsConfig.getSMSMessageVersion();
        if(smsversion!=null && smsversion.equalsIgnoreCase("V2")){
            com.wso2telco.gsma.authenticators.sms.message.v2.OutboundSMSMessageRequest outboundv2 = new com.wso2telco.gsma.authenticators.sms.message.v2.OutboundSMSMessageRequest();

            List<SenderAddress> senderAddresses = new ArrayList<>();
            List<OperatorMapping> operators = smsConfig.getOperatorMappings();
            if(operators!=null && !operators.isEmpty()){
                for (OperatorMapping operatorinlist : operators) {
                    if(operator.equalsIgnoreCase(operatorinlist.getOperator())){
                        SenderAddress senderAddress=new SenderAddress();
                        senderAddress.setSenderAddress(operatorinlist.getSenderAddress());
                        senderAddress.setOperatorCode(operatorinlist.getOperatorCode());
                        senderAddress.setSenderName(operatorinlist.getSenderName());
                        senderAddresses.add(senderAddress);
                        break;
                    }
                }
            }
            if(!senderAddresses.isEmpty()) {
                outboundv2.setSenderAddresses(senderAddresses);
                outbound = outboundv2;
            }else{
                throw new AuthenticationFailedException("SMS Authentication failed, operator mapping invalid to send SMS");
            }
        }else{
            String senderAddress = smsConfig.getSenderAddress();
            senderAddress = senderAddress.trim() == null ? "26451" : senderAddress.trim();
            outbound.setSenderAddress(senderAddress);
        }

        outbound.setReceiptRequest(receipt);
        outbound.setOutboundTextMessage(messageObj);
        outbound.setAddress(address);

        SendSMSRequest req = new SendSMSRequest();

        req.setOutboundSMSMessageRequest(outbound);

        Gson gson = new GsonBuilder().serializeNulls().create();

        returnString = gson.toJson(req);

        postRequest(smsConfig.getEndpoint(), returnString, operator, futureCallback);

        return returnString;
    }

    /**
     * Post request.
     *
     * @param url        the url
     * @param requestStr the request str
     * @param operator   the operator
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void postRequest(String url, String requestStr, String operator, BasicFutureCallback futureCallback)
            throws IOException {


//        HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(url);

        postRequest.addHeader("accept", "application/json");
        postRequest.addHeader("Authorization", "Bearer " + smsConfig.getAuthToken());

        if (operator != null) {
            postRequest.addHeader("operator", operator);
        }

        StringEntity input = new StringEntity(requestStr);
        input.setContentType("application/json");

        postRequest.setEntity(input);

        Util.sendAsyncRequest(postRequest, futureCallback,false);
    }
}
