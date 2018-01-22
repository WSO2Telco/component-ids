/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
package com.wso2telco.gsma.authenticators.model;

public class SpConsent {

    private String consumerKey;

    private int operatorId;

    private int scopeId;

    private int expPeriod;

    private String consentType;

    private String validityType;

    private int consentId;


    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public int getOperatorID() {
        return operatorId;
    }

    public void setOperatorID(int operatorID) {
        this.operatorId = operatorId;
    }

    public int getScope() {
        return scopeId;
    }

    public void setScope(int scope) {
        this.scopeId = scope;
    }

    public int getExpPeriod() {
        return expPeriod;
    }

    public void setExpPeriod(int expPeriod) {
        this.expPeriod = expPeriod;
    }

    public String getConsentType() {
        return consentType;
    }

    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }

    public String getValidityType() {
        return validityType;
    }

    public void setValidityType(String validityType) {
        this.validityType = validityType;
    }

    public int getConsentId() {
        return consentId;
    }

    public void setConsentId(int consentId) {
        this.consentId = consentId;
    }
}
