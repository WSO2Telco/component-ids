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
import com.wso2telco.core.config.model.OperatorSmsConfig;
import com.wso2telco.core.sp.config.utils.exception.DataAccessException;
import com.wso2telco.core.sp.config.utils.service.SpConfigService;
import com.wso2telco.core.sp.config.utils.service.impl.SpConfigServiceImpl;
import com.wso2telco.core.sp.config.utils.util.ConfigKey;
import com.wso2telco.gsma.authenticators.sms.SMSFutureCallback;
import com.wso2telco.gsma.authenticators.sms.SendSMS;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WelcomeSmsUtil {

    private static SpConfigService spConfigService = new SpConfigServiceImpl();

    private static Log log = LogFactory.getLog(WelcomeSmsUtil.class);

    public static void handleWelcomeSms(AuthenticationContext context, UserStatus userStatus, String msisdn, String
            operator,
                                  MobileConnectConfig.SMSConfig smsConfig)
            throws DataAccessException, IOException, AuthenticationFailedException {

        BasicFutureCallback futureCallback = userStatus != null ? new SMSFutureCallback(userStatus.cloneUserStatus(),"SMS") : new SMSFutureCallback();

        List<OperatorSmsConfig> operatorSmsConfigs = smsConfig.getOperatorSmsConfigs();

        SendSMS sendSMS = new SendSMS();

        boolean isWelcomeSmsDisabledForOperator = false;
        OperatorSmsConfig operatorSmsConfig = null;

        for (OperatorSmsConfig config : operatorSmsConfigs) {

            isWelcomeSmsDisabledForOperator = config.getName().equals(operator) && config.isDisabled();

            if (!isWelcomeSmsDisabledForOperator) {
                operatorSmsConfig = config;
                break;
            }
        }
        if (!isWelcomeSmsDisabledForOperator) {

            Map<String, String> welcomeSMSConfig = spConfigService.getWelcomeSMSConfig(context.getRelyingParty());

            String welcomeSmsDisabledForAllSps = welcomeSMSConfig.get(ConfigKey.ALL);
            if (welcomeSmsDisabledForAllSps != null) {

                if ("false".equalsIgnoreCase(welcomeSmsDisabledForAllSps)) {
                    sendWelcomeSms(msisdn, operator, futureCallback, sendSMS, operatorSmsConfig, welcomeSMSConfig,
                            context.getRelyingParty());
                } else {
                    log.info("Welcome Sms disabled for all operators for client id [ " + context.getRelyingParty() +
                            " ]");
                }
            } else {
                sendWelcomeSms(msisdn, operator, futureCallback, sendSMS, operatorSmsConfig, welcomeSMSConfig,
                        context.getRelyingParty());
            }
        } else {
            log.info("Welcome Sms disabled for operator [ msisdn : " + msisdn + " , operator : " +
                    operator + " ]");
        }
    }

    private static void sendWelcomeSms(String msisdn, String operator, BasicFutureCallback futureCallback, SendSMS sendSMS,
                                OperatorSmsConfig operatorSmsConfig, Map<String, String> welcomeSMSConfig, String
                                        clientId) throws IOException, AuthenticationFailedException {

        String welcomeSmsDisabledForCurrentSp = welcomeSMSConfig.get(operator.trim());
        
        if (welcomeSmsDisabledForCurrentSp != null) {	
        	 if ("false".equalsIgnoreCase(welcomeSmsDisabledForCurrentSp)) {
        		 log.info("Sending Welcome sms [ msisdn : " + msisdn + " , operator : " + operator + " ]");
                 sendSMS.sendSMS(msisdn, operatorSmsConfig.getMessage(), operator, futureCallback);
        	 } else {
        		 log.info("Welcome message is disabled [ client id :" + clientId + " , operator : " + operator + " ]");
        	 }  
        } else {
        	 log.info("Sending Welcome sms [ msisdn : " + msisdn + " , operator : " + operator + " ]");
             sendSMS.sendSMS(msisdn, operatorSmsConfig.getMessage(), operator, futureCallback);
        }
               
    }
}
