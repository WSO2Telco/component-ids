/*******************************************************************************
 * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.wso2telco.sp.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Base64;
import java.util.HashMap;

import com.google.gson.Gson;
import com.wso2telco.core.spprovisionservice.sp.entity.AdminServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.ProvisionType;
import com.wso2telco.core.spprovisionservice.sp.entity.ServiceProviderDto;
import com.wso2telco.sp.discovery.exception.DicoveryException;
import com.wso2telco.sp.entity.CrValidateRes;
import com.wso2telco.sp.entity.EksDiscovery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class RemoteDiscovery extends DiscoveryLocator implements InlineConnectionStrategies {

    protected static String ACCEPT = "Accept";
    protected static String CONTENT_TYPE_HEADER_KEY = "Content-Type";
    protected static String CONTENT_TYPE_HEADER_VAL_TYPE_EKS = "application/x-www-form-urlencoded";
    protected static String CONTENT_TYPE_HEADER_VAL_TYPE_CR = "application/json";
    protected static String AUTHORIZATION_HEADER = "Authorization";
    protected static String HTTP_POST = "POST";
    protected static String HTTP_GET = "GET";
    protected static String MSISDN = "msisdn";
    protected static String REDIRECT_URL = "Redirect_URL";
    protected static String CLIENT_ID = "client_id";
    protected static String CLIENT_SECRET = "client_secret";
    protected static String BASIC = "Basic";
    protected static String QES_OPERATOR = "?";
    protected static String EQA_OPERATOR = "=";
    protected static String AMP_OPERATOR = "&";
    protected static String SPACE = " ";
    protected static String COLON = ":";
    protected static String NEW_LINE = "\n";

    private static Log log = LogFactory.getLog(RemoteDiscovery.class);

    public abstract Map<String, String> buildRequestProperties(String encodedBasicAuthCode);

    public abstract String buildEndPointUrl(DiscoveryServiceConfig discoveryServiceConfig,
            DiscoveryServiceDto discoveryServiceDto);
    
    public abstract <K,T> ServiceProviderDto createServiceProviderDtoBy(K k,T t);

    protected String getJsonWithDiscovery(String endPointUrl, String requestMethod, String data,
            Map<String, String> requestProperties) throws DicoveryException {
        String responseJson = null;
        HttpURLConnection conn = null;
        try {
            log.info("Trying dicovery call. < endpoint:" + endPointUrl + " > <request method : " + requestMethod
                    + "> <Data : " + data + " >");
            URL url = new URL(endPointUrl);
            conn = (HttpURLConnection) url.openConnection();

            setConnectionRequestMethod(requestMethod, conn);
            setRequestProperties(requestProperties, conn);
            boolean isDoOutput = setOutPutStrategy(data, conn);
            if (isDoOutput) {
                writeToOutputStream(data, conn);
            }

            if (conn.getResponseCode() == 401) {
                conn.disconnect();
                throw new DicoveryException("Failed : HTTP error code : " + conn.getResponseCode(), false);
            }

            if (conn.getResponseCode() != 200) {
                conn.disconnect();
                throw new DicoveryException("Failed : HTTP error code : " + conn.getResponseCode(), true);
            }
            responseJson = getJsonBy(conn.getInputStream());
            conn.disconnect();
        } catch (MalformedURLException e) {
            conn.disconnect();
            throw new DicoveryException(e.getMessage(), true);
        } catch (IOException e) {
            conn.disconnect();
            throw new DicoveryException(e.getMessage(), true);
        }
        return responseJson;
    }

    @Override
    public void setConnectionRequestMethod(String requestMethod, HttpURLConnection conn) throws ProtocolException {
        log.info("Setting connection request method...");
        conn.setRequestMethod(requestMethod);
    }

    @Override
    public void setRequestProperties(Map<String, String> requestPrperties, HttpURLConnection conn) {
        log.info("Setting request properties...");
        for (Map.Entry<String, String> propEntry : requestPrperties.entrySet()) {
            conn.setRequestProperty(propEntry.getKey(), propEntry.getValue());
        }
    }

    @Override
    public boolean setOutPutStrategy(String data, HttpURLConnection conn) {
        log.info("Setting output strategy...");
        boolean isDoOutput = false;
        if (data != null && !data.isEmpty()) {
            conn.setDoOutput(true);
            isDoOutput = true;
        }
        return isDoOutput;
    }

    @Override
    public void writeToOutputStream(String data, HttpURLConnection conn) throws IOException {
        log.info("Writing to output stream start...");
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();
        log.info("Writing to output stream end...");
    }

    @Override
    public String getJsonBy(InputStream inputStream) throws IOException {
        log.info("Concatenating the JSON inputs...");
        BufferedReader br = new BufferedReader(new InputStreamReader((inputStream)));
        String output;
        StringBuilder jsonStrBuilder = new StringBuilder();
        while ((output = br.readLine()) != null) {
            jsonStrBuilder.append(output).append(NEW_LINE);
        }
        return jsonStrBuilder.toString();
    }

    protected String buildBasicAuthCode(String clientId, String clientSecret) {
        log.info("Building the basic authcode for Client Id :" + clientId + " , client secret : " + clientSecret);
        String encodedBasicAuthCode = "";
        StringBuilder basicAuthStrBuilder = new StringBuilder();
        if (clientId != null && !clientId.isEmpty()) {
            basicAuthStrBuilder.append(clientId);
            if (clientSecret != null && !clientSecret.isEmpty()) {
                basicAuthStrBuilder.append(COLON).append(clientSecret);
            }
            if (basicAuthStrBuilder != null && !basicAuthStrBuilder.toString().isEmpty()) {
                encodedBasicAuthCode = Base64.getEncoder().encodeToString(basicAuthStrBuilder.toString().getBytes());
            }
        }
        return encodedBasicAuthCode;
    }

}
