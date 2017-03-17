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

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by isuru on 3/9/17.
 */
public class WelcomeSmsUtil {

    private static SpConfigService spConfigService = new SpConfigServiceImpl();

    private static Log log = LogFactory.getLog(WelcomeSmsUtil.class);

    public static void handleWelcomeSms(AuthenticationContext context, UserStatus userStatus, String msisdn, String
            operator,
                                  MobileConnectConfig.SMSConfig smsConfig) throws DataAccessException, IOException {

        BasicFutureCallback futureCallback = userStatus != null ? new SMSFutureCallback(userStatus.cloneUserStatus()) : new SMSFutureCallback();

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
                                        clientId) throws IOException {

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
