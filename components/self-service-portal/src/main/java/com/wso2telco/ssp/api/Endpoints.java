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

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.ssp.exception.ApiException;
import com.wso2telco.ssp.model.OrderByType;
import com.wso2telco.ssp.model.PagedResults;
import com.wso2telco.ssp.service.AdminService;
import com.wso2telco.ssp.service.DbService;
import com.wso2telco.ssp.service.DiscoveryService;
import com.wso2telco.ssp.service.UserService;
import com.wso2telco.ssp.util.Pagination;
import com.wso2telco.ssp.util.PrepareResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * API Endpoints
 */
@Path("v1/")
public class Endpoints {

    private static Log log = LogFactory.getLog(Endpoints.class);

    private static MobileConnectConfig.SelfServicePortalConfig selfServicePortalConfig;

    static {
        selfServicePortalConfig =
                new ConfigurationServiceImpl().getDataHolder().getMobileConnectConfig().getSelfServicePortalConfig();
    }

    /**
     * Redirects user to OAuth page of MIG
     * @param msisdn login hint msisdn
     * @param acr acr value
     * @return redirect result
     * @throws ApiException Checked exception thrown to indicate that API request fails to process
     */
    @GET
    @Path("auth/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response RedirectToOAuthPage(@QueryParam("msisdn") String msisdn,
                                        @QueryParam("acr") String acr) throws ApiException {
        String callbackUrl = selfServicePortalConfig.getCallbackUrl();
        String clientKey = selfServicePortalConfig.getSPOAuthClientKey();
        String authorizeCall = selfServicePortalConfig.getAuthorizeCall();

        if(StringUtils.isEmpty(msisdn)){
            throw new ApiException("MSISDN Missing", "no_msisdn", Response.Status.BAD_REQUEST);
        }

        if(StringUtils.isEmpty(callbackUrl)){
            throw new ApiException("Missing Callback URL", "missing_config", Response.Status.SERVICE_UNAVAILABLE);
        }

        if(StringUtils.isEmpty(clientKey)){
            throw new ApiException("Missing Client Key", "missing_config", Response.Status.SERVICE_UNAVAILABLE);
        }

        if(StringUtils.isEmpty(authorizeCall)){
            throw new ApiException("Missing Authorize Call URL", "missing_config", Response.Status.SERVICE_UNAVAILABLE);
        }

        if(StringUtils.isEmpty(acr)){
            acr = "2";
        }

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("login_hint", msisdn));
        params.add(new BasicNameValuePair("nonce", "kkk"));
        params.add(new BasicNameValuePair("state", "aaa"));
        params.add(new BasicNameValuePair("redirect_uri", callbackUrl));
        params.add(new BasicNameValuePair("client_id", clientKey));
        params.add(new BasicNameValuePair("acr_values", acr));
        params.add(new BasicNameValuePair("scope", "openid"));
        params.add(new BasicNameValuePair("response_type", "code"));

        //todo: call discovery service
        String operator = DiscoveryService.getOperator(msisdn);

        Map<String, String> data = new HashMap<String, String>();
        data.put("operator", operator);
        String uri = StrSubstitutor.replace(authorizeCall, data);

        try {
            return PrepareResponse.Redirect(uri, params);
        }catch (URISyntaxException e){
            throw new ApiException("Invalid URL in Configs", "invalid_config", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Callback URL of the OAuth call from MIG. This API endpoint redirects the request either with
     * success code or error code back to self service portal URL. You can set redirect URI of the
     * Service Provider in MIG for this API endpoint. Callback endpoint takes code parameter from
     * initial auth call and redirects to redirect url (configured in mobile connect) with access token.
     * On failure redirects to redirect url with 'error' parameter.
     * @param code auth code
     * @return redirect response
     * @throws ApiException Checked exception thrown to indicate that API request fails to process
     */
    @GET
    @Path("auth/callback")
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetTokenFromAuthCode(@QueryParam("code") String code) throws ApiException {

        String urlFragment = "";
        String uiLoginUrl = selfServicePortalConfig.getUILoginUrl();
        if(StringUtils.isEmpty(uiLoginUrl)){
            throw new ApiException("Missing UI Login URL", "missing_config", Response.Status.SERVICE_UNAVAILABLE);
        }

        //return an error if code is not provided
        if(StringUtils.isEmpty(code)){
            urlFragment = "/0/code_not_provided";
            if(log.isDebugEnabled()){
                log.debug("Code not provided for auth/callback");
            }
        }

        //exchange code with access token
        if(StringUtils.isNotEmpty(code)){

            //get access token from code
            String accessToken = UserService.getAccessTokenFromCode(code);

            if(StringUtils.isNotEmpty(accessToken)) {
                urlFragment = "/1/" + accessToken;

                if(log.isDebugEnabled()){
                    log.debug("Access token retrieved : " + accessToken);
                }
            }else{
                urlFragment = "/0/login_unsuccessful";

                if(log.isDebugEnabled()){
                    log.debug("No access token auth/callback");
                }
            }
        }

        try {
            return PrepareResponse.Redirect(uiLoginUrl + urlFragment, null);
        }catch (URISyntaxException e){
            throw new ApiException("Invalid URL in Configs", "invalid_config", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Validates the access token
     * @param accessToken access token
     * @return user info response on valid access token
     * @throws ApiException Checked exception thrown to indicate that API request fails to process
     */
    @GET
    @Path("auth/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ValidateToken(@QueryParam("access_token") String accessToken) throws ApiException {

        if(StringUtils.isEmpty(accessToken)){
            throw new ApiException("Access Token Missing", "no_access_token", Response.Status.UNAUTHORIZED);
        }

        String output = UserService.getUserInfo(accessToken);

        JSONObject outputResponse = new JSONObject(output);
        if(!outputResponse.isNull("sub")){
            return PrepareResponse.Success(outputResponse);
        }

        String error_message = !outputResponse.isNull("error_description") ?
                outputResponse.getString("error_description") : "Token error";
        String error_code = !outputResponse.isNull("error") ? outputResponse.getString("error") :
                "error";

        throw new ApiException(error_message, error_code, Response.Status.UNAUTHORIZED);
    }

    /**
     * Returns paged login history result set
     * @param accessToken access token
     * @param page page number
     * @param limit results per page
     * @return login history results
     * @throws ApiException Checked exception thrown to indicate that API request fails to process
     */
    @GET
    @Path("user/login_history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response LoginHistory(@QueryParam("access_token") String accessToken,
                                 @QueryParam("page") String page,
                                 @QueryParam("limit") String limit) throws ApiException {

        // call user info to validate access token
        String output = UserService.getUserInfo(accessToken);
        JSONObject outputResponse = new JSONObject(output);
        if(outputResponse.isNull(selfServicePortalConfig.getMsisdnClaim())){
            throw new ApiException("Invalid Token", "invalid_token", Response.Status.UNAUTHORIZED);
        }

        // paging object to limit result set per call
        Pagination pagination = new Pagination(page, limit);

        try {
            PagedResults lh = DbService.getLoginHistoryByMsisdn(
                    outputResponse.getString(selfServicePortalConfig.getMsisdnClaim()),
                    "id", OrderByType.ASC, pagination);
            return PrepareResponse.Success(lh);
        }catch (DBUtilException e){
            throw new ApiException(e.getMessage(), "login_history_error", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns application login counts.
     * @param accessToken access token
     * @return application login results
     * @throws ApiException Checked exception thrown to indicate that API request fails to process
     */
    @GET
    @Path("user/app_logins")
    @Produces(MediaType.APPLICATION_JSON)
    public Response LoginHistory(@QueryParam("access_token") String accessToken) throws ApiException {

        // call user info to validate access token
        String output = UserService.getUserInfo(accessToken);
        JSONObject outputResponse = new JSONObject(output);
        if(outputResponse.isNull(selfServicePortalConfig.getMsisdnClaim())){
            throw new ApiException("Invalid Token", "invalid_token", Response.Status.UNAUTHORIZED);
        }

        try {
            PagedResults lh = DbService.getLoginApplicationsByMsisdn(
                    outputResponse.getString(selfServicePortalConfig.getMsisdnClaim()),
                    "count", OrderByType.DESC);
            return PrepareResponse.Success(lh);
        }catch (DBUtilException e){
            throw new ApiException(e.getMessage(), "app_login_error", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns user LOA
     * @param accessToken access token
     * @return user LOA
     * @throws ApiException Checked exception thrown to indicate that API request fails to process
     */
    @GET
    @Path("user/loa")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ResetPin(@QueryParam("access_token") String accessToken) throws ApiException {
        // call user info to validate access token
        String output = UserService.getUserInfo(accessToken);
        JSONObject outputResponse = new JSONObject(output);
        if(outputResponse.isNull(selfServicePortalConfig.getMsisdnClaim())){
            throw new ApiException("Invalid Token", "invalid_token", Response.Status.UNAUTHORIZED);
        }

        AdminService adminService = new AdminService();
        String loa = adminService.getLoa(outputResponse.getString(selfServicePortalConfig.getMsisdnClaim()));

        return PrepareResponse.Success("loa", loa);
    }

    /**
     * Resets the PIN of a user. Current PIN must match with the provided current PIN
     * @param accessToken access token
     * @param current current PIN
     * @param new_pin new PIN
     * @return Success or Error
     * @throws ApiException Checked exception thrown to indicate that API request fails to process
     */
    @POST
    @Path("user/pin_reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ResetPin(@QueryParam("access_token") String accessToken,
                                 @QueryParam("current") String current,
                                 @QueryParam("new_pin") String new_pin) throws ApiException {

        if(StringUtils.isEmpty(current)){
            throw new ApiException("Current PIN not provided", "current_pin_missing", Response.Status.BAD_REQUEST);
        }

        if(StringUtils.isEmpty(new_pin)){
            throw new ApiException("New PIN not provided", "new_pin_missing", Response.Status.BAD_REQUEST);
        }

        // call user info to validate access token
        String output = UserService.getUserInfo(accessToken);
        JSONObject outputResponse = new JSONObject(output);
        if(outputResponse.isNull(selfServicePortalConfig.getMsisdnClaim())){
            throw new ApiException("Invalid Token", "invalid_token", Response.Status.UNAUTHORIZED);
        }

        AdminService adminService = new AdminService();
        String current_pin = adminService.getPin(outputResponse.getString(selfServicePortalConfig.getMsisdnClaim()));

        if(current.equals(current_pin)){
            // current pin loaded from IS and provided pin are matched
            adminService.setPin(outputResponse.getString(selfServicePortalConfig.getMsisdnClaim()), new_pin);
        } else {
            throw new ApiException("PIN mismatched", "pin_mismatched", Response.Status.BAD_REQUEST);
        }

        return PrepareResponse.Success("success", true);
    }
}
