/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.um.ws.api.stub.SetUserClaimValues;

import java.rmi.RemoteException;

// TODO: Auto-generated Javadoc
//import org.wso2.


/**
 * The Class LoginAdminServiceClient.
 */
public class LoginAdminServiceClient {

    private static Log log = LogFactory.getLog(LoginAdminServiceClient.class);


    /**
     * The service name.
     */
    private final String serviceName = "AuthenticationAdmin";

    /**
     * The authentication admin stub.
     */
    private AuthenticationAdminStub authenticationAdminStub;

    /**
     * The end point.
     */
    private String endPoint;

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /**
     * Instantiates a new login admin service client.
     *
     * @param backEndUrl the back end url
     * @throws AxisFault the axis fault
     */
    public LoginAdminServiceClient(String backEndUrl) throws AxisFault {
        //String path = "D:/currLife/is/wso2is-5.0.0/repository/resources/security/"
        //         + "wso2carbon.jks";

        //  System.setProperty("javax.net.ssl.trustStore", path);
        //  System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        this.endPoint = backEndUrl + "/services/" + serviceName;
        authenticationAdminStub = new AuthenticationAdminStub(endPoint);

    }

    /**
     * Authenticate.
     *
     * @param userName the user name
     * @param password the password
     * @return the string
     * @throws RemoteException                       the remote exception
     * @throws LoginAuthenticationExceptionException the login authentication exception exception
     */
    public String authenticate(String userName, String password)
            throws RemoteException, LoginAuthenticationExceptionException {

        String sessionCookie = null;

        if (authenticationAdminStub.login(userName, password, "localhost")) {
            if (log.isDebugEnabled()) {
                log.debug("Login Successful");
            }
            ServiceContext serviceContext = authenticationAdminStub
                    ._getServiceClient().getLastOperationContext()
                    .getServiceContext();
            sessionCookie = (String) serviceContext
                    .getProperty(HTTPConstants.COOKIE_STRING);
            if (log.isDebugEnabled()) {
                log.debug(sessionCookie);
            }
        }
        return sessionCookie;
    }

    /**
     * Log out.
     *
     * @throws RemoteException                        the remote exception
     * @throws LogoutAuthenticationExceptionException the logout authentication exception exception
     */
    public void logOut() throws RemoteException, LogoutAuthenticationExceptionException {
        authenticationAdminStub.logout();
    }

    /**
     * Login user.
     *
     * @param userName the user name
     * @param password the password
     * @return the string
     * @throws RemoteUserStoreManagerServiceUserStoreExceptionException the remote user store manager service user
     *                                                                  store exception exception
     */
    public String LoginUser(String userName, String password) throws
            RemoteUserStoreManagerServiceUserStoreExceptionException {
        String sessionKey = null;

        // load config values
        MobileConnectConfig.SessionUpdaterConfig sessionUpdaterConfig = configurationService.getDataHolder()
                .getMobileConnectConfig().getSessionUpdaterConfig();

        //String path = "/home/gayan/Documents/Dev/GSMA/IS_OpenId/testSetup1908/wso2is-5.0.0/repository/resources
        // /security/"
        //        + "wso2carbon.jks";

        try {
            LoginAdminServiceClient lAdmin = new LoginAdminServiceClient(sessionUpdaterConfig.getAdmin_url());
            String sessionCookie = lAdmin.authenticate(sessionUpdaterConfig.getAdminusername(), sessionUpdaterConfig
                    .getAdminpassword());
            ClaimManagementClient claimManager = new ClaimManagementClient(sessionUpdaterConfig.getAdmin_url(),
                    sessionCookie);
            claimManager.setClaim();
        } catch (AxisFault e) {
            log.error(e);
        } catch (RemoteException e) {
            log.error(e);
        } catch (LoginAuthenticationExceptionException e) {
            log.error(e);
        }
        return sessionKey;

    }

    /**
     * Sets the pin.
     *
     * @param pin the new pin
     */
    public void setPIN(String pin) {
        ServiceClient serviceClient;
        Options option;


        SetUserClaimValues claimAdmin = new SetUserClaimValues();

        //String username = claimAdmin.getUserName();
        // Options option
        //claimAdmin.setClaims(param);
    }

}
