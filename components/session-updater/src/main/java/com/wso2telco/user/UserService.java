package com.wso2telco.user;

import com.wso2telco.core.config.ConfigLoader;
import com.wso2telco.entity.RegisterUserStatusInfo;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.operator.FindOperatorFactory;
import com.wso2telco.sms.SendSMS;
import com.wso2telco.util.UserState;
import org.json.JSONArray;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public class UserService {

    public void msisdnStatusUpdate(JSONArray msisdnArr,String operator, List<RegisterUserStatusInfo> userRegistrationStatusList) throws IOException, UserStoreException, RemoteUserStoreManagerServiceUserStoreExceptionException, LoginAuthenticationExceptionException, IdentityException {

        Map<String, String> discoveredOperatorNameMap = ConfigLoader.getInstance().getMobileConnectConfig().getOperatorDiscoveryNameMap();
        UserRegistration userRegistration = new UserRegistration();
        //Iterate msisdn list
        for (int i = 0; i < msisdnArr.length(); i++) {
            String msisdn = (String) msisdnArr.get(i);

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

                if (userRegistration.isExistingUser(msisdn)) {
                    operatorIfRegistered = userRegistration.getRegisteredOperator(msisdn);
                }

                if (operatorIfRegistered != null && !operatorIfRegistered.isEmpty()) {
                    if (operatorIfRegistered.equalsIgnoreCase(operator)) {
                        statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.INUSE);
                    } else {
                        FindOperatorFactory findOperatorFactory = new FindOperatorFactory();
                        String discoveredOperator = findOperatorFactory.getRecoveryOption().findOperatorByMsisdn(msisdn);
                        discoveredOperator = discoveredOperatorNameMap.get(discoveredOperator);

                        if (discoveredOperator != null && !discoveredOperator.isEmpty()) {
                            if (operator.equalsIgnoreCase(discoveredOperator)) {
                                userRegistration.updateOperator(operator, msisdn);
                                statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.UPDATED);
                            } else {
                                statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.DENIED);
                            }
                        } else {
                            statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.INUSE_OPERATOR_VERIFICATION_FAILED);
                        }

                    }

                } else {
                    //if new user, create profile
                    if (userRegistration.createUserProfile(msisdn, operator)) {
                        statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.OK);

                        //Publish data
                        UserStatus userStatus = new UserStatus();
                        userStatus.setMsisdn(msisdn);
                        userStatus.setOperator(operator);
                        userStatus.setStatus(UserState.OFFLINE_USER_REGISTRATION.name());
                        // Utility.publishNewUserData(userStatus);

                        //send welcome sms
                        SendSMS sendSMS = new SendSMS();
                        sendSMS.sendWelcomeSMS(msisdn, operator);
                    } else {
                        ///if the create user profile operation get failed update the relevant status
                        statusInfo.setStatus(RegisterUserStatusInfo.registerStatus.FAILED);
                    }
                }
            }
            userRegistrationStatusList.add(statusInfo);
        }
    }
}
