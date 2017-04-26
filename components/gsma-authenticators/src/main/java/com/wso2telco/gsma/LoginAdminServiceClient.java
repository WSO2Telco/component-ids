package com.wso2telco.gsma;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;

import java.rmi.RemoteException;

/**
 * This class is used as a client for AuthenticationAdminStub
 */
public class LoginAdminServiceClient {

    private static final String serviceName = "AuthenticationAdmin";
    private AuthenticationAdminStub authenticationAdminStub;
    private String endPoint;

    /**
     * Creates a new LoginAdminServiceClient object and initialising the AuthenticationAdminStub
     *
     * @param backEndUrl https server url
     * @throws AxisFault Throws this when AuthenticationAdminStub failed to initialize
     */
    public LoginAdminServiceClient(String backEndUrl) throws AxisFault {
        this.endPoint = backEndUrl + "/services/" + serviceName;
        authenticationAdminStub = new AuthenticationAdminStub(endPoint);
    }

    /**
     * This method is use to get authentication for accses admin services
     *
     * @param password password
     * @param userName username
     * @return return a session
     * @throws RemoteException                       Throws this when failed connect with the AuthenticationAdminService
     * @throws LoginAuthenticationExceptionException Throws this when failed to authenticate with given username and
     *                                               password
     */
    public String authenticate(String userName, String password) throws RemoteException,
            LoginAuthenticationExceptionException {
        String sessionCookie = null;
        if (authenticationAdminStub.login(userName, password, "localhost")) {
            ServiceContext serviceContext = authenticationAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        }
        return sessionCookie;
    }

    public void logOut() throws RemoteException, LogoutAuthenticationExceptionException {
        authenticationAdminStub.logout();
    }
}