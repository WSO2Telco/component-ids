package com.wso2telco.gsma.manager;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.manager.client.LoginAdminServiceClient;
import com.wso2telco.gsma.manager.client.RemoteUserStoreServiceAdminClient;
import com.wso2telco.gsma.manager.client.UserRegistrationAdminServiceClient;
import com.wso2telco.gsma.manager.entity.UserRegistrationData;
import com.wso2telco.gsma.manager.util.UserProfileClaimsConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;
import org.wso2.carbon.identity.user.registration.stub.dto.UserDTO;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.rmi.RemoteException;
import java.sql.SQLException;

public class UserProfileManager {

    private static UserProfileManager userProfileManager;
    private RemoteUserStoreServiceAdminClient remoteUserStoreServiceAdminClient;
    private static Log log = LogFactory.getLog(UserProfileManager.class);

    private UserProfileManager() {
    }

    /*static block initialization for exception handling*/
    static {
        try {
            userProfileManager = new UserProfileManager();
        } catch (Exception e) {
            log.error("Error occured" + e);
            throw new RuntimeException("Exception occured in creating singleton instance");
        }
    }

    public static UserProfileManager getInstance() {
        return userProfileManager;
    }

    /**
     * check whether the user exists or not and depend on that call add user or update user
     *
     * @param userRegistrationData
     * @throws Exception
     * @throws UserRegistrationAdminServiceException
     * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws LoginAuthenticationExceptionException
     * @throws UserRegistrationAdminServiceIdentityException
     * @throws SQLException
     * @throws RemoteException
     */
    public void manageProfile(UserRegistrationData userRegistrationData) throws RemoteException, SQLException, UserRegistrationAdminServiceIdentityException, LoginAuthenticationExceptionException, RemoteUserStoreManagerServiceUserStoreExceptionException, UserRegistrationAdminServiceException, Exception {

        if (isUserExists(userRegistrationData.getUserName())) {
            updateUserProfile(userRegistrationData);
        } else {
            createUserProfile(userRegistrationData);
        }

    }


    /**
     * check whether the user exists or not
     *
     * @param userName
     * @return true if user exists.
     * @throws Exception
     * @throws SQLException
     */
    public boolean isUserExists(String userName) throws SQLException, Exception {
        /*getting remote user store service admin client connection */
        getAdminClient();
        boolean userExists = false;
        if (remoteUserStoreServiceAdminClient.isExistingUser(userName)) {
            userExists = true; // user already exists
        }
        log.info("is user exists" + userExists);
        return userExists;
    }

    /**
     * getting connection to the admin services of the identity server
     *
     * @throws Exception
     * @throws SQLException
     */
    public void getAdminClient() throws SQLException, RemoteException, Exception {
		/*reading admin url from application properties*/
        LoginAdminServiceClient lAdmin = new LoginAdminServiceClient(DataHolder.getInstance().getMobileConnectConfig().getAdminUrl());
		/*getting session cookie by sending username and password to the authenticate admin service*/
        String sessionCookie = lAdmin.authenticate(DataHolder.getInstance().getMobileConnectConfig().getAdminUsername(),
                DataHolder.getInstance().getMobileConnectConfig().getAdminPassword());
		/*using the session cookie as a key getting user store admin client*/
        remoteUserStoreServiceAdminClient = new RemoteUserStoreServiceAdminClient(
                DataHolder.getInstance().getMobileConnectConfig().getAdminUrl(), sessionCookie);
        log.debug("RemoteUserStoreServiceAdminClient " + remoteUserStoreServiceAdminClient);
    }

    /**
     * add new user profile for the registration of new user
     *
     * @param userRegistrationData
     * @throws SQLException
     * @throws UserRegistrationAdminServiceException
     * @throws UserRegistrationAdminServiceIdentityException
     * @throws RemoteException
     * @throws Exception
     */
    public void createUserProfile(UserRegistrationData userRegistrationData) throws RemoteException,
            UserRegistrationAdminServiceIdentityException, UserRegistrationAdminServiceException, SQLException, AuthenticatorException {
		
		/*reading admin url from application properties*/
        String adminURL = DataHolder.getInstance().getMobileConnectConfig().getAdminUrl() + UserProfileClaimsConstant.SERVICE_URL;
        log.debug(adminURL);
		/*getting user registration admin service*/
        UserRegistrationAdminServiceClient userRegistrationAdminServiceClient = new UserRegistrationAdminServiceClient(
                adminURL);
		/*by sending the claim dialects gets exsisting claims list*/
        UserFieldDTO[] userFieldDTOs = userRegistrationAdminServiceClient
                .readUserFieldsForUserRegistration(userRegistrationData.getClaim());
		
		/*get user registration claim value list*/
        String[] fieldValues = userRegistrationData.getFieldValues().split(",");
        for (int count = 0; count < fieldValues.length; count++) {
            userFieldDTOs[count].setFieldValue(fieldValues[count]);
            log.info("userFieldDTOs : " + userFieldDTOs[count].getFieldName() + " = " + fieldValues[count]);
			/*for the ussd pin registration add pin*/
            if (userRegistrationData.getHashPin() != null && userFieldDTOs[count].getFieldName().equals("pin")) {
                userFieldDTOs[count].setFieldValue(userRegistrationData.getHashPin());
                log.info("userFieldDTOs : " + userFieldDTOs[count].getFieldName() + " = " + userRegistrationData.getHashPin());

            }
        }

        //setting properties of user DTO
        UserDTO userDTO = new UserDTO();
        userDTO.setOpenID(userRegistrationData.getOpenId());
        userDTO.setPassword(userRegistrationData.getPassword());
        userDTO.setUserFields(userFieldDTOs);
        userDTO.setUserName(userRegistrationData.getUserName());

        // add user DTO to the user registration admin service client
        userRegistrationAdminServiceClient.addUser(userDTO);

        //update databse after success registration of user
        DBUtils.updateIdsRegStatus(userRegistrationData.getUserName(), UserProfileClaimsConstant.REG_STATUS);
        DBUtils.updateAuthenticateData(userRegistrationData.getUserName(), UserProfileClaimsConstant.AUTH_STATUS);
        log.info("user registration successfull " + userRegistrationData.getUserName());
    }

    /**
     * update user profile when LOA2 registered user registration with LOA3 or
     * update pin for pin reset
     *
     * @param userRegistrationData
     * @throws SQLException
     * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws LoginAuthenticationExceptionException
     * @throws UserRegistrationAdminServiceIdentityException
     * @throws RemoteException
     * @throws Exception
     */
    public void updateUserProfile(UserRegistrationData userRegistrationData) throws RemoteException,
            UserRegistrationAdminServiceIdentityException, LoginAuthenticationExceptionException,
            RemoteUserStoreManagerServiceUserStoreExceptionException, SQLException, AuthenticatorException {

        String userName = userRegistrationData.getUserName();
        String pinHash = userRegistrationData.getHashPin();

        if (userRegistrationData.isUpdateProfile()) {

            log.info("user profile update for the loa2 to loa3 registration " + userRegistrationData.getUserName());
			/*reading admin url from application properties*/
            String adminURL = DataHolder.getInstance().getMobileConnectConfig().getAdminUrl() + UserProfileClaimsConstant.SERVICE_URL;
            log.debug(adminURL);
			/*getting user registration admin service*/
            UserRegistrationAdminServiceClient userRegistrationAdminServiceClient = new UserRegistrationAdminServiceClient(
                    adminURL);
			/*by sending the claim dialects gets exsisting claims list*/
            UserFieldDTO[] userFieldDTOs = userRegistrationAdminServiceClient
                    .readUserFieldsForUserRegistration(userRegistrationData.getClaim());
			
			/*get user registration claim value list*/
            String[] fieldValues = userRegistrationData.getFieldValues().split(",");

            for (int count = 0; count < fieldValues.length; count++) {
				
								
				/*updating loa cliam for the loa2 to loa3 registration*/
                if (userFieldDTOs[count].getClaimUri().equals(UserProfileClaimsConstant.LOA)) {
                    log.debug(userFieldDTOs[count].getClaimUri() + " : " + fieldValues[count]);
                    remoteUserStoreServiceAdminClient.setUserClaim(userName, UserProfileClaimsConstant.LOA,
                            fieldValues[count], UserCoreConstants.DEFAULT_PROFILE);
                }
				/*updating challenge question 1 cliam for the loa2 to loa3 registration*/
                if (userFieldDTOs[count].getClaimUri().equals(UserProfileClaimsConstant.CHALLENGEQUESTION1)) {
                    log.debug(userFieldDTOs[count].getClaimUri() + " : " + fieldValues[count]);
                    remoteUserStoreServiceAdminClient.setUserClaim(userName,
                            UserProfileClaimsConstant.CHALLENGEQUESTION1, fieldValues[count],
                            UserCoreConstants.DEFAULT_PROFILE);
                }
				/*updating challenge question 2 cliam for the loa2 to loa3 registration*/
                if (userFieldDTOs[count].getClaimUri().equals(UserProfileClaimsConstant.CHALLENGEQUESTION2)) {
                    log.debug(userFieldDTOs[count].getClaimUri() + " : " + fieldValues[count]);
                    remoteUserStoreServiceAdminClient.setUserClaim(userName,
                            UserProfileClaimsConstant.CHALLENGEQUESTION2, fieldValues[count],
                            UserCoreConstants.DEFAULT_PROFILE);
                }

            }
        }

        log.info("user profile update for pin " + userRegistrationData.getUserName());
		/*updating pin cliam for the loa2 to loa3 registration or pin reset*/
        log.debug(UserProfileClaimsConstant.PIN + " : " + pinHash);
        remoteUserStoreServiceAdminClient.setUserClaim(userName, UserProfileClaimsConstant.PIN, pinHash,
                UserCoreConstants.DEFAULT_PROFILE);

        //update databse after success profile update
        DBUtils.updateIdsRegStatus(userName, UserProfileClaimsConstant.REG_STATUS);
        DBUtils.updateAuthenticateData(userName, UserProfileClaimsConstant.AUTH_STATUS);
        log.info("user profile update successfull " + userRegistrationData.getUserName());
    }


}
