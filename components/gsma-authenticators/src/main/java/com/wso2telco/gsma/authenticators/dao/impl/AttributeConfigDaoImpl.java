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
package com.wso2telco.gsma.authenticators.dao.impl;

import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.dao.AttributeConfigDao;
import com.wso2telco.gsma.authenticators.model.SpConsent;
import com.wso2telco.gsma.authenticators.model.UserConsentDetails;
import com.wso2telco.gsma.authenticators.model.UserConsentHistory;
import com.wso2telco.gsma.authenticators.util.TableName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AttributeConfigDaoImpl implements AttributeConfigDao {


    /**
     * The mconnect datasource.
     */
    private static volatile DataSource mConnectDatasource = null;
    private static final Log log = LogFactory.getLog(AttributeConfigDaoImpl.class);

    private static void initializeConnectDatasource() throws NamingException {
        if (mConnectDatasource != null) {
            return;
        }

        String dataSourceName = null;
        try {
            Context ctx = new InitialContext();
            ConfigurationService configurationService = new ConfigurationServiceImpl();
            dataSourceName = configurationService.getDataHolder().getMobileConnectConfig().getDataSourceName();
            mConnectDatasource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml:" + e);
        }
    }


    /**
     * Gets the connect db connection.
     *
     * @return the connect db connection
     * @throws AuthenticatorException the authenticator exception
     */
    private static Connection getConnectDBConnection() throws SQLException, NamingException {
        initializeConnectDatasource();

        if (mConnectDatasource != null) {
            return mConnectDatasource.getConnection();
        }
        throw new SQLException("Connect Datasource not initialized properly");
    }

    public List<SpConsent> getScopeExpireTime(String operator, String consumerKey, String scope)
            throws NamingException, DBUtilException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String[] scopeValues = (scope.substring(1, scope.length() - 1)).split(",");

        StringBuilder params = new StringBuilder("(select param_id from scope_parameter where scope= ?)");
        for (int i = 1; i < scopeValues.length; i++) {
            params.append(",(select param_id from scope_parameter where scope= ?)");
        }

        String query = "SELECT con.scope_id,con.exp_period,con.operator_id ,con.consent_id FROM " + TableName.CONSENT
                + " con INNER JOIN " + TableName.OPERATOR + " op ON op.ID=con.operator_id INNER JOIN " + TableName
                .SCOPE_PARAMETER + " scp ON scp" +
                ".param_id=con.scope_id where op.operatorname=? AND con.client_id=? AND con.scope_id in (" + params +
                ");";

        List<SpConsent> spConsentList = new ArrayList();

        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, operator);
            preparedStatement.setString(2, consumerKey);
            for (int i = 0; i < scopeValues.length; i++) {
                preparedStatement.setString(i + 3, scopeValues[i].trim());
            }

            if (log.isDebugEnabled()) {
                log.debug("Query in method getScopeExpireTime:" + preparedStatement);
            }
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                SpConsent spConsent = new SpConsent();
                spConsent.setScope(resultSet.getInt("scope_id"));
                spConsent.setExpPeriod(resultSet.getInt(Constants.EXP_PERIOD));
                spConsent.setOperatorID(resultSet.getInt("operator_id"));
                spConsent.setConsentId(resultSet.getInt("consent_id"));
                spConsentList.add(spConsent);
            }
        } catch (SQLException e) {
            log.error("Exception occurred while retrieving data to the database for consumerKey: " + consumerKey + " " +
                    ",operator: " + operator + " scope: " + scope + " : " + e.getMessage());
            throw new DBUtilException(e.getMessage(), e);

        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return spConsentList;
    }

    public List<ScopeParam> getScopeParams(String scopes) throws NamingException, DBUtilException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String[] scopeValues = scopes.split("\\s+|\\+");
        StringBuilder params = new StringBuilder("?");

        for (int i = 1; i < scopeValues.length; i++) {
            params.append(",?");
        }

        String query = "SELECT scp.scope,con.consent_type,vt.validity_type FROM " + TableName.SCOPE_PARAMETER + " scp" +
                " INNER JOIN " + TableName.CONSENT_TYPE + " con ON scp.consent_type=con.consent_typeID INNER JOIN " +
                TableName.CONSENT_VALIDITY_TYPE +
                " vt ON scp.consent_validity_type=vt.validity_id WHERE scp.scope in (" + params + ");";

        List<ScopeParam> scopeParams = new ArrayList();
        try {

            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < scopeValues.length; i++) {
                preparedStatement.setString(i + 1, scopeValues[i]);
            }

            if (log.isDebugEnabled()) {
                log.debug("Query in method getScopeParams:" + preparedStatement);
            }

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ScopeParam scopeParam = new ScopeParam();
                scopeParam.setScope(resultSet.getString("scope"));
                scopeParam.setConsentValidityType(resultSet.getString("validity_type"));
                scopeParam.setConsentType(resultSet.getString("consent_type"));
                scopeParams.add(scopeParam);
            }

        } catch (SQLException e) {
            log.error("Exception occurred while retrieving data to the database for scopes : " + scopes + ": " + e
                    .getMessage());
            throw new DBUtilException(e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return scopeParams;

    }


    public UserConsentDetails getUserConsentDetails(UserConsentDetails userConsentDetails) throws NamingException,
            DBUtilException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        UserConsentDetails userConsent = null;

        String query = "SELECT usercon.consent_status as activestatus ,usercon.expire_time as consent_expire_time " +
                "FROM " + TableName.USER_CONSENT + " usercon INNER JOIN " + TableName.CONSENT + " con ON con" +
                ".consent_id = usercon" +
                ".consent_id INNER JOIN " + TableName.OPERATOR + " op ON op.ID=con.operator_id INNER JOIN " +
                TableName.SCOPE_PARAMETER + " scp ON scp" +
                ".param_id=con.scope_id WHERE op.operatorname=? AND scp.scope=? AND con.client_id=? AND usercon" +
                ".msisdn=?;";

        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userConsentDetails.getOperatorName());
            preparedStatement.setString(2, userConsentDetails.getScope());
            preparedStatement.setString(3, userConsentDetails.getConsumerKey());
            preparedStatement.setString(4, userConsentDetails.getMsisdn());

            if (log.isDebugEnabled()) {
                log.debug("Query in method getUserConsentDetails:" + preparedStatement);
            }

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                userConsent = new UserConsentDetails();
                userConsent.setConsumerKey(userConsentDetails.getConsumerKey());
                userConsent.setScope(userConsentDetails.getScope());
                userConsent.setOperatorName(userConsentDetails.getOperatorName());
                userConsent.setMsisdn(userConsentDetails.getMsisdn());
                userConsent.setRevokeStatus(String.valueOf(resultSet.getBoolean("activestatus")));
                userConsent.setConsentExpireDatetime(resultSet.getString("consent_expire_time"));

            }
        } catch (SQLException e) {
            log.error("Exception occurred while retrieving data to the database for consumerKey: " +
                    userConsentDetails.getConsumerKey() + " and msisdn: " + userConsentDetails.getMsisdn() + " :" + e
                    .getMessage());
            throw new DBUtilException(e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return userConsent;
    }

    @Override
    public void saveUserConsentedAttributes(List<UserConsentHistory> userConsentHistory) throws NamingException,
            DBUtilException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String query = "INSERT INTO " + TableName.USER_CONSENT + "(consent_id,msisdn,expire_time,consent_status) " +
                "VALUES (?,?,?,?);";

        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(query);
            for (UserConsentHistory userConsentHistory1 : userConsentHistory) {

                preparedStatement.setInt(1, userConsentHistory1.getConsentId());
                preparedStatement.setString(2, userConsentHistory1.getMsisdn());
                preparedStatement.setString(3, userConsentHistory1.getConsentExpireTime());
                preparedStatement.setBoolean(4, Boolean.parseBoolean(userConsentHistory1.getConsentStatus()));
                preparedStatement.addBatch();
            }

            if (log.isDebugEnabled()) {
                log.debug("Query in method saveUserConsentedAttributes:" + preparedStatement);
            }

            preparedStatement.executeBatch();

        } catch (SQLException e) {
            log.error("Exception occurred while inserting data to the database for history : " + userConsentHistory
                    .toString() + " :" + e.getMessage());
            throw new DBUtilException(e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }
    }

    public String getSpConfigValue(String operator, String clientID, String key)
            throws NamingException, DBUtilException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String result = null;
        String query = "SELECT sp_config.config_value from " + TableName.SP_CONFIGURATION + " sp_config where " +
                "sp_config.client_id=? AND sp_config.operator=? AND sp_config.config_key=?;";

        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, clientID);
            preparedStatement.setString(2, operator);
            preparedStatement.setString(3, key);

            if (log.isDebugEnabled()) {
                log.debug("Query in method getSpConfigValue:" + preparedStatement);
            }

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                result = resultSet.getString("config_value");
            }
        } catch (SQLException e) {
            log.error("Exception occurred while retrieving the data from database for operator: " + operator + " and " +
                    "clientID: " + clientID + " : " + e.getMessage());
            throw new DBUtilException(e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return result;
    }
}
