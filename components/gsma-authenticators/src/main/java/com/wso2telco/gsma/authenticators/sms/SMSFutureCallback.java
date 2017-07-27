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
package com.wso2telco.gsma.authenticators.sms;

import com.wso2telco.gsma.authenticators.util.BasicFutureCallback;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;

public class SMSFutureCallback extends BasicFutureCallback {
    private static Log log = LogFactory.getLog(SMSFutureCallback.class);

    private UserStatus userStatus;
    private String type;

    public SMSFutureCallback() {
    }

    public SMSFutureCallback(UserStatus userStatus, String type) {
        this.userStatus = userStatus;
        this.type=type;
    }

    public void completed(HttpResponse response) {
        String typetext="SMS";
        DataPublisherUtil.UserState userState=null;
        if ((response.getStatusLine().getStatusCode() == 200)) {
            if(type.equalsIgnoreCase("SMS")){
                userState=DataPublisherUtil.UserState.SEND_SMS;
            }else if(type.equalsIgnoreCase("SMSOTP")){
                userState=DataPublisherUtil.UserState.SEND_SMS_OTP;
                typetext="SMS OTP";
            }else{
                userState=DataPublisherUtil.UserState.SEND_SMS;
            }
            log.info("Success Request - " + postRequest.getURI().getSchemeSpecificPart());
            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus, userState, typetext+" sent");

        } else {
            if(type.equalsIgnoreCase("SMS")){
                userState=DataPublisherUtil.UserState.SEND_SMS_FAIL;
            }else if(type.equalsIgnoreCase("SMSOTP")){
                userState=DataPublisherUtil.UserState.SEND_SMS_OTP_FAIL;
                typetext="SMS OTP";
            }else{
                userState=DataPublisherUtil.UserState.SEND_SMS_FAIL;
            }
            log.error("Failed Request - " + postRequest.getURI().getSchemeSpecificPart());
            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus, userState, typetext+" sending " +
                            "failed");
        }
        closeClient();
    }

    public void failed(Exception exception) {
        String typetext="SMS";
        DataPublisherUtil.UserState userState=null;
        if(type.equalsIgnoreCase("SMS")){
            userState=DataPublisherUtil.UserState.SEND_SMS_FAIL;
        }else if(type.equalsIgnoreCase("SMSOTP")){
            userState=DataPublisherUtil.UserState.SEND_SMS_OTP_FAIL;
            typetext="SMS OTP";
        }else{
            userState=DataPublisherUtil.UserState.SEND_SMS_FAIL;
        }
        DataPublisherUtil
                .updateAndPublishUserStatus(userStatus,userState, typetext+" sending " +
                        "failed");
        super.failed(exception);
    }
}
