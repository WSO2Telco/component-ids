package org.wso2.carbon.identity.application.authentication.endpoint.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement(name="Scopes")
@XmlAccessorType(XmlAccessType.FIELD)
public class Scopes{

    @XmlElement(name = "Scope")
    private List<Scope> scopeList;

    public List<Scope> getScopeList() {
        return scopeList;
    }

    public void setScopeList(List<Scope> scopeList) {
        this.scopeList = scopeList;
    }
}
