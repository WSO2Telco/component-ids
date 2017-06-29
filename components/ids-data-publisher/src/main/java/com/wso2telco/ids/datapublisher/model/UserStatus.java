package com.wso2telco.ids.datapublisher.model;

import java.sql.Timestamp;

public class UserStatus {


    private int id;
    private Timestamp time;
    private String status;
    private String msisdn;
    private String state;
    private String nonce;
    private String scope;
    private String acrValue;
    private String sessionId;
    private int isMsisdnHeader;
    private String ipHeader;
    private int isNewUser;
    private String loginHint;
    private String operator;
    private String userAgent;
    private String comment;
    private String consumerKey;
    private String appId;
    private String telcoScope;
    private String xForwardIP;
    private String transactionId;

    public UserStatus(){}

    public String getxForwardIP() {
        return xForwardIP;
    }

    public String getAppId() {
        return appId;
    }

    public int getId() {
        return id;
    }

    public Timestamp getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNonce() {
        return nonce;
    }

    public String getScope() {
        return scope;
    }

    public String getAcrValue() {
        return acrValue;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getIpHeader() {
        return ipHeader;
    }

    public String getLoginHint() {
        return loginHint;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setIsMsisdnHeader(int isMsisdnHeader) {
        this.isMsisdnHeader = isMsisdnHeader;
    }

    public int getIsMsisdnHeader() {
        return isMsisdnHeader;
    }

    public int getIsNewUser() {
        return isNewUser;
    }

    public void setIsNewUser(int isNewUser) {
        this.isNewUser = isNewUser;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getTelcoScope() {
        return telcoScope;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }


    public UserStatus cloneUserStatus() {
        return new UserStatusBuilder(getSessionId()).id(id).time(time).status(status).msisdn(msisdn).state(state)
                .nonce(nonce).scope(scope).acrValue(acrValue).ipHeader(ipHeader).isNewUser(isNewUser)
                .loginHint(loginHint).operator(operator).userAgent(userAgent).comment(comment).consumerKey(consumerKey)
                .appId(appId).telcoScope(telcoScope).xForwardIP(xForwardIP).transactionId(transactionId).build();
    }

    private UserStatus(UserStatusBuilder builder) {
        this.id = builder.id;
        this.time = builder.time;
        this.status = builder.status;
        this.msisdn = builder.msisdn;
        this.state = builder.state;
        this.nonce = builder.nonce;
        this.scope = builder.scope;
        this.acrValue = builder.acrValue;
        this.sessionId = builder.sessionId;
        this.isMsisdnHeader = builder.isMsisdnHeader;
        this.ipHeader = builder.ipHeader;
        this.isNewUser = builder.isNewUser;
        this.loginHint = builder.loginHint;
        this.operator = builder.operator;
        this.userAgent = builder.userAgent;
        this.comment = builder.comment;
        this.consumerKey = builder.consumerKey;
        this.appId = builder.appId;
        this.telcoScope = builder.telcoScope;
        this.xForwardIP = builder.xForwardIP;
        this.transactionId = builder.transactionId;
    }

    public static class UserStatusBuilder {
        private int id;
        private Timestamp time;
        private String status;
        private String msisdn;
        private String state;
        private String nonce;
        private String scope;
        private String acrValue;
        private String sessionId;
        private int isMsisdnHeader;
        private String ipHeader;
        private int isNewUser;
        private String loginHint;
        private String operator;
        private String userAgent;
        private String comment;
        private String consumerKey;
        private String appId;
        private String telcoScope;
        private String xForwardIP;
        private String transactionId;

        public UserStatusBuilder(String sessionId) {
            this.sessionId = sessionId;
        }

        public UserStatus build() {
            if (msisdn != null && !msisdn.isEmpty()) {
                setIsMsisdnHeader(1);
            } else {
                setIsMsisdnHeader(0);
            }
            return new UserStatus(this);
        }

        public UserStatusBuilder id(int id) {
            this.id = id;
            return this;
        }

        public UserStatusBuilder time(Timestamp time) {
            this.time = time;
            return this;
        }

        public UserStatusBuilder status(String status) {
            this.status = status;
            return this;
        }

        public UserStatusBuilder msisdn(String msisdn) {
            this.msisdn = msisdn;

            return this;
        }

        public UserStatusBuilder state(String state) {
            this.state = state;
            return this;
        }

        public UserStatusBuilder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public UserStatusBuilder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public UserStatusBuilder acrValue(String acrValue) {
            this.acrValue = acrValue;
            return this;
        }

        private void setIsMsisdnHeader(int isMsisdnHeader) {
            this.isMsisdnHeader = isMsisdnHeader;
        }

        public UserStatusBuilder ipHeader(String ipHeader) {
            this.ipHeader = ipHeader;
            return this;
        }

        public UserStatusBuilder isNewUser(int isNewUser) {
            this.isNewUser = isNewUser;
            return this;
        }

        public UserStatusBuilder loginHint(String loginHint) {
            this.loginHint = loginHint;
            return this;
        }

        public UserStatusBuilder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public UserStatusBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public UserStatusBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public UserStatusBuilder consumerKey(String consumerKey) {
            this.consumerKey = consumerKey;
            return this;
        }

        public UserStatusBuilder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public UserStatusBuilder telcoScope(String telcoScope) {
            this.telcoScope = telcoScope;
            return this;
        }

        public UserStatusBuilder xForwardIP(String xForwardIP) {
            this.xForwardIP = xForwardIP;
            return this;
        }

        public UserStatusBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

    }
}
