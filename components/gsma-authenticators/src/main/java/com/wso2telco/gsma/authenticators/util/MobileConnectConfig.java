package com.wso2telco.gsma.authenticators.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MobileConnectConfig")
public class MobileConnectConfig {

    private String authEndpointUrl;


    @XmlElement(name = "AuthenticationEndpoint", defaultValue = "authenticationendpoint")
    public String getAuthEndpointUrl() {
        return authEndpointUrl;
    }

    public void setAuthEndpointUrl(String authEndpointUrl) {
        this.authEndpointUrl = authEndpointUrl;
    }
}
