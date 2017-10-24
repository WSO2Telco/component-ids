/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com)
 * <p>
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.gsma.authenticators.sms;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.Utility;
import com.wso2telco.gsma.authenticators.model.SMSMessage;
import com.wso2telco.gsma.authenticators.util.BasicFutureCallback;
import com.wso2telco.gsma.authenticators.util.OutboundMessage;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;

// TODO: Auto-generated Javadoc

/**
 * The Class SMSAuthenticator.
 */
public class SMSOTPAuthenticator extends SMSAuthenticator {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1189332409518227376L;

    //private Log log = LogFactory.getLog(SMSOTPAuthenticator.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.identity.application.authentication.framework
     * .AbstractApplicationAuthenticator#initiateAuthenticationRequest(javax.servlet.http.HttpServletRequest, javax
     * .servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context
     * .AuthenticationContext)
     */
    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws AuthenticationFailedException {

        //log.info("Initiating authentication request");
        UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);
        SMSMessage smsMessage = getRedirectInitAuthentication(response, context, userStatus);
        if (smsMessage != null && smsMessage.getRedirectURL() != null && !smsMessage.getRedirectURL().isEmpty()) {
            try {
                MobileConnectConfig connectConfig = configurationService.getDataHolder().getMobileConnectConfig();
                MobileConnectConfig.SMSConfig smsConfig = connectConfig.getSmsConfig();
                int otpLength = smsConfig.getOTPLength();
                //String otp = Utility.genarateOTP(otpLength);
                String otp =smsMessage.getMsisdn().substring(smsMessage.getMsisdn().length()-smsConfig.getOTPLength());
                String hashedotp = Utility.generateSHA256Hash(otp);
                String sessionDataKey = context.getContextIdentifier();
                DBUtils.insertOTPForSMS(sessionDataKey, hashedotp, UserResponse.PENDING.name());
                // prepare the USSD message from template
                HashMap<String, String> variableMap = new HashMap<String, String>();
                variableMap.put("smsotp", otp);
                String otpmessageText = OutboundMessage.prepare(smsMessage.getClient_id(),
                        OutboundMessage.MessageType.SMS_OTP, variableMap, smsMessage.getOperator());
                smsMessage.setMessageText(otpmessageText + smsMessage.getMessageText());
               /* if (log.isDebugEnabled()) {
                    log.debug("OTP Message: " + smsMessage.getMessageText());
                }
                log.info("OTP Message: " + smsMessage.getMessageText());*/
                BasicFutureCallback futureCallback = userStatus != null ? new SMSFutureCallback(
                        userStatus.cloneUserStatus(), "SMSOTP") : new SMSFutureCallback();
                smsMessage.setFutureCallback(futureCallback);
                String smsResponse = new SendSMS().sendSMS(smsMessage.getMsisdn(), smsMessage.getMessageText(),smsMessage.getOperator(), smsMessage.getFutureCallback());
                response.sendRedirect(smsMessage.getRedirectURL());
            } catch (Exception e) {
                DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                        DataPublisherUtil.UserState.SMS_AUTH_PROCESSING_FAIL, e.getMessage());
                throw new AuthenticationFailedException(e.getMessage(), e);
            }
        } else {
            throw new AuthenticationFailedException("SMS Authentication failed while trying to authenticate");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.identity.application.authentication.framework
     * .AbstractApplicationAuthenticator#processAuthenticationResponse(javax.servlet.http.HttpServletRequest, javax
     * .servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context
     * .AuthenticationContext)
     */
    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws AuthenticationFailedException {
        String sessionDataKey = request.getParameter("sessionDataKey");
        String status = null;
        try {
            super.processAuthenticationResponse(request, response, context);
            status = UserResponse.APPROVED.name();            
        } catch (AuthenticationFailedException e) {
            status = UserResponse.REJECTED.name();
            throw e;
        } finally {
            try {
                if (sessionDataKey != null && !sessionDataKey.isEmpty() && status != null && !status.isEmpty()) {
                    DBUtils.updateOTPForSMS(sessionDataKey, status);
                }
            } catch (Exception e) {
                //log.error("Error while updating sms otp status", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getFriendlyName()
     */
    @Override
    public String getFriendlyName() {
        return Constants.SMSOTP_AUTHENTICATOR_FRIENDLY_NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
     */
    @Override
    public String getName() {
        return Constants.SMSOTP_AUTHENTICATOR_NAME;
    }

}
