package org.wso2.carbon.identity.application.authentication.endpoint.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="ScopeConfigs")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScopeConfigs {

    @XmlElement(name = "Scopes")
    private Scopes scopes;

    public Scopes getScopes() {
        return scopes;
    }

    public void setScopes(Scopes scopes) {
        this.scopes = scopes;
    }
}







