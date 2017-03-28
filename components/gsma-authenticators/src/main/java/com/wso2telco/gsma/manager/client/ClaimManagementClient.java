/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wso2telco.gsma.manager.client;


import com.wso2telco.core.config.DataHolder;
import com.wso2telco.gsma.authenticators.internal.CustomAuthenticatorServiceComponent;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.um.ws.api.stub.SetUserClaimValues;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.rmi.RemoteException;
//import org.wso2.carbon.um.ws.api.stub.getC

/**
 * @author tharanga_07219
 */
public class ClaimManagementClient {
    private final String serviceName = "RemoteUserStoreManagerService";
    private SetUserClaimValues setUserClaim;
    private String endPoint;
    private RemoteUserStoreManagerServiceStub remoteUser;

    private static final Log log = LogFactory.getLog(ClaimManagementClient.class);

    public ClaimManagementClient(String backEndUrl, String sessionCookie)
            throws AxisFault {
        this.endPoint = backEndUrl + "/services/" + serviceName;
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
        remoteUser._getServiceClient().setOptions(option);
    }

    public void setClaim() throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {
        if (log.isDebugEnabled()) {
            log.debug("Remote User "
                    + remoteUser.getProfileNames(DataHolder.getInstance().getMobileConnectConfig().getAdminUrl()));
        }
    }

    public String getRegisteredLOA(String msisdn) throws RemoteException,
            RemoteUserStoreManagerServiceUserStoreExceptionException, AuthenticationFailedException,
            UserStoreException {

        int tenantId = -1234;
        UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService()
                .getTenantUserRealm(tenantId);

        if (userRealm != null) {
            UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
            return userStoreManager.getUserClaimValue(msisdn, "http://wso2.org/claims/loa", null);

        } else {
            throw new AuthenticationFailedException("Cannot find the user realm for the given tenant: " + tenantId);
        }
    }
}
