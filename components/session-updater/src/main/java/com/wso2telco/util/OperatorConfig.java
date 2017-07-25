package com.wso2telco.util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public  class OperatorConfig {

    private String name;
    private String message;
    private boolean welcomemessagedisabled;
    private String smsEndpoint;

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "Message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @XmlElement(name = "WelcomeMessageDisabled")
    public boolean getWelcomemessagedisabled() {
        return welcomemessagedisabled;
    }

    public void setWelcomemessagedisabled(boolean welcomemessagedisabled) {
        this.welcomemessagedisabled = welcomemessagedisabled;
    }

    @XmlElement(name = "SMSEndpoint")
    public String getSmsEndpoint() {
        return smsEndpoint;
    }

    public void setSmsEndpoint(String smsEndpoint) {
        this.smsEndpoint = smsEndpoint;
    }
}