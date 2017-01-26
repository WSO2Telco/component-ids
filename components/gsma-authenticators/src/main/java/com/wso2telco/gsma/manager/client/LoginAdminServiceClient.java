package com.wso2telco.gsma.manager.client;

import com.wso2telco.core.config.DataHolder;
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

//import org.wso2.


public class LoginAdminServiceClient {
	private final String serviceName = "AuthenticationAdmin";
    private AuthenticationAdminStub authenticationAdminStub;
    private String endPoint;

    private static final Log log = LogFactory.getLog(LoginAdminServiceClient.class);

    public LoginAdminServiceClient(String backEndUrl) throws AxisFault {
        //String path = "D:/currLife/is/wso2is-5.0.0/repository/resources/security/"
       //         + "wso2carbon.jks";
        
      //  System.setProperty("javax.net.ssl.trustStore", path);
      //  System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        
        this.endPoint = backEndUrl + "/services/" + serviceName;
        authenticationAdminStub = new AuthenticationAdminStub(endPoint);
        
    }

    public String authenticate(String userName, String password)
            throws RemoteException, LoginAuthenticationExceptionException {

        String sessionCookie = null;

        if (authenticationAdminStub.login(userName, password, "localhost")) {
            log.info("Login Successful");

            ServiceContext serviceContext = authenticationAdminStub
                    ._getServiceClient().getLastOperationContext()
                    .getServiceContext();
            sessionCookie = (String) serviceContext
                    .getProperty(HTTPConstants.COOKIE_STRING);
            if(log.isDebugEnabled()) {
                log.debug(sessionCookie);
            }
        }

        return sessionCookie;
    }

    public void logOut() throws RemoteException,
            LogoutAuthenticationExceptionException {
        authenticationAdminStub.logout();
    }
    
    public String LoginUser(String userName,String password) throws RemoteUserStoreManagerServiceUserStoreExceptionException{
        String sessionKey = null;
        
        //String path = "/home/gayan/Documents/Dev/GSMA/IS_OpenId/testSetup1908/wso2is-5.0.0/repository/resources/security/"
        //        + "wso2carbon.jks";
        
        try {
				String adminURL = DataHolder.getInstance().getMobileConnectConfig().getAdminUrl();
                LoginAdminServiceClient lAdmin = new LoginAdminServiceClient(adminURL);
                String sessionCookie = lAdmin.authenticate(DataHolder.getInstance().getMobileConnectConfig().getAdminUsername(),
                        DataHolder.getInstance().getMobileConnectConfig().getAdminPassword());
                ClaimManagementClient claimManager = new ClaimManagementClient(adminURL,sessionCookie);

        } catch (AxisFault e) {
                log.error(e);
        } catch (RemoteException e) {
                log.error(e);
        } catch (LoginAuthenticationExceptionException e) {
                log.error(e);
        } 
        return sessionKey;
        
    }
    
    public void setPIN(String pin){
        ServiceClient serviceClient;
        Options option;
        
        
        SetUserClaimValues claimAdmin = new SetUserClaimValues();
        
        //String username = claimAdmin.getUserName();
        
       // Options option
        //claimAdmin.setClaims(param);
    }

}
