package com.axiata.dialog.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.identity.application.common.model.xsd.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationManagementServiceClient {

    private IdentityApplicationManagementServiceStub stub;


    public ApplicationManagementServiceClient(String cookie, String backendServerURL)
            throws AxisFault {

        String serviceURL = backendServerURL + "/services/IdentityApplicationManagementService";
        stub = new IdentityApplicationManagementServiceStub(serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(HTTPConstants.COOKIE_STRING, cookie);
    }

    public List<String> getAuthenticatorList(){

        LocalAuthenticatorConfig[] localAuthenticatorConfigs = null;
        try {
            localAuthenticatorConfigs = stub.getAllLocalAuthenticators();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
            e.printStackTrace();
        }
        List<String> authenticators = new ArrayList<String>();
        if (localAuthenticatorConfigs != null) {
            for(LocalAuthenticatorConfig authenticator : localAuthenticatorConfigs) {
                authenticators.add(authenticator.getName());
            }
        }
        return authenticators;
    }
}
