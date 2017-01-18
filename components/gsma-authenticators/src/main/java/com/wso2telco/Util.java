package com.wso2telco;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
     * @param latch          CountDownLatch
     * @throws java.io.IOException
     */
    public static void sendAsyncRequest(final HttpPost postRequest, FutureCallback futureCallback, CountDownLatch latch)
            throws IOException {
        CloseableHttpAsyncClient client;
        client = HttpAsyncClients.createDefault();
        client.start();
        client.execute(postRequest, futureCallback);
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("Error occurred while calling end points - " + e);
        }
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
