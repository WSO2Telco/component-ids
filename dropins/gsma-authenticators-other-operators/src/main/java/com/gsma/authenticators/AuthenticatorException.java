package com.gsma.authenticators;

public class AuthenticatorException extends Exception {
    public AuthenticatorException() {
        super();
    }

    public AuthenticatorException(String message) {
        super(message);
    }

    public AuthenticatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
