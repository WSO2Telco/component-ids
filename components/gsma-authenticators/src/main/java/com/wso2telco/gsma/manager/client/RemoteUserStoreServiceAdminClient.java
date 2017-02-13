package com.wso2telco.gsma.manager.client;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.gsma.authenticators.Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.rmi.RemoteException;

public class RemoteUserStoreServiceAdminClient {
    private final String serviceName = "RemoteUserStoreManagerService";
    private RemoteUserStoreManagerServiceStub remoteUserStoreManagerServiceStub;
    private String endPoint;
    private static ClaimManagementClient claimManagementClient;

    public RemoteUserStoreServiceAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
        this.endPoint = backEndUrl + "/services/" + serviceName;
        remoteUserStoreManagerServiceStub = new RemoteUserStoreManagerServiceStub(endPoint);
        //Authenticate Your stub from sessionCookie
        ServiceClient serviceClient;
        Options option;

        serviceClient = remoteUserStoreManagerServiceStub._getServiceClient();
        option = serviceClient.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
    }

    public void setUserClaim(String userName, String claimURI, String claimValue, String profileName) throws
            RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {

        remoteUserStoreManagerServiceStub.setUserClaimValue(userName, claimURI, claimValue, profileName);
    }

    public void setUserClaims(String userName, ClaimValue[] claimValues, String profileName) throws
            RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {
        remoteUserStoreManagerServiceStub.setUserClaimValues(userName, claimValues, profileName);
    }

    public boolean isExistingUser(String username) throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        return remoteUserStoreManagerServiceStub.isExistingUser(username);
    }

    public String getCurrentPin(String username) throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        return remoteUserStoreManagerServiceStub.getUserClaimValue(username, Constants.PIN_CLAIM, UserCoreConstants.DEFAULT_PROFILE);
    }

    public String getCurrentLoa(String username) throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        return remoteUserStoreManagerServiceStub.getUserClaimValue(username, Constants.LOA_CLAIM, UserCoreConstants.DEFAULT_PROFILE);
    }

    public String getChallengeQuestionAndAnswer1(String username) throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        return remoteUserStoreManagerServiceStub.getUserClaimValue(username, Constants.CHALLENGE_QUESTION_1_CLAIM,
                UserCoreConstants.DEFAULT_PROFILE);
    }
    
    public String getChallengeQuestionAndAnswer2(String username) throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {


        return remoteUserStoreManagerServiceStub.getUserClaimValue(username, Constants.CHALLENGE_QUESTION_2_CLAIM,
                UserCoreConstants.DEFAULT_PROFILE);
    }
    
    public ClaimValue[]  getChallengeQuestionAndAnswers(String username) throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        
    	String[] claims = {Constants.CHALLENGE_QUESTION_1_CLAIM, Constants.CHALLENGE_QUESTION_2_CLAIM};       
        return remoteUserStoreManagerServiceStub.getUserClaimValuesForClaims(username, claims, UserCoreConstants.DEFAULT_PROFILE);
    }

}