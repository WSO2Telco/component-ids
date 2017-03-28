package com.wso2telco.gsma.authenticators.util;

public enum TableName {

    CLIENT_STATUS("clientstatus"),

    AUTHENTICATED_LOGIN("authenticated_login"),

    REG_STATUS("regstatus"),

    ALLOWED_AUTHENTICATORS_MNO("allowed_authenticators_mno"),

    ALLOWED_AUTHENTICATORS_SP("allowed_authenticators_sp");

    private final String text;

    /**
     * @param text
     */
    private TableName(final String text) {
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
