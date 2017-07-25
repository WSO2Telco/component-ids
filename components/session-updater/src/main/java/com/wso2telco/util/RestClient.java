package com.wso2telco.util;


import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.user.UserRegistration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class RestClient {

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(RestClient.class);


    public void postRequest(String url, String requestStr, String operator) throws IOException {
        MobileConnectConfig.USSDConfig ussdConfig = configurationService.getDataHolder().getMobileConnectConfig()
                .getUssdConfig();

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

        if (log.isDebugEnabled()) {
            log.debug("Posting data  [ " + requestStr + " ] to url [ " + url + " ]");
        }
        HttpClient client = new DefaultHttpClient();
        client.execute(postRequest);
    }
}
