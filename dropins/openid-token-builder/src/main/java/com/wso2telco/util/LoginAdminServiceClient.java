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

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;

// TODO: Auto-generated Javadoc
/**
 * The Class LoginAdminServiceClient.
 */
public class LoginAdminServiceClient {
    
    /** The authentication admin stub. */
    private AuthenticationAdminStub authenticationAdminStub;

    /**
     * Instantiates a new login admin service client.
     *
     * @param backEndUrl the back end url
     * @throws AxisFault the axis fault
     */
    public LoginAdminServiceClient(String backEndUrl) throws AxisFault {
        String endPoint = backEndUrl + "/services/AuthenticationAdmin";
        authenticationAdminStub = new AuthenticationAdminStub(endPoint);
    }

    /**
     * Authenticate.
     *
     * @param userName the user name
     * @param password the password
     * @return the string
     * @throws RemoteException the remote exception
     * @throws LoginAuthenticationExceptionException the login authentication exception exception
     */
    public String authenticate(String userName, String password)
            throws RemoteException, LoginAuthenticationExceptionException {

        String sessionCookie = null;

        if (authenticationAdminStub.login(userName, password, "localhost")) {
            ServiceContext serviceContext = authenticationAdminStub
                    ._getServiceClient().getLastOperationContext()
                    .getServiceContext();
            sessionCookie = (String) serviceContext
                    .getProperty(HTTPConstants.COOKIE_STRING);
        }
        return sessionCookie;
    }

    /**
     * Log out.
     *
     * @throws RemoteException the remote exception
     * @throws LogoutAuthenticationExceptionException the logout authentication exception exception
     */
    public void logOut() throws RemoteException, LogoutAuthenticationExceptionException {
        authenticationAdminStub.logout();
    }
}
