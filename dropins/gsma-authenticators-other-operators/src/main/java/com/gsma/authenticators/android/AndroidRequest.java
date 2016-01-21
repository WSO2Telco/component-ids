package com.gsma.authenticators.android;


import com.gsma.authenticators.model.AuthenticateUserRequest;

public class AndroidRequest {

    AuthenticateUserRequest authenticateUserRequest;

    public AuthenticateUserRequest getAuthenticateUserRequest() {
        return authenticateUserRequest;
    }

    public void setAuthenticateUserRequest(AuthenticateUserRequest authenticateUserRequest) {
        this.authenticateUserRequest = authenticateUserRequest;
    }
}
