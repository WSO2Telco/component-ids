package com.gsma.authenticators.dialog.ussd;

/**
 * Created by paraparan on 5/14/15.
 */
public class Pinresponse {

    private String userResponse;
    private String userPin;

    public String getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(String userResponse) {
        this.userResponse = userResponse;
    }

    public String getUserPin() {
        return userPin;
    }

    public void setUserPin(String userPin) {
        this.userPin = userPin;
    }

}
