package com.wso2telco.gsma.authenticators.ussd.command;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.core.config.MobileConnectConfig;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.model.OutboundUSSDMessageRequest;
import com.wso2telco.gsma.authenticators.model.ResponseRequest;
import com.wso2telco.gsma.authenticators.ussd.USSDRequest;
import com.wso2telco.gsma.authenticators.util.Application;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by isuru on 12/23/16.
 */
public class LoginUssdCommand extends UssdCommand {

    private Application application = new Application();

    private static Log log = LogFactory.getLog(LoginUssdCommand.class);

    @Override
    protected String getUrl(String msisdn) {

        MobileConnectConfig.USSDConfig ussdConfig = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig();

        String url = ussdConfig.getEndpoint();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1) + "/" + "tel:+" + msisdn;

        } else {
            url = url + "/tel:+" + msisdn;
        }

        return url;
    }

    @Override
    protected USSDRequest getUssdRequest(String msisdn, String sessionID, String serviceProvider, String operator) {
        MobileConnectConfig.USSDConfig ussdConfig = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig();

        USSDRequest ussdRequest = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(ussdConfig.getShortCode());
        outboundUSSDMessageRequest.setKeyword(ussdConfig.getKeyword());
        log.info("massage:" + ussdConfig.getMessage() + serviceProvider);
        outboundUSSDMessageRequest.setOutboundUSSDMessage(ussdConfig.getUssdLoginMessage() + application.changeApplicationName(serviceProvider)
                + "\n1. Yes\n2. No");
        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();
        responseRequest.setNotifyURL(DataHolder.getInstance().getMobileConnectConfig().getUssdConfig().getLoginNotifyUrl());
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);
        outboundUSSDMessageRequest.setUssdAction(Constants.MTINIT);

        ussdRequest.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);
        return ussdRequest;
    }
}
