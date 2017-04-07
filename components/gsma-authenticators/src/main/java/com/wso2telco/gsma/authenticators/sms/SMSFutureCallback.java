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

    public SMSFutureCallback() {
    }

    public SMSFutureCallback(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public void completed(HttpResponse response) {
        if ((response.getStatusLine().getStatusCode() == 200)) {
            log.info("Success Request - " + postRequest.getURI().getSchemeSpecificPart());
            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.SEND_SMS, "SMS sent");

        } else {
            log.error("Failed Request - " + postRequest.getURI().getSchemeSpecificPart());
            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.SEND_SMS_FAIL, "SMS sending " +
                            "failed");
        }
        closeClient();
    }

    public void failed(Exception exception) {
        DataPublisherUtil
                .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.SEND_SMS_FAIL, "SMS sending " +
                        "failed");
        super.failed(exception);
    }
}
