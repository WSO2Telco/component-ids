package com.wso2telco;

import com.wso2telco.core.config.ReadMobileConnectConfig;
import com.wso2telco.gsma.authenticators.util.BasicFutureCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Util {
    private static Log log = LogFactory.getLog(Util.class);


    /**
     * Asynchronously call the REST endpoint
     *
     * @param postRequest    Request
     * @param futureCallback Call back function
     * @throws java.io.IOException
     */
    public static void sendAsyncRequest(final HttpPost postRequest, BasicFutureCallback futureCallback)
            throws IOException {
        ReadMobileConnectConfig readMobileConnectConfig = new ReadMobileConnectConfig();

        int socketTimeout = 60000;
        int connectTimeout = 60000;
        int connectionRequestTimeout = 30000;

        try {
            Map<String, String> timeoutConfigMap = readMobileConnectConfig
                    .query("TimeoutConfig");
            socketTimeout = Integer.parseInt(timeoutConfigMap.get("SocketTimeout")) * 1000;
            connectTimeout = Integer.parseInt(timeoutConfigMap.get("ConnectTimeout")) * 1000;
            connectionRequestTimeout = Integer.parseInt(timeoutConfigMap.get("ConnectionRequestTimeout")) * 1000;

        } catch (Exception e) {

            log.debug("Error in reading TimeoutConfig:using default timeouts:"
                              + e);
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout).build();

        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig).build();
        futureCallback.setClient(client);
        client.start();
        client.execute(postRequest, futureCallback);
    }

    /**
     * Converts query params string to a map
     * @param params query params as a string
     * @return Map of query params
     */
    public static Map<String, String> createQueryParamMap(String params) {
        String[] queryParams = params.split("&");
        Map<String, String> paramMap = new HashMap<>();

        for (String queryParam : queryParams) {
            String[] param = queryParam.split("=");
            String key = param[0];
            String value = null;
            if (param.length > 1) {
                value = param[1];
            }
            paramMap.put(key, value);
        }
        return paramMap;
    }
}
