package com.wso2telco.gsma.authenticators.internal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by aushani on 9/22/17.
 */
public class AuthenticatorEnum {

    public enum TrustedStatus {

        FULLY_TRUSTED("fullytrusted"),
        TRUSTED("trusted"),
        UNTRUSTED("normal"),
        UNDEFINED("undefined");

        private final String trustedStatus;

        TrustedStatus(String trustedStatus) {
            this.trustedStatus = trustedStatus;
        }

        public String getTrustedStatus() {
            return this.trustedStatus;
        }
    }

    public enum ConsentType {
        IMPLICIT("implicit"),
        EXPLICIT("explicit"),
        NOCONSENT("no consent"),
        UNDEFINED("undefined");

        private String consent;

        private ConsentType(String text) {
            this.consent = text;
        }

        public String getConsent(){
            return consent;

        }

        static Map<String,ConsentType> valueMap= new HashMap<>();
        static {
            Iterator<ConsentType> enumTy = EnumSet.allOf(ConsentType.class).iterator();
            while(enumTy.hasNext()){
                ConsentType consentType = enumTy.next();
                valueMap.put(consentType.getConsent(), consentType);
            }
        }

        public static ConsentType get(final String code){
            ConsentType consentType =valueMap.get(code);
            if(consentType==null){
                consentType =consentType.UNDEFINED;
            }
            return consentType;
        }
    }
}
