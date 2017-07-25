package com.wso2telco.operator;

import org.json.JSONException;

import java.io.IOException;

public interface FindOperator {

    public String findOperatorByMsisdn(String msisdn) throws IOException, org.apache.http.ParseException, JSONException;

}
