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
package com.wso2telco;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminService;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceStub;
import org.wso2.carbon.identity.user.registration.stub.dto.UserDTO;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;

import java.rmi.RemoteException;

public class UserRegistrationAdminServiceClient {

    private static Log log = LogFactory.getLog(UserRegistrationAdminServiceClient.class);
    private UserRegistrationAdminService userRegistrationAdminService;

    public UserRegistrationAdminServiceClient(String backendUrl) {
        try {
            userRegistrationAdminService = new UserRegistrationAdminServiceStub(null, backendUrl);
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void addUser(UserDTO userDTO) throws RemoteException, UserRegistrationAdminServiceException {
        userRegistrationAdminService.addUser(userDTO);
    }

    public UserFieldDTO[] readUserFieldsForUserRegistration(String dialect) throws
            UserRegistrationAdminServiceIdentityException, RemoteException {
        return userRegistrationAdminService.readUserFieldsForUserRegistration(dialect);
    }
}
