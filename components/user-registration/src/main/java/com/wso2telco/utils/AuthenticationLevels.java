package com.wso2telco.utils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


@XmlRootElement(name = "AuthenticationLevels")
public class AuthenticationLevels {
    private List<AuthenticationLevel> authenticationLevelList;

    @XmlElement(name = "AuthenticationLevel")
    public List<AuthenticationLevel> getAuthenticationLevelList() {
        return authenticationLevelList;
    }

    public void setAuthenticationLevelList(List<AuthenticationLevel> authenticationLevelList) {
        this.authenticationLevelList = authenticationLevelList;
    }

    /**
     * Gets the loa.
     *
     * @param level the level
     * @return the loa
     */
    public AuthenticationLevel getLOA(String level) {
        for (AuthenticationLevel loa : authenticationLevelList) {
            if (loa.getLevel().equals(level)) {
                return loa;
            }
        }
        return null;
    }

    public void init() {
        for (AuthenticationLevel loa : authenticationLevelList) {
            loa.init();
        }
    }

}