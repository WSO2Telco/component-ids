package com.wso2telco.gsma.authenticators.attributeShare.internal;

/**
 * Created by aushani on 7/30/17.
 */
public enum SPType {

    TSP1("trustedsp1"),
    TSP2("trustedsp2"),
    NORMAL("normal"),
    UNDEFINED("undefined");

    private String spType;

    private SPType(String spTypes) {
        this.spType = spTypes;
    }


}
