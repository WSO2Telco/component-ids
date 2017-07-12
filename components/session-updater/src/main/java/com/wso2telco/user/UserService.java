package com.wso2telco.user;

import com.wso2telco.core.config.ConfigLoader;
import com.wso2telco.entity.RegisterUserStatusInfo;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import com.wso2telco.operator.FindOperatorFactory;
import com.wso2telco.sms.SendSMS;
import com.wso2telco.util.Constants;
import com.wso2telco.util.UserState;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UserService {
    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(UserService.class);
    private UserRegistration userRegistration = new UserRegistration();

    public void msisdnStatusUpdate(JSONArray msisdnArr, String operator, List<RegisterUserStatusInfo> userRegistrationStatusList) throws IOException, UserStoreException, RemoteUserStoreManagerServiceUserStoreExceptionException, LoginAuthenticationExceptionException, IdentityException {

        Map<String, String> discoveredOperatorNameMap = ConfigLoader.getInstance().getMobileConnectConfig().getOperatorDiscoveryNameMap();
        //Iterate msisdn list
        for (int i = 0; i < msisdnArr.length(); i++) {
            String msisdn = (String) msisdnArr.get(i);
            UserStatus userStatus = new UserStatus();

            //individual operation status
            RegisterUserStatusInfo statusInfo;

            //set msisdn to dto
            statusInfo = new RegisterUserStatusInfo();
            statusInfo.setMsisdn(msisdn);

            //validate msisdn
            RegisterUserStatusInfo.registerStatus msisdnValidationCode = userRegistration.validateMsisdn(msisdn);
            if (msisdnValidationCode != null) {
                statusInfo.setStatus(msisdnValidationCode);
            } else {

                msisdn = msisdn.substring(5);
                //Assumption - Every mobile number has to register with an operator
                //This is to avoid 2 backend calls to user existance and operator retrieval
                String operatorIfRegistered = null;
                String userStatusValue=null;

                if (userRegistration.isExistingUser(msisdn)) {
                    operatorIfRegistered = userRegistration.getRegisteredOperator(msisdn);
                    userStatusValue=userRegistration.getUserStatus(msisdn);
                }

                if (operatorIfRegistered != null && !operatorIfRegistered.isEmpty()) {
                    if (operatorIfRegistered.equalsIgnoreCase(operator) && !userStatusValue.equalsIgnoreCase(UserState.INACTIVE.name())) {
                        statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.INUSE);
                    } else {
                        FindOperatorFactory findOperatorFactory = new FindOperatorFactory();
                        String discoveredOperator =findOperatorFactory.getRecoveryOption().findOperatorByMsisdn(msisdn);
                        discoveredOperator = discoveredOperatorNameMap.get(discoveredOperator);

                        if (discoveredOperator != null && !discoveredOperator.isEmpty()) {
                            if (operator.equalsIgnoreCase(discoveredOperator)) {
                                userRegistration.updateOperator(operator, msisdn);
                                statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.UPDATED);

                                UserStatus userStatusUpdate = new UserStatus();
                                userStatusUpdate.setMsisdn(msisdn);
                                userStatusUpdate.setOperator(operatorIfRegistered);
                                userStatusUpdate.setStatus(UserState.OFFLINE_OPERATOR_MOVE_OUT.name());
                                DataPublisherUtil.publishNewUserData(userStatusUpdate);
                                userStatus.setStatus(UserState.OFFLINE_OPERATOR_MOVE_IN.name());

                            } else {
                                statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.DENIED);
                                userStatus.setStatus(UserState.OFFLINE_INVALID_OPERATOR.name());
                            }
                        } else {
                            statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.INUSE_OPERATOR_VERIFICATION_FAILED);
                            userStatus.setStatus(UserState.OFFLINE_OPERATOR_VERIFICATION_FAILED.name());

                        }
                    }

                } else {
                    //if new user, create profile
                    if (userRegistration.createUserProfile(msisdn, operator)) {
                        statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.OK);
                        userStatus.setStatus(UserState.OFFLINE_USER_REGISTRATION.name());
                        //send welcome sms
                        SendSMS sendSMS = new SendSMS();
                        sendSMS.sendWelcomeSMS(msisdn, operator);
                    } else {
                        ///if the create user profile operation get failed update the relevant status
                        statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.FAILED);
                        userStatus.setStatus(UserState.OFFLINE_USER_REGISTRATION_FAILED.name());
                    }
                }
            }
            userRegistrationStatusList.add(statusInfo);
            //Publish data
            userStatus.setMsisdn(msisdn);
            userStatus.setOperator(operator);
            DataPublisherUtil.publishNewUserData(userStatus);
        }
    }

    public JSONArray getmsisdnArr(String jsonBody) {
        //Cast the jsonBody to json object
        org.json.JSONObject jsonObj;
        JSONArray msisdnArr = null;
        try {
            jsonObj = new org.json.JSONObject(jsonBody);
            if (log.isDebugEnabled()) {
                log.debug("Json body : " + jsonBody);
            }
            msisdnArr = jsonObj.getJSONArray("msisdn");
        } catch (JSONException e) {
            log.error("Invalid message format", e);
        }
        return msisdnArr;
    }


    public void unRegisterStatusUpdate(JSONArray msisdnArr, String operator, List<RegisterUserStatusInfo> userRegistrationStatusList) throws IOException, UserStoreException, RemoteUserStoreManagerServiceUserStoreExceptionException, LoginAuthenticationExceptionException, IdentityException {

        RemoteUserStoreManagerServiceStub adminStub = userRegistration.getUserStoreManagerStub();

        //Iterate msisdn list
        for (int i = 0; i < msisdnArr.length(); i++) {
            String msisdn = (String) msisdnArr.get(i);
            //individual operation status
            RegisterUserStatusInfo statusInfo = new RegisterUserStatusInfo();
            //set msisdn to dto
            statusInfo.setMsisdn(msisdn);

            UserStatus userStatus = new UserStatus();
            userStatus.setMsisdn(msisdn);
            userStatus.setOperator(operator);
            //validate msisdn
            RegisterUserStatusInfo.registerStatus msisdnValidationCode = userRegistration.validateMsisdn(msisdn);
            if (msisdnValidationCode != null) {
                statusInfo.setStatus(msisdnValidationCode);
                userStatus.setStatus(UserState.OFFLINE_USER_UNREGISTRATION_FAILED_.name() + msisdnValidationCode);
            } else {
                if (msisdn != null && !msisdn.isEmpty()) {
                    msisdn = msisdn.substring(5);
                    //validate msisdn
                    msisdnValidationCode = userRegistration.validateUnregisterUser(msisdn, operator, adminStub);
                    if (msisdnValidationCode != null) {
                        statusInfo.setStatus(msisdnValidationCode);
                        userStatus.setStatus(UserState.OFFLINE_USER_UNREGISTRATION_FAILED_.name() + msisdnValidationCode);
                    } else {
                        adminStub.setUserClaimValue(msisdn,Constants.STATUS_CLAIM_NAME, UserState.INACTIVE.name(), Constants.DEFAULT_PROFILE);
                        statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.OK);
                        userStatus.setStatus(UserState.OFFLINE_USER_UNREGISTRATION_SUCCESS.name());
                    }
                } else {
                    statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.MSISDN_EMPTY);
                    userStatus.setStatus(UserState.OFFLINE_USER_UNREGISTRATION_FAILED_.name() + statusInfo.getStatus());
                }
            }
            userRegistrationStatusList.add(statusInfo);
            //Publish data
            DataPublisherUtil.publishNewUserData(userStatus);
        }
    }
}
