package com.wso2telco.gsma.authenticators.ussd.command;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
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
public class PinRegistrationUssdCommand extends UssdCommand {

    private Application application = new Application();

    private static Log log = LogFactory.getLog(PinRegistrationUssdCommand.class);

    /** The Configuration service */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    @Override
    protected String getUrl(String msisdn) {

        MobileConnectConfig.USSDConfig ussdConfig = configurationService.getDataHolder().getMobileConnectConfig().getUssdConfig();

        String url = ussdConfig.getEndpoint();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1) + "/" + "tel:+" + msisdn;

        } else {
            url = url + "/tel:+" + msisdn;
        }

        return url;
    }

    @Override
    protected USSDRequest getUssdRequest(String msisdn, String sessionID, String serviceProvider, String operator, String client_id) {
        MobileConnectConfig.USSDConfig ussdConfig = configurationService.getDataHolder().getMobileConnectConfig().getUssdConfig();

        USSDRequest req = new USSDRequest();

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(ussdConfig.getShortCode());
        outboundUSSDMessageRequest.setKeyword(ussdConfig.getKeyword());

        // TODO: 12/28/16 check the usage and enable this

//        if (noOfAttempts == 1) {
//            outboundUSSDMessageRequest.setOutboundUSSDMessage(FileUtil.getApplicationProperty("message"));
//        } else if (noOfAttempts == 2) {
//            outboundUSSDMessageRequest.setOutboundUSSDMessage(FileUtil.getApplicationProperty("retry_message"));
//        } else {
//            outboundUSSDMessageRequest.setOutboundUSSDMessage(FileUtil.getApplicationProperty("error_message"));
//        }

        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();

        responseRequest.setNotifyURL(ussdConfig.getPinRegistrationNotifyUrl());
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);


        outboundUSSDMessageRequest.setUssdAction(Constants.MTINIT);

        req.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);
        return req;
    }
}
