/*******************************************************************************
 * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.gsma.authenticators.dao;

import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.gsma.authenticators.model.SPConsent;
import com.wso2telco.gsma.authenticators.model.UserConsentDetails;
import com.wso2telco.gsma.authenticators.model.UserConsentHistory;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.List;

public interface AttributeConfigDAO {

    List<SPConsent> getScopeExprieTime(String operator, String consumerKey, String scope) throws SQLException, NamingException;

    UserConsentDetails getUserConsentDetails(UserConsentDetails userConsentDetails) throws SQLException, NamingException;

    public String getSPConfigValue(String operator, String clientID, String key)
            throws SQLException, NamingException;

    public void saveUserConsentedAttributes(List<UserConsentHistory> userConsentHistory) throws SQLException, NamingException;

    public List<ScopeParam> getScopeParams(String scopes) throws SQLException, NamingException;


}
