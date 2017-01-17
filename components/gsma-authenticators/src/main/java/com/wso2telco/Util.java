package com.wso2telco;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.io.IOException;
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

}
