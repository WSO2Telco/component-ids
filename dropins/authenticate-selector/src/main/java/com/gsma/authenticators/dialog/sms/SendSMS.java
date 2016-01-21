package com.gsma.authenticators.dialog.sms;

import com.gsma.authendictorselector.AuthenticatorSelector;
import com.gsma.utils.ReadMobileConnectConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

/**
 * Created by paraparan on 5/13/15.
 */
public class SendSMS implements AuthenticatorSelector{

//    private static final Logger LOG = Logger.getLogger(SendSMS.class.getName());
//    private MobileConnectConfig.SMSConfig smsConfig;
    ReadMobileConnectConfig readMobileConnectConfig = new ReadMobileConnectConfig();
    Map<String, String> readMobileConnectConfigResult;
//    public SendSMS(String operator) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
//        readMobileConnectConfigResult = readMobileConnectConfig.query("SMS/" + operator);
//    }
    String msisdn;
    String message;

    public SendSMS(String msisdn, String message) {
        this.msisdn = msisdn;
        this.message = message;
    }

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
//            LOG.info("Error occured while calling end points");
            System.out.println("Error occured");
        }
        else{
//            LOG.info("Success Request");
            System.out.println("Success");
        }
    }

    @Override
    public String invokeAuthendicator() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        return sendSMS(msisdn,message);
    }
}

