/*
 * Pinresponse.java
 * Aug 15, 2014  11:28:23 AM
 * Roshan.Saputhanthri
 *
 * Copyright (C) Dialog Axiata PLC. All Rights Reserved.
 */

package com.gsma.authenticators.ussd;

/**
 * <TO-DO> <code>Pinresponse</code>
 * @version $Id: Pinresponse.java,v 1.00.000
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
