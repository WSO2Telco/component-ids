package com.wso2telco.gsma.authenticators.attributeShare.internal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by aushani on 8/2/17.
 */

public enum ValidityType {
    LONG_LIVE("long_live"),
    TRANSACTIONAL("transactional"),
    UNDEFINED("undefined");

    private String type;

    private ValidityType(String text) {
        this.type = text;

    }

    public String getValidityType() {
        return type;

    }

    static Map<String, ValidityType> valueMap = new HashMap<>();

    static {
        Iterator<ValidityType> enumTy = EnumSet.allOf(ValidityType.class).iterator();
        while (enumTy.hasNext()) {
            ValidityType validityType = enumTy.next();
            valueMap.put(validityType.getValidityType(),validityType);
        }
    }

    public static ValidityType get(final String code) {
        ValidityType validityType = valueMap.get(code);
        if (validityType == null) {
            validityType = validityType.UNDEFINED;
        }
        return validityType;

    }


}
