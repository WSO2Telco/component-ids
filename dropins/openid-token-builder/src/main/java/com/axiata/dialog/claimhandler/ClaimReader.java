package com.axiata.dialog.claimhandler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import java.rmi.RemoteException;


public class ClaimReader {
    private RemoteUserStoreManagerServiceStub remoteUser;

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

    public String getClaim(String msisdn, String claim)
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {
        return remoteUser.getUserClaimValue(msisdn, claim, "default");
    }
}
