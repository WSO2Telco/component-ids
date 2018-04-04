/*******************************************************************************
 * Copyright  (c) 2015-2018, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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

package com.wso2telco.model.backchannel;

/**
 * This class is using to keep auth validation response details in BackChannel Scenario.
 */
public class BackChannelOauthResponse {
    private String auth_req_id;
    private String correlation_id;
    private String error;
    private String error_description;

    public String getAuthReqId() {
        return auth_req_id;
    }

    public void setAuthReqId(String auth_req_id) {
        this.auth_req_id = auth_req_id;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return error_description;
    }

    public void setErrorDescription(String error_description) {
        this.error_description = error_description;
    }

    public String getCorrelationId() {
        return correlation_id;
    }

    public void setCorrelationId(String correlation_id) {
        this.correlation_id = correlation_id;
    }
}
