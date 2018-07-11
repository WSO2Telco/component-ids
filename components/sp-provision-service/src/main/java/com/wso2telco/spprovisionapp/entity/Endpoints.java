package com.wso2telco.spprovisionapp.entity;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.spprovisionapp.api.ApiCallsInAM;
import com.wso2telco.spprovisionapp.api.ApiCallsInIS;
import com.wso2telco.spprovisionapp.conn.ApimgtConnectionUtil;
import com.wso2telco.spprovisionapp.conn.ConnectdbConnectionUtil;
import com.wso2telco.spprovisionapp.utils.DataBaseFunction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * The following tasks should be performed when this API is called (in exact order)
 * 1. Login and Sign In to the API Manager
 * 2. Create Application
 * 3. Activate Application
 * 4. Update application to the approved state
 * 5. Subscribe to the APIs
 * 6. Update Subscriptions
 * 7. Populate Subscription Validator
 * 8. Generate client key and client secret
 * 9. Scope Configurations for the application
 * 10. Test the application by getting Authorize code,Token and UserInfo
 * 11. Update the client key and client secret
 * 12. Test the updated application by getting Authorize code,Token and UserInfo
 * 13. Jira comment generation
 */
@Path("/")
public class Endpoints {
    private static final Log log = LogFactory.getLog(Endpoints.class);
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    private static MobileConnectConfig mobileConnectConfigs;

    static {
        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/operator/{operatorName}")
    public Response serviceProviderProvision(@Context HttpServletRequest httpServletRequest,
                                             @Context HttpServletResponse httpServletResponse,
                                             @PathParam("operatorName") String operatorName,
                                             String requestParamInfo) {

        log.info("Service provision API called with operatorName: " + operatorName);
        String accessToken = null;
        try {
            JSONObject requestBody = stringToJsonObject(requestParamInfo);
            String userName = requestBody.getString("userName");
            String appName = requestBody.getString("applicationName");
            String appDescription = appName; //add this as a request body parameter
            String firstName = requestBody.getString("firstName");
            String lastName = requestBody.getString("lastName");
            String devMail = requestBody.getString("developerEmail");
            String applicationTier = requestBody.getString("applicationTier");
            String newConsumerKey = requestBody.getString("newConsumerKey");
            String newConsumerSecret = requestBody.getString("newConsumerSecret");
            String apis = requestBody.getString("api");
            String callbackUrl = requestBody.getString("callbackUrl");
            String scopes = requestBody.getString("scopes");
            boolean trustedServiceProvider = requestBody.getBoolean("trustedServiceProvider");

            //create a user in AM
            ApiCallsInAM amApiCalls = new ApiCallsInAM();
            String respCreateUserCall = removeHttpCode(amApiCalls.createNewUserInAm(appName, firstName, lastName,
                    devMail));
            if (log.isDebugEnabled()) {
                log.debug("SPProvisionAPI: AM user [username: " + userName + "] create response - "
                        + respCreateUserCall);
            }
            if (checkResponseErrors(respCreateUserCall)) {
                log.error("SP Provision API: Failed to create new AM user. - " + respCreateUserCall);
                return Response.status(500).entity(respCreateUserCall).build();
            } else {
                log.info("SPProvisionAPI: AM User created." );
            }

            //Login to AM
            String respLoginCall = removeHttpCode(amApiCalls.loginToAm(appName));
            if (log.isDebugEnabled()) {
                log.debug("SPProvisionAPI: AM user login response - " + respLoginCall);
            }
            if (checkResponseErrors(respLoginCall)) {
                log.error("SP Provision API: AM user login failed. - " + respLoginCall);
                return Response.status(500).entity(respLoginCall).build();
            } else {
                log.info("SPProvisionAPI: Logged into AM successfully");
            }

            //Create application in AM
            String respCreateApp = removeHttpCode(amApiCalls.createApplication(appName,
                    appDescription, callbackUrl, applicationTier));
            if (log.isDebugEnabled()) {
                log.debug("SPProvisionAPI: Create application response - " + respCreateApp);
            }
            if (checkResponseErrors(respCreateApp)) {
                log.error("SP Provision API: Failed to create AM application. - " + respCreateApp);
                return Response.status(500).entity(respCreateApp).build();
            } else {
                log.info("SPProvisionAPI: AM application created successfully");
            }

            //Activate AM application
            String respActivateApp = DataBaseFunction.activateApplication(ApimgtConnectionUtil.getConnection(), appName);
            if (log.isDebugEnabled()) {
                log.debug("SPProvisionAPI: Activate AM application response - " + respActivateApp);
            }
            if (checkResponseErrors(respActivateApp)) {
                log.error("SPProvisionAPI: Failed to activate AM application. - " + respActivateApp);
                return Response.status(500).entity(respActivateApp).build();
            } else {
                log.info("SPProvisionAPI: AM application activated successfully");
            }

            //Update AM application
            String respApproveApp = DataBaseFunction.updateApplication(appName, ApimgtConnectionUtil.getConnection());
            if (log.isDebugEnabled()) {
                log.debug("SPProvisionAPI: AM application update response - " + respApproveApp);
            }
            if (checkResponseErrors(respApproveApp)) {
                log.error("SP Provision API: Failed update AM application. - " + respApproveApp);
                return Response.status(500).entity(respApproveApp).build();
            } else {
                log.info("SPProvisionAPI: AM application updated successfully");
            }

            //Subscribing to APIs
            List<MobileConnectConfig.Api> apiList = mobileConnectConfigs.getSpProvisionConfig()
                    .getApiConfigs().getApiList();
            String[] requestedApis = apis.split(",");
            for (MobileConnectConfig.Api api: apiList) {
                for (String requestedApi: requestedApis) {
                    if (api.getApiName().equalsIgnoreCase(requestedApi)) {
                        String respSubscribeCall = removeHttpCode(subscribeToApi(api, userName, appName, amApiCalls));

                        if (log.isDebugEnabled()) {
                            log.debug("SPProvisionAPI: Subscribe to API [" + api.getApiName() + "] response - "
                                    + respSubscribeCall);
                        }

                        if (checkResponseErrors(respSubscribeCall)) {
                            log.error("SP Provision API: Failed to create API subscription " +
                                    "[" + requestedApi + "] - " + respSubscribeCall);
                            return Response.status(500).entity(respSubscribeCall).build();
                        } else {
                            log.info("SPProvisionAPI: Subscribed to API [" + api.getApiName() + "] successfully");
                        }
                    }
                }
            }

            //Update subscriptions
            String respUpdateSubscriptions = DataBaseFunction.updateSubscriptions(ApimgtConnectionUtil.getConnection());
            if (log.isDebugEnabled()) {
                log.debug("SPProvisionAPI: Update subscriptions response - " + respUpdateSubscriptions);
            }
            if (checkResponseErrors(respUpdateSubscriptions)) {
                log.error("SP Provision API: Failed to update subscriptions. - " + respUpdateSubscriptions);
                return Response.status(500).entity(respUpdateSubscriptions).build();
            } else {
                log.info("APProvisionAPI: Subscriptions updated successfully");
            }

            //Populate subscription validator
            String respPopulateSubscriptionValidator = DataBaseFunction.populateSubscriptionValidator(
                    ApimgtConnectionUtil.getConnection());
            if (log.isDebugEnabled()) {
                log.debug("SPProvisionAPI: Populate subscription validator response - " +
                        respPopulateSubscriptionValidator);
            }
            if (checkResponseErrors(respPopulateSubscriptionValidator)) {
                log.error("SP Provision API: Failed to populate subscription validator. - "
                        + respPopulateSubscriptionValidator);
                return Response.status(500).entity(respPopulateSubscriptionValidator).build();
            } else {
                log.info("SPProvisionAPI: Subscription validator populated successfully");
            }


            //IS Calls
            ApiCallsInIS isApiCalls = new ApiCallsInIS();
            String[] status = isApiCalls.createServiceProvider(appName, callbackUrl, appDescription);

            //Access token Generation
            String generatedConsumerKey = null;
            String generatedConsumerSecret = null;
            if (status[0].equals("success")) {
                generatedConsumerKey = status[1];
                generatedConsumerSecret = status[2];
                accessToken = amApiCalls.getAccessToken(generatedConsumerKey, generatedConsumerSecret);

                if (log.isDebugEnabled()) {
                    log.debug("SPProvisionAPI: Generated Client Key: " +  generatedConsumerKey);
                    log.debug("SPProvisionAPI: Generated Client Secret: " + generatedConsumerSecret);
                    log.debug("SPProvisionAPI: Access Token: " + accessToken);
                }
            }

            //insert values to AM databases
            if (null != accessToken) {
                DataBaseFunction.insertValuesToAmDatabases(appName, generatedConsumerKey, generatedConsumerSecret,
                        accessToken);
                log.info("SPProvisionAPI: AM database values inserted successfully");
            } else {
                log.error("SPProvisionAPI: Failed to insert values into AM database [Access token is null]");
                return Response.status(500).entity("{error: true, message: 'Access Token generation failed'}").build();
            }

            //scope configuration
            String[] scopeList = scopes.split(",");
            String scopeConfigRet = DataBaseFunction.scopeConfiguration(ConnectdbConnectionUtil.getConnection(),
                    newConsumerKey, scopeList, operatorName);
            if (log.isDebugEnabled()) {
                log.debug("SPProvisionAPI: Scope configuration response - " + scopeConfigRet);
            }
            if (checkResponseErrors(scopeConfigRet)) {
                log.error("SPProvisionAPI: Scope configuration failed");
                return Response.status(500).entity(scopeConfigRet).build();
            }

            if (trustedServiceProvider) {
                String trustedStatusRet = DataBaseFunction.trustedStatusConfiguration(ConnectdbConnectionUtil.getConnection(),
                        newConsumerKey, operatorName);

                if (checkResponseErrors(trustedStatusRet)) {
                    log.error("SPProvisionAPI: Error occurred while configuring trusted status - " + trustedStatusRet);
                } else {
                    log.info("SPProvisionAPI: Trusted service provider enabled");
                }
            }

            //Update new with keys
            String updateKeys = DataBaseFunction.updateClientAndSecretKeys(generatedConsumerKey, newConsumerKey,
                    generatedConsumerSecret, newConsumerSecret, accessToken);
            if (log.isDebugEnabled()) {
                log.debug("SPProvisionAPI: Client key update response - " + updateKeys);
            }
            if (checkResponseErrors(updateKeys)) {
                log.error("SPProvisionAPI: Client key update failed");
                return Response.status(500).entity(updateKeys).build();
            }

            //Testing updated keys
            String newKeys[] = isApiCalls.getClientSecret(appName);
            if (newConsumerKey.equals(newKeys[0]) && newConsumerSecret.equals(newKeys[1])) {
                log.info("SPProvisionAPI: Client keys updated successfully");
            }

            String oAuthRequestCurl = mobileConnectConfigs.getSpProvisionConfig().getAuthUrl()
                    + "?client_id=" + newConsumerKey + "&response_type=code&scope=openid"
                    + "&redirect_uri=" + callbackUrl + "&acr_values=2&state=state_33945636-d3b7-4b12-b7b6-288e5a9683a7"
                    + "&nonce=nonce_a75674c9-2007-4e36-afee-ccf7c865a25d";

            String tokenRequestCurl = "curl -v -X POST --user " + newConsumerKey + ":" + newConsumerSecret + " "
                    + "-H \"Content-Type: application/x-www-form-urlencoded;charset=UTF-8\" -k -d "
                    + "\"grant_type=authorization_code&code=d3ce9ee75a5de3ca955b1798b39bf2&"
                    + "redirect_uri=" + callbackUrl + "\" " + mobileConnectConfigs.getSpProvisionConfig().getTokenUrl();

            String userInfoCurl = "curl -i " + mobileConnectConfigs.getSpProvisionConfig().getUserInfoUrl() +
                    "?schema=openid -H \"Authorization: Bearer 9d55e77b3f823d84ae5fdff1d7135fcd\"";

            return Response.ok("{error: false, message: 'success', accessToken: '" + accessToken + "'}",
                    MediaType.APPLICATION_JSON).build();

        } catch (JSONException e) {
            log.error("Error occurred while parsing one of API responses", e);
            return Response.status(500).entity("{error: true, message: '" + e.getMessage() + "'}").build();
        } catch (SQLException e) {
            log.error("Database error occurred", e);
            return Response.status(500).entity("{error: true, message: '" + e.getMessage() + "'}").build();
        } catch (IOException e) {
            log.error("IOException occurred: " + e.getMessage(), e);
            return Response.status(500).entity("{error: true, message: '" + e.getMessage() + "'}").build();
        }
    }

    private String subscribeToApi(MobileConnectConfig.Api api, String userName, String appName,
                                  ApiCallsInAM amApiCalls) {
        String apiName = api.getApiName();
        String version = api.getApiVersion();
        String provider = api.getApiprovider();
        String tier = api.getApiTier();
        String respSubscribeToApi = amApiCalls.addSubscritions(userName, appName, apiName,
                version, provider, tier);
        return respSubscribeToApi;
    }

    /**
     * Checks AM API responses for errors
     * @param response Response string in JSON format ex: {'error': true, 'message': 'error message'}
     * @return false if there were no errors, true otherwise
     * @throws JSONException if parsing failed
     */
    private boolean checkResponseErrors(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        if (jsonObject.has("error")) {
            return jsonObject.getBoolean("error");
        }
        return false;
    }

    private String removeHttpCode(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        if (jsonObject.has("output")) {
            return jsonObject.getJSONObject("output").toString();
        }
        return response;
    }

    /**
     * Parses json string into JSONObject
     * @param jsonStr String to be parsed into json
     * @return JSONObject
     * @throws JSONException if parsing failed
     */
    private JSONObject stringToJsonObject(String jsonStr) throws JSONException {
        return new JSONObject(jsonStr);
    }


}
