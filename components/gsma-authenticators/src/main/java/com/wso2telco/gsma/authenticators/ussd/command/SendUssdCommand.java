package com.wso2telco.gsma.authenticators.ussd.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wso2telco.Util;
import com.wso2telco.core.config.DataHolder;
import com.wso2telco.core.config.MobileConnectConfig;
import com.wso2telco.gsma.authenticators.ussd.USSDRequest;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by isuru on 12/23/16.
 */
public abstract class SendUssdCommand {

    private static Log log = LogFactory.getLog(SendUssdCommand.class);

    public void execute(String msisdn, String sessionID, String serviceProvider, String operator) throws IOException {
        USSDRequest ussdRequest = getUssdRequest(msisdn, sessionID, serviceProvider, operator);

        Gson gson = new GsonBuilder().serializeNulls().create();
        String reqString = gson.toJson(ussdRequest);

        postRequest(getUrl(msisdn), reqString, operator);
    }

    protected abstract String getAccessToken();

    protected abstract String getUrl(String msisdn);

    protected abstract USSDRequest getUssdRequest(String msisdn, String sessionID, String serviceProvider, String operator);

    /**
     * Post request.
     *
     * @param url        the url
     * @param requestStr the request str
     * @param operator   the operator
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void postRequest(String url, String requestStr, String operator) throws IOException {
        MobileConnectConfig.USSDConfig ussdConfig = DataHolder.getInstance().getMobileConnectConfig().getUssdConfig();

        final HttpPost postRequest = new HttpPost(url);
        postRequest.addHeader("accept", "application/json");
        postRequest.addHeader("Authorization", "Bearer " + ussdConfig.getAuthToken());

        if (operator != null) {
            postRequest.addHeader("operator", operator);
        }

        StringEntity input = new StringEntity(requestStr);
        input.setContentType("application/json");

        postRequest.setEntity(input);
        final CountDownLatch latch = new CountDownLatch(1);

        log.info("Posting data  [ " + requestStr + " ] to url [ " + url + " ]");

        FutureCallback<HttpResponse> futureCallback = new FutureCallback<HttpResponse>() {
            @Override
            public void completed(final HttpResponse response) {
                latch.countDown();
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED
                        || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    log.error("Error occurred while calling end point - " + response.getStatusLine().getStatusCode() +
                            "; Error - " +
                            response.getStatusLine().getReasonPhrase());
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Success Request - " + postRequest.getURI().getSchemeSpecificPart());
                    }
                }
            }

            @Override
            public void failed(final Exception ex) {
                latch.countDown();
                log.error("Error occurred while calling end point - " + postRequest.getURI().getSchemeSpecificPart() +
                        "; Error - " + ex);
            }

            @Override
            public void cancelled() {
                latch.countDown();
                log.warn("Operation cancelled while calling end point - " +
                        postRequest.getURI().getSchemeSpecificPart());
            }
        };
        Util.sendAsyncRequest(postRequest, futureCallback, latch);
    }
}
