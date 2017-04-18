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
package com.wso2telco.ssp.api;

import com.sun.jersey.core.util.Base64;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.ssp.model.OrderByType;
import com.wso2telco.ssp.model.PagedResults;
import com.wso2telco.ssp.service.DbService;
import com.wso2telco.ssp.util.Constants;
import com.wso2telco.ssp.util.HttpClientProvider;
import com.wso2telco.ssp.util.Pagination;
import com.wso2telco.ssp.util.PrepareResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * API Endpoints
 */
@Path("/api/v1/")
public class Endpoints {

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
     * Validates the access token
     * @param accessToken access token
     * @return user info response on valid access token
     * @throws IOException
     */
    @GET
    @Path("auth/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ValidateToken(@QueryParam("access_token") String accessToken) throws IOException {

        String output = getUserInfo(accessToken);

        JSONObject outputResponse = new JSONObject(output);
        if(!outputResponse.isNull("sub")){
            return PrepareResponse.Success(outputResponse);
        }

        String error_message = !outputResponse.isNull("error_description") ?
                outputResponse.getString("error_description") : "Token error";
        String error_code = !outputResponse.isNull("error") ? outputResponse.getString("error") :
                "error";
        return PrepareResponse.Error(error_message, error_code, Response.Status.UNAUTHORIZED);
    }

    /**
     * Callback endpoint takes code parameter from initial auth call and redirects to redirect url (configured in
     * mobile connect) with access token. On failure redirects to redirect url with 'error' parameter.
     * @param code auth code
     * @return redirect response
     * @throws IOException
     * @throws URISyntaxException
     * @throws JSONException
     */
    @GET
    @Path("auth/callback")
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetTokenFromAuthCode(@QueryParam("code") String code)
            throws IOException, URISyntaxException, JSONException {

        ArrayList<NameValuePair> params = new ArrayList<>();

        //return an error if code is not provided
        if(StringUtils.isEmpty(code)){
            params.add(new BasicNameValuePair(Constants.REDIRECT_PARAM_ERROR, "Code not provided"));

            if(log.isDebugEnabled()){
                log.debug("Code not provided for auth/callback");
            }
        }

        //exchange code with access token
        if(StringUtils.isNotEmpty(code)){

            //get access token from code
            String accessToken = getAccessTokenFromCode(code);

            if(StringUtils.isNotEmpty(accessToken)) {
                params.add(new BasicNameValuePair(Constants.REDIRECT_PARAM_ACCESS_TOKEN, accessToken));

                if(log.isDebugEnabled()){
                    log.debug("Access token retrieved : " + accessToken);
                }
            }else{
                params.add(new BasicNameValuePair(Constants.REDIRECT_PARAM_ERROR, "Login unsuccessful"));

                if(log.isDebugEnabled()){
                    log.debug("No access token auth/callback");
                }
            }
        }

        return PrepareResponse.Redirect(selfServicePortalConfig.getUILoginUrl(), params);
    }

    /**
     * Returns paged login history result set
     * @param accessToken access token
     * @param page page number
     * @param limit results per page
     * @return login history results
     * @throws IOException
     */
    @GET
    @Path("user/login_history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response LoginHistory(@QueryParam("access_token") String accessToken,
                                 @QueryParam("page") String page,
                                 @QueryParam("limit") String limit) throws IOException {

        // call user info to validate access token
        String output = getUserInfo(accessToken);
        JSONObject outputResponse = new JSONObject(output);
        if(outputResponse.isNull("phone_number")){
            return PrepareResponse.Error("Invalid Token", "invalid_token", Response.Status.UNAUTHORIZED);
        }

        // paging object to limit result set per call
        Pagination pagination = new Pagination(page, limit);

        try {
            PagedResults lh = DbService.getLoginHistoryByMsisdn(outputResponse.getString("phone_number"),
                    "id", OrderByType.ASC, pagination);
            return PrepareResponse.Success(new JSONObject(lh));
        }catch (Exception e){
            return PrepareResponse.Error(e.getMessage(), "login_history_error", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Send user info call to MIG and returns the output as a string
     * @param accessToken access token
     * @return user info response
     * @throws IOException
     */
    private String getUserInfo(String accessToken) throws IOException {
        HttpClient client = HttpClientProvider.GetHttpClient();
        HttpPost httpPost = new HttpPost("https://localhost:9443/oauth2/userinfo?schema=openid");

        httpPost.setHeader(Constants.HEADER_AUTHORIZATION, "Bearer " + accessToken);

        HttpResponse httpResponse = client.execute(httpPost);
        return IOUtils.toString(httpResponse.getEntity().getContent());
    }

    /**
     * Get access token from code
     * @param code code
     * @return access token
     * @throws IOException
     * @throws JSONException
     */
    private String getAccessTokenFromCode(String code)
            throws IOException, JSONException {

        String tokenEndpoint = selfServicePortalConfig.getTokenEndpoint();
        HttpClient client = HttpClientProvider.GetHttpClient();
        HttpPost httpPost = new HttpPost(tokenEndpoint);

        ArrayList<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair(Constants.TOKEN_REQUEST_GRANT_TYPE, "authorization_code"));
        postParameters.add(new BasicNameValuePair(Constants.TOKEN_REQUEST_CODE, code));
        postParameters.add(new BasicNameValuePair(Constants.TOKEN_REQUEST_REDIRECT_URI,
                selfServicePortalConfig.getCallbackUrl()));

        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));

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
            throw e;
        }catch (UnsupportedEncodingException e){
            log.error("Error occurred trying to build post parameters");
            throw e;
        }catch (IOException e){
            log.error("Error occurred trying to call webservice : " + tokenEndpoint);
            throw e;
        }
    }
}
