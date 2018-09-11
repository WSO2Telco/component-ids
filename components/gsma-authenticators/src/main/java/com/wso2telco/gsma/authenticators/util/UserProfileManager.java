/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.gsma.authenticators.util;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.internal.AuthenticatorEnum;
import com.wso2telco.gsma.manager.client.LoginAdminServiceClient;
import com.wso2telco.gsma.manager.client.RemoteUserStoreServiceAdminClient;
import com.wso2telco.gsma.manager.client.UserRegistrationAdminServiceClient;
import com.wso2telco.gsma.manager.util.UserProfileClaimsConstant;

import com.wso2telco.ids.datapublisher.util.DBUtil;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;
import org.wso2.carbon.identity.user.registration.stub.dto.UserDTO;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class UserProfileManager {

    private static final String CLAIM = "http://wso2.org/claims";

    private static final String OPERATOR_CLAIM_NAME = "http://wso2.org/claims/operator";

    private static final String LOA_CLAIM_NAME = "http://wso2.org/claims/loa";

    private static final String SCOPE_OPENID = "openid";

    private static final String MOBILE_CLAIM_NAME = "http://wso2.org/claims/mobile";

    private static final String STATUS_CLAIM_NAME = "http://wso2.org/claims/status";

    private static final String STATUS_ACTIVE = "ACTIVE";

    private static final String STATUS_PARTIALLY_ACTIVE = "PARTIALLY_ACTIVE";

    private static final String STATUS_INACTIVE = "INACTIVE";

    private static Log log = LogFactory.getLog(UserProfileManager.class);

    private static RemoteUserStoreServiceAdminClient remoteUserStoreServiceAdminClient;

    public UserProfileManager() {
        init();
    }

    public boolean createUserProfileLoa2(String username, String operator, boolean isAttributeScope, String spType,
                                         String attrbShareType) throws
            UserRegistrationAdminServiceIdentityException, RemoteException {
        boolean isNewUser = false;
        try {
            if (AdminServiceUtil.isUserExists(username)) {
                try {

                    updateUserStatus(username, isAttributeScope, spType, attrbShareType);
                } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
                    log.error("RemoteUserStoreManagerServiceUserStoreExceptionException : " + e.getMessage());
                }
            } else {

                String adminURL = DataHolder.getInstance().getMobileConnectConfig().getAdminUrl() +
                        UserProfileClaimsConstant.SERVICE_URL;
                if (log.isDebugEnabled()) {
                    log.debug(adminURL);
                }
                UserRegistrationAdminServiceClient userRegistrationAdminServiceClient = new
                        UserRegistrationAdminServiceClient(
                        adminURL);

                UserFieldDTO[] userFieldDTOs = new UserFieldDTO[0];
                try {
                    userFieldDTOs = userRegistrationAdminServiceClient
                            .readUserFieldsForUserRegistration(CLAIM);
                } catch (UserRegistrationAdminServiceIdentityException e) {
                    log.error("UserRegistrationAdminServiceIdentityException : " + e.getMessage());
                } catch (RemoteException e) {
                    log.error("RemoteException : " + e.getMessage());
                }

                for (int count = 0; count < userFieldDTOs.length; count++) {

                    if (OPERATOR_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                        userFieldDTOs[count].setFieldValue(operator);
                    } else if (LOA_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {

                        //                if (scope.equals(SCOPE_CPI)) {
                        //                    userFieldDTOs[count].setFieldValue(LOA_CPI_VALUE);
                        //                } else if (scope.equals(SCOPE_MNV)) {
                        //                    userFieldDTOs[count].setFieldValue(LOA_MNV_VALUE);
                        //                } else {
                        //                    //nop
                        //                }
                        userFieldDTOs[count].setFieldValue(String.valueOf(UserProfileClaimsConstant.LOA2));
                    } else if (MOBILE_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                        userFieldDTOs[count].setFieldValue(username);
                    } else if (STATUS_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                        if (isAttributeScope && spType.equalsIgnoreCase(AuthenticatorEnum.TrustedStatus.UNTRUSTED
                                .name()) && attrbShareType.equalsIgnoreCase(AuthenticatorEnum
                                .AttributeShareScopeTypes.PROVISIONING_SCOPE.getAttributeShareScopeType())) {
                            userFieldDTOs[count].setFieldValue(STATUS_ACTIVE);
                        } else if (isAttributeScope) {
                            userFieldDTOs[count].setFieldValue(STATUS_PARTIALLY_ACTIVE);
                        } else
                            userFieldDTOs[count].setFieldValue(STATUS_ACTIVE);
                    } else {
                        userFieldDTOs[count].setFieldValue("");
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("Value :" + userFieldDTOs[count].getFieldValue() + " : Claim " + userFieldDTOs[count]
                                .getClaimUri() + " : Name " + userFieldDTOs[count].getFieldName());
                    }
                }
                // setting properties of user DTO
                UserDTO userDTO = new UserDTO();
                userDTO.setOpenID(SCOPE_OPENID);
                userDTO.setPassword(DataHolder.getInstance().getMobileConnectConfig().getAdminPassword());
                userDTO.setUserFields(userFieldDTOs);
                userDTO.setUserName(username);

                // add user DTO to the user registration admin service client
                try {
                    userRegistrationAdminServiceClient.addUser(userDTO);

                    log.info("User successfully added [ " + username + " ] ");

                    isNewUser = true;
                } catch (Exception e) {
                    log.error("Error occurred while adding User", e);
                }
            }
        } catch (UserStoreException e) {
            log.error("UserStoreException : " + e.getMessage());
        } catch (AuthenticationFailedException e) {
            log.error("AuthenticationFailedException : " + e.getMessage());
        }
        return isNewUser;
    }

    public boolean createUserProfileLoa3(String username, String operator, String challengeAnswer1,
                                         String challengeAnswer2, String pin, boolean isAttributeScope, String
                                                 spType, String attrbShareType) throws
            UserRegistrationAdminServiceIdentityException, RemoteException {
        boolean isNewUser = false;
        try {
            if (AdminServiceUtil.isUserExists(username)) {
                try {
                    updateUserStatus(username, isAttributeScope, spType, attrbShareType);
                } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
                    log.error("RemoteUserStoreManagerServiceUserStoreExceptionException : " + e.getMessage());
                }
            } else {

                String adminURL = DataHolder.getInstance().getMobileConnectConfig().getAdminUrl() +
                        UserProfileClaimsConstant.SERVICE_URL;
                if (log.isDebugEnabled()) {
                    log.debug(adminURL);
                }
                UserRegistrationAdminServiceClient userRegistrationAdminServiceClient = new
                        UserRegistrationAdminServiceClient(
                        adminURL);

                UserFieldDTO[] userFieldDTOs = new UserFieldDTO[0];
                try {
                    userFieldDTOs = userRegistrationAdminServiceClient
                            .readUserFieldsForUserRegistration(CLAIM);


                    for (int count = 0; count < userFieldDTOs.length; count++) {

                        if (OPERATOR_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                            userFieldDTOs[count].setFieldValue(operator);
                        } else if (LOA_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                            userFieldDTOs[count].setFieldValue(String.valueOf(UserProfileClaimsConstant.LOA3));
                        } else if (MOBILE_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                            userFieldDTOs[count].setFieldValue(username);
                        } else if (UserProfileClaimsConstant.CHALLENGEQUESTION1.equalsIgnoreCase(userFieldDTOs[count]
                                .getClaimUri())) {
                            userFieldDTOs[count].setFieldValue(challengeAnswer1);
                        } else if (UserProfileClaimsConstant.CHALLENGEQUESTION2.equalsIgnoreCase(userFieldDTOs[count]
                                .getClaimUri())) {
                            userFieldDTOs[count].setFieldValue(challengeAnswer2);
                        } else if (UserProfileClaimsConstant.PIN.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                            userFieldDTOs[count].setFieldValue(getHashValue(pin));
                        } else if (STATUS_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                            if (isAttributeScope && spType.equalsIgnoreCase(AuthenticatorEnum.TrustedStatus.UNTRUSTED
                                    .name()) && attrbShareType.equalsIgnoreCase(AuthenticatorEnum
                                    .AttributeShareScopeTypes.PROVISIONING_SCOPE.getAttributeShareScopeType())) {
                                userFieldDTOs[count].setFieldValue(STATUS_ACTIVE);
                            } else if (isAttributeScope) {
                                userFieldDTOs[count].setFieldValue(STATUS_PARTIALLY_ACTIVE);
                            } else
                                userFieldDTOs[count].setFieldValue(STATUS_ACTIVE);
                        } else {
                            userFieldDTOs[count].setFieldValue("");
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Value :" + userFieldDTOs[count].getFieldValue() + " : Claim " +
                                    userFieldDTOs[count]
                                            .getClaimUri() + " : Name " + userFieldDTOs[count].getFieldName());
                        }
                    }
                } catch (UserRegistrationAdminServiceIdentityException e) {
                    log.error("UserRegistrationAdminServiceIdentityException : " + e.getMessage());
                } catch (RemoteException e) {
                    log.error("RemoteException : " + e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    log.error(e);
                } catch (UnsupportedEncodingException e) {
                    log.error(e);
                }
                // setting properties of user DTO
                UserDTO userDTO = new UserDTO();
                userDTO.setOpenID(SCOPE_OPENID);
                userDTO.setPassword(DataHolder.getInstance().getMobileConnectConfig().getAdminPassword());
                userDTO.setUserFields(userFieldDTOs);
                userDTO.setUserName(username);

                // add user DTO to the user registration admin service client
                try {
                    userRegistrationAdminServiceClient.addUser(userDTO);

                    log.info("User successfully added [ " + username + " ] ");

                    isNewUser = true;
                } catch (Exception e) {
                    log.error("Error occurred while adding User", e);
                }
            }
        } catch (UserStoreException e) {
            log.error("UserStoreException : " + e.getMessage());
        } catch (AuthenticationFailedException e) {
            log.error("AuthenticationFailedException : " + e.getMessage());
        }

        return isNewUser;
    }

    public String getCurrentPin(String username) throws RemoteUserStoreManagerServiceUserStoreExceptionException,
            RemoteException {
        return remoteUserStoreServiceAdminClient.getCurrentPin(username);
    }


    public void setCurrentPin(String username, String pin) throws
            RemoteUserStoreManagerServiceUserStoreExceptionException,
            RemoteException, UnsupportedEncodingException, NoSuchAlgorithmException {

        remoteUserStoreServiceAdminClient.setUserClaim(username, UserProfileClaimsConstant.PIN,
                getHashValue(pin), UserCoreConstants.DEFAULT_PROFILE);
    }

    private static String getHashValue(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value.getBytes("UTF-8"));

        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

//    public void updateUserProfile(UserRegistrationData userRegistrationData) throws RemoteException,
//            UserRegistrationAdminServiceIdentityException, LoginAuthenticationExceptionException,
//            RemoteUserStoreManagerServiceUserStoreExceptionException, SQLException {
//
//        String userName = userRegistrationData.getUserName();
//        String pinHash = userRegistrationData.getHashPin();
//        String requested_acr = "";
//        String challenge_question_answer_1 = "";
//        String challenge_question_answer_2 = "";
//
//
//        log.info("user profile update for the loa2 to loa3 registration " + userRegistrationData.getUserName());
//        String adminURL = DataHolder.getInstance().getMobileConnectConfig().getAdminUrl() +
// UserProfileClaimsConstant.SERVICE_URL;
//        log.debug(adminURL);
//        UserRegistrationAdminServiceClient userRegistrationAdminServiceClient = new
// UserRegistrationAdminServiceClient(
//                adminURL);
//        UserFieldDTO[] userFieldDTOs = userRegistrationAdminServiceClient
//                .readUserFieldsForUserRegistration(userRegistrationData.getClaim());
//
//        String[] fieldValues = userRegistrationData.getFieldValues().split(",");
//
//        for (int count = 0; count < fieldValues.length; count++) {
//            if (userFieldDTOs[count].getClaimUri().equals(UserProfileClaimsConstant.LOA)) {
//                log.info(userFieldDTOs[count].getClaimUri() + " : " + fieldValues[count]);
//                requested_acr = fieldValues[count];
//            }
//            if (userFieldDTOs[count].getClaimUri().equals(UserProfileClaimsConstant.CHALLENGEQUESTION1)) {
//                log.info(userFieldDTOs[count].getClaimUri() + " : " + fieldValues[count]);
//                challenge_question_answer_1 = fieldValues[count];
//            }
//            if (userFieldDTOs[count].getClaimUri().equals(UserProfileClaimsConstant.CHALLENGEQUESTION2)) {
//                log.debug(userFieldDTOs[count].getClaimUri() + " : " + fieldValues[count]);
//                challenge_question_answer_2 = fieldValues[count];
//            }
//
//        }
//
//        if (Integer.parseInt(requested_acr) == UserProfileClaimsConstant.LOA2) {
//                /* update user profile - loa */
//
//            updateUserProfileForLOA2(userName);
//        } else if (Integer.parseInt(requested_acr) == UserProfileClaimsConstant.LOA3) {
//                /* update user profile - loa, challenge question, pin */
//            updateUserProfileForLOA3(challenge_question_answer_1, challenge_question_answer_2,
//                    userName);
//            updateUserProfilePIN(userName, pinHash);
//        }
//
//			/* PIN reset for the LOA3 registered users */
////            updateUserProfilePIN(userName, pinHash); // TODO: 1/3/17 enable for pin reset flow
//
//        // update databse after success profile update
////        DatabaseUtils.updateAuthenticateData(userName, UserProfileClaimsConstant.AUTH_STATUS);
////        DatabaseUtils.updateRegStatus(userName, UserProfileClaimsConstant.REG_STATUS);
//        log.info("user profile update successfull " + userRegistrationData.getUserName());
//    }

    /**
     * update user profile for USSD (LOA2) registration update loa
     *
     * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws RemoteException                                          fieldValues, userName
     */

    private void updateUserProfileForLOA2(String userName)
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {

		/* updating loa cliam for the loa2 to loa3 registration */

        remoteUserStoreServiceAdminClient.setUserClaim(userName, UserProfileClaimsConstant.LOA, Constants.LOA2,
                UserCoreConstants.DEFAULT_PROFILE);

    }


    /**
     * update user profile for PIN (LOA3) registration update loa, challenge
     * questions
     *
     * @param challengeQuestionAnswer1 challenge answer1
     * @param challengeQuestionAnswer2 challenge answer2
     * @param pin                      pin
     * @param userName                 username
     * @throws RemoteUserStoreManagerServiceUserStoreExceptionException remote service exception
     * @throws RemoteException                                          remote exception
     * @throws UnsupportedEncodingException                             unsupported encoding exception
     * @throws NoSuchAlgorithmException                                 no such algorithm exception
     */
    public void updateUserProfileForLOA3(String challengeQuestionAnswer1,
                                         String challengeQuestionAnswer2, String pin, String userName, boolean
                                                 isStatusUpdate, boolean isAttributeScope, String spType, String
                                                 attrbShareType)
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException,
            UnsupportedEncodingException, NoSuchAlgorithmException {

		/* updating loa cliam of the user profile */
        if (log.isDebugEnabled()) {
            log.debug("user profile update for loa " + Constants.LOA3);
        }

        if (isStatusUpdate) {
            updateUserStatus(userName, isAttributeScope, spType, attrbShareType);
        }
        remoteUserStoreServiceAdminClient.setUserClaim(userName, UserProfileClaimsConstant.LOA, Constants.LOA3,
                UserCoreConstants.DEFAULT_PROFILE);


		/*
         * updating challenge question 1 cliam of the user profile
		 */
        if (log.isDebugEnabled()) {
            log.debug("user profile update for challengeQuestionAnswer1 " + challengeQuestionAnswer1);
        }
        remoteUserStoreServiceAdminClient.setUserClaim(userName, UserProfileClaimsConstant.CHALLENGEQUESTION1,
                challengeQuestionAnswer1, UserCoreConstants.DEFAULT_PROFILE);

		/*
         * updating challenge question 2 cliam of the user profile
		 */
        if (log.isDebugEnabled()) {
            log.debug("user profile update for challengeQuestionAnswer2 " + challengeQuestionAnswer2);
        }
        remoteUserStoreServiceAdminClient.setUserClaim(userName, UserProfileClaimsConstant.CHALLENGEQUESTION2,
                challengeQuestionAnswer2, UserCoreConstants.DEFAULT_PROFILE);

        if (log.isDebugEnabled()) {
            log.debug("user profile update for pin ");
        }
        remoteUserStoreServiceAdminClient.setUserClaim(userName, UserProfileClaimsConstant.PIN,
                getHashValue(pin), UserCoreConstants.DEFAULT_PROFILE);

    }

    public String getCurrentLoa(String username) throws RemoteUserStoreManagerServiceUserStoreExceptionException,
            RemoteException {
        return remoteUserStoreServiceAdminClient.getCurrentLoa(username);
    }

    public String getChallengeQuestionAndAnswer1(String username) throws
            RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        return remoteUserStoreServiceAdminClient.getChallengeQuestionAndAnswer1(username);
    }

    public String getChallengeQuestionAndAnswer2(String username) throws
            RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {
        return remoteUserStoreServiceAdminClient.getChallengeQuestionAndAnswer2(username);
    }

    public Map<String, String> getChallengeQuestionAndAnswers(String username) throws
            RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        ClaimValue[] claimValues = remoteUserStoreServiceAdminClient.getChallengeQuestionAndAnswers(username);

        Map<String, String> challengeQuestionMap = new HashMap<String, String>();
        if (claimValues != null && claimValues.length > 0) {
            for (ClaimValue claimValue : claimValues) {
                challengeQuestionMap.put(claimValue.getClaimURI(), claimValue.getValue());
            }
        }
        return challengeQuestionMap;
    }

    /**
     * update user profile for PIN reset update pin
     *
     * @param userName, pinHash
     * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws RemoteException
     */
    private void updateUserProfilePIN(String userName, String pinHash)
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {

        if (log.isDebugEnabled()) {
            log.debug("user profile update for pin " + userName);
            log.debug(UserProfileClaimsConstant.PIN + " : " + pinHash);
        }
        /* updating pin cliam for the loa2 to loa3 registration or pin reset */
        remoteUserStoreServiceAdminClient.setUserClaim(userName, UserProfileClaimsConstant.PIN, pinHash,
                UserCoreConstants.DEFAULT_PROFILE);

    }

//    static {
//        authenticate();
//    }

    public void init() {
        try {

            String adminURL = DataHolder.getInstance().getMobileConnectConfig().getAdminUrl();
            LoginAdminServiceClient lAdmin = new LoginAdminServiceClient(adminURL);
            String sessionCookie = lAdmin.authenticate(DataHolder.getInstance().getMobileConnectConfig()
                            .getAdminUsername(),
                    DataHolder.getInstance().getMobileConnectConfig().getAdminPassword());
            remoteUserStoreServiceAdminClient = new RemoteUserStoreServiceAdminClient(
                    DataHolder.getInstance().getMobileConnectConfig().getAdminUrl(), sessionCookie);

        } catch (AxisFault axisFault) {
            log.error(axisFault);
        } catch (RemoteException e) {
            log.error(e);
        } catch (LoginAuthenticationExceptionException e) {
            log.error(e);
        }
    }

    /**
     * update user profile status
     *
     * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws RemoteException                                          fieldValues, userName,isAttributeScope
     */
    private void updateUserStatus(String userName, boolean isAttributeScope, String spType, String attrbShareType)
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException {

        String userStatus;
        try {
            userStatus = AdminServiceUtil.getUserStatus(userName);
            if (isAttributeScope && spType.equalsIgnoreCase(AuthenticatorEnum.TrustedStatus.UNTRUSTED.name()) &&
                    attrbShareType.equalsIgnoreCase(AuthenticatorEnum.AttributeShareScopeTypes.PROVISIONING_SCOPE
                            .getAttributeShareScopeType())) {
                updateUserStatus(userStatus, userName, STATUS_ACTIVE);
            } else if (isAttributeScope) {
                updateUserStatus(userStatus, userName, STATUS_PARTIALLY_ACTIVE);
            } else {
                updateUserStatus(userStatus, userName, STATUS_ACTIVE);
            }

        } catch (IdentityException e) {
            log.error("IdentityException for User- " + userName + ":" + e.getMessage());
        } catch (UserStoreException e) {
            log.error("UserStoreException- " + userName + ":" + e.getMessage());
        } catch (LoginAuthenticationExceptionException e) {
            log.error("LoginAuthenticationExceptionException- " + userName + ":" + e.getMessage());
        }
    }

    private void updateUserStatus(String userStatus, String userName, String statusToBeUpdate) {
        try {

            if (userStatus.equals(STATUS_INACTIVE) || userStatus.equals(STATUS_PARTIALLY_ACTIVE)) {
                remoteUserStoreServiceAdminClient.setUserClaim(userName, STATUS_CLAIM_NAME, statusToBeUpdate,
                        UserCoreConstants.DEFAULT_PROFILE);
            }
        } catch (RemoteException e) {
            log.error("RemoteException- " + userName + ":" + e.getMessage());
        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
            log.error("RemoteUserStoreManagerServiceUserStoreExceptionException- " + userName + ":" + e.getMessage());
        }
    }

    public void updateMIGUserRoles(String userName, String clientID, String apiScopes){
        try {
            ArrayList<String> userRolesScope = DBUtils.getRoleNameFromScope(apiScopes);
            String[] userRoles = AdminServiceUtil.getRoleListOfUser(userName);
            for(String roles : userRoles){
                if(!roles.startsWith("Internal") && !roles.startsWith("Application")){
                    userRolesScope.remove(roles);
                }
            }
            AdminServiceUtil.updateRoleListOfUser(userName,null, Arrays.copyOf(userRolesScope.toArray(), userRolesScope.toArray().length, String[].class));
        } catch (UserStoreException e) {
            log.error("UserStoreException- " + userName + ":" + e.getMessage());
        } catch (NullPointerException e){
            log.error("NullPointerException- " + userName + ":" + e.getMessage());
        } catch (Exception e){
            log.error("Exception- " + userName + ":" + e.getMessage());
        }
    }
}
