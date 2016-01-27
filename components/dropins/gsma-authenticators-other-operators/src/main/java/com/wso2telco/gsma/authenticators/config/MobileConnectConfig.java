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
package com.wso2telco.gsma.authenticators.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class MobileConnectConfig.
 */
@XmlRootElement(name = "MobileConnectConfig")
public class MobileConnectConfig {

    /** The data source name. */
    private String dataSourceName;
    
    /** The encrypt append. */
    private String encryptAppend;
    
    /** The keyfile. */
    private String keyfile;
    
    /** The gsma exchange config. */
    private GSMAExchangeConfig gsmaExchangeConfig;
    
    /** The sms config. */
    private SMSConfig smsConfig;
    
    /** The ussd config. */
    private USSDConfig ussdConfig;
    
    /** The listener webapp host. */
    private String listenerWebappHost;
    
    /** The me pin config. */
    private MePinConfig mePinConfig;
    
    /** The android config. */
    private AndroidConfig androidConfig;

    /** The mss. */
    @XmlElement(name = "MSS")
    protected MSS mss;
    
    /** The headerenrich. */
    @XmlElement(name = "HEADERENRICH")
    protected HEADERENRICH headerenrich;

    /**
     * Gets the data source name.
     *
     * @return the data source name
     */
    @XmlElement(name = "DataSourceName", defaultValue = "jdbc/CONNECT_DB")
    public String getDataSourceName() {
        return dataSourceName;
    }
    
    /**
     * Gets the encrypt append.
     *
     * @return the encrypt append
     */
    @XmlElement(name = "EncryptAppend")
    public String getEncryptAppend() {
        return encryptAppend;
    }
    
    /**
     * Gets the keyfile.
     *
     * @return the keyfile
     */
    @XmlElement(name = "Keyfile")
    public String getKeyfile() {
        return keyfile;
    }

    /**
     * Gets the gsma exchange config.
     *
     * @return the gsma exchange config
     */
    @XmlElement(name = "GSMAExchangeConfig")
    public GSMAExchangeConfig getGsmaExchangeConfig() {
        return gsmaExchangeConfig;
    }

    /**
     * Gets the sms config.
     *
     * @return the sms config
     */
    @XmlElement(name = "SMS")
    public SMSConfig getSmsConfig() {
        return smsConfig;
    }

    /**
     * Gets the ussd config.
     *
     * @return the ussd config
     */
    @XmlElement(name = "USSD")
    public USSDConfig getUssdConfig() {
        return ussdConfig;
    }


    /**
     * Gets the android config.
     *
     * @return the android config
     */
    @XmlElement(name = "ANDROID")
    public AndroidConfig getAndroidConfig() {
        return androidConfig;
    }

    /**
     * Gets the listener webapp host.
     *
     * @return the listener webapp host
     */
    @XmlElement(name = "ListenerWebappHost")
    public String getListenerWebappHost() {
        if (listenerWebappHost == null || listenerWebappHost.isEmpty()) {
            return "http://" + System.getProperty("carbon.local.ip") + ":9764";
        }
        return listenerWebappHost;
    }

    /**
     * Gets the me pin config.
     *
     * @return the me pin config
     */
    @XmlElement(name = "MePIN")
    public MePinConfig getMePinConfig() {
        return mePinConfig;
    }
    
    /**
     * Sets the data source name.
     *
     * @param dataSourceName the new data source name
     */
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
    
    /**
     * Sets the encrypt append.
     *
     * @param EncryptAppend the new encrypt append
     */
    public void setEncryptAppend(String EncryptAppend) {
        this.encryptAppend = EncryptAppend;
    }
    
    /**
     * Sets the keyfile.
     *
     * @param Keyfile the new keyfile
     */
    public void setKeyfile(String Keyfile) {
        this.keyfile = Keyfile;
    }

    /**
     * Sets the gsma exchange config.
     *
     * @param gsmaExchangeConfig the new gsma exchange config
     */
    public void setGsmaExchangeConfig(GSMAExchangeConfig gsmaExchangeConfig) {
        this.gsmaExchangeConfig = gsmaExchangeConfig;
    }
    
    /**
     * Sets the sms config.
     *
     * @param smsConfig the new sms config
     */
    public void setSmsConfig(SMSConfig smsConfig) {
        this.smsConfig = smsConfig;
    }

    /**
     * Sets the ussd config.
     *
     * @param ussdConfig the new ussd config
     */
    public void setUssdConfig(USSDConfig ussdConfig) {
        this.ussdConfig = ussdConfig;
    }

    /**
     * Sets the listener webapp host.
     *
     * @param listenerWebappHost the new listener webapp host
     */
    public void setListenerWebappHost(String listenerWebappHost) {
        this.listenerWebappHost = listenerWebappHost;
    }


    /**
     * Sets the android config.
     *
     * @param androidConfig the new android config
     */
    public void setAndroidConfig(AndroidConfig androidConfig) {
        this.androidConfig = androidConfig;
    }


    /**
     * Sets the me pin config.
     *
     * @param mePinConfig the new me pin config
     */
    public void setMePinConfig(MePinConfig mePinConfig) {
        this.mePinConfig = mePinConfig;
    }

    /**
     * Gets the headerenrich.
     *
     * @return the headerenrich
     */
    public HEADERENRICH getHEADERENRICH() {
        return headerenrich;
    }

    /**
     * Sets the headerenrich.
     *
     * @param value the new headerenrich
     */
    public void setHEADERENRICH(HEADERENRICH value) {
        this.headerenrich = value;
    }

    /**
     * The Class GSMAExchangeConfig.
     */
    public static class GSMAExchangeConfig {
        
        /** The serving operator host. */
        private String servingOperatorHost;
        
        /** The organization. */
        private String organization;
        
        /** The auth token. */
        private String authToken;
        
        /** The serving operator. */
        private ServingOperator servingOperator;

        /**
         * Gets the serving operator host.
         *
         * @return the serving operator host
         */
        @XmlElement(name = "SOHost")
        public String getServingOperatorHost() {
            return servingOperatorHost;
        }

        /**
         * Gets the organization.
         *
         * @return the organization
         */
        @XmlElement(name = "Organization")
        public String getOrganization() {
            return organization;
        }

        /**
         * Gets the auth token.
         *
         * @return the auth token
         */
        @XmlElement(name = "AuthToken")
        public String getAuthToken() {
            return authToken;
        }
        
        /**
         * Gets the serving operator.
         *
         * @return the serving operator
         */
        @XmlElement(name = "ServingOperator")
		public ServingOperator getServingOperator() {
			return servingOperator;
		}

        /**
         * Sets the serving operator host.
         *
         * @param servingOperatorHost the new serving operator host
         */
        public void setServingOperatorHost(String servingOperatorHost) {
            this.servingOperatorHost = servingOperatorHost;
        }

        /**
         * Sets the organization.
         *
         * @param organization the new organization
         */
        public void setOrganization(String organization) {
            this.organization = organization;
        }

        /**
         * Sets the auth token.
         *
         * @param authToken the new auth token
         */
        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

		/**
		 * Sets the serving operator.
		 *
		 * @param servingOperator the new serving operator
		 */
		public void setServingOperator(ServingOperator servingOperator) {
			this.servingOperator = servingOperator;
		}
    }

    /**
     * The Class SMSConfig.
     */
    public static class SMSConfig {

        /** The endpoint. */
        private String endpoint;
        
        /** The auth token. */
        private String authToken;
        
        /** The message. */
        private String message;

        /**
         * Gets the endpoint.
         *
         * @return the endpoint
         */
        @XmlElement(name = "Endpoint")
        public String getEndpoint() {
            return endpoint;
        }

        /**
         * Gets the auth token.
         *
         * @return the auth token
         */
        @XmlElement(name = "AuthToken")
        public String getAuthToken() {
            return authToken;
        }

        /**
         * Gets the message.
         *
         * @return the message
         */
        @XmlElement(name = "MessageContent")
        public String getMessage() {
            return message;
        }

        /**
         * Sets the endpoint.
         *
         * @param endpoint the new endpoint
         */
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        /**
         * Sets the auth token.
         *
         * @param authToken the new auth token
         */
        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        /**
         * Sets the message.
         *
         * @param message the new message
         */
        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * The Class USSDConfig.
     */
    public static class USSDConfig {

        /** The endpoint. */
        private String endpoint;
        
        /** The auth token. */
        private String authToken;
        
        /** The message. */
        private String message;
        
        /** The short code. */
        private String shortCode;
        
        /** The keyword. */
        private String keyword;
        
        /** The pinauth. */
        private String pinauth;
        
        /** The dash board. */
        private String dashBoard;

        /**
         * Gets the endpoint.
         *
         * @return the endpoint
         */
        @XmlElement(name = "Endpoint")
        public String getEndpoint() {
            return endpoint;
        }

        /**
         * Gets the auth token.
         *
         * @return the auth token
         */
        @XmlElement(name = "AuthToken")
        public String getAuthToken() {
            return authToken;
        }

        /**
         * Gets the message.
         *
         * @return the message
         */
        @XmlElement(name = "MessageContent")
        public String getMessage() {
            return message;
        }

        /**
         * Gets the short code.
         *
         * @return the short code
         */
        @XmlElement(name = "ShortCode")
        public String getShortCode() {
            return shortCode;
        }

        /**
         * Gets the keyword.
         *
         * @return the keyword
         */
        @XmlElement(name = "Keyword")
        public String getKeyword() {
            return keyword;
        }

        /**
         * Gets the pinauth.
         *
         * @return the pinauth
         */
        @XmlElement(name = "Pinauth")
        public String getPinauth() {
            return pinauth;
        }
        
        /**
         * Gets the dash board.
         *
         * @return the dash board
         */
        @XmlElement(name = "DashBoard")
        public String getDashBoard() {
            return dashBoard;
        }
        
        
        /**
         * Sets the endpoint.
         *
         * @param endpoint the new endpoint
         */
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        /**
         * Sets the auth token.
         *
         * @param authToken the new auth token
         */
        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        /**
         * Sets the message.
         *
         * @param message the new message
         */
        public void setMessage(String message) {
            this.message = message;
        }

        /**
         * Sets the short code.
         *
         * @param shortCode the new short code
         */
        public void setShortCode(String shortCode) {
            this.shortCode = shortCode;
        }

        /**
         * Sets the keyword.
         *
         * @param keyword the new keyword
         */
        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }
        
        /**
         * Sets the pinauth.
         *
         * @param pinauth the new pinauth
         */
        public void setPinauth(String pinauth) {
            this.pinauth = pinauth;
        }
        
        /**
         * Sets the dash board.
         *
         * @param dashBoard the new dash board
         */
        public void setDashBoard(String dashBoard) {
            this.dashBoard = dashBoard;
        }
        
    }

    /**
     * The Class HEADERENRICH.
     */
    public static class HEADERENRICH {

        /** The endpoint. */
        private String endpoint;
        
        /** The enrichflg. */
        private String enrichflg;
        
        /** The message. */
        private String message;
        
        /** The mobile ip ranges. */
        private List<String> mobileIPRanges;

        /**
         * Gets the mobile ip ranges.
         *
         * @return the mobile ip ranges
         */
        @XmlElementWrapper(name = "MobileIPRanges")
        @XmlElement(name = "IPRange")
        public List<String> getMobileIPRanges() {
            return mobileIPRanges;
        }

        /**
         * Sets the mobile ip ranges.
         *
         * @param mobileIPRanges the new mobile ip ranges
         */
        public void setMobileIPRanges(List<String> mobileIPRanges) {
            this.mobileIPRanges = mobileIPRanges;
        }

        /**
         * Gets the endpoint.
         *
         * @return the endpoint
         */
        @XmlElement(name = "Endpoint")
        public String getEndpoint() {
            return endpoint;
        }

        /**
         * Gets the enrichflg.
         *
         * @return the enrichflg
         */
        @XmlElement(name = "Enrichflg")
        public String getEnrichflg() {
            return enrichflg;
        }

        /**
         * Sets the endpoint.
         *
         * @param endpoint the new endpoint
         */
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        /**
         * Sets the enrichflg.
         *
         * @param enrichflg the new enrichflg
         */
        public void setEnrichflg(String enrichflg) {
            this.enrichflg = enrichflg;
        }
        
        
    }

    /**
     * Gets the mss.
     *
     * @return the mss
     */
    public MSS getMSS() {
        return mss;
    }

    /**
     * Sets the mss.
     *
     * @param mss the new mss
     */
    public void setMSS(MSS mss) {
        this.mss = mss;
    }

    /**
     * The Class MSS.
     */
    public static class MSS {
        
        /** The endpoint. */
        private String endpoint;
        
        /** The success status. */
        private int successStatus;
        
        /** The mss text. */
        private String mssText;
        
        /** The mss pin test. */
        private String mssPinTest;

        /**
         * Gets the mss text.
         *
         * @return the mss text
         */
        @XmlElement(name = "MssText")
        public String getMssText() {
            return mssText;
        }

        /**
         * Sets the mss text.
         *
         * @param mssText the new mss text
         */
        public void setMssText(String mssText) {
            this.mssText = mssText;
        }

        /**
         * Gets the mss pin test.
         *
         * @return the mss pin test
         */
        @XmlElement(name = "MssPinText")
        public String getMssPinTest() {
            return mssPinTest;
        }

        /**
         * Sets the mss pin test.
         *
         * @param mssPinTest the new mss pin test
         */
        public void setMssPinTest(String mssPinTest) {
            this.mssPinTest = mssPinTest;
        }



        /**
         * Gets the endpoint.
         *
         * @return the endpoint
         */
        @XmlElement(name = "Endpoint")
        public String getEndpoint() {
            return endpoint;
        }

        /**
         * Gets the success status.
         *
         * @return the success status
         */
        @XmlElement(name = "SuccessStatus")
        public int getSuccessStatus() {
            return successStatus;
        }

        /**
         * Sets the endpoint.
         *
         * @param endpoint the new endpoint
         */
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        /**
         * Sets the success status.
         *
         * @param successStatus the new success status
         */
        public void setSuccessStatus(int successStatus) {
            this.successStatus = successStatus;
        }

    }

    /**
     * The Class MePinConfig.
     */
    public static class MePinConfig {

        /** The endpoint. */
        private String endpoint;
        
        /** The client id. */
        private String clientID;
        
        /** The auth token. */
        private String authToken;
        
        /** The confirmation policy. */
        private String confirmationPolicy;
        
        /** The short message text. */
        private String shortMessageText;
        
        /** The header text. */
        private String headerText;
        
        /** The message text. */
        private String messageText;

        /**
         * Gets the endpoint.
         *
         * @return the endpoint
         */
        @XmlElement(name = "Endpoint")
        public String getEndpoint() {
            return endpoint;
        }

        /**
         * Gets the client id.
         *
         * @return the client id
         */
        @XmlElement(name = "ClientID")
        public String getClientID() {
            return clientID;
        }

        /**
         * Gets the auth token.
         *
         * @return the auth token
         */
        @XmlElement(name = "AuthToken")
        public String getAuthToken() {
            return authToken;
        }

        /**
         * Gets the confirmation policy.
         *
         * @return the confirmation policy
         */
        @XmlElement(name = "ConfirmationPolicy")
        public String getConfirmationPolicy() {
            return confirmationPolicy;
        }

        /**
         * Gets the short message text.
         *
         * @return the short message text
         */
        @XmlElement(name = "ShortMessageText")
        public String getShortMessageText() {
            return shortMessageText;
        }

        /**
         * Gets the header text.
         *
         * @return the header text
         */
        @XmlElement(name = "HeaderText")
        public String getHeaderText() {
            return headerText;
        }

        /**
         * Gets the message text.
         *
         * @return the message text
         */
        @XmlElement(name = "MessageText")
        public String getMessageText() {
            return messageText;
        }

        /**
         * Sets the endpoint.
         *
         * @param endpoint the new endpoint
         */
        public void setEndpoint(String endpoint) {this.endpoint = endpoint;}

        /**
         * Sets the client id.
         *
         * @param clientID the new client id
         */
        public void setClientID(String clientID) {this.clientID = clientID;}

        /**
         * Sets the auth token.
         *
         * @param authToken the new auth token
         */
        public void setAuthToken(String authToken) {this.authToken = authToken;}

        /**
         * Sets the confirmation policy.
         *
         * @param confirmationPolicy the new confirmation policy
         */
        public void setConfirmationPolicy(String confirmationPolicy) {this.confirmationPolicy = confirmationPolicy;}

        /**
         * Sets the short message text.
         *
         * @param shortMessageText the new short message text
         */
        public void setShortMessageText(String shortMessageText) {this.shortMessageText = shortMessageText;}

        /**
         * Sets the header text.
         *
         * @param headerText the new header text
         */
        public void setHeaderText(String headerText) {this.headerText = headerText;}

        /**
         * Sets the message text.
         *
         * @param messageText the new message text
         */
        public void setMessageText(String messageText) {this.messageText = messageText;}
    }


    /**
     * The Class AndroidConfig.
     */
    public static class AndroidConfig {

        /** The endpoint. */
        private String endpoint;
        
        /** The dash board. */
        private String dashBoard;
        
        /** The API key. */
        private String APIKey;

        /**
         * Gets the endpoint.
         *
         * @return the endpoint
         */
        @XmlElement(name = "Endpoint")
        public String getEndpoint() {
            return endpoint;
        }

        /**
         * Gets the dash board.
         *
         * @return the dash board
         */
        @XmlElement(name = "DashBoard")
        public String getDashBoard() {
            return dashBoard;
        }

        /**
         * Sets the endpoint.
         *
         * @param endpoint the new endpoint
         */
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        /**
         * Sets the dash board.
         *
         * @param dashBoard the new dash board
         */
        public void setDashBoard(String dashBoard) {
            this.dashBoard = dashBoard;
        }

        /**
         * Gets the API key.
         *
         * @return the API key
         */
        @XmlElement(name = "APIKey")
        public String getAPIKey() {
            return APIKey;
        }

        /**
         * Sets the API key.
         *
         * @param APIKey the new API key
         */
        public void setAPIKey(String APIKey) {
            this.APIKey = APIKey;
        }
    }
}
