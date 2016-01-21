package com.axiata.mife.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Scope")
@XmlAccessorType(XmlAccessType.FIELD)
public class Scope{

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "Claims")
    private Claims claims;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Claims getClaims() {
        return claims;
    }

    public void setClaims(Claims claims) {
        this.claims = claims;
    }
}