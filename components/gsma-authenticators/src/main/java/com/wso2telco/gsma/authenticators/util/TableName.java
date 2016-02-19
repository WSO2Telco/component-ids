package com.wso2telco.gsma.authenticators.util;

public enum TableName {
	
	CLIENT_STATUS("clientstatus"),
	
	AUTHENTICATED_LOGIN("authenticated_login");
	
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
