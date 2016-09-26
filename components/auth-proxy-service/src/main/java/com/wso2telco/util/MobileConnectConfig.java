/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com)
 * <p>
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package com.wso2telco.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MobileConnectConfig")
public class MobileConnectConfig {

    protected MSISDN msisdn;
    protected AuthProxy authProxy;

    @XmlElement(name = "MSISDN")
    public MSISDN getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(MSISDN msisdn) {
        this.msisdn = msisdn;
    }

    @XmlElement(name = "AuthProxy")
    public AuthProxy getAuthProxy() {
        return authProxy;
    }

    public void setAuthProxy(AuthProxy authProxy) {
        this.authProxy = authProxy;
    }

    public static class MSISDN {
        private String encryptionKey;

        @XmlElement(name = "EncryptionKey")
        public String getEncryptionKey() {
            return encryptionKey;
        }

        public void setEncryptionKey(String encryptionKey) {
            this.encryptionKey = encryptionKey;
        }

    }

    public static class AuthProxy {
        private String authorizeURL;
        private String ipHeader;
        private String dataSourceName;

        @XmlElement(name = "AuthorizeURL")
        public String getAuthorizeURL() {
            return authorizeURL;
        }

        public void setAuthorizeURL(String authorizeURL) {
            this.authorizeURL = authorizeURL;
        }

        @XmlElement(name = "DataSourceName")
        public String getDataSourceName() {
            return dataSourceName;
        }

        public void setDataSourceName(String dataSourceName) {
            this.dataSourceName = dataSourceName;
        }

        @XmlElement(name = "ISAdminURL")
        public String getIpHeader() {
            return ipHeader;
        }

        public void setIpHeader(String ipHeader) {
            this.ipHeader = ipHeader;
        }
    }
}
