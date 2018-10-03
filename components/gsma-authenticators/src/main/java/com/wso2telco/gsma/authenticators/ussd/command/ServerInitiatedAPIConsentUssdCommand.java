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

package com.wso2telco.gsma.authenticators.ussd.command;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.model.OutboundUSSDMessageRequest;
import com.wso2telco.gsma.authenticators.model.ResponseRequest;
import com.wso2telco.gsma.authenticators.ussd.USSDRequest;
import com.wso2telco.gsma.authenticators.util.Application;
import com.wso2telco.gsma.authenticators.util.OutboundMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.HashMap;
import java.util.Map;

public class ServerInitiatedAPIConsentUssdCommand extends UssdCommand {

    private Application application = new Application();

    /**
     * The logger
     */
    private static Log log = LogFactory.getLog(ServerInitiatedAPIConsentUssdCommand.class);

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    private static MobileConnectConfig mobileConnectConfigs = null;

    private static AuthenticationContext context = null;

    static {
        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();

    }

    public ServerInitiatedAPIConsentUssdCommand(AuthenticationContext context){
        this.context = context;
    }


    @Override
    protected String getUrl(String msisdn) {

        MobileConnectConfig.USSDConfig ussdConfig = configurationService.getDataHolder().getMobileConnectConfig()
                .getUssdConfig();

        String url = ussdConfig.getEndpoint();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1) + "/" + "tel:+" + msisdn;

        } else {
            url = url + "/tel:+" + msisdn;
        }

        return url;
    }

    @Override
    protected USSDRequest getUssdRequest(String msisdn, String sessionID, String serviceProvider, String operator,
                                         String client_id) {
        MobileConnectConfig.USSDConfig ussdConfig = configurationService.getDataHolder().getMobileConnectConfig()
                .getUssdConfig();
        String ussdNotifyUrl = mobileConnectConfigs.getBackChannelConfig().getUssdNotifyUrl();
        StringBuilder apiScopes = new StringBuilder();
        StringBuilder inputs = new StringBuilder();
        Map<String, String> approveNeededScopes = (Map<String, String>) context.getProperty(Constants.APPROVE_NEEDED_SCOPES);
        USSDRequest ussdRequest = new USSDRequest();

        // prepare the USSD message from template
        HashMap<String, String> variableMap = new HashMap<String, String>();
        variableMap.put("application", application.changeApplicationName(serviceProvider));
        for(String api: approveNeededScopes.keySet()){
            apiScopes.append(api).append(",");
        }
        apiScopes.deleteCharAt(apiScopes.length()-1);
        variableMap.put("apis", apiScopes.toString());
        if((boolean)context.getProperty(Constants.APPROVE_ALL_ENABLE)){
            inputs.append("1. Approve Once&#xA;2. Approve Always&#xA;3. Deny");
        }else{
            inputs.append("1. Approve Once&#xA;3. Deny");
        }
        variableMap.put("inputs", inputs.toString());
        String message = OutboundMessage.prepare(client_id, OutboundMessage.MessageType.USSD_APICONSENT, variableMap,
                operator);

        if (log.isDebugEnabled()) {
            log.debug("Message : " + message);
        }

        OutboundUSSDMessageRequest outboundUSSDMessageRequest = new OutboundUSSDMessageRequest();
        outboundUSSDMessageRequest.setAddress("tel:+" + msisdn);
        outboundUSSDMessageRequest.setShortCode(ussdConfig.getShortCode());
        outboundUSSDMessageRequest.setKeyword(ussdConfig.getKeyword());
        outboundUSSDMessageRequest.setOutboundUSSDMessage(message);
        outboundUSSDMessageRequest.setClientCorrelator(sessionID);

        ResponseRequest responseRequest = new ResponseRequest();
        //responseRequest.setNotifyURL(configurationService.getDataHolder().getMobileConnectConfig().getUssdConfig().getLoginNotifyUrl());
        responseRequest.setNotifyURL(ussdNotifyUrl); //todo: get this from config
        responseRequest.setCallbackData("");

        outboundUSSDMessageRequest.setResponseRequest(responseRequest);
        outboundUSSDMessageRequest.setUssdAction(Constants.MTINIT);

        ussdRequest.setOutboundUSSDMessageRequest(outboundUSSDMessageRequest);
        return ussdRequest;
    }
}
