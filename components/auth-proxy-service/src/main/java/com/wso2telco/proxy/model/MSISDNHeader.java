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
package com.wso2telco.proxy.model;

/**
 * This class is using to keep decryption properties of msisdn header.
 */
public class MSISDNHeader {
    private String msisdnHeaderName;
    private boolean isHeaderEncrypted;
    private String headerEncryptionMethod;
    private String headerEncryptionKey;
    private int priority;

    public String getMsisdnHeaderName() {
        return msisdnHeaderName;
    }

    public void setMsisdnHeaderName(String msisdnHeaderName) {
        this.msisdnHeaderName = msisdnHeaderName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getHeaderEncryptionKey() {
        return headerEncryptionKey;
    }

    public void setHeaderEncryptionKey(String headerEncryptionKey) {
        this.headerEncryptionKey = headerEncryptionKey;
    }

    public String getHeaderEncryptionMethod() {
        return headerEncryptionMethod;
    }

    public void setHeaderEncryptionMethod(String headerEncryptionMethod) {
        this.headerEncryptionMethod = headerEncryptionMethod;
    }

    public boolean isHeaderEncrypted() {
        return isHeaderEncrypted;
    }

    public void setHeaderEncrypted(boolean headerEncrypted) {
        isHeaderEncrypted = headerEncrypted;
    }
}
