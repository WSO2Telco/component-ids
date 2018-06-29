package com.wso2telco.serviceprovider.provision.entity;

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
import java.sql.SQLException;

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


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spprovision/operator/{operatorName}/{environment}")
    public Response serviceProviderProvision(@Context HttpServletRequest httpServletRequest,
                                             @Context HttpServletResponse httpServletResponse,
                                             @PathParam("operatorName") String operatorName,
                                             @PathParam("environment") String environment,
                                             String requestParamInfo) throws Exception {

        System.out.println("Service provision API called with operatorName: " + operatorName + ", Environment: " + environment);

        try {
            JSONObject requestBody = stringToJsonObject(requestParamInfo);
            String appName = requestBody.getString("applicationName");
            String appDescription = appName; //add this as a request body parameter
            String firstName = requestBody.getString("firstName");
            String lastName = requestBody.getString("lastName");
            String devMail = requestBody.getString("developerEmail");
            String applicationTier = requestBody.getString("applicationTier");
            String newConsumerKey = requestBody.getString("newConsumerKey");
            String newConsumerSecret = requestBody.getString("newConsumerSecret");
            String callbackUrl = requestBody.getString("callbackUrl");
            String scopes = requestBody.getString("scopes");
            boolean trustedServiceProvider = requestBody.getBoolean("trustedServiceProvider");

            AmApiCalls amApiCalls = new AmApiCalls();
            String respCreateUserCall = amApiCalls.createNewUserInAm(appName, firstName, lastName, devMail);
            if (checkAmResponseErrors(respCreateUserCall)) {
                return Response.status(500).entity(respCreateUserCall).build();
            }

            String respLoginCall = amApiCalls.loginToAm(appName);
            if (checkAmResponseErrors(respLoginCall)) {
                return Response.status(500).entity(respLoginCall).build();
            }

            String respCreateApp = amApiCalls.createApplication(appName,
                    appDescription, callbackUrl, applicationTier);
            if (checkAmResponseErrors(respCreateApp)) {
                return Response.status(500).entity(respCreateApp).build();
            }

            String respActivateApp = DbUtils.activateApplication(ApimgtConnectionUtil.getConnection(), appName);
            if (checkAmResponseErrors(respActivateApp)) {
                return Response.status(500).entity(respActivateApp).build();
            }

            String respApproveApp = DbUtils.updateApplication(ApimgtConnectionUtil.getConnection(), appName);
            System.out.println(respApproveApp);
            if (checkAmResponseErrors(respApproveApp)) {
                return Response.status(500).entity(respApproveApp).build();
            }

            //to-do: subscribe to apis, call here

            String respUpdateSubscriptions = DbUtils.updateSubscriptions(ApimgtConnectionUtil.getConnection());
            if (checkAmResponseErrors(respUpdateSubscriptions)) {
                return Response.status(500).entity(respUpdateSubscriptions).build();
            }


            String respPopulateSubscriptionValidator = DbUtils.populateSubscriptionValidator(
                    ApimgtConnectionUtil.getConnection());
            if (checkAmResponseErrors(respPopulateSubscriptionValidator)) {
                return Response.status(500).entity(respPopulateSubscriptionValidator).build();
            }

            //IS Calls Start from Here
            IsApiCalls isApiCalls = new IsApiCalls();
            String[] status = isApiCalls.createServiceProvider(appName, callbackUrl, appDescription);

            //Access token Generation
            String accessToken = null;
            if (status[0].equals("success")) {
                accessToken = amApiCalls.getAccessToken(status[1], status[2]);
                System.out.println("Access token:" + accessToken);
            }
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

//            String oAuthRequestCurl = auth_url
//                    + "?client_id=" + consumerKeyValue + "&response_type=code&scope=openid"
//                    + "&redirect_uri=" + callbackUrl + "&acr_values=2&state=state_33945636-d3b7-4b12-b7b6-288e5a9683a7"
//                    + "&nonce=nonce_a75674c9-2007-4e36-afee-ccf7c865a25d";
//
//            String tokenRequestCurl = "curl -v -X POST --user " + consumerKeyValue + ":" + consumerSecretValue + " "
//                    + "-H \"Content-Type: application/x-www-form-urlencoded;charset=UTF-8\" -k -d "
//                    + "\"grant_type=authorization_code&code=d3ce9ee75a5de3ca955b1798b39bf2&"
//                    + "redirect_uri=" + callbackUrl + "\" " + token_url;
//
//            String userInfoCurl = "curl -i " + user_info_url + "?schema=openid -H \"Authorization: Bearer 9d55e77b3f823d84ae5fdff1d7135fcd\"";


        } catch (JSONException je) {
            je.printStackTrace();
            return Response.status(500).entity("{error: true, message: '" + je.getMessage() + "'}").build();
        } catch (SQLException se) {
            se.printStackTrace();
            return Response.status(500).entity("{error: true, message: '" + se.getMessage() + "'}").build();
        }

        return Response.ok("{error: false, message: 'success'}", MediaType.APPLICATION_JSON).build();
    }

    /**
     * Checks AM API responses for errors
     * @param response AM API call response
     * @return false if there were no errors, true otherwise
     * @throws JSONException if parsing failed
     */
    private boolean checkAmResponseErrors(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);
        JSONObject outputObject = jsonObject.getJSONObject("output");
        return outputObject.getBoolean("error");
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
