package com.wso2telco.gsma.authenticators.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import java.io.IOException;

public class BasicFutureCallback implements FutureCallback<HttpResponse> {


    private static Log log = LogFactory.getLog(BasicFutureCallback.class);
    protected HttpPost postRequest = new HttpPost();
    protected CloseableHttpAsyncClient client;


    public void cancelled() {
        log.warn("Operation cancelled while calling end point - " +
                this.getPostRequest().getURI().getSchemeSpecificPart());
        closeClient();
    }

    public void completed(HttpResponse response) {
        if ((response.getStatusLine().getStatusCode() == 200)) {
            log.info("Success Request - " + postRequest.getURI().getSchemeSpecificPart());

        } else {
            log.error("Failed Request - " + postRequest.getURI().getSchemeSpecificPart());
        }
        closeClient();
    }

    public void failed(Exception exception) {
        log.error("Error occurred while calling end point - " + postRequest.getURI().getSchemeSpecificPart() +
                "; Error - " + exception);
        closeClient();
    }

    public HttpPost getPostRequest() {
        return postRequest;
    }

    public void setPostRequest(HttpPost postRequest) {
        this.postRequest = postRequest;
    }

    public void setClient(CloseableHttpAsyncClient client) {
        this.client = client;
    }

    protected void closeClient() {
        try {
            client.close();
        } catch (IOException e) {
            log.error("Error closing async client", e);
        }
    }

}
