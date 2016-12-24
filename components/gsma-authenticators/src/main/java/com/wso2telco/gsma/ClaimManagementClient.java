package com.wso2telco.gsma;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;

import java.rmi.RemoteException;

public class ClaimManagementClient {
    private final String serviceName = "RemoteUserStoreManagerService";
    private String endPoint;
    private RemoteUserStoreManagerServiceStub remoteUser;

    public ClaimManagementClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + "/services/" + serviceName;
        remoteUser = new RemoteUserStoreManagerServiceStub(endPoint);

        ServiceClient serviceClient = null;
        Options option;

        serviceClient = remoteUser._getServiceClient();

        option = serviceClient.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
    }

    public String getRegisteredLOA(String msisdn)
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {
        return remoteUser.getUserClaimValue(msisdn, "http://wso2.org/claims/loa", "default");
    }

    public boolean isUserExist(String msisdn) throws RemoteException,
                                                     RemoteUserStoreManagerServiceUserStoreExceptionException {
        return remoteUser.isExistingUser(msisdn);
    }
}

