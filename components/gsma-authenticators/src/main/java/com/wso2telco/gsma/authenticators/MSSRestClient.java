/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.gsma.authenticators;

import com.google.gson.Gson;
import com.wso2telco.core.config.MSSServiceURL;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.model.MSSRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;


// TODO: Auto-generated Javadoc

/**
 * The Class MSSRestClient.
 */
public class MSSRestClient extends Thread {

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(MSSRestClient.class);

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /**
     * The context identifier.
     */
    String contextIdentifier;

    /**
     * The mss request.
     */
    MSSRequest mssRequest;

    /**
     * Instantiates a new MSS rest client.
     *
     * @param contextIdentifier the context identifier
     * @param mssRequest        the mss request
     */
    public MSSRestClient(String contextIdentifier, MSSRequest mssRequest) {
        this.contextIdentifier = contextIdentifier;
        this.mssRequest = mssRequest;

    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {

            Gson gson = new Gson();
            org.apache.http.client.HttpClient client = new DefaultHttpClient();

            String serviceURL = String.format(MSSServiceURL.MSS_SIGNATURE_SERVICE,
                    configurationService.getDataHolder().getMobileConnectConfig().getMSS().getEndpoint());
            String json = gson.toJson(mssRequest);

            HttpPost httprequest = new HttpPost(serviceURL);
            httprequest.addHeader("Accept", "application/json");
            StringEntity entity = new StringEntity(json, "application/json", "ISO-8859-1");
            httprequest.setEntity(entity);
            HttpResponse httpResponse = client.execute(httprequest);
            if (httpResponse.getStatusLine().getStatusCode() == configurationService.getDataHolder()
                    .getMobileConnectConfig().getMSS().getSuccessStatus()) {
                DBUtils.updateUserResponse(contextIdentifier, String.valueOf(UserResponse.APPROVED));

            } else {
                DBUtils.updateUserResponse(contextIdentifier, String.valueOf(UserResponse.REJECTED));

            }

        } catch (Exception ex) {
            log.error("Exception during Instantiating a new MSS rest client  " + ex);

        }

    }


    /**
     * The Enum UserResponse.
     */
    private enum UserResponse {

        /**
         * The pending.
         */
        PENDING,

        /**
         * The approved.
         */
        APPROVED,

        /**
         * The rejected.
         */
        REJECTED
    }


}
