package com.wso2telco.gsma.authenticators.attributeShare.internal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by aushani on 8/2/17.
 */
 public enum ConsentType {

   /* IMPLICIT("implicit"),
    EXPLICIT("explicit"),
    NOCONSENT("no consent");

        private String consent;

        private ConsentType(String text) {
            this.consent = text;
        }


        public String getConsent(){
            return consent;

        }

    @Override
    public String toString() {
        return super.toString();
    }*/


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
    	ConsentType spType =valueMap.get(code);
    	if(spType==null){
    		spType =spType.UNDEFINED;
    	}
    	return spType;
    }
}