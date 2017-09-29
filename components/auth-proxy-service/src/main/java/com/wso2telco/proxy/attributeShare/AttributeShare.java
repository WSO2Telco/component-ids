/*
 * ******************************************************************************
 *  * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *  *
 *  * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package com.wso2telco.proxy.attributeShare;

import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.proxy.dao.AttShareDAO;
import com.wso2telco.proxy.dao.attShareDAOImpl.AttShareDAOImpl;
import com.wso2telco.proxy.util.AuthProxyConstants;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AttributeShare {

    private static Log log = LogFactory.getLog(AttributeShare.class);

    private static AttShareDAO attShareDAO;
    private static Map<String, String> scopeTypes;

    private AttributeShare(){}
    static {

        try {
            attShareDAO = new AttShareDAOImpl();
            scopeTypes  =attShareDAO.getScopeParams();
        } catch (SQLException |DBUtilException e) {
             log.error("error occurred while scope retreiving the data from database");
        }
    }

    /**
     *
     * @param scopeName
     * @param operatorName
     * @param clientId
     * @param loginhintMsisdn
     * @param msisdn
     * @return
     * @throws AuthenticationFailedException
     */
    public static Map<String,String> validateAttShareScopes(String scopeName, String operatorName, String clientId, String loginhintMsisdn, String msisdn) throws AuthenticationFailedException {

        List<String> scopeList = new ArrayList(Arrays.asList(scopeName.split(" ")));
        String trustedStatus = null;
        boolean isAttributeShare = false;
        Map<String,String> attShareScoprDetails = new HashMap<>();
        try {
            if(!scopeTypes.isEmpty()){
                for (String scope : scopeList) {
                    String attrSharetype = scopeTypes.get(scope);
                    if (attrSharetype != null) {
                        isAttributeShare = true;
                        trustedStatus = ScopeFactory.getAttribAttrubteSharable(attrSharetype).attShareDetails(operatorName, clientId,loginhintMsisdn, msisdn);
                    }
                }
            }

        } catch (AuthenticationFailedException e){
            log.debug(e.getMessage());
            throw new AuthenticationFailedException(e.getMessage(),e);

        }
       attShareScoprDetails.put(AuthProxyConstants.ATTR_SHARE_SCOPE, Boolean.toString(isAttributeShare));
       attShareScoprDetails.put(AuthProxyConstants.TRUSTED_STATUS,trustedStatus);

       return  attShareScoprDetails;

    }
}
