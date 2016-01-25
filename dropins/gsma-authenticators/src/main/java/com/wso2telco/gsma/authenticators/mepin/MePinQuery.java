/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
package com.wso2telco.gsma.authenticators.mepin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.ReadMobileConnectConfig;

// TODO: Auto-generated Javadoc
/**
 * The Class MePinQuery.
 */
public class MePinQuery {

    /** The log. */
    private static Log log = LogFactory.getLog(MePinQuery.class);

    /**
     * Creates the transaction.
     *
     * @param mepinID the mepin id
     * @param sessionID the session id
     * @param serviceProvider the service provider
     * @param confirmation_policy the confirmation_policy
     * @return the json object
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws XPathExpressionException the x path expression exception
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     */
    protected JsonObject createTransaction(String mepinID, String sessionID, String serviceProvider,String confirmation_policy) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {


        log.debug("Started handling transaction creation");

        String charset = "UTF-8";
        ReadMobileConnectConfig readMobileConnectConfig = new ReadMobileConnectConfig();
        Map<String, String> readMobileConnectConfigResult;
        readMobileConnectConfigResult = readMobileConnectConfig.query("MePIN");
        String url = readMobileConnectConfigResult.get("Endpoint");//mePinConfig.getEndpoint();

        log.debug("MePIN URL: " + url);

        String identifier = sessionID;
        String short_message = readMobileConnectConfigResult.get("ShortMessageText");//"Confirm Authentication";//mePinConfig.getShortMessageText();
//        String header = mePinConfig.getHeaderText() + " " + serviceProvider;
        String header = readMobileConnectConfigResult.get("HeaderText") + " " + serviceProvider;
        String message = readMobileConnectConfigResult.get("MessageText") + " " + serviceProvider;
//        String message = mePinConfig.getMessageText() + " " + serviceProvider;
        String client_id = readMobileConnectConfigResult.get("ClientID");//"815ed87f78f81f4e108650f0cbb98f46";//mePinConfig.getClientID();
        String account = mepinID;
        String expiry_time = "60"; //expiry time in seconds
        String listenerWebappHost = readMobileConnectConfigResult.get("ListenerWebappHost");
        if (listenerWebappHost == null || listenerWebappHost.isEmpty()) {
            listenerWebappHost = "http://" + System.getProperty("carbon.local.ip") + ":9764";
        }
        String callback_url = listenerWebappHost + Constants.LISTNER_WEBAPP_MEPIN_CONTEXT;
        //String confirmation_policy = mePinConfig.getConfirmationPolicy();

        String query = String.format("identifier=%s&short_message=%s&header=%s&message=%s&client_id=%s&account=%s" +
                        "&expiry_time=%s&callback_url=%s&confirmation_policy=%s",
                URLEncoder.encode(identifier, charset),
                URLEncoder.encode(short_message, charset),
                URLEncoder.encode(header, charset),
                URLEncoder.encode(message, charset),
                URLEncoder.encode(client_id, charset),
                URLEncoder.encode(account, charset),
                URLEncoder.encode(expiry_time, charset),
                URLEncoder.encode(callback_url, charset),
                URLEncoder.encode(confirmation_policy, charset)
        );
        log.debug("MePin query: " + query);

        String response = postRequest(url, query, charset);

        JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
        log.debug("MePin JSON Response: " + responseJson);

        return responseJson;
    }

    /**
     * Post request.
     *
     * @param url the url
     * @param query the query
     * @param charset the charset
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws XPathExpressionException the x path expression exception
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     */
    private String postRequest(String url, String query, String charset) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {

        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();

        ReadMobileConnectConfig readMobileConnectConfig = new ReadMobileConnectConfig();
        Map<String, String> readMobileConnectConfigResult;
        readMobileConnectConfigResult = readMobileConnectConfig.query("MePIN");
        connection.setDoOutput(true); // Triggers POST.
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
        connection.setRequestProperty("Authorization", "Basic "+readMobileConnectConfigResult.get("AuthToken"));
//        connection.setRequestProperty("Authorization", "Basic "+mePinConfig.getAuthToken());

        OutputStream output = connection.getOutputStream();
        String responseString = "";

        output.write(query.getBytes(charset));

        int status = connection.getResponseCode();

        if (log.isDebugEnabled()) {
            log.debug("MePIN Response Code :" + status);
        }

        try {
            switch (status) {
                case 200:
                case 201:
                case 400:
                case 403:
                case 404:
                case 500:
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    responseString = sb.toString();
                    break;
            }
        } catch (Exception httpex) {
            if (connection.getErrorStream() != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                responseString = sb.toString();
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("MePIN Response :" + responseString);
        }
        return responseString;
    }
}
