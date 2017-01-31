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

package com.wso2telco.gsma.authenticators.ussd.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wso2telco.Util;
import com.wso2telco.core.config.DataHolder;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.gsma.authenticators.ussd.USSDRequest;
import com.wso2telco.gsma.authenticators.util.BasicFutureCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public abstract class UssdCommand {

    private static Log log = LogFactory.getLog(UssdCommand.class);

    public void execute(String msisdn, String sessionID, String serviceProvider, String operator, String client_id) throws IOException {

//        AuthenticationContext ctx = getAuthenticationContext(sessionID);
//        String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(ctx.getQueryParams(),
//                ctx.getCallerSessionKey(), ctx.getContextIdentifier());
//
//        Map<String, String> paramMap = Util.createQueryParamMap(queryParams);
//
//        String client_id = paramMap.get(Constants.CLIENT_ID);

        USSDRequest ussdRequest = getUssdRequest(msisdn, sessionID, serviceProvider, operator, client_id);

        Gson gson = new GsonBuilder().serializeNulls().create();
        String reqString = gson.toJson(ussdRequest);

        postRequest(getUrl(msisdn), reqString, operator);
    }

    protected abstract String getUrl(String msisdn);

    protected abstract USSDRequest getUssdRequest(String msisdn, String sessionID, String serviceProvider, String operator, String client_id);

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
        BasicFutureCallback futureCallback = new BasicFutureCallback();
        final HttpPost postRequest = futureCallback.getPostRequest();
        try {
            postRequest.setURI(new URI(url));
        } catch (URISyntaxException ex) {
            log.error("Malformed URL - " + url, ex);
        }

        postRequest.addHeader("accept", "application/json");
        postRequest.addHeader("Authorization", "Bearer " + ussdConfig.getAuthToken());

        if (operator != null) {
            postRequest.addHeader("operator", operator);
        }

        StringEntity input = new StringEntity(requestStr);
        input.setContentType("application/json");

        postRequest.setEntity(input);

        if(log.isDebugEnabled()) {
            log.debug("Posting data  [ " + requestStr + " ] to url [ " + url + " ]");
        }
        Util.sendAsyncRequest(postRequest, futureCallback);
    }

    /**
     * Gets authentication context from session id
     * @param sessionID Session ID
     * @return Authentication Context
     */
    private AuthenticationContext getAuthenticationContext(String sessionID) {
        AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(sessionID);
        Object cacheEntryObj = AuthenticationContextCache.getInstance().getValueFromCache(cacheKey);
        return ((AuthenticationContextCacheEntry) cacheEntryObj).getContext();
    }
}
