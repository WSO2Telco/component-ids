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

    public List<SpConsent> getScopeExpireTime(String operator, String consumerKey, String scopes)
            throws NamingException, DBUtilException {

        Connection connectionConsent = null;
        PreparedStatement preparedStatementConsent = null;
        ResultSet resultSetConsent = null;

        String[] scopeValues = (scopes.substring(1, scopes.length() - 1)).split(",");
        List<SpConsent> spConsentList = new ArrayList();
        try {
            for (int i = 0; i < scopeValues.length; i++) {
                String scope = scopeValues[i];
                String clientId = consumerKey;
                String operatorId = operator;

                if (!consentDataAvailable(scope, operatorId, clientId)) {

                    if (!consentDataAvailable(scope, "ALL", clientId)) {

                        if (!consentDataAvailable(scope, operatorId, "ALL")) {
                            clientId = "ALL";
                            operatorId = "ALL";
                        } else {
                            clientId = "ALL";
                            operatorId = operator;
                        }
                    } else {
                        clientId = consumerKey;
                        operatorId = "ALL";
                    }
                } else {
                    clientId = consumerKey;
                    operatorId = operator;
                }


                String query = "SELECT con.scope_id,con.exp_period,con.operator_id ,con.consent_id FROM " +
                        TableName.CONSENT
                        + " con INNER JOIN " + TableName
                        .SCOPE_PARAMETER + " scp ON scp" +
                        ".param_id=con.scope_id where con.operator_id=? AND con.client_id=? AND con.scope_id= " +
                        "(SELECT param_id FROM " + TableName.SCOPE_PARAMETER + " WHERE scope=?);";

                connectionConsent = getConnectDBConnection();
                preparedStatementConsent = connectionConsent.prepareStatement(query);
                preparedStatementConsent.setString(1, operatorId);
                preparedStatementConsent.setString(2, clientId);
                preparedStatementConsent.setString(3, scope);

                if (log.isDebugEnabled()) {
                    log.debug("Query in method getScopeExpireTime:" + preparedStatementConsent);
                }
                resultSetConsent = preparedStatementConsent.executeQuery();
                while (resultSetConsent.next()) {
                    SpConsent spConsent = new SpConsent();
                    spConsent.setScope(resultSetConsent.getInt("scope_id"));
                    spConsent.setExpPeriod(resultSetConsent.getInt(Constants.EXP_PERIOD));
                    spConsent.setOperatorID(resultSetConsent.getString("operator_id"));
                    spConsent.setConsentId(resultSetConsent.getInt("consent_id"));
                    spConsentList.add(spConsent);
                }
                IdentityDatabaseUtil.closeAllConnections(connectionConsent, resultSetConsent, preparedStatementConsent);
            }

        } catch (DBUtilException | SQLException e) {
            log.error("Exception occurred while retrieving data to the database for scopes : " + scopes + ": " + e
                    .getMessage());
            throw new DBUtilException(e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connectionConsent, resultSetConsent, preparedStatementConsent);
        }
        return spConsentList;
    }

    public List<ScopeParam> getScopeParams(String scopes, String operator, String consumerKey) throws
            NamingException, DBUtilException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String[] scopeValues = scopes.split("\\s+|\\+");
        List<ScopeParam> scopeParamsList = new ArrayList();
        try {
            for (int i = 1; i < scopeValues.length; i++) {
                connection = getConnectDBConnection();
                String scope = scopeValues[i];
                String clientId = consumerKey;
                String operatorId = operator;
                if (!consentDataAvailable(scope, operatorId, clientId)) {

                    if (!consentDataAvailable(scope, "ALL", clientId)) {

                        if (!consentDataAvailable(scope, operatorId, "ALL")) {
                            clientId = "ALL";
                            operatorId = "ALL";
                        } else {
                            clientId = "ALL";
                            operatorId = operator;
                        }
                    } else {
                        clientId = consumerKey;
                        operatorId = "ALL";
                    }
                } else {
                    clientId = consumerKey;
                    operatorId = operator;
                }


                String query = "SELECT scp.scope,con.consent_type,vt.validity_type " +
                        "FROM " + TableName.SCOPE_PARAMETER + " scp ," + TableName.CONSENT + " cons " +
                        "INNER JOIN " + TableName.CONSENT_TYPE + " con on cons.consent_type=con.consent_typeID " +
                        "INNER JOIN " + TableName.CONSENT_VALIDITY_TYPE + " vt on cons.consent_validity_type=vt" +
                        ".validity_id " +
                        "WHERE cons.operator_id =? and cons.client_id=? " +
                        "and scp.scope =? and scp.param_id=cons.scope_id ;";


                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, operatorId);
                preparedStatement.setString(2, clientId);
                preparedStatement.setString(3, scope);

                if (log.isDebugEnabled()) {
                    log.debug("Query in method getScopeParams:" + preparedStatement);
                }

                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    ScopeParam scopeParam = new ScopeParam();
                    scopeParam.setScope(resultSet.getString("scope"));
                    scopeParam.setConsentValidityType(resultSet.getString("validity_type"));
                    scopeParam.setConsentType(resultSet.getString("consent_type"));
                    scopeParamsList.add(scopeParam);
                }

                IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);

            }

        } catch (DBUtilException | SQLException e) {
            log.error("Exception occurred while retrieving data to the database for scopes : " + scopes + ": " + e
                    .getMessage());
            throw new DBUtilException(e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }
        return scopeParamsList;
    }

    public boolean consentDataAvailable(String scope, String operatorId, String clientId) throws
            DBUtilException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        boolean avalability = false;

        try {
            connection = getConnectDBConnection();
            String query1 = "SELECT * from " + TableName.CONSENT + " where scope_id = (select param_id from " +
                    "scope_parameter where " +
                    "scope" +
                    " = ?) and operator_id=? and client_id=?;";

            preparedStatement = connection.prepareStatement(query1);
            preparedStatement.setString(1, scope);
            preparedStatement.setString(2, operatorId);
            preparedStatement.setString(3, clientId);

            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                avalability = false;
            } else {
                avalability = true;
            }
        } catch (SQLException | NamingException e) {
            log.error("Exception occurred while retrieving data to the database for scope : " + scope + ": " + e
                    .getMessage());
            throw new DBUtilException(e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }
        return avalability;
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
                ".consent_id INNER JOIN " + TableName.OPERATOR + " op ON op.operatorname=usercon.operator INNER JOIN " +
                TableName.SCOPE_PARAMETER + " scp ON scp" +
                ".param_id=con.scope_id WHERE op.operatorname=? AND scp.scope=? AND usercon.client_id=? AND usercon" +
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
        String query = "INSERT INTO " + TableName.USER_CONSENT + "(consent_id,msisdn,expire_time,consent_status," +
                "client_id,operator) " +
                "VALUES (?,?,?,?,?,?);";

        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(query);
            for (UserConsentHistory userConsentHistory1 : userConsentHistory) {

                preparedStatement.setInt(1, userConsentHistory1.getConsentId());
                preparedStatement.setString(2, userConsentHistory1.getMsisdn());
                preparedStatement.setString(3, userConsentHistory1.getConsentExpireTime());
                preparedStatement.setBoolean(4, Boolean.parseBoolean(userConsentHistory1.getConsentStatus()));
                preparedStatement.setString(5, userConsentHistory1.getClientId());
                preparedStatement.setString(6, userConsentHistory1.getOperatorName());
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
