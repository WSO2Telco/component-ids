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

import com.wso2telco.gsma.authenticators.internal.CustomAuthenticatorServiceComponent;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.rmi.RemoteException;


public class AdminServiceUtil {

    public static boolean isUserExists(String msisdn) throws UserStoreException, AuthenticationFailedException {
        int tenantId = -1234;
        UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService()
                .getTenantUserRealm(tenantId);

        if (userRealm != null) {
            UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
            return userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(msisdn));
        } else {
            throw new AuthenticationFailedException("Cannot find the user realm for the given tenant: " + tenantId);
        }
    }
    
    public static String getUserStatus(String username) throws IdentityException, UserStoreException,
            RemoteException, LoginAuthenticationExceptionException,
            RemoteUserStoreManagerServiceUserStoreExceptionException {
        final int tenantId = -1234;
        UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService().getTenantUserRealm(tenantId);
        UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
        return userStoreManager.getUserClaimValue(username, "http://wso2.org/claims/status", null);
    }

    public static String[] getRoleListOfUser(String username) throws IdentityException, UserStoreException,
            RemoteException, LoginAuthenticationExceptionException,
            RemoteUserStoreManagerServiceUserStoreExceptionException {
        final int tenantId = -1234;
        UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService().getTenantUserRealm(tenantId);
        UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
        return userStoreManager.getRoleListOfUser(username);
    }

    public static String getSPUserName(String clientid) throws IdentityApplicationManagementException {
        return ApplicationManagementService.getInstance().getServiceProviderByClientId(clientid, "oauth2", MultitenantConstants.SUPER_TENANT_DOMAIN_NAME).getOwner().getUserName();
    }

    public static void updateRoleListOfUser(String username, String[] deletedRoles, String[] newRoles) throws UserStoreException {
        final int tenantId = -1234;
        UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService().getTenantUserRealm(tenantId);
        UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
        userStoreManager.updateRoleListOfUser(username, deletedRoles, newRoles);
    }
}
