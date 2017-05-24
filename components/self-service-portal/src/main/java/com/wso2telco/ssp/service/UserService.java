/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.ssp.service;

import com.sun.jersey.core.util.Base64;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.ssp.api.Endpoints;
import com.wso2telco.ssp.exception.ApiException;
import com.wso2telco.ssp.util.Constants;
import com.wso2telco.ssp.util.HttpClientProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * User service contains methods to fetch data from MIG
 */
public class UserService {

    private static Log log = LogFactory.getLog(Endpoints.class);

    private static MobileConnectConfig.SelfServicePortalConfig selfServicePortalConfig;

    private static String bearerToken;

    static {
        selfServicePortalConfig =
                new ConfigurationServiceImpl().getDataHolder().getMobileConnectConfig().getSelfServicePortalConfig();
        byte[] encodedBytes = Base64.encode(selfServicePortalConfig.getSPOAuthClientKey() + ":"
                + selfServicePortalConfig.getSPOAuthClientSecret());
        bearerToken = new String(encodedBytes);
    }

    /**
     * Send user info call to MIG and returns the output as a string
     * @param accessToken access token
     * @return user info response
     * @throws ApiException Checked exception thrown to indicate that API request fails to process
     */
    public static String getUserInfo(String accessToken) throws ApiException {
        HttpClient client = HttpClientProvider.GetHttpClient();
        HttpPost httpPost = new HttpPost(selfServicePortalConfig.getUserInfoCall());

        httpPost.setHeader(Constants.HEADER_AUTHORIZATION, "Bearer " + accessToken);

        try {
            HttpResponse httpResponse = client.execute(httpPost);
            return IOUtils.toString(httpResponse.getEntity().getContent());
        }catch (IOException e){
            throw new ApiException("Server Error", "server_error", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get access token from code
     * @param code code
     * @return access token
     * @throws ApiException Checked exception thrown to indicate that API request fails to process
     */
    public static String getAccessTokenFromCode(String code) throws ApiException {

        String tokenEndpoint = selfServicePortalConfig.getTokenEndpoint();
        HttpClient client = HttpClientProvider.GetHttpClient();
        HttpPost httpPost = new HttpPost(tokenEndpoint);

        ArrayList<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair(Constants.TOKEN_REQUEST_GRANT_TYPE, "authorization_code"));
        postParameters.add(new BasicNameValuePair(Constants.TOKEN_REQUEST_CODE, code));
        postParameters.add(new BasicNameValuePair(Constants.TOKEN_REQUEST_REDIRECT_URI,
                selfServicePortalConfig.getCallbackUrl()));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
        }catch (UnsupportedEncodingException e){
            log.error("Post Parameter Error : " + e.getMessage());
            throw new ApiException("Post Parameter Error", "encoding_error", Response.Status.INTERNAL_SERVER_ERROR);
        }

        httpPost.setHeader(Constants.HEADER_AUTHORIZATION, "Bearer " + bearerToken);
        httpPost.setHeader(Constants.HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded");

        if(log.isDebugEnabled()){
            log.debug("Calling token endpoint");
        }

        try {
            HttpResponse httpResponse = client.execute(httpPost);
            String output = IOUtils.toString(httpResponse.getEntity().getContent());
            JSONObject json = new JSONObject(output);
            return json.getString(Constants.TOKEN_RESPONSE_ACCESS_TOKEN);
        }catch (JSONException e){
            log.error("Error occurred trying to parse response");
            throw new ApiException("Server Error", "response_error", Response.Status.INTERNAL_SERVER_ERROR);
        }catch (UnsupportedEncodingException e){
            log.error("Error occurred trying to build post parameters");
            throw new ApiException("Server Error", "encoding_error", Response.Status.INTERNAL_SERVER_ERROR);
        }catch (IOException e){
            log.error("Error occurred trying to call webservice : " + tokenEndpoint);
            throw new ApiException("Server Error", "webservice_error", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
