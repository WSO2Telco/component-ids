package com.wso2telco.sms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wso2telco.Endpoints;
import com.wso2telco.core.config.ConfigLoader;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.model.OperatorSmsConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.util.ReceiptRequest;
import com.wso2telco.util.RestClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SendSMS {

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(SendSMS.class);

    /**
     * Send welcome SMS configured operator wise
     *
     * @param msisdn
     * @param operator
     */
    public void sendWelcomeSMS(String msisdn, String operator) {
        try {
            ArrayList<OperatorSmsConfig> welcomeSMSMessageConfigurationList =
                    (ArrayList<OperatorSmsConfig>) ConfigLoader.getInstance().getMobileConnectConfig().getSmsConfig().getOperatorSmsConfigs();
            String welcomeSMSMessage = null;
            Boolean operatorWelcomeMessageDisabled = false;
            String smsEndpoint = null;
            for (OperatorSmsConfig operatorConfig :welcomeSMSMessageConfigurationList){
                if(operatorConfig.getName().equals(operator)){
                    welcomeSMSMessage=operatorConfig.getMessage();
                    operatorWelcomeMessageDisabled=null;//operatorConfig.getWelcomemessagedisabled();
                    smsEndpoint = operatorConfig.getSmsEndpoint();
                    smsEndpoint = smsEndpoint.replace("{sender}", msisdn); //used if the endpoint required msisdn as a
                    // path param.
                    break;
                }

            }
            if(!operatorWelcomeMessageDisabled) {
                this.sendSMS(msisdn, welcomeSMSMessage, operator, smsEndpoint);
                log.info("Sending Welcome SMS to: " + msisdn + ", Operator:" + operator + " , MessageContent:" +
                        welcomeSMSMessage + ", SMS Endpoint:" + smsEndpoint);
            }else {
                log.info("Welcome message disabled");
            }

        } catch (Exception ex) {
            log.error("Sending Welcome SMS Failed (Msisdn:" + msisdn + ", Operator:" + operator + ")" + ex);
        }
    }


    /**
     * Send SMS
     *
     * @param msisdn
     * @param message
     * @param operator
     * @param smsEndpoint
     * @return
     * @throws IOException
     */
    protected String sendSMS(String msisdn, String message, String operator, String smsEndpoint)
            throws IOException {
        String returnString;
        List<String> address = new ArrayList<String>();
        address.add("tel:+" + msisdn);
        OutboundSMSTextMessage messageObj = new OutboundSMSTextMessage();
        messageObj.setMessage(message);
        OutboundSMSMessageRequest outbound = new OutboundSMSMessageRequest();
        ReceiptRequest receipt = new ReceiptRequest();

        MobileConnectConfig.SMSConfig smsConfig=null;
        smsConfig =  configurationService.getDataHolder().getMobileConnectConfig().getSmsConfig();

        receipt.setCallbackData("some-data-useful");
        receipt.setNotifyURL(smsConfig.getShortUrlService());
        outbound.setReceiptRequest(receipt);
        outbound.setOutboundTextMessage(messageObj);
        outbound.setAddress(address);

        String senderAddress = smsConfig.getSenderAddress();
        senderAddress = senderAddress.trim() == null ? "" : senderAddress.trim();
        outbound.setSenderAddress(senderAddress);

        SendSMSRequest req = new SendSMSRequest();

        req.setOutboundSMSMessageRequest(outbound);

        Gson gson = new GsonBuilder().serializeNulls().create();

        returnString = gson.toJson(req);

        log.info("Just before post");
        RestClient restClient=new RestClient();
        restClient.postRequest(smsConfig.getEndpoint(), returnString, operator);

        log.info("Operator SMS Endpoint : " + smsEndpoint);
        restClient.postRequest(smsEndpoint, returnString, operator);
        log.info("After post");
        return returnString;
    }


}
