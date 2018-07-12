/** *****************************************************************************
 * Copyright  (c) 2015-2018, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
 ***************************************************************************** */
package com.wso2telco.spprovisionapp.api;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.spprovisionapp.exception.SpProvisionServiceException;
import org.apache.log4j.Logger;
import org.wso2.carbon.identity.application.common.model.xsd.*;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;

public class ApiCallsInIS {

    private static ApplicationManagementClient applicationManagementClient;
    private static OauthAdminClient oauthAdminClient;
    private String oAuthVersion, grantType;
    static final Logger logInstance = Logger.getLogger(ApiCallsInIS.class);
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    private static MobileConnectConfig mobileConnectConfigs;

    static {
        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
    }


    public ApiCallsInIS() {

        applicationManagementClient = new ApplicationManagementClient();
        oauthAdminClient = new OauthAdminClient();
        oAuthVersion = mobileConnectConfigs.getSpProvisionConfig().getOauthVersion();
        grantType = mobileConnectConfigs.getSpProvisionConfig().getGrantTypes();
    }


    @SuppressWarnings("empty-statement")
    public String[] getClientSecret(String appName) {

        String consumerKey = "", secretKey = "";
        try {
            ServiceProvider serviceProvider = applicationManagementClient.getSpApplicationData(appName);

            if (serviceProvider != null) {
                InboundProvisioningConfig inboundProvisioningConfig = serviceProvider.getInboundProvisioningConfig();
                InboundAuthenticationRequestConfig[] x = serviceProvider.getInboundAuthenticationConfig()
                        .getInboundAuthenticationRequestConfigs();

                for (int i = 0; i < x.length; i++) {
                    if (x[i].getInboundAuthType().equals(ApiConstants.IsConstants.AUTH_TYPE_OAUTH2)) {
                        consumerKey = x[i].getInboundAuthKey();
                        Property[] property = x[i].getProperties();
                        secretKey = property[0].getValue();
                        break;
                    }
                }
            }

        } catch (SpProvisionServiceException ex) {
            logInstance.error("SpProvisionServiceException occurred:" + ex.getMessage(), ex);
        }

        String[] clientAndSecret_ = {consumerKey, secretKey};

        return clientAndSecret_;
    }

    /*
     * Create application in IS side
     */
    public String[] createServiceProvider(String appName, String callBackUrl, String appDescription) {
        String[] responseMessage = new String[3];

        try {
            String consumerKey, consumerSecret;
            OAuthConsumerAppDTO oauthConsumerAppDto = new OAuthConsumerAppDTO();
            oauthConsumerAppDto.setApplicationName(appName);
            oauthConsumerAppDto.setCallbackUrl(callBackUrl);
            oauthConsumerAppDto.setGrantTypes(grantType);
            oauthConsumerAppDto.setOAuthVersion(oAuthVersion);

            if (oauthAdminClient.getOauthApplicationDataByApplicationName(appName) == null) {
                oauthAdminClient.registerOauthApplicationData(oauthConsumerAppDto);
            } else {
                responseMessage[0] = "failure.App is already available in OAuth Database";
                return responseMessage;
            }

            oauthConsumerAppDto = oauthAdminClient.getOauthApplicationDataByApplicationName(appName);
            consumerKey = oauthConsumerAppDto.getOauthConsumerKey();
            consumerSecret = oauthConsumerAppDto.getOauthConsumerSecret();

            ServiceProvider serviceProviderApp = new ServiceProvider();
            serviceProviderApp.setApplicationName(appName);
            serviceProviderApp.setDescription(appDescription);

            if (applicationManagementClient.getSpApplicationData(appName) == null) {
                applicationManagementClient.createSpApplication(serviceProviderApp);
            } else {
                responseMessage[0] = "failure.App is already available in SP Database";
                return responseMessage;
            }

            if (applicationManagementClient.getSpApplicationData(appName) != null) {

                serviceProviderApp = applicationManagementClient.getSpApplicationData(appName);
                serviceProviderApp.setSaasApp(false);

                serviceProviderApp = setClaimConfigObject(serviceProviderApp);
                serviceProviderApp = setInboundAuthenticationConfigObject(serviceProviderApp, consumerKey,
                        consumerSecret);
                serviceProviderApp = setInboundProvisioningConfigObject(serviceProviderApp);
                serviceProviderApp = setPermissionsAndRoleConfigObject(serviceProviderApp);
                serviceProviderApp = setLocalAndOutboundAuthenticationConfigObject(serviceProviderApp);

                String updateStatus = applicationManagementClient.updateSpApplication(serviceProviderApp);

                if (updateStatus.equals("Success")) {
                    String[] keys = getClientSecret(appName);

                    responseMessage[0] = "success";
                    responseMessage[1] = keys[0];
                    responseMessage[2] = keys[1];

                } else {
                    String[] keys = getClientSecret(appName);

                    responseMessage[0] = "failure.Error in update SP Application";
                    responseMessage[1] = keys[0];
                    responseMessage[2] = keys[1];
                }
            }

        } catch (SpProvisionServiceException ex) {
            logInstance.error("SpProvisionServiceException occured:" + ex.getMessage(), ex);
            responseMessage[0] = "failure";
        }
        return responseMessage;
    }

    /*
     * Set ServiceProviderDto details to a ClaimConfig object
     */
    private ServiceProvider setClaimConfigObject(ServiceProvider serviceProviderObject) {

        ClaimConfig claimConfig = new ClaimConfig();
        claimConfig.setAlwaysSendMappedLocalSubjectId(false);
        claimConfig.setLocalClaimDialect(true);
        serviceProviderObject.setClaimConfig(claimConfig);
        return serviceProviderObject;
    }

    /*
     * Set ServiceProviderDto details to a InboundAuthenticationRequestConfig object
     */
    private InboundAuthenticationRequestConfig[] setInboundAuthenticationRequestConfigObject(
            ServiceProvider serviceProviderDto, String consumerKey, String consumerSecret) {

        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig1 = new InboundAuthenticationRequestConfig();
        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig2 = new InboundAuthenticationRequestConfig();
        inboundAuthenticationRequestConfig1.setInboundAuthKey(serviceProviderDto.getApplicationName());
        inboundAuthenticationRequestConfig1.setInboundAuthType(ApiConstants.IsConstants.AUTH_TYPE_PASSIVE_STS);

        inboundAuthenticationRequestConfig2.setInboundAuthKey(consumerKey);
        inboundAuthenticationRequestConfig2.setInboundAuthType(ApiConstants.IsConstants.AUTH_TYPE_OAUTH2);
        inboundAuthenticationRequestConfig2.setProperties(setPropertyObject(consumerSecret));
        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig[] = {inboundAuthenticationRequestConfig1,
            inboundAuthenticationRequestConfig2};

        return inboundAuthenticationRequestConfig;
    }

    /*
     * Set ServiceProviderDto details to a InboundAuthenticationConfig object
     */
    private ServiceProvider setInboundAuthenticationConfigObject(ServiceProvider serviceProviderObject,
                                                                 String consumerKey, String consumerSecret) {

        InboundAuthenticationRequestConfig[] inboundAuthenticationRequestConfig
                = setInboundAuthenticationRequestConfigObject(serviceProviderObject, consumerKey, consumerSecret);
        InboundAuthenticationConfig inboundAuthenticationConfig1 = new InboundAuthenticationConfig();
        inboundAuthenticationConfig1.setInboundAuthenticationRequestConfigs(inboundAuthenticationRequestConfig);
        serviceProviderObject.setInboundAuthenticationConfig(inboundAuthenticationConfig1);
        return serviceProviderObject;
    }

    /*
     * Set ServiceProviderDto details to a Property object
     */
    private Property[] setPropertyObject(String consumerSecret) {

        Property property1 = new Property();
        property1.setConfidential(false);
        property1.setDefaultValue(null);
        property1.setName(ApiConstants.IsConstants.OAUTH_CONSUMER_SECRET);
        property1.setValue(consumerSecret);
        property1.setRequired(false);
        Property[] property = {property1};
        return property;
    }

    /*
     * Set ServiceProviderDto details to a PermissionsAndRoleConfig object
     */
    private ServiceProvider setPermissionsAndRoleConfigObject(ServiceProvider serviceProviderObject) {

    	 PermissionsAndRoleConfig permissionsAndRoleConfig1 = new PermissionsAndRoleConfig();
         serviceProviderObject.setPermissionAndRoleConfig(permissionsAndRoleConfig1);
         return serviceProviderObject;
    }

    /*
     *Set ServiceProviderDto details to a LocalAndOutboundAuthenticationConfig object
     */
    private ServiceProvider setLocalAndOutboundAuthenticationConfigObject(ServiceProvider serviceProviderObject) {
        AuthenticationStep[] authenticationStep = setAuthenticationStepObject();

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig1 =
                new LocalAndOutboundAuthenticationConfig();
        localAndOutboundAuthenticationConfig1.setAuthenticationType(ApiConstants.IsConstants.LOA_OUTBOUND_AUTH_TYPE);
        localAndOutboundAuthenticationConfig1.setAuthenticationSteps(authenticationStep);
        serviceProviderObject.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig1);
        return serviceProviderObject;
    }

    /*
     * Set ServiceProviderDto details to a LocalAuthenticatorConfig object
     */
    private LocalAuthenticatorConfig[] setLocalAuthenticatorConfigObject() {
        LocalAuthenticatorConfig localAuthenticatorConfig1 = new LocalAuthenticatorConfig();
        localAuthenticatorConfig1.setDisplayName(ApiConstants.IsConstants.LOA_DISPLAY_NAME);
        localAuthenticatorConfig1.setEnabled(false);
        localAuthenticatorConfig1.setName(ApiConstants.IsConstants.LOA_NAME);
        localAuthenticatorConfig1.setValid(true);
        LocalAuthenticatorConfig localAuthenticatorConfig[] = {localAuthenticatorConfig1};

        return localAuthenticatorConfig;
    }

    /*
     * Set ServiceProviderDto details to a AuthenticationStep object
     */
    private AuthenticationStep[] setAuthenticationStepObject() {
        AuthenticationStep authenticationStep1 = new AuthenticationStep();
        LocalAuthenticatorConfig[] localAuthenticatorConfig = setLocalAuthenticatorConfigObject();
        authenticationStep1.setAttributeStep(true);
        authenticationStep1.setSubjectStep(true);
        authenticationStep1.setLocalAuthenticatorConfigs(localAuthenticatorConfig);
        AuthenticationStep authenticationStep[] = {authenticationStep1};
        return authenticationStep;
    }

    /*
     * Set ServiceProviderDto details to a InboundProvisioningConfig object
     */
    private ServiceProvider setInboundProvisioningConfigObject(ServiceProvider serviceProviderObject) {

        InboundProvisioningConfig inboundProvisioningConfig1 = new InboundProvisioningConfig();
        inboundProvisioningConfig1.setProvisioningEnabled(false);
        inboundProvisioningConfig1.setProvisioningUserStore(ApiConstants.IsConstants.PROVISIONING_USER_STORE);
        serviceProviderObject.setInboundProvisioningConfig(inboundProvisioningConfig1);
        return serviceProviderObject;
    }
}