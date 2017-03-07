package com.wso2telco.util;

public enum Params {

    SCOPE("scope"),
    STATE("state"),
    NONCE("nonce"),
    CODE("code"),
    RESPONSE_TYPE("response_type"),
    AMR("amr"),
    ACR_VALUES("acr_values"),
    REDIRECT_URI("redirect_uri"),
    CLIENT_ID("client_id");

    private final String text;

    /**
     * @param text
     */
    private Params(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
