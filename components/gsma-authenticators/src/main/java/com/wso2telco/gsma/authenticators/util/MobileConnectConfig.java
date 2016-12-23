package com.wso2telco.gsma.authenticators.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MobileConnectConfig")
public class MobileConnectConfig {

    private String authEndpointUrl;

    private String defaultClaimUrl;

    private String openIdRegClaimUrl;

    @XmlElement(name = "AuthenticationEndpoint", defaultValue = "authenticationendpoint")
    public String getAuthEndpointUrl() {
        return authEndpointUrl;
    }

    public void setAuthEndpointUrl(String authEndpointUrl) {
        this.authEndpointUrl = authEndpointUrl;
    }

    @XmlElement(name = "DefaultClaimUrl", defaultValue = "http://wso2.org/claims")
    public String getDefaultClaimUrl() {
        return defaultClaimUrl;
    }

    public void setDefaultClaimUrl(String defaultClaimUrl) {
        this.defaultClaimUrl = defaultClaimUrl;
    }

    @XmlElement(name = "OpenIdRegClaimUrl", defaultValue = "http://schema.openid.net/2007/05/claims")
    public String getOpenIdRegClaimUrl() {
        return openIdRegClaimUrl;
    }

    public void setOpenIdRegClaimUrl(String openIdRegClaimUrl) {
        this.openIdRegClaimUrl = openIdRegClaimUrl;
    }
}
