/*******************************************************************************
 * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
package com.wso2telco.sp.entity;

public class Application {
    private String appName;

    private String appStatus;

    private String description;

    private String devStatus;

    private String devName;

    private SupportedApis[] supportedApis;

    private String devId;

    private String devOrgId;

    private String redirectUri;

    private String appType;

    private String appCredId;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(String appStatus) {
        this.appStatus = appStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDevStatus() {
        return devStatus;
    }

    public void setDevStatus(String devStatus) {
        this.devStatus = devStatus;
    }

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public SupportedApis[] getSupportedApis() {
        return supportedApis;
    }

    public void setSupportedApis(SupportedApis[] supportedApis) {
        this.supportedApis = supportedApis;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getDevOrgId() {
        return devOrgId;
    }

    public void setDevOrgId(String devOrgId) {
        this.devOrgId = devOrgId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppCredId() {
        return appCredId;
    }

    public void setAppCredId(String appCredId) {
        this.appCredId = appCredId;
    }

    @Override
    public String toString() {
        return "ClassPojo [appName = " + appName + ", appStatus = " + appStatus + ", description = " + description
                + ", devStatus = " + devStatus + ", devName = " + devName + ", supportedApis = " + supportedApis
                + ", devId = " + devId + ", devOrgId = " + devOrgId + ", redirectUri = " + redirectUri + ", appType = "
                + appType + ", appCredId = " + appCredId + "]";
    }
}