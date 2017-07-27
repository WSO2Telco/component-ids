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
