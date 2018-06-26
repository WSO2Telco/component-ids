package com.wso2telco.serviceprovider.provision.entity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import java.io.IOException;

@Path("/")
public class Endpoints {

    @POST
    @Path("/spprovision/operator/{operatorName}/{environment}")
    public void serviceProviderProvision(@Context HttpServletRequest httpServletRequest, @Context
            HttpServletResponse httpServletResponse, @PathParam("operatorName") String operatorName,
                                         @PathParam("environment") String environment) throws Exception {
        System.out.println(operatorName + ": " + environment);
    }

    /*
     * Create application in AM
     */
    public String createApplication(String appName, String description, String callback, String tier)
            throws IOException {
        return null;
    }

    private void activateApplication() {}

    private void approveApplication() {}

    private void subscribeToApi() {}

    private void updateSubscriptions() {}

    private void updateSubscriptionValidator() {}

    private void generateClientCredentials() {}

    private void configureScopes() {}

    private String getAuthCode() {
        return null;
    }

    private String getAccessToken() {
        return null;
    }

    private String getUserInfo() {
        return null;
    }

    private void updateClientCredentials(String clientKey, String clientSecret) {}


}
