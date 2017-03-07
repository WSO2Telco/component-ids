package com.wso2telco.util;

public enum TableName {

    SP_LOGIN_HISTORY("sp_login_history"),

    MCX_CROSS_OPERATOR_TRANSACTION_LOG("mcx_cross_operator_transaction_log");

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
