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

import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import javax.naming.NamingException;
import java.sql.SQLException;

public class ServiceProviderManager {

    private static Log log = LogFactory.getLog(ServiceProviderManager.class);

    public String checkSPType(String operator,String clientID) throws AuthenticationFailedException {
        String spType;
        try {
            spType = DBUtil.getSPConfigValue(operator, clientID, Constants.SP_TYPE);
        } catch (SQLException e) {
            log.error("SQL Exception occurred while retrieving data from Database", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (NamingException e) {
            log.error("Naming Exception occurred while retrieving data from Database", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
        return spType;
    }

    public String checkConsentModel() {

        return "consent";
    }
}
