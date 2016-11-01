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
