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

import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.exception.AuthenticatorException;

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


public class DbUtil {

    /**
     * The m connect datasource.
     */
    private static volatile DataSource mConnectDatasource = null;

    /**
     * The WSO2 APIM datasource
     */
    private static volatile DataSource wso2APIMDatasource = null;

    private static final Log log = LogFactory.getLog(DbUtil.class);

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    private static void initializeDatasources() throws AuthenticatorException {
        if (mConnectDatasource != null) {
            return;
        }

        String dataSourceName = null;
        try {
            Context ctx = new InitialContext();
            dataSourceName = configurationService.getDataHolder().getMobileConnectConfig().getDataSourceName();
            mConnectDatasource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            handleException("Error while looking up the data source: " + dataSourceName, e);
        }
    }


    /**
     * Gets the WSO2 API Manager db connection.
     *
     * @return the WSO2 API Manager db connection
     * @throws SQLException           the SQL exception
     * @throws AuthenticatorException the authenticator exception
     */
    private static Connection getWSO2APIMDBConnection() throws SQLException, NamingException {
        initializeWSO2APIMDatasource();

        if (wso2APIMDatasource != null) {
            return wso2APIMDatasource.getConnection();
        }
        throw new SQLException("Connect Datasource not initialized properly");
    }

    private static void initializeWSO2APIMDatasource() throws NamingException {
        if (wso2APIMDatasource != null) {
            return;
        }

        String dataSourceName = null;
        try {
            Context ctx = new InitialContext();
            ConfigurationService configurationService = new ConfigurationServiceImpl();
            dataSourceName = configurationService.getDataHolder().getMobileConnectConfig().getWso2APIMDataSourceName();
            wso2APIMDatasource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            throw new NamingException("Error while looking up the data source : " + dataSourceName);
        }
    }

    /**
     * Gets the connect db connection.
     *
     * @return the connect db connection
     * @throws SQLException           the SQL exception
     * @throws AuthenticatorException the authenticator exception
     */
    private static Connection getConnectDBConnection() throws SQLException, AuthenticatorException {
        initializeDatasources();

        if (mConnectDatasource != null) {
            return mConnectDatasource.getConnection();
        }
        throw new SQLException("Sessions Datasource not initialized properly");
    }

    public static int readPinAttempts(String sessionId) throws SQLException, AuthenticatorException {

        Connection connection;
        PreparedStatement ps;
        int noOfAttempts = 0;
        ResultSet rs;

        String sql = "select attempts from `multiplepasswords` where " + "ussdsessionid=?;";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setString(1, sessionId);

        rs = ps.executeQuery();

        while (rs.next()) {
            noOfAttempts = rs.getInt("attempts");
        }
        if (connection != null) {
            connection.close();
        }

        return noOfAttempts;
    }

    public static void updateMultiplePasswordNoOfAttempts(String username, int attempts) throws SQLException,
            AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;

        String sql = "update `multiplepasswords` set  attempts=? where  username=?;";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setInt(1, attempts);
        ps.setString(2, username);
        ps.execute();

        if (connection != null) {
            connection.close();
        }
    }

    public static String updateRegistrationStatus(String uuid, String status) throws SQLException,
            AuthenticatorException {

        Connection connection;
        PreparedStatement ps;
        String sql = "UPDATE regstatus SET status = ? WHERE uuid = ?;";
        connection = getConnectDBConnection();
        ps = connection.prepareStatement(sql);
        ps.setString(1, status);
        ps.setString(2, uuid);
        log.info(ps.toString());
        ps.execute();

        if (connection != null) {
            connection.close();
        }
        return uuid;
    }

    public static void incrementSuccessPinAttempts(String sessionId) throws SQLException, AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;

        String sql = "update multiplepasswords set attempts=attempts +1 where ussdsessionid = ?;";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setString(1, sessionId);
        ps.execute();

        if (connection != null) {
            connection.close();
        }
    }

    public static void updatePin(int pin, String sessionId) throws SQLException, AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;

        String sql = "update multiplepasswords set pin=? where ussdsessionid = ?;";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setInt(1, pin);
        ps.setString(2, sessionId);
        ps.execute();

        if (connection != null) {
            connection.close();
        }
    }

    public static void insertPinAttempt(String msisdn, int attempts, String sessionId) throws SQLException,
            AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;

        String sql = "insert into multiplepasswords(username, attempts, ussdsessionid) values  (?,?,?);";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setString(1, msisdn);
        ps.setInt(2, attempts);
        ps.setString(3, sessionId);
        ps.execute();

        if (connection != null) {
            connection.close();
        }
    }

    public static String getContextIDForHashKey(String hashKey) throws AuthenticatorException, SQLException {
        String sessionDataKey = null;

        String sql = "select contextid from sms_hashkey_contextid_mapping where hashkey=?";

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnectDBConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, hashKey);
            rs = ps.executeQuery();
            while (rs.next()) {
                sessionDataKey = rs.getString("contextid");
            }
        } catch (SQLException e) {
            handleException(e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, ps);
        }
        return sessionDataKey;
    }

    /**
     * get client Secret value for the given Client ID
     *
     * @param clientId unique client ID
     */
    public static String getClientSecret(String clientId)
            throws ConfigurationException, AuthenticatorException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String queryToGetOperatorProperty = "SELECT CONSUMER_SECRET FROM idn_oauth_consumer_apps WHERE CONSUMER_KEY=?;";
        String clientSecretValue = null;

        try {
            connection = getWSO2APIMDBConnection();
            preparedStatement = connection.prepareStatement(queryToGetOperatorProperty);
            preparedStatement.setString(1, clientId);
            ;
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                clientSecretValue = resultSet.getString("CONSUMER_SECRET");
            }
        } catch (SQLException e) {
            handleException(
                    "Error occurred while getting client Secret for ClientId - " + clientId, e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }
        return clientSecretValue;
    }

    /**
     * Handle exception.
     *
     * @param msg the msg
     * @param t   the t
     * @throws AuthenticatorException the authenticator exception
     */
    private static void handleException(String msg, Throwable t) throws AuthenticatorException {
        log.error(msg, t);
        throw new AuthenticatorException(msg, t);
    }
}
