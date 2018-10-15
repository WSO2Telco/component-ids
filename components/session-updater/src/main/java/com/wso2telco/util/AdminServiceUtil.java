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
package com.wso2telco.util;

import com.wso2telco.exception.CommonAuthenticatorException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class AdminServiceUtil {

    public static void deleteExistingUser(String msisdn) throws UserStoreException, CommonAuthenticatorException {
        int tenantId = -1234;
        CarbonContext cntxt = CarbonContext.getThreadLocalCarbonContext();
        UserRealm userRealm = cntxt.getUserRealm();

        if (userRealm != null) {
            UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();
            userStoreManager.deleteUser(MultitenantUtils.getTenantAwareUsername(msisdn));
        } else {
            throw new CommonAuthenticatorException("Cannot find the user realm for the given tenant: " + tenantId);
        }
    }

}
