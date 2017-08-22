/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
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

package com.wso2telco.gsma.authenticators.attributeShare;

import com.wso2telco.core.config.model.ScopeDetailsConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.dao.AttributeConfigDAO;
import com.wso2telco.gsma.authenticators.dao.impl.AttributeConfigDAOimpl;
import com.wso2telco.gsma.authenticators.attributeShare.internal.SPType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*This factory class created because the user consent mechanism can be vary with the SP type.
* TSP1 consent mechanisms not included yet.
* Currently created one object for both TSP2 and Normal because it seems same same functionality.
* In future these funcionality can be changed
*/
public class AttributeShareFactory {

    private static Log log = LogFactory.getLog(AttributeShareFactory.class);
    private static ScopeDetailsConfig scopeDetailsConfigs = null;
    private static Map<String, ScopeDetailsConfig.Scope> scopeMap = null;
    static ConsentedSP consentedSP;
    static TrustedSP trustedSP;
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    static {
        //Load scope-config.xml file.
        scopeDetailsConfigs = configurationService.getDataHolder().getScopeDetailsConfig();

        //Load scope related request optional parameters.
        scopeMap = new HashMap<String, ScopeDetailsConfig.Scope>();
        List<ScopeDetailsConfig.Scope> scopes = scopeDetailsConfigs.getScope();

        for (ScopeDetailsConfig.Scope sc : scopes) {
            scopeMap.put(sc.getName(), sc);
        }
    }


    public static AttributeSharable getAttributeSharable(String operator, String clientID) throws Exception {

        AttributeSharable attributeSharable = null;

        String spType;
        try {

            AttributeConfigDAO attributeConfigDAO = new AttributeConfigDAOimpl();
            spType = attributeConfigDAO.getSPConfigValue(operator, clientID, Constants.SP_TYPE);

            if (spType != null && (spType.equalsIgnoreCase(SPType.TSP2.name()) || spType.equalsIgnoreCase(SPType.NORMAL.name()))) {
                if (consentedSP == null) {
                    consentedSP = new ConsentedSP();
                }
                attributeSharable = consentedSP;

            } if(spType != null && (spType.equalsIgnoreCase(SPType.TSP1.name()))){
                if (trustedSP == null) {
                    trustedSP = new TrustedSP();
                }
                attributeSharable = trustedSP;
            }

        } catch (SQLException e) {
            log.error("SQL Exception occurred while retrieving data from Database", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        } catch (NamingException e) {
            log.error("Naming Exception occurred while retrieving data from Database", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
        return attributeSharable;
    }

    public static List<String> getScopestoDisplay(Map<String, List<String>> attributeSet) {

        List<String> consentAttribute = new ArrayList<>();
        List<String> claimSet;

        if (!attributeSet.get("explicitScopes").isEmpty()) {

            for (int i = 0; i < attributeSet.get("explicitScopes").size(); i++) {
                claimSet = scopeMap.get(attributeSet.get("explicitScopes").get(i)).getClaimSet();
                for (int j = 0; j < claimSet.size(); j++) {
                    if (!consentAttribute.contains(claimSet.get(j))) {
                        consentAttribute.add(claimSet.get(j));
                    }
                }
            }
        }
        return consentAttribute;
    }

}
