/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wso2telco.gsma.manager.client;


import com.wso2telco.core.config.DataHolder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.um.ws.api.stub.SetUserClaimValues;

import java.rmi.RemoteException;
//import org.wso2.carbon.um.ws.api.stub.getC

/**
 *
 * @author tharanga_07219
 */
public class ClaimManagementClient {
    private final String serviceName = "RemoteUserStoreManagerService";
    private SetUserClaimValues setUserClaim;
    private String endPoint;
    private RemoteUserStoreManagerServiceStub remoteUser;
    
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
    
    public void setClaim() throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException{
        System.out.println("Remote User "
                + remoteUser.getProfileNames(DataHolder.getInstance().getMobileConnectConfig().getAdminUrl()));
    }

    public String getRegisteredLOA(String msisdn) throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException{

        return remoteUser.getUserClaimValue(msisdn, "http://wso2.org/claims/loa", "default");
    }

    
}
