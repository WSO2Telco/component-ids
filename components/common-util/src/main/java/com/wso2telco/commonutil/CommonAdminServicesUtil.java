package com.wso2telco.commonutil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

public class CommonAdminServicesUtil {

    private static Log log = LogFactory.getLog(CommonAdminServicesUtil.class);

    public static boolean isUserExists(String userName) throws UserStoreException {
        UserStoreManager userStoreManager;
        boolean isUserExists = false;

        try {
            UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();

            if (userRealm != null) {
                userStoreManager = userRealm.getUserStoreManager();
                isUserExists = userStoreManager.isExistingUser(userName);
            }

        } catch (UserStoreException ex) {
            log.error("Error in checking if user exists", ex);
            throw ex;
        }

        return isUserExists;
    }
}
