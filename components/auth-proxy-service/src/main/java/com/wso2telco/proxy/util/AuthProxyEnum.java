package com.wso2telco.proxy.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by aushani on 9/20/17.
 */
public class AuthProxyEnum {

    public enum TRUSTEDSTATUS {
        FULLY_TRUSTED("fullytrusted"),
        TRUSTED("trusted"),
        UNTRUSTED("normal"),
        UNDEFINED("undefined");

        private String trustedStatus;

        private TRUSTEDSTATUS(String status) {
            this.trustedStatus = status;
        }

        public String gettrustedStatus() {
            return trustedStatus;

        }

        static Map<String, TRUSTEDSTATUS> statusMap = new HashMap<>();

        static {
            Iterator<TRUSTEDSTATUS> enumStatus = EnumSet.allOf(TRUSTEDSTATUS.class).iterator();
            while (enumStatus.hasNext()) {
                TRUSTEDSTATUS statusType = enumStatus.next();
                statusMap.put(statusType.gettrustedStatus(), statusType);
            }
        }

        public static TRUSTEDSTATUS getStatus(final String code) {
            TRUSTEDSTATUS status = statusMap.get(code);
            if (status == null) {
                status = status.UNDEFINED;
            }
            return status;

        }
    }

    public enum SCOPETYPE {
        ATT_VERIFICATION,
        ATT_SHARE;
    }

    public enum TABLENAME {

        SP_CONFIGURATION("sp_configuration"),
        TRUSTED_STATUS("trustedStatus");

        private final String text;

        /**
         * @param text
         */
        private TABLENAME(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }


}
