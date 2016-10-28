package com.wso2telco.genz.model;

/**
 * This class is using to keep login hint properties.
 */
public class LoginHintProperties {
    private String loginHintFormatRegex;
    private boolean isEncrypted;
    private String encryptionImplementation;
    private String encryptionKey;
    private int priority;

    public String getLoginHintFormatRegex() {
        return loginHintFormatRegex;
    }

    public void setLoginHintFormatRegex(String loginHintFormatRegex) {
        this.loginHintFormatRegex = loginHintFormatRegex;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getEncryptionImplementation() {
        return encryptionImplementation;
    }

    public void setEncryptionImplementation(String encryptionImplementation) {
        this.encryptionImplementation = encryptionImplementation;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
