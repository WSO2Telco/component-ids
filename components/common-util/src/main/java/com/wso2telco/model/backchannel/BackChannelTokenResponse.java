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
 * This class is using to keep token response details in BackChannel Scenario.
 */
public class BackChannelTokenResponse {

    private String auth_req_id;
    private String access_token;
    private String token_type;
    private String id_token;
    private int expires_in;
    private String refresh_token;
    private String correlation_id;
    private String error;
    private String error_description;

    public BackChannelTokenResponse() {
    }

    public BackChannelTokenResponse(String status, BackChannelTokenResponse backChannelTokenResponse) {

        if (status.equalsIgnoreCase("APPROVED")) {
            this.auth_req_id = backChannelTokenResponse.getAuthReqId();
            this.access_token = backChannelTokenResponse.getAccessToken();
            this.token_type = backChannelTokenResponse.getTokenType();
            this.id_token = backChannelTokenResponse.getIdToken();
            this.expires_in = backChannelTokenResponse.getExpiresIn();
            this.refresh_token = backChannelTokenResponse.getRefreshToken();
            this.correlation_id = backChannelTokenResponse.getCorrelationId();
        } else {
            this.auth_req_id = backChannelTokenResponse.getAuthReqId();
            this.correlation_id = backChannelTokenResponse.getCorrelationId();
            this.error = backChannelTokenResponse.getError();
            this.error_description = backChannelTokenResponse.getErrorDescription();
        }
    }

    public String getAuthReqId() {
        return auth_req_id;
    }

    public void setAuthReqId(String auth_req_id) {
        this.auth_req_id = auth_req_id;
    }

    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String access_token) {
        this.access_token = access_token;
    }

    public String getTokenType() {
        return token_type;
    }

    public void setTokenType(String token_type) {
        this.token_type = token_type;
    }

    public String getIdToken() {
        return id_token;
    }

    public void setIdToken(String id_token) {
        this.id_token = id_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getCorrelationId() {
        return correlation_id;
    }

    public void setCorrelationId(String correlation_id) {
        this.correlation_id = correlation_id;
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

    public int getExpiresIn() {
        return expires_in;
    }

    public void setExpiresIn(int expires_in) {
        this.expires_in = expires_in;
    }
}
