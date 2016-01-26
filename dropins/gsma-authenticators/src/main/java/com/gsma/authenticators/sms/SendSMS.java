/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gsma.authenticators.sms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gsma.authenticators.DataHolder;
import com.gsma.authenticators.config.MobileConnectConfig;
import com.gsma.authenticators.config.ReadMobileConnectConfig;
import com.gsma.authenticators.model.ReceiptRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

public class SendSMS {
    private static final Logger LOG = Logger.getLogger(SendSMS.class.getName());
    private MobileConnectConfig.SMSConfig smsConfig;

    protected String sendSMS(String msisdn, String message,String operator) throws IOException {
        String returnString = null;
        
        List<String> address = new ArrayList<String>();
        address.add("tel:+" + msisdn);


        OutboundSMSTextMessage messageObj = new OutboundSMSTextMessage();
        messageObj.setMessage(message);
        
        OutboundSMSMessageRequest outbound = new OutboundSMSMessageRequest();
        
        
        ReceiptRequest receipt = new ReceiptRequest();
        
        receipt.setCallbackData("some-data-useful");
        receipt.setNotifyURL("https://india.mconnect.com");
        outbound.setReceiptRequest(receipt);
        
      
        outbound.setOutboundTextMessage(messageObj);
        outbound.setAddress(address);
        
        

        ReadMobileConnectConfig readMobileConnectConfig = new ReadMobileConnectConfig();
        Map<String, String> readMobileConnectConfigResult= null;
        try {
            readMobileConnectConfigResult = readMobileConnectConfig.query("SMS");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

       
       String senderAddress = readMobileConnectConfigResult.get("SenderAddres");
       senderAddress =senderAddress.trim()==null?"26451":senderAddress.trim();
        
        outbound.setSenderAddress(senderAddress);
        
        SendSMSRequest req = new SendSMSRequest();
        
        req.setOutboundSMSMessageRequest(outbound);
        
        Gson gson = new GsonBuilder().serializeNulls().create();
        
        returnString = gson.toJson(req);

        smsConfig = DataHolder.getInstance().getMobileConnectConfig().getSmsConfig();
        postRequest(smsConfig.getEndpoint(),returnString,operator);
        
        return returnString;
        
    }
    
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
            LOG.info("Error occured while calling end points");
        }
        else{
            LOG.info("Success Request");
        }
        
    }
    
    
    
}
