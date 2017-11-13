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
package com.wso2telco;


public class UserRegistrationData {

    private String userName;
    private String msisdn;
    private String openId;
    private String password;
    private String claim;
    private String domain;
    private String fieldValues;
    private long userRegistrationTime;
    private boolean updateProfile;
    private String hashPin;


    public UserRegistrationData(String userName, String msisdn, String openId, String password, String claim, String
            domain, String params, boolean updateProfile) {
        this.userName = userName;
        this.msisdn = msisdn;
        this.openId = openId;
        this.password = password;
        this.claim = claim;
        this.domain = domain;
        this.fieldValues = params;
        this.updateProfile = updateProfile;

        userRegistrationTime = System.currentTimeMillis();
    }


    public String getHashPin() {
        return hashPin;
    }


    public void setHashPin(String hashPin) {
        this.hashPin = hashPin;
    }

    public boolean isUpdateProfile() {
        return updateProfile;
    }


    public void setUpdateProfile(boolean updateProfile) {
        this.updateProfile = updateProfile;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(String fieldValues) {
        this.fieldValues = fieldValues;
    }

    public long getUserRegistrationTime() {
        return userRegistrationTime;
    }
}
