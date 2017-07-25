package com.wso2telco.user;


import com.wso2telco.LoginAdminServiceClient;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.entity.RegisterUserStatusInfo;
import com.wso2telco.ids.datapublisher.util.FileUtil;
import com.wso2telco.util.Constants;
import com.wso2telco.util.UserState;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceStub;
import org.wso2.carbon.identity.user.registration.stub.dto.UserDTO;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.SecureRandom;

public class UserRegistration {

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(UserRegistration.class);

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();


    /**
     * Check if the msisdn format is under valid criteria
     *
     * @param msisdn
     * @return Error code if any, or null
     */
    public RegisterUserStatusInfo.registerStatus validateMsisdn(String msisdn) {
        String regex = "tel:\\+[0-9]+";
        int msisdnLength = 17;

        if (StringUtils.isEmpty(msisdn)) {
            return RegisterUserStatusInfo.registerStatus.MSISDN_EMPTY;
        }
        if (msisdn.length() != msisdnLength || !msisdn.matches(regex)) {
            return RegisterUserStatusInfo.registerStatus.INVALID_MSISDN_FORMAT;
        }
        return null;
    }

    public String getRegisteredOperator(String username)
            throws IdentityException, UserStoreException, RemoteException, LoginAuthenticationExceptionException,
            RemoteUserStoreManagerServiceUserStoreExceptionException {
        RemoteUserStoreManagerServiceStub remoteUserStoreManagerServiceStub = getUserStoreManagerStub();
        return remoteUserStoreManagerServiceStub.getUserClaimValue(username, Constants.OPERATOR_CLAIM_NAME, null);
    }

    public boolean isExistingUser(String username)
            throws IdentityException, UserStoreException, RemoteException, LoginAuthenticationExceptionException,
            RemoteUserStoreManagerServiceUserStoreExceptionException {
        RemoteUserStoreManagerServiceStub remoteUserStoreManagerServiceStub = getUserStoreManagerStub();
        return remoteUserStoreManagerServiceStub.isExistingUser(username);
    }

    public String getUserStatus(String username)
            throws IdentityException, UserStoreException, RemoteException, LoginAuthenticationExceptionException,
            RemoteUserStoreManagerServiceUserStoreExceptionException {
        RemoteUserStoreManagerServiceStub remoteUserStoreManagerServiceStub = getUserStoreManagerStub();
        return remoteUserStoreManagerServiceStub.getUserClaimValue(username, Constants.STATUS_CLAIM_NAME, null);
    }

    /**
     * If the provided operator is not matching with the existing operator, update the operator
     *
     * @param operator
     * @param msisdn
     * @throws RemoteException
     * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws LoginAuthenticationExceptionException
     */
    public void updateOperator(String operator, String msisdn)
            throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException,
            LoginAuthenticationExceptionException {
        RemoteUserStoreManagerServiceStub remoteUserStoreManagerServiceStub = getUserStoreManagerStub();
        remoteUserStoreManagerServiceStub.setUserClaimValue(msisdn, Constants.OPERATOR_CLAIM_NAME, operator,UserCoreConstants.DEFAULT_PROFILE);
        remoteUserStoreManagerServiceStub.setUserClaimValue(msisdn, Constants.STATUS_CLAIM_NAME, UserState.ACTIVE.name(),UserCoreConstants.DEFAULT_PROFILE);
    }

    /**
     * Create new user profile
     *
     * @param username
     * @param operator
     * @return true if new profile is created, false if failed
     */
    public boolean createUserProfile(String username, String operator) {
        boolean isNewUser = false;

        /* reading admin url from application properties */
        String adminURL = FileUtil.getApplicationProperty(Constants.APPLICATION_PROPERTY_IS_ADMIN_URL)
                + Constants.SERVICE_URL;
        if (log.isDebugEnabled()) {
            log.debug(adminURL);
        }
        /*  getting user registration admin service */
        /*  by sending the claim dialects, gets existing claims list */
        org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO[] userFieldDTOs =
                new org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO[0];
        UserRegistrationAdminService userRegistrationAdminService=null;

        try {
            userRegistrationAdminService = new UserRegistrationAdminServiceStub();
            userFieldDTOs=userRegistrationAdminService.readUserFieldsForUserRegistration(Constants.CLAIM);
        } catch (UserRegistrationAdminServiceIdentityException e) {
            log.error("UserRegistrationAdminServiceIdentityException : " + e.getMessage());
        } catch (RemoteException e) {
            log.error("RemoteException : " + e.getMessage());
        }

        for (int count = 0; count < userFieldDTOs.length; count++) {

            if (Constants.OPERATOR_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                userFieldDTOs[count].setFieldValue(operator);
            } else if (Constants.LOA_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                userFieldDTOs[count].setFieldValue(Constants.LOA_MNV_VALUE);
            } else if (Constants.MOBILE_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                userFieldDTOs[count].setFieldValue(username);
            } else if (Constants.REG_MODE_CLAIM_NAME.equalsIgnoreCase(userFieldDTOs[count].getClaimUri())) {
                userFieldDTOs[count].setFieldValue(Constants.REG_MODE_OFFLINE);
            } else {
                userFieldDTOs[count].setFieldValue("");
            }
            if (log.isDebugEnabled()) {
                log.debug("Value :" + userFieldDTOs[count].getFieldValue() + " : Claim " +
                        userFieldDTOs[count].getClaimUri() + " : Name " +
                        userFieldDTOs[count].getFieldName());

            }
        }

        // setting properties of user DTO UserDTO
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword(generateRandomPassword());
        userDTO.setUserFields(userFieldDTOs);
        userDTO.setUserName(username);
        // add user DTO to the user registration admin service client
        try {
            userRegistrationAdminService.addUser(userDTO);
            //userRegistrationAdminServiceClient.addUser(userDTO);
            log.info("user registration successful - " + username);
            isNewUser = true;
        } catch (Exception e) {
            log.error("Error in adding User :" + e.getMessage());
        }
        return isNewUser;
    }

    public RemoteUserStoreManagerServiceStub getUserStoreManagerStub() throws RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException,
            LoginAuthenticationExceptionException {

        MobileConnectConfig.SessionUpdaterConfig sessionUpdaterConfig = configurationService.getDataHolder().getMobileConnectConfig().getSessionUpdaterConfig();

        String serviceEndPoint =  sessionUpdaterConfig.getAdmin_url() + "/services/" +Constants.SERVICE;
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null,null);
        RemoteUserStoreManagerServiceStub remoteUserStoreManagerServiceStub;
        remoteUserStoreManagerServiceStub = new RemoteUserStoreManagerServiceStub(configContext, serviceEndPoint);

        //Authenticate Your stub from sessionCookie
        ServiceClient serviceClient;
        Options option;
        LoginAdminServiceClient lAdmin = new LoginAdminServiceClient(sessionUpdaterConfig.getAdmin_url());
        String sessionCookie = lAdmin.authenticate(
                sessionUpdaterConfig.getAdminusername(),
                sessionUpdaterConfig.getAdminpassword());

        serviceClient = remoteUserStoreManagerServiceStub._getServiceClient();
        option = serviceClient.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);

        return remoteUserStoreManagerServiceStub;
    }

    /**
     * Generate a random password
     *
     * @return set of random characters
     */
    public String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    /**
     * Check if the msisdn format is under valid criteria
     *
     * @param msisdn
     * @return Error code if any, or null
     */
    public RegisterUserStatusInfo.registerStatus validateUnregisterUser(String msisdn, String operator,RemoteUserStoreManagerServiceStub adminStub) {
        try {
            if (adminStub.isExistingUser(msisdn)){
                String useroperator=adminStub.getUserClaimValue(msisdn,Constants.OPERATOR_CLAIM_NAME,UserCoreConstants.DEFAULT_PROFILE);
                if(useroperator!=null && !useroperator.isEmpty() && useroperator.equalsIgnoreCase(operator)){
                    String status=null;
                    try{
                        status=adminStub.getUserClaimValue(msisdn,Constants.STATUS_CLAIM_NAME,UserCoreConstants.DEFAULT_PROFILE);
                    }catch (Exception e){
                        log.info(msisdn + ": status not found");
                    }
                    if(!(status!=null && !status.isEmpty() && status.equals(UserState.INACTIVE.name())) ) {
                        return null;
                    }else{
                        return RegisterUserStatusInfo.registerStatus.MSISDN_NOT_FOUND;
                    }
                }else{
                    return RegisterUserStatusInfo.registerStatus.OPERATOR_VERIFICATION_FAILED;
                }
            }else{
                log.info(msisdn + ": is not a registered user for un-registration");
                return RegisterUserStatusInfo.registerStatus.MSISDN_NOT_FOUND;
            }
        }
        catch (Exception e){
            log.error("Error occurred while validating unregister MSISDN",e);
            return RegisterUserStatusInfo.registerStatus.PROCESSING_FAILED;
        }
    }
}
