package com.wso2telco.utils;

import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "AuthenticationLevel")
public class AuthenticationLevel {
    private String level;
    private Authentication authentication;
    private List<MIFEAbstractAuthenticator> authenticators = null;

    @XmlElement(name = "Authentication")
    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    @XmlElement(name = "Level")
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void init() {
        authenticators = new ArrayList<MIFEAbstractAuthenticator>();
        List<Authenticators> authenticatorConfigs = authentication.getAuthenticatorsList();

        for (Authenticators a : authenticatorConfigs) {
            List<Authenticator> s = a.getAuthenticators();
            for (Authenticator authenticator : s) {
                AuthenticationLevel.MIFEAbstractAuthenticator mifeAuth = new AuthenticationLevel.MIFEAbstractAuthenticator();
                mifeAuth.setAuthenticator(FrameworkUtils.getAppAuthenticatorByName(authenticator
                                                                                           .getAuthenticatorName()));
                mifeAuth.setOnFailAction(authenticator.getOnfail());
                authenticators.add(mifeAuth);
            }
        }
    }

    /**
     * Gets the authenticators.
     *
     * @return the authenticators
     */
    public List<MIFEAbstractAuthenticator> getAuthenticators() {
        return authenticators;
    }

    /**
     * The Class MIFEAbstractAuthenticator.
     */
    // Inner type to handle authenticators and respective external attributes
    public class MIFEAbstractAuthenticator {

        /** The authenticator. */
        private ApplicationAuthenticator authenticator;

        /** The on fail action. */
        private String onFailAction;

        /**
         * Gets the authenticator.
         *
         * @return the authenticator
         */
        public ApplicationAuthenticator getAuthenticator() {
            return authenticator;
        }

        /**
         * Sets the authenticator.
         *
         * @param authenticator the new authenticator
         */
        public void setAuthenticator(ApplicationAuthenticator authenticator) {
            this.authenticator = authenticator;
        }

        /**
         * Gets the on fail action.
         *
         * @return the on fail action
         */
        public String getOnFailAction() {
            return onFailAction;
        }

        /**
         * Sets the on fail action.
         *
         * @param onFailAction the new on fail action
         */
        public void setOnFailAction(String onFailAction) {
            this.onFailAction = onFailAction;
        }
    }
}