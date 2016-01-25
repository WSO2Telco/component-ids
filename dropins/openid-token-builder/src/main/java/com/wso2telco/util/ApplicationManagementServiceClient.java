/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
 ******************************************************************************/
package com.wso2telco.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class ApplicationManagementServiceClient.
 */
public class ApplicationManagementServiceClient {

    /** The stub. */
    private IdentityApplicationManagementServiceStub stub;


    /**
     * Instantiates a new application management service client.
     *
     * @param cookie the cookie
     * @param backendServerURL the backend server url
     * @throws AxisFault the axis fault
     */
    public ApplicationManagementServiceClient(String cookie, String backendServerURL)
            throws AxisFault {

        String serviceURL = backendServerURL + "/services/IdentityApplicationManagementService";
        stub = new IdentityApplicationManagementServiceStub(serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * Gets the authenticator list.
     *
     * @return the authenticator list
     */
    public List<String> getAuthenticatorList(){

        LocalAuthenticatorConfig[] localAuthenticatorConfigs = null;
        try {
            localAuthenticatorConfigs = stub.getAllLocalAuthenticators();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            e.printStackTrace();
        }
        List<String> authenticators = new ArrayList<String>();
        if (localAuthenticatorConfigs != null) {
            for(LocalAuthenticatorConfig authenticator : localAuthenticatorConfigs) {
                authenticators.add(authenticator.getName());
            }
        }
        return authenticators;
    }
}
