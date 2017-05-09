package com.wso2telco.ssp.service;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.ssp.exception.ApiException;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.user.core.UserCoreConstants;

import javax.ws.rs.core.Response;
import java.rmi.RemoteException;

/**
 * Calls to Admin Services of IS and retrieve user data
 */
public class AdminService {

    private final String serviceName = "RemoteUserStoreManagerService";

    private final String adminServiceName = "AuthenticationAdmin";

    private RemoteUserStoreManagerServiceStub remoteUserStore;

    private AuthenticationAdminStub authenticationAdminStub;

    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /**
     * Constructs the AdminService object and creates admin and remote user store stubs
     * @throws ApiException
     */
    public AdminService() throws ApiException {

        String endPoint = DataHolder.getInstance().getMobileConnectConfig().getAdminUrl() + "/services/";
        try {
            authenticationAdminStub = new AuthenticationAdminStub(endPoint + adminServiceName);

            String sessionCookie = authenticate(configurationService.getDataHolder().getMobileConnectConfig()
                            .getAdminUsername(),
                    configurationService.getDataHolder().getMobileConnectConfig().getAdminPassword());

            remoteUserStore = new RemoteUserStoreManagerServiceStub(endPoint + serviceName);
            //Authenticate Your stub from sessionCookie
            ServiceClient serviceClient = remoteUserStore._getServiceClient();
            Options option = serviceClient.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
        }catch (Exception e){
            throw new ApiException(e.getMessage(), "admin_service_error", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Authenticates the admin and get the session string
     * @param userName admin username
     * @param password admin password
     * @return Session string
     * @throws RemoteException
     * @throws LoginAuthenticationExceptionException
     */
    private String authenticate(String userName, String password)
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
     * Sets a claim to user's default profile
     * @param userName user store username
     * @param claimURI claim URI
     * @param claimValue new claim value
     * @throws ApiException
     */
    protected void setUserClaim(String userName, String claimURI, String claimValue) throws ApiException {
        try{
            remoteUserStore.setUserClaimValue(userName, claimURI, claimValue, UserCoreConstants.DEFAULT_PROFILE);
        }catch (Exception e){
            throw new ApiException(e.getMessage(), "admin_service_error", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets user claim value from default profile
     * @param username user store username
     * @param claimURI claim URI
     * @return claim value
     * @throws ApiException
     */
    protected String getUserClaim(String username, String claimURI) throws ApiException {
        try {
            return remoteUserStore.getUserClaimValue(username, claimURI, UserCoreConstants.DEFAULT_PROFILE);
        }catch (Exception e){
            throw new ApiException(e.getMessage(), "admin_service_error", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get PIN of a user
     * @param username user store username
     * @return PIN
     * @throws ApiException
     */
    public String getPin(String username) throws ApiException {
        return getUserClaim(username, "http://wso2.org/claims/pin");
    }

    /**
     * Get LOA of a user
     * @param username user store username
     * @return LOA
     * @throws ApiException
     */
    public String getLoa(String username) throws ApiException {
        return getUserClaim(username, "http://wso2.org/claims/loa");
    }

    /**
     * Sets PIN of a user
     * @param username user store username
     * @param pin new PIN
     * @throws ApiException
     */
    public void setPin(String username, String pin) throws ApiException {
        setUserClaim(username, "http://wso2.org/claims/pin", pin);
    }
}