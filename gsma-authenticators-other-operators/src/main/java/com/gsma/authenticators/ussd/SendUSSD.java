package com.gsma.authenticators.ussd;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gsma.authenticators.Constants;
import com.gsma.authenticators.DataHolder;
import com.gsma.authenticators.config.MobileConnectConfig;
import com.gsma.authenticators.model.OutboundUSSDMessageRequest;
import com.gsma.authenticators.model.ResponseRequest;

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

public class SendUSSD {

    private static Log log = LogFactory.getLog(SendUSSD.class);
    private MobileConnectConfig.USSDConfig ussdConfig;
    private static String CONST_MTINIT = "mtinit";

    protected String sendUSSD(String msisdn, String sessionID, String serviceProvider) throws IOException {
        ussdConfig = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig();

        USSDRequest req = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(ussdConfig.getShortCode());
        outboundUSSDMessageRequest.setKeyword(ussdConfig.getKeyword());
        outboundUSSDMessageRequest.setOutboundUSSDMessage(serviceProvider + ":" + ussdConfig.getMessage()+"\n1. OK\n2. Cancel");
        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();
        responseRequest.setNotifyURL(DataHolder.getInstance().getMobileConnectConfig().getListenerWebappHost() +
                Constants.LISTNER_WEBAPP_USSD_CONTEXT);
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
        return postRequest(endpoint, reqString);
    }
    
    protected String sendUSSDPIN(String msisdn, String sessionID, String serviceProvider) throws IOException {
        ussdConfig = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig();

        USSDRequest req = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(ussdConfig.getShortCode());
        outboundUSSDMessageRequest.setKeyword(ussdConfig.getKeyword());
        outboundUSSDMessageRequest.setOutboundUSSDMessage(serviceProvider + ":" + ussdConfig.getMessage()+"\n\nPIN");
        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();
        responseRequest.setNotifyURL(DataHolder.getInstance().getMobileConnectConfig().getListenerWebappHost() +
                Constants.LISTNER_WEBAPP_USSDPIN_CONTEXT);
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
        return postRequest(endpoint, reqString);
    }
    

    private String postRequest(String url, String requestStr) throws IOException {

//        HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(url);

        postRequest.addHeader("accept", "application/json");
        postRequest.addHeader("Authorization", "Bearer " + ussdConfig.getAuthToken());


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
}
