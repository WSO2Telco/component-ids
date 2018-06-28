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

import com.wso2telco.serviceprovider.provision.exceptions.SpProvisionServiceException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.log4j.Logger;
import org.wso2.carbon.identity.application.common.model.xsd.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;

import java.rmi.RemoteException;
import java.util.Properties;

public class ApplicationManagementClient {

    private IdentityApplicationManagementServiceStub stub = null;
    private ServiceClient client = null;
    private String applicationManagmentHostUrl, userName, password;
    private Properties popertiesFromPropertyFile;
    static final Logger logInstance = Logger.getLogger(ApplicationManagementClient.class);
    public ApplicationManagementClient(String environment) {

        String host;
        //PropertyFileHandler propertyFileHandler = new PropertyFileHandler();

        //try {
            //popertiesFromPropertyFile = propertyFileHandler.popertiesFromPropertyFile();
            if (environment.equalsIgnoreCase("preprod")) {
                host = ""; //popertiesFromPropertyFile.getProperty("host_preprod_IS");
                userName = ""; //popertiesFromPropertyFile.getProperty("preprod_IS_Username");
                password = ""; //popertiesFromPropertyFile.getProperty("preprod_IS_password");

            } else {
                host = ""; //popertiesFromPropertyFile.getProperty("host_prod_IS");
                userName = ""; //popertiesFromPropertyFile.getProperty("prod_IS_Username");
                password = ""; //popertiesFromPropertyFile.getProperty("prod_IS_password");
            }

            applicationManagmentHostUrl = host + "/services/IdentityApplicationManagementService";

        //} catch (IOException ex) {
              //logInstance.error("Error occured in reading Property files" + ex.toString(), ex);
        //}

        createAndAuthenticateStub();
    }

    private void createAndAuthenticateStub() {
        try {
            stub = new IdentityApplicationManagementServiceStub(null, applicationManagmentHostUrl);
            client = stub._getServiceClient();
        } catch (AxisFault axisFault) {
            logInstance.error("axisFault" + axisFault.getMessage());
            axisFault.printStackTrace();
        }
    }

    /*
     * Get Service Provider data by passing Application name
     */
    public ServiceProvider getSpApplicationData(String applicationName) throws SpProvisionServiceException {

        ServiceProvider serviceProvider = null;
        authenticate(client);
        ApplicationBasicInfo[] applicationBasicInfo;

        applicationBasicInfo = getAllApplicationBasicInfo();
        if (applicationBasicInfo != null) {
            for (ApplicationBasicInfo appInfo : applicationBasicInfo) {
                if (appInfo.getApplicationName().equals(applicationName)) {
                    try {
                        serviceProvider = stub.getApplication(applicationName);
                    } catch (RemoteException e) {
                        logInstance.error("RemoteException occurred when getting Sp Application data for application :"
                                + applicationName + e.toString(), e);
                        throw new SpProvisionServiceException(e.getMessage());
                    } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
                        logInstance.error("IdentityApplicationManagementServiceIdentityApplicationManagementException "
                                + "occurred when getting Sp Application data for application :"
                                + applicationName + e.toString(), e);
                        throw new SpProvisionServiceException(e.getMessage());
                    }
                    break;
                }
            }
        }
        return serviceProvider;
    }

    /*
     * Get all Service Providers information
     */
    private ApplicationBasicInfo[] getAllApplicationBasicInfo() throws SpProvisionServiceException {

        ApplicationBasicInfo[] applicationBasicInfo = null;
        authenticate(client);
        try {
            applicationBasicInfo = stub.getAllApplicationBasicInfo();
        } catch (RemoteException e) {
            logInstance.error("RemoteException occurred when getting all Sp Application data" + e.toString(), e);
            throw new SpProvisionServiceException(e.getMessage());
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            logInstance.error("IdentityApplicationManagementServiceIdentityApplicationManagementException occurred when getting all Sp Application data" + e.toString(), e);
            throw new SpProvisionServiceException(e.getMessage());
        }
        return applicationBasicInfo;
    }

    /*
     * Update Service Provider data
     */
    public String updateSpApplication(ServiceProvider serviceProvider) throws SpProvisionServiceException {

        authenticate(client);
        String status = "Failure";

        if (serviceProvider != null) {

            try {
                stub.updateApplication(serviceProvider);
                status = "Success";
            } catch (RemoteException e) {
                logInstance.error("RemoteException occurred when updating Sp Application:" + serviceProvider.getApplicationName() + e.toString(), e);
                status = "Success";
            } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
                logInstance.error("IdentityApplicationManagementServiceIdentityApplicationManagementException occurred when updating Sp Application:" + serviceProvider.getApplicationName() + e.toString(), e);
                throw new SpProvisionServiceException(e.getMessage());
            } catch (Exception e) {
                logInstance.error("Exception occurred when updating Sp Application:" + serviceProvider.getApplicationName() + e.toString(), e);
                throw new SpProvisionServiceException(e.getMessage());
            }
        } else {
             logInstance.error("Service provider details are null");
        }

        return status;
    }

    /*
     * Create Service Provider in IS side
     */
    public void createSpApplication(ServiceProvider serviceProviderDto) throws SpProvisionServiceException {

        authenticate(client);

        if (serviceProviderDto != null) {
            try {
                stub.createApplication(serviceProviderDto);
            } catch (RemoteException e) {
                throw new SpProvisionServiceException(e.getMessage());
            } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
                throw new SpProvisionServiceException(e.getMessage());
            } catch (NullPointerException e) {
                throw new SpProvisionServiceException(e.getMessage());
            } catch (Exception e) {
                throw new SpProvisionServiceException(e.getMessage());
            }
        } else {
            logInstance.error("Service provider details are null");
        }
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
