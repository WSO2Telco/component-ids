package com.wso2telco.serviceprovider.provision.entity;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.serviceprovider.provision.api.AmApiCalls;
import com.wso2telco.serviceprovider.provision.api.IsApiCalls;
import com.wso2telco.serviceprovider.provision.util.DbUtils;
import com.wso2telco.serviceprovider.provision.util.conn.ApimgtConnectionUtil;
import com.wso2telco.serviceprovider.provision.util.conn.ConnectdbConnectionUtil;
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
    @Path("/spprovision/operator/{operatorName}/{environment}")
    public Response serviceProviderProvision(@Context HttpServletRequest httpServletRequest,
                                             @Context HttpServletResponse httpServletResponse,
                                             @PathParam("operatorName") String operatorName,
                                             @PathParam("environment") String environment,
                                             String requestParamInfo) /*throws Exception*/ {

        log.info("Service provision API called with operatorName: " + operatorName + ", Environment: " + environment);

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
            AmApiCalls amApiCalls = new AmApiCalls();
            String respCreateUserCall = removeHttpCode(amApiCalls.createNewUserInAm(appName, firstName, lastName,
                    devMail));
            if (checkResponseErrors(respCreateUserCall)) {
                log.error("SP Provision API: Failed to create new AM user. - " + respCreateUserCall);
                return Response.status(500).entity(respCreateUserCall).build();
            }
            System.out.println("1.1 AM User Created - " + respCreateUserCall);

            //Login to AM
            String respLoginCall = removeHttpCode(amApiCalls.loginToAm(appName));
            if (checkResponseErrors(respLoginCall)) {
                log.error("SP Provision API: AM user login failed. - " + respLoginCall);
                return Response.status(500).entity(respLoginCall).build();
            }
            System.out.println("1.2 Logged into AM - " + respLoginCall);

            //Create application in AM
            String respCreateApp = removeHttpCode(amApiCalls.createApplication(appName,
                    appDescription, callbackUrl, applicationTier));
            if (checkResponseErrors(respCreateApp)) {
                log.error("SP Provision API: Failed to create AM application. - " + respCreateApp);
                return Response.status(500).entity(respCreateApp).build();
            }
            System.out.println("2. App created - " + respCreateApp);

            //Activate AM application
            String respActivateApp = DbUtils.activateApplication(ApimgtConnectionUtil.getConnection(), appName);
            if (checkResponseErrors(respActivateApp)) {
                log.error("SP Provision API: Failed to activate AM application. - " + respActivateApp);
                return Response.status(500).entity(respActivateApp).build();
            }
            System.out.println("3. App activated - " + respActivateApp);

            //Update AM application
            String respApproveApp = DbUtils.updateApplication(ApimgtConnectionUtil.getConnection(), appName);
            if (checkResponseErrors(respApproveApp)) {
                log.error("SP Provision API: Failed update AM application. - " + respApproveApp);
                return Response.status(500).entity(respApproveApp).build();
            }
            System.out.println("4. App updated - " + respApproveApp);

            //Subscribing to APIs
            List<MobileConnectConfig.Api> apiList = mobileConnectConfigs.getSpProvisionConfig()
                    .getApiConfigs().getApiList();
            String[] requestedApis = apis.split(",");
            for (MobileConnectConfig.Api api: apiList) {
                for (String requestedApi: requestedApis) {
                    if (api.getApiName().equalsIgnoreCase(requestedApi)) {
                        String respSubscribeCall = removeHttpCode(subscribeToApi(api, userName, appName, amApiCalls));
                        if (checkResponseErrors(respSubscribeCall)) {
                            log.error("SP Provision API: Failed to create API subscription " +
                                    "[" + requestedApi + "] - " + respSubscribeCall);
                            return Response.status(500).entity(respSubscribeCall).build();
                        }
                        System.out.println("5. Subscribed ["+ api +"]- " + respSubscribeCall);
                    }
                }
            }

            //Update subscriptions
            String respUpdateSubscriptions = DbUtils.updateSubscriptions(ApimgtConnectionUtil.getConnection());
            if (checkResponseErrors(respUpdateSubscriptions)) {
                log.error("SP Provision API: Failed to update subscriptions. - " + respUpdateSubscriptions);
                return Response.status(500).entity(respUpdateSubscriptions).build();
            }
            System.out.println("6. Subscriptions updated - " + respUpdateSubscriptions);

            //Populate subscription validator
            String respPopulateSubscriptionValidator = DbUtils.populateSubscriptionValidator(
                    ApimgtConnectionUtil.getConnection());
            if (checkResponseErrors(respPopulateSubscriptionValidator)) {
                log.error("SP Provision API: Failed to populate subscription validator. - " + respPopulateSubscriptionValidator);
                return Response.status(500).entity(respPopulateSubscriptionValidator).build();
            }
            System.out.println("6. Subscriptions validator populated - " + respPopulateSubscriptionValidator);

            System.out.println("IS Calls Start");

            //IS Calls
            IsApiCalls isApiCalls = new IsApiCalls();
            String[] status = isApiCalls.createServiceProvider(appName, callbackUrl, appDescription);
            System.out.println(status.length);
            for (String s: status) {
                System.out.println(s);
            }

            //Access token Generation
            String accessToken = null;
            if (status[0].equals("success")) {
                accessToken = amApiCalls.getAccessToken(status[1], status[2]);
            }
            System.out.println("Access Token: " + accessToken);

            //insert values to AM databases
            if (null != accessToken) {
                DbUtils.insertValuesToAmDatabases(appName, status[1], status[2], accessToken);
            } else {
                return Response.status(500).entity("{error: true, message: 'Access Token generation failed'}").build();
            }

            //scope configuration
            String[] scopeList = scopes.split(",");
            String scopeConfigRet = DbUtils.scopeConfiguration(ConnectdbConnectionUtil.getConnection(),
                    newConsumerKey, scopeList);

            if (trustedServiceProvider) {
                scopeConfigRet = DbUtils.trustedStatusCofiguration(ConnectdbConnectionUtil.getConnection(),
                        newConsumerKey);
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


        } catch (JSONException e) {
            e.printStackTrace();
            return Response.status(500).entity("{error: true, message: '" + e.getMessage() + "'}").build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(500).entity("{error: true, message: '" + e.getMessage() + "'}").build();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(500).entity("{error: true, message: '" + e.getMessage() + "'}").build();
        }

        return Response.ok("{error: false, message: 'success'}", MediaType.APPLICATION_JSON).build();
    }

    private String subscribeToApi(MobileConnectConfig.Api api, String userName, String appName, AmApiCalls amApiCalls) {
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
        System.out.println("Removing httpCode from: " + response);
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
