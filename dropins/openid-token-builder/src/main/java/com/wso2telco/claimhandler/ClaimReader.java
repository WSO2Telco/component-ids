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
package com.wso2telco.claimhandler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import java.rmi.RemoteException;


// TODO: Auto-generated Javadoc
/**
 * The Class ClaimReader.
 */
public class ClaimReader {
    
    /** The remote user. */
    private RemoteUserStoreManagerServiceStub remoteUser;

    /**
     * Instantiates a new claim reader.
     *
     * @param backEndUrl the back end url
     * @param sessionCookie the session cookie
     * @throws AxisFault the axis fault
     */
    public ClaimReader(String backEndUrl, String sessionCookie)
            throws AxisFault {
        String endPoint = backEndUrl + "/services/" + "RemoteUserStoreManagerService";
        remoteUser = new RemoteUserStoreManagerServiceStub(endPoint);

        // Authenticate Your stub from sessionCooke
        ServiceClient serviceClient = null;
        Options option;
        serviceClient = remoteUser._getServiceClient();
        option = serviceClient.getOptions();
        option.setManageSession(true);
        option.setProperty(
                org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                sessionCookie);
    }

    /**
     * Gets the claim.
     *
     * @param msisdn the msisdn
     * @param claim the claim
     * @return the claim
     * @throws RemoteException the remote exception
     * @throws RemoteUserStoreManagerServiceUserStoreExceptionException the remote user store manager service user store exception exception
     */
    public String getClaim(String msisdn, String claim)
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {
        return remoteUser.getUserClaimValue(msisdn, claim, "default");
    }
}
