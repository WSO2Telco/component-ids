/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
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
import com.wso2telco.core.sp.config.utils.service.SpConfigService;
import com.wso2telco.core.sp.config.utils.service.impl.SpConfigServiceImpl;
import org.apache.commons.lang.text.StrSubstitutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the static method to prepare USSD/SMS outbound message for a given SP app.
 */
public class OutboundMessage {

    /**
     * Message type enum
     */
    public enum MessageType {
        USSD_LOGIN,
        USSD_REGISTRATION,
        USSD_PIN_LOGIN,
        USSD_PIN_REGISTRATION,
        SMS_LOGIN,
        SMS_REGISTRATION
    }

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /**
     * The SP Configuration service
     */
    private static SpConfigService spConfigService = new SpConfigServiceImpl();

    /**
     * Temporary reference for ussd config
     */
    private static MobileConnectConfig.USSDConfig ussdConfig;

    /**
     * Temporary reference for sms config
     */
    private static MobileConnectConfig.SMSConfig smsConfig;

    /**
     * Map to store operator specific messages loaded from config file
     */
    private static HashMap<String, MobileConnectConfig.OperatorSpecificMessage> operatorSpecificMessageMap = new
            HashMap<>();

    /**
     * Map to store operator specific PIN messages loaded from config file
     */
    private static HashMap<String, MobileConnectConfig.OperatorSpecificMessage> operatorSpecificPinMessageMap = new
            HashMap<>();

    /**
     * Map to store operator specific SMS messages loaded from config file
     */
    private static HashMap<String, MobileConnectConfig.OperatorSpecificMessage> operatorSpecificSmsMessageMap = new
            HashMap<>();

    static {
        ussdConfig = configurationService.getDataHolder().getMobileConnectConfig().getUssdConfig();
        for (MobileConnectConfig.OperatorSpecificMessage osm : ussdConfig.getOperatorSpecificMessages()
                .getOperatorSpecificMessage()) {
            operatorSpecificMessageMap.put(osm.getOperator(), osm);
        }

        for (MobileConnectConfig.OperatorSpecificMessage osm : ussdConfig.getOperatorSpecificPinMessages()
                .getOperatorSpecificPinMessage()) {
            operatorSpecificPinMessageMap.put(osm.getOperator(), osm);
        }

        for (MobileConnectConfig.OperatorSpecificMessage osm : smsConfig.getOperatorSpecificMessages()
                .getOperatorSpecificMessage()) {
            operatorSpecificSmsMessageMap.put(osm.getOperator(), osm);
        }
    }

    /**
     * Prepare the USSD/SMS message from database config table for service provider, operator specific message or default
     * message stored in mobile-connect.xml. Message template can have ${variable} and relevant data to apply to the
     * template should be passed with map parameter.
     *
     * @param clientId    sp client id
     * @param messageType ussd/sms message type
     * @param map         additional variable data map for the message template
     * @param operator    operator name
     * @return prepared ussd message
     */
    public static String prepare(String clientId, MessageType messageType, HashMap<String, String> map, String
            operator) {

        MobileConnectConfig.OperatorSpecificMessage operatorSpecificMessage = null;
        String template = null;

        Map<String, String> data = new HashMap<String, String>();
        // add default map values here
        // data.put("key", "value");

        if (map != null && map.size() > 0) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                data.put(entry.getKey(), entry.getValue());
            }
        }

        // Load operator specific message from hash map
        if (operator != null) {
            if (messageType == MessageType.USSD_LOGIN || messageType == MessageType.USSD_REGISTRATION) {
                operatorSpecificMessage = operatorSpecificMessageMap.get(operator);
            }

            if (messageType == MessageType.USSD_PIN_LOGIN || messageType == MessageType.USSD_PIN_REGISTRATION) {
                operatorSpecificMessage = operatorSpecificPinMessageMap.get(operator);
            }

            if (messageType == MessageType.SMS_LOGIN || messageType == MessageType.SMS_REGISTRATION) {
                operatorSpecificMessage = operatorSpecificSmsMessageMap.get(operator);
            }

            data.put("operator", operator);
        }

        // RULE 1 : first try to get login/registration messages from sp config table
        if (messageType == MessageType.USSD_LOGIN) {
            template = spConfigService.getUSSDLoginMessage(clientId);
        }

        if (messageType == MessageType.USSD_REGISTRATION) {
            template = spConfigService.getUSSDRegistrationMessage(clientId);
        }

        if (messageType == MessageType.USSD_PIN_LOGIN) {
            template = spConfigService.getUSSDPinLoginMessage(clientId);
        }

        if (messageType == MessageType.USSD_PIN_REGISTRATION) {
            template = spConfigService.getUSSDPinRegistrationMessage(clientId);
        }

        if (messageType == MessageType.SMS_LOGIN) {
            template = spConfigService.getSMSLoginMessage(clientId);
        }

        if (messageType == MessageType.SMS_REGISTRATION) {
            template = spConfigService.getSMSRegistrationMessage(clientId);
        }

        if (template == null) {
            // RULE 2 : if message template is not found, try loading them from operator specific config from xml
            if (operatorSpecificMessage != null) {
                if (messageType == MessageType.USSD_LOGIN) {
                    template = operatorSpecificMessage.getLoginMessage();
                }

                if (messageType == MessageType.USSD_REGISTRATION) {
                    template = operatorSpecificMessage.getRegistrationMessage();
                }

                if (messageType == MessageType.USSD_PIN_LOGIN) {
                    template = operatorSpecificMessage.getLoginMessage();
                }

                if (messageType == MessageType.USSD_PIN_REGISTRATION) {
                    template = operatorSpecificMessage.getRegistrationMessage();
                }

                if (messageType == MessageType.SMS_LOGIN) {
                    template = operatorSpecificMessage.getLoginMessage();
                }

                if (messageType == MessageType.SMS_REGISTRATION) {
                    template = operatorSpecificMessage.getRegistrationMessage();
                }
            } else {
                // RULE 3 : if no operator specific message is found, try loading from common messages
                if (messageType == MessageType.USSD_LOGIN) {
                    template = ussdConfig.getUssdLoginMessage();
                }

                if (messageType == MessageType.USSD_REGISTRATION) {
                    template = ussdConfig.getUssdRegistrationMessage();
                }

                if (messageType == MessageType.USSD_PIN_LOGIN) {
                    template = ussdConfig.getPinLoginMessage();
                }

                if (messageType == MessageType.USSD_PIN_REGISTRATION) {
                    template = ussdConfig.getPinRegistrationMessage();
                }

                if (messageType == MessageType.SMS_LOGIN) {
                    template = smsConfig.getLoginMessage();
                }

                if (messageType == MessageType.SMS_REGISTRATION) {
                    template = smsConfig.getRegistrationMessage();
                }
            }
        }

        return template == null ? null : StrSubstitutor.replace(template, data);
    }
}
