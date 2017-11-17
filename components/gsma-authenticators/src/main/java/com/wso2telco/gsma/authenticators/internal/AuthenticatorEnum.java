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

        private final String trustedStatu;

        TrustedStatus(String trustedStatus) {
            this.trustedStatu = trustedStatus;
        }

        public String getTrustedStatu() {
            return this.trustedStatu;
        }
    }

    public enum AttributeShareScopeTypes {

        VERIFICATION_SCOPE("ATT_VERIFICATION"),
        PROVISIONING_SCOPE("ATT_SHARE");

        private final String attributeShareScopeType;

        AttributeShareScopeTypes(String attributeShareScopeTypes) {
            this.attributeShareScopeType = attributeShareScopeTypes;
        }

        public String getAttributeShareScopeType() {
            return this.attributeShareScopeType;
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
            return valueMap.get(code);
        }
    }
}
