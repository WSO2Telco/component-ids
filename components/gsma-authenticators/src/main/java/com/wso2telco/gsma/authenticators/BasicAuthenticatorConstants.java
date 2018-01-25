package com.wso2telco.gsma.authenticators;

/**
 * Constants used by the BasicAuthenticator
 */
public abstract class BasicAuthenticatorConstants {

    public static final String AUTHENTICATOR_NAME = "BasicAuthenticator";
    public static final String AUTHENTICATOR_FRIENDLY_NAME = "basic";
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String FAILED_USERNAME = "&failedUsername=";
    public static final String ERROR_CODE = "&errorCode=";
    public static final String AUTHENTICATORS = "&authenticators=";
    public static final String LOCAL = "LOCAL";
    public static final String UTF_8 = "UTF-8";

    private BasicAuthenticatorConstants() {
    }
}