/** *****************************************************************************
 * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
package com.wso2telco.serviceprovider.provision.api;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.serviceprovider.provision.exceptions.SpProvisionServiceException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.log4j.Logger;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceException;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;

import java.rmi.RemoteException;
import java.util.Properties;


public class OauthAdminClient {
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    private static MobileConnectConfig mobileConnectConfigs;

    private String adminServiceHostUrl, userName, password;
    private OAuthAdminServiceStub oAuthAdminServiceStub = null;
    private ServiceClient client = null;
    static final Logger logInstance = Logger.getLogger(OauthAdminClient.class);

    static {
        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
    }

    public OauthAdminClient() {
        String host = mobileConnectConfigs.getSpProvisionConfig().getApiManagerUrl(); //popertiesFromPropertyFile.getProperty("host_preprod_IS");
        userName = mobileConnectConfigs.getSpProvisionConfig().getMigUserName(); //popertiesFromPropertyFile.getProperty("preprod_IS_Username");
        password = mobileConnectConfigs.getSpProvisionConfig().getMigUserPassword(); //popertiesFromPropertyFile.getProperty("preprod_IS_password");
        adminServiceHostUrl = host + "/services/OAuthAdminService";

        createAndAuthenticateStub();
    }

    public void createAndAuthenticateStub() {
        try {
            oAuthAdminServiceStub = new OAuthAdminServiceStub(null, adminServiceHostUrl);
            oAuthAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(2 * 1000 * 60);
            client = oAuthAdminServiceStub._getServiceClient();

        } catch (AxisFault e) {
             logInstance.error("AxisFault exception in creating OAuth Admin Service stub" + e.toString(), e);
        }
    }

    /*
     * Get Service Provider Application Details using consumerKey
     */
    public OAuthConsumerAppDTO getOauthApplicationDataByApplicationName(String appName)
            throws SpProvisionServiceException {

        OAuthConsumerAppDTO apps = null;
        OAuthConsumerAppDTO[] allAppDetails;

        authenticate(client);

        allAppDetails = getAllOAuthApplicationData();
        if (allAppDetails != null) {
            for (OAuthConsumerAppDTO oAuthConsumerAppDTO : allAppDetails) {
                if (oAuthConsumerAppDTO.getApplicationName().equals(appName)) {
                    apps = oAuthConsumerAppDTO;
                    break;
                }
            }
        }

        return apps;
    }

    public void registerOauthApplicationData(OAuthConsumerAppDTO adminServiceDto) throws SpProvisionServiceException {

        authenticate(client);
        try {
            oAuthAdminServiceStub.registerOAuthApplicationData(adminServiceDto);
        } catch (RemoteException e) {
            throw new SpProvisionServiceException(e.getMessage());
        } catch (OAuthAdminServiceException e) {
            throw new SpProvisionServiceException(e.getMessage());
        }
    }

    /*
     * Get all Oauth application details
     */
    private OAuthConsumerAppDTO[] getAllOAuthApplicationData() throws SpProvisionServiceException {

        OAuthConsumerAppDTO[] allAppDetails;
        authenticate(client);

        try {
            allAppDetails = oAuthAdminServiceStub.getAllOAuthApplicationData();
        } catch (RemoteException e) {
            throw new SpProvisionServiceException(e.getMessage());
        } catch (OAuthAdminServiceException e) {
            throw new SpProvisionServiceException(e.getMessage());
        }
        return allAppDetails;
    }

    public void authenticate(ServiceClient client) {
        Options option = client.getOptions();
        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(userName);
        auth.setPassword(password);
        auth.setPreemptiveAuthentication(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
        option.setManageSession(true);
    }
}
