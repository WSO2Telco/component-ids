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
import com.wso2telco.proxy.dao.AttShareDAO;
import com.wso2telco.proxy.util.AuthProxyEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AttShareDAOImpl implements AttShareDAO {

    private static Log log = LogFactory.getLog(AttShareDAOImpl.class);

    @Override
    public String getSPTypeConfigValue(String operatorName, String clientId, String trustedSatus) throws SQLException,DBUtilException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String status = null;

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ");
        sqlBuilder.append("ts.status ");
        sqlBuilder.append("FROM ");
        sqlBuilder.append(AuthProxyEnum.TABLENAME.TRUSTED_STATUS);
        sqlBuilder.append(" ts ");
        sqlBuilder.append(" INNER JOIN ");
        sqlBuilder.append(AuthProxyEnum.TABLENAME.SP_CONFIGURATION);
        sqlBuilder.append(" sp_config ");
        sqlBuilder.append( "ON sp_config.config_value=ts.id_Trusted_type ");
        sqlBuilder.append(" where");
        sqlBuilder.append(" sp_config.client_id=? ");
        sqlBuilder.append(" AND sp_config.operator=? ");
        sqlBuilder.append(" AND sp_config.config_key=? ");

        try {
            connection = DbUtils.getConnectDbConnection();
            preparedStatement = connection.prepareStatement(sqlBuilder.toString());
            preparedStatement.setString(1, clientId);
            preparedStatement.setString(2, operatorName);
            preparedStatement.setString(3, trustedSatus);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                status =  resultSet.getString("status");
            }
        } catch (DBUtilException e) {
            log.error("DBUtilException exception occurred while retrieving the data from database : "+ e.getMessage());
            throw new DBUtilException(e.getMessage(), e);
        } catch (SQLException e){
            log.error("SQLException exception occurred while retrieving the data from database : "+e.getMessage());
            throw new SQLException(e.getMessage(),e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return status;
    }


    @Override
    public Map<String, String> getScopeParams() throws SQLException,DBUtilException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Map<String, String> scopeParams = new HashMap<>();
        try {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT ");
            sqlBuilder.append("scp.scope,scp.scope_type ");
            sqlBuilder.append("FROM ");
            sqlBuilder.append("scope_parameter scp ");

            connection = DbUtils.getConnectDbConnection();
                preparedStatement = connection.prepareStatement(sqlBuilder.toString());
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String scopeType = resultSet.getString("scope_type");
                    if(AuthProxyEnum.SCOPETYPE.ATT_VERIFICATION.name().equalsIgnoreCase(scopeType) ||AuthProxyEnum.SCOPETYPE.ATT_SHARE.name().equalsIgnoreCase(scopeType)){
                        scopeParams.put(resultSet.getString("scope"),scopeType);
                    }
                }

        } catch (DBUtilException e) {
            log.error("DBUtilException exception occurred while retrieving the data from database : "+ e.getMessage());
            throw new DBUtilException(e.getMessage(), e);
        } catch (SQLException e){
            log.error("SQLException exception occurred while retrieving the data from database : "+e.getMessage());
            throw new SQLException(e.getMessage(),e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }
        return scopeParams;
    }
}
