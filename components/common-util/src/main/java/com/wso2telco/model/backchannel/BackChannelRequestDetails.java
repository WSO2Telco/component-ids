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
 * This class is using to keep user details in BackChannel Scenario.
 */
public class BackChannelRequestDetails {
    private String correlationId;
    private String sessionId;
    private String msisdn;
    private String authCode;
    private String notificationBearerToken;
    private String notificationUrl;
    private String requestIniticatedTime;
    private String authRequestId;
    private String clientId;
    private String redirectUrl;
    private String scopes;
    private String operator;
    private boolean isNewUser;
    private String spName;
    private boolean isLongLive;
    private boolean isFullyTrusted;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getNotificationBearerToken() {
        return notificationBearerToken;
    }

    public void setNotificationBearerToken(String notificationBearerToken) {
        this.notificationBearerToken = notificationBearerToken;
    }

    public String getRequestIniticatedTime() {
        return requestIniticatedTime;
    }

    public void setRequestIniticatedTime(String requestIniticatedTime) {
        this.requestIniticatedTime = requestIniticatedTime;
    }

    public String getAuthRequestId() {
        return authRequestId;
    }

    public void setAuthRequestId(String authRequestId) {
        this.authRequestId = authRequestId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }

    public String getSpName() {
        return spName;
    }

    public void setSpName(String spName) {
        this.spName = spName;
    }

    public boolean isLongLive() {
        return isLongLive;
    }

    public void setLongLive(boolean longLive) {
        isLongLive = longLive;
    }

    public boolean isFullyTrusted() {
        return isFullyTrusted;
    }

    public void setFullyTrusted(boolean fullyTrusted) {
        isFullyTrusted = fullyTrusted;
    }
}


