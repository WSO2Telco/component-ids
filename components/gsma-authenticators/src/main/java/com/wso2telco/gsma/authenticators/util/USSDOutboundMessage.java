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

package com.wso2telco.gsma.authenticators.util;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import org.apache.commons.lang.text.StrSubstitutor;

import java.util.HashMap;
import java.util.Map;

public class USSDOutboundMessage {

    public enum MessageType{
        LOGIN,
        REGISTRATION
    }

    /** The Configuration service */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    private static MobileConnectConfig.USSDConfig ussdConfig;

    private static HashMap<String, MobileConnectConfig.OperatorSpecificMessage> operatorSpecificMessageMap = new HashMap<>();

    static {
        ussdConfig = configurationService.getDataHolder().getMobileConnectConfig().getUssdConfig();
        for(MobileConnectConfig.OperatorSpecificMessage osm : ussdConfig.getOperatorSpecificMessages().getOperatorSpecificMessage()){
            operatorSpecificMessageMap.put(osm.getOperator(), osm);
        }
    }

    public static String prepare(MessageType messageType, HashMap<String, String> map, String operator){

        MobileConnectConfig.OperatorSpecificMessage operatorSpecificMessage = null;
        String template = "";
        Map<String, String> data = new HashMap<String, String>();
        // add default map values here
        // data.put("key", "value");

        if(map != null && map.size() > 0) {
            for (Map.Entry<String, String> entry : map.entrySet()){
                data.put(entry.getKey(), entry.getValue());
            }
        }

        // Load operator specific message from hash map
        if(operator != null) {
            operatorSpecificMessage = operatorSpecificMessageMap.get(operator);
            data.put("operator", operator);
        }

        if(operatorSpecificMessage != null){
            if(messageType == MessageType.LOGIN){
                template = operatorSpecificMessage.getLoginMessage();
            }

            if(messageType == MessageType.REGISTRATION){
                template = operatorSpecificMessage.getRegistrationMessage();
            }
        }else{
            if(messageType == MessageType.LOGIN){
                template = ussdConfig.getUssdLoginMessage();
            }

            if(messageType == MessageType.REGISTRATION){
                template = ussdConfig.getUssdRegistrationMessage();
            }
        }

        return StrSubstitutor.replace(template, data);
    }
}
