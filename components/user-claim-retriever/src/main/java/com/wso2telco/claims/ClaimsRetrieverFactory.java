package com.wso2telco.claims;


import com.wso2telco.util.ClaimsRetrieverType;

public class ClaimsRetrieverFactory {

    private ClaimsRetrieverFactory(){
    }

    public static ClaimsRetriever getClaimsRetriever(String retrieverType) {
        ClaimsRetriever claimsRetriever;
        if (retrieverType.equalsIgnoreCase(ClaimsRetrieverType.LOCAL.toString())) {
            claimsRetriever=new LocalClaimsRetriever();
        } else {
            claimsRetriever=new RemoteClaimsRetriever();
        }

        return claimsRetriever;
    }
}
