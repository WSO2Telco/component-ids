package com.axiata.mife.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name="Claims")
@XmlAccessorType(XmlAccessType.FIELD)
public class Claims {

    @XmlElement(name = "ClaimValue")
    private List<String> claimValues;


    public List<String> getClaimValues() {
        return claimValues;
    }

    public void setClaimValues(List<String> claimValues) {
        this.claimValues = claimValues;
    }
}
