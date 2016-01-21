package com.gsma.authenticators.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "MobileConnectConfig")
public class MobileConnectConfig {

    private String dataSourceName;
    private String encryptAppend;
    private String keyfile;
    private GSMAExchangeConfig gsmaExchangeConfig;
    private SMSConfig smsConfig;
    private USSDConfig ussdConfig;
    private String listenerWebappHost;
    private MePinConfig mePinConfig;
    private AndroidConfig androidConfig;

    @XmlElement(name = "MSS")
    protected MSS mss;
    
    @XmlElement(name = "HEADERENRICH")
    protected HEADERENRICH headerenrich;

    @XmlElement(name = "DataSourceName", defaultValue = "jdbc/CONNECT_DB")
    public String getDataSourceName() {
        return dataSourceName;
    }
    
    @XmlElement(name = "EncryptAppend")
    public String getEncryptAppend() {
        return encryptAppend;
    }
    
    @XmlElement(name = "Keyfile")
    public String getKeyfile() {
        return keyfile;
    }

    @XmlElement(name = "GSMAExchangeConfig")
    public GSMAExchangeConfig getGsmaExchangeConfig() {
        return gsmaExchangeConfig;
    }

    @XmlElement(name = "SMS")
    public SMSConfig getSmsConfig() {
        return smsConfig;
    }

    @XmlElement(name = "USSD")
    public USSDConfig getUssdConfig() {
        return ussdConfig;
    }


    @XmlElement(name = "ANDROID")
    public AndroidConfig getAndroidConfig() {
        return androidConfig;
    }

    @XmlElement(name = "ListenerWebappHost")
    public String getListenerWebappHost() {
        if (listenerWebappHost == null || listenerWebappHost.isEmpty()) {
            return "http://" + System.getProperty("carbon.local.ip") + ":9764";
        }
        return listenerWebappHost;
    }

    @XmlElement(name = "MePIN")
    public MePinConfig getMePinConfig() {
        return mePinConfig;
    }
    
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }
    
    public void setEncryptAppend(String EncryptAppend) {
        this.encryptAppend = EncryptAppend;
    }
    public void setKeyfile(String Keyfile) {
        this.keyfile = Keyfile;
    }

    public void setGsmaExchangeConfig(GSMAExchangeConfig gsmaExchangeConfig) {
        this.gsmaExchangeConfig = gsmaExchangeConfig;
    }
    
    public void setSmsConfig(SMSConfig smsConfig) {
        this.smsConfig = smsConfig;
    }

    public void setUssdConfig(USSDConfig ussdConfig) {
        this.ussdConfig = ussdConfig;
    }

    public void setListenerWebappHost(String listenerWebappHost) {
        this.listenerWebappHost = listenerWebappHost;
    }


    public void setAndroidConfig(AndroidConfig androidConfig) {
        this.androidConfig = androidConfig;
    }


    public void setMePinConfig(MePinConfig mePinConfig) {
        this.mePinConfig = mePinConfig;
    }

    public HEADERENRICH getHEADERENRICH() {
        return headerenrich;
    }

    public void setHEADERENRICH(HEADERENRICH value) {
        this.headerenrich = value;
    }

    public static class GSMAExchangeConfig {
        private String servingOperatorHost;
        private String organization;
        private String authToken;
        private ServingOperator servingOperator;

        @XmlElement(name = "SOHost")
        public String getServingOperatorHost() {
            return servingOperatorHost;
        }

        @XmlElement(name = "Organization")
        public String getOrganization() {
            return organization;
        }

        @XmlElement(name = "AuthToken")
        public String getAuthToken() {
            return authToken;
        }
        
        @XmlElement(name = "ServingOperator")
		public ServingOperator getServingOperator() {
			return servingOperator;
		}

        public void setServingOperatorHost(String servingOperatorHost) {
            this.servingOperatorHost = servingOperatorHost;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

		public void setServingOperator(ServingOperator servingOperator) {
			this.servingOperator = servingOperator;
		}
    }

    public static class SMSConfig {

        private String endpoint;
        private String authToken;
        private String message;

        @XmlElement(name = "Endpoint")
        public String getEndpoint() {
            return endpoint;
        }

        @XmlElement(name = "AuthToken")
        public String getAuthToken() {
            return authToken;
        }

        @XmlElement(name = "MessageContent")
        public String getMessage() {
            return message;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class USSDConfig {

        private String endpoint;
        private String authToken;
        private String message;
        private String shortCode;
        private String keyword;
        private String pinauth;
        private String dashBoard;

        @XmlElement(name = "Endpoint")
        public String getEndpoint() {
            return endpoint;
        }

        @XmlElement(name = "AuthToken")
        public String getAuthToken() {
            return authToken;
        }

        @XmlElement(name = "MessageContent")
        public String getMessage() {
            return message;
        }

        @XmlElement(name = "ShortCode")
        public String getShortCode() {
            return shortCode;
        }

        @XmlElement(name = "Keyword")
        public String getKeyword() {
            return keyword;
        }

        @XmlElement(name = "Pinauth")
        public String getPinauth() {
            return pinauth;
        }
        @XmlElement(name = "DashBoard")
        public String getDashBoard() {
            return dashBoard;
        }
        
        
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setShortCode(String shortCode) {
            this.shortCode = shortCode;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }
        
        public void setPinauth(String pinauth) {
            this.pinauth = pinauth;
        }
        
        public void setDashBoard(String dashBoard) {
            this.dashBoard = dashBoard;
        }
        
    }

    public static class HEADERENRICH {

        private String endpoint;
        private String enrichflg;
        private String message;
        private List<String> mobileIPRanges;

        @XmlElementWrapper(name = "MobileIPRanges")
        @XmlElement(name = "IPRange")
        public List<String> getMobileIPRanges() {
            return mobileIPRanges;
        }

        public void setMobileIPRanges(List<String> mobileIPRanges) {
            this.mobileIPRanges = mobileIPRanges;
        }

        @XmlElement(name = "Endpoint")
        public String getEndpoint() {
            return endpoint;
        }

        @XmlElement(name = "Enrichflg")
        public String getEnrichflg() {
            return enrichflg;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public void setEnrichflg(String enrichflg) {
            this.enrichflg = enrichflg;
        }
        
        
    }

    public MSS getMSS() {
        return mss;
    }

    public void setMSS(MSS mss) {
        this.mss = mss;
    }

    public static class MSS {
        private String endpoint;
        private int successStatus;
        private String mssText;
        private String mssPinTest;

        @XmlElement(name = "MssText")
        public String getMssText() {
            return mssText;
        }

        public void setMssText(String mssText) {
            this.mssText = mssText;
        }

        @XmlElement(name = "MssPinText")
        public String getMssPinTest() {
            return mssPinTest;
        }

        public void setMssPinTest(String mssPinTest) {
            this.mssPinTest = mssPinTest;
        }



        @XmlElement(name = "Endpoint")
        public String getEndpoint() {
            return endpoint;
        }

        @XmlElement(name = "SuccessStatus")
        public int getSuccessStatus() {
            return successStatus;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public void setSuccessStatus(int successStatus) {
            this.successStatus = successStatus;
        }

    }

    public static class MePinConfig {

        private String endpoint;
        private String clientID;
        private String authToken;
        private String confirmationPolicy;
        private String shortMessageText;
        private String headerText;
        private String messageText;

        @XmlElement(name = "Endpoint")
        public String getEndpoint() {
            return endpoint;
        }

        @XmlElement(name = "ClientID")
        public String getClientID() {
            return clientID;
        }

        @XmlElement(name = "AuthToken")
        public String getAuthToken() {
            return authToken;
        }

        @XmlElement(name = "ConfirmationPolicy")
        public String getConfirmationPolicy() {
            return confirmationPolicy;
        }

        @XmlElement(name = "ShortMessageText")
        public String getShortMessageText() {
            return shortMessageText;
        }

        @XmlElement(name = "HeaderText")
        public String getHeaderText() {
            return headerText;
        }

        @XmlElement(name = "MessageText")
        public String getMessageText() {
            return messageText;
        }

        public void setEndpoint(String endpoint) {this.endpoint = endpoint;}

        public void setClientID(String clientID) {this.clientID = clientID;}

        public void setAuthToken(String authToken) {this.authToken = authToken;}

        public void setConfirmationPolicy(String confirmationPolicy) {this.confirmationPolicy = confirmationPolicy;}

        public void setShortMessageText(String shortMessageText) {this.shortMessageText = shortMessageText;}

        public void setHeaderText(String headerText) {this.headerText = headerText;}

        public void setMessageText(String messageText) {this.messageText = messageText;}
    }


    public static class AndroidConfig {

        private String endpoint;
        private String dashBoard;
        private String APIKey;

        @XmlElement(name = "Endpoint")
        public String getEndpoint() {
            return endpoint;
        }

        @XmlElement(name = "DashBoard")
        public String getDashBoard() {
            return dashBoard;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public void setDashBoard(String dashBoard) {
            this.dashBoard = dashBoard;
        }

        @XmlElement(name = "APIKey")
        public String getAPIKey() {
            return APIKey;
        }

        public void setAPIKey(String APIKey) {
            this.APIKey = APIKey;
        }
    }
}
