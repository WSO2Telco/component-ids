package com.wso2telco.claims;


import com.wso2telco.util.ClaimsRetrieverType;

public class ClaimsRetrieverFactory {

    public static ClaimsRetriever getClaimsRetriever(String retrieverType) {
        if (retrieverType.equalsIgnoreCase(ClaimsRetrieverType.Local.toString())) {
            return new LocalClaimsRetriever();
        } else {
            return new RemoteClaimsRetriever();
        }
    }
}
