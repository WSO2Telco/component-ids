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
package com.wso2telco.proxy.dao.attsharedaoimpl;

import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.core.dbutils.DbUtils;
import com.wso2telco.proxy.dao.AttributeShareDao;
import com.wso2telco.proxy.util.AuthProxyEnum;
import com.wso2telco.proxy.util.TableNameConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AttributeShareDaoImpl implements AttributeShareDao {

    private static Log log = LogFactory.getLog(AttributeShareDaoImpl.class);

    /***
     * Get Sp Type from the given clientID
     * @param operatorName
     * @param clientId
     * @param trustedStatus
     * @throws DBUtilException
     */
    @Override
    public String getSpTypeConfigValue(String operatorName, String clientId, String trustedStatus) throws
            DBUtilException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String status = null;
        //todo : use left join instead of inner join
        String query = "SELECT ts.status FROM " + TableNameConstants.TRUSTED_STATUS + " ts INNER JOIN " +
                TableNameConstants.SP_CONFIGURATION + " spconfig ON spconfig.config_value=ts.id_Trusted_type where " +
                "spconfig.client_id=? AND spconfig.config_key=? AND (spconfig.operator=? OR spconfig.operator='ALL');";

        try {
            connection = DbUtils.getConnectDbConnection();
            preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, clientId);
            preparedStatement.setString(2, trustedStatus);
            preparedStatement.setString(3, operatorName);

            if (log.isDebugEnabled()) {
                log.debug("Query in method getSpTypeConfigValue:" + preparedStatement);
            }

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                status = resultSet.getString("status");
            }
        } catch (DBUtilException | SQLException e) {
            log.error("Exception occurred while retrieving the data from database for Operator: " + operatorName + " " +
                    ",ClientID: " + clientId + " ,Trusted Status:" + trustedStatus + " :" + e.getMessage());
            throw new DBUtilException(e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return status;
    }

    /***
     * Load all scopes and related details from the database
     * @throws DBUtilException
     */
    @Override
    public Map<String, String> getScopeParams() throws DBUtilException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Map<String, String> scopeParams = new HashMap<>();
        String query = "SELECT scp.scope,scp.scope_type FROM " + TableNameConstants.SCOPE_PARAMETER + " scp;";
        try {
            connection = DbUtils.getConnectDbConnection();
            preparedStatement = connection.prepareStatement(query);

            if (log.isDebugEnabled()) {
                log.debug("Query in method getScopeParams:" + preparedStatement);
            }
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String scopeType = resultSet.getString("scope_type");
                if (AuthProxyEnum.SCOPETYPE.ATT_VERIFICATION.name().equalsIgnoreCase(scopeType) || AuthProxyEnum
                        .SCOPETYPE.ATT_SHARE.name().equalsIgnoreCase(scopeType) || AuthProxyEnum
                        .SCOPETYPE.APICONSENT.name().equalsIgnoreCase(scopeType)) {
                    scopeParams.put(resultSet.getString("scope"), scopeType);
                    log.info("=============" + resultSet.getString("scope") + "==="+scopeType+"====");
                }
            }

        } catch (DBUtilException | SQLException e) {
            log.error("Exception occurred while retrieving the scopes from database : " + e.getMessage());
            throw new DBUtilException(e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }
        return scopeParams;
    }
}
