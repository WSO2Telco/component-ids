package com.wso2telco.claims;


import com.wso2telco.util.ClaimsRetrieverType;

public class ClaimsRetrieverFactory {

    private ClaimsRetrieverFactory(){
    }

    public static ClaimsRetriever getClaimsRetriever(String retrieverType) {
        if (retrieverType.equalsIgnoreCase(ClaimsRetrieverType.LOCAL.toString())) {
            return new LocalClaimsRetriever();
        } else {
            return new RemoteClaimsRetriever();
        }
    }
}
