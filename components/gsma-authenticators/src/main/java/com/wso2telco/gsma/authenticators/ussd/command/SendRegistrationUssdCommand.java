package com.wso2telco.gsma.authenticators.ussd.command;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.core.config.MobileConnectConfig;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.model.OutboundUSSDMessageRequest;
import com.wso2telco.gsma.authenticators.model.ResponseRequest;
import com.wso2telco.gsma.authenticators.ussd.USSDRequest;
import com.wso2telco.gsma.authenticators.util.Application;

/**
 * Created by isuru on 12/23/16.
 */
public class SendRegistrationUssdCommand extends SendUssdCommand {

    private Application application = new Application();

    @Override
    protected String getAccessToken() {
        return null;
    }

    @Override
    protected String getUrl(String msisdn) {
        MobileConnectConfig.USSDConfig ussdConfig = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig();

        return ussdConfig.getRegistrationNotifyUrl();
    }

    @Override
    protected USSDRequest getUssdRequest(String msisdn, String sessionID, String serviceProvider, String operator) {
        MobileConnectConfig.USSDConfig ussdConfig = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig();

        USSDRequest ussdRequest = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(ussdConfig.getShortCode());
        outboundUSSDMessageRequest.setKeyword(ussdConfig.getKeyword());
        outboundUSSDMessageRequest.setClientCorrelator(sessionID);
        outboundUSSDMessageRequest.setOutboundUSSDMessage(ussdConfig.getUssdRegistrationMessage() + "\n1. OK\n2. Cancel");


        ResponseRequest responseRequest = new ResponseRequest();

        responseRequest.setNotifyURL(ussdConfig.getRegistrationNotifyUrl());
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);


        outboundUSSDMessageRequest.setUssdAction(Constants.MTINIT);

        ussdRequest.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);

        return ussdRequest;
    }
}
