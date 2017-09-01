package com.wso2telco.claims;

import java.util.Map;

public interface RemoteClaims {

    Map<String, Object> getTotalClaims(String operatorEndPoint,String msisdn);

}
