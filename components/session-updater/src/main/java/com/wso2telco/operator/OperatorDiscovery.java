package com.wso2telco.operator;

import com.wso2telco.core.config.ConfigLoader;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OperatorDiscovery implements FindOperator {

    @Override
    public String findOperatorByMsisdn(String msisdn) throws IOException, org.apache.http.ParseException, JSONException {

        final String discoveryUrl = ConfigLoader.getInstance().getMobileConnectConfig().getOperatorRecovery().getRecoveryOptionURL();
        final String discoveryAuthCode = ConfigLoader.getInstance().getMobileConnectConfig().getOperatorRecovery().getRecoveryOptionAuthCode();

        String operator = "";
        org.apache.http.client.HttpClient client = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(discoveryUrl);
        postRequest.addHeader("Authorization", "Basic " + discoveryAuthCode);
        postRequest.addHeader("Cache-Control", "no-cache");
        postRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("MSISDN", msisdn));
        UrlEncodedFormEntity requestContent = new UrlEncodedFormEntity(urlParameters);

        postRequest.setEntity(requestContent);
        HttpResponse httpResponse = client.execute(postRequest);

        if ((httpResponse.getStatusLine().getStatusCode() == 200)) {
            org.json.JSONObject responseFullPayload = new org.json.JSONObject(EntityUtils.toString(httpResponse.getEntity()));
            org.json.JSONObject response = (org.json.JSONObject)responseFullPayload.get("response");
            operator = response.get("serving_operator").toString();
        }

        return operator;

    }
}
