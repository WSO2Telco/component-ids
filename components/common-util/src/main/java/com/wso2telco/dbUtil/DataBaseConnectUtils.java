/*******************************************************************************
 * Copyright (c) 2015-2018, WSO2.Telco Inc. (http://www.wso2telco.com)
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
package com.wso2telco.dbUtil;

import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.model.BackChannelUserDetails;
import com.wso2telco.exception.CommonAuthenticatorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * This class is used to read operator, msisdn and login hint properties.
 */
public class DataBaseConnectUtils {
    private static final Log log = LogFactory.getLog(DataBaseConnectUtils.class);

    /**
     * The m connect datasource.
     */
    private static volatile DataSource mConnectDatasource = null;

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

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
            throw new NamingException("Error while looking up the data source : " + dataSourceName);
        }
    }


    /**
     * Gets the connect db connection.
     *
     * @return the connect db connection
     * @throws SQLException                 the SQL exception
     * @throws CommonAuthenticatorException the authenticator exception
     */
    private static Connection getConnectDBConnection() throws SQLException, NamingException {
        initializeConnectDatasource();

        if (mConnectDatasource != null) {
            return mConnectDatasource.getConnection();
        }
        throw new SQLException("Connect Datasource not initialized properly");
    }

    /**
     * Add user details in Back Channeling Scenario
     *
     * @param backChannelUserDetails BackChannelUserDetails
     */
    public static void addBackChannelUserDetails(BackChannelUserDetails backChannelUserDetails) throws
            ConfigurationException, CommonAuthenticatorException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String addUserDetailsQuery =
                "insert into backchannel_request_details(correlation_id,msisdn,notification_bearer_token," +
                        "notification_url,request_initiated_time) values(?,?,?,?,NOW());";

        try {
            connection = getConnectDBConnection();

            if (log.isDebugEnabled()) {
                log.debug("Executing the query " + addUserDetailsQuery);
            }

            preparedStatement = connection.prepareStatement(addUserDetailsQuery);
            preparedStatement.setString(1, backChannelUserDetails.getCorrelationId());
            preparedStatement.setString(2, backChannelUserDetails.getMsisdn());
            preparedStatement.setString(3, backChannelUserDetails.getNotificationBearerToken());
            preparedStatement.setString(4, backChannelUserDetails.getNotificationUrl());
            preparedStatement.execute();

        } catch (SQLException e) {
            handleException(
                    "Error occurred while inserting user details for : " + backChannelUserDetails.getMsisdn() + "in " +
                            "BackChannel Scenario.",
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection);
        }
    }

    /**
     * Update user details in Back Channeling Scenario : update Session ID
     *
     * @param sessionId     ID of the session
     * @param correlationId unique ID of the user
     */
    public static void updateSessionIdInBackChannel(String correlationId, String sessionId) throws
            ConfigurationException, CommonAuthenticatorException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String updateUserDetailsQuery = null;

        updateUserDetailsQuery =
                "update backchannel_request_details set session_id=? where correlation_id=?;";

        try {
            connection = getConnectDBConnection();

            if (log.isDebugEnabled()) {
                log.debug("Executing the query " + updateUserDetailsQuery);
            }

            preparedStatement = connection.prepareStatement(updateUserDetailsQuery);
            preparedStatement.setString(1, sessionId);
            preparedStatement.setString(2, correlationId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            handleException(
                    "Error occurred while updating user details for : " + correlationId + "in " +
                            "BackChannel Scenario.",
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection);
        }
    }

    /**
     * Update user details in Back Channeling Scenario : update Oauthcode and Accesstoken
     *
     * @param code          Auth code
     * @param correlationId unique ID of the user
     * @param token         Access Token
     */
    public static void updateCodeAndTokenInBackChannel(String correlationId, String code, String token) throws
            ConfigurationException, CommonAuthenticatorException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String updateUserDetailsQuery = null;

        updateUserDetailsQuery =
                "update backchannel_request_details set access_token=?,auth_code=? where correlation_id=?;";

        try {
            connection = getConnectDBConnection();

            if (log.isDebugEnabled()) {
                log.debug("Executing the query " + updateUserDetailsQuery);
            }

            preparedStatement = connection.prepareStatement(updateUserDetailsQuery);
            preparedStatement.setString(1, token);
            preparedStatement.setString(2, code);
            preparedStatement.setString(3, correlationId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            handleException(
                    "Error occurred while updating user details for : " + correlationId + "in " +
                            "BackChannel Scenario.",
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection);
        }
    }

    /**
     * Get user details in Back Channeling Scenario using sessionID
     *
     * @param sessionId Id of the session
     */
    public static BackChannelUserDetails getBackChannelUserDetails(String sessionId) throws ConfigurationException,
            CommonAuthenticatorException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        BackChannelUserDetails backChannelUserDetails = null;
        ResultSet resultSet = null;

        String getUserDetailsQuery =
                "select * from backchannel_request_details where session_id=?";

        try {
            connection = getConnectDBConnection();

            if (log.isDebugEnabled()) {
                log.debug("Executing the query " + getUserDetailsQuery);
            }

            preparedStatement = connection.prepareStatement(getUserDetailsQuery);
            preparedStatement.setString(1, sessionId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                backChannelUserDetails = new BackChannelUserDetails();
                backChannelUserDetails.setSessionId(resultSet.getString("session_id"));
                backChannelUserDetails.setNotificationUrl(resultSet.getString("notification_url"));
                backChannelUserDetails.setNotificationBearerToken(resultSet.getString("notification_bearer_token"));
                backChannelUserDetails.setAccessToken(resultSet.getString("aceess_token"));
                backChannelUserDetails.setAuthCode(resultSet.getString("auth_code"));
                backChannelUserDetails.setMsisdn(resultSet.getString("msisdn"));
                backChannelUserDetails.setRequestIniticatedTime(resultSet.getString("request_initiated_time"));
            }
        } catch (SQLException e) {
            handleException(
                    "Error occurred while getting user related details for session: " + sessionId + "in BackChannel " +
                            "Scenario.",
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }

        return backChannelUserDetails;
    }

    /**
     * Update token details in Back Channeling Scenario : update access_token,refresh_token,scope etc
     *
     * @param correlationId          unique ID of the user
     * @param backChannelUserDetails Access Token
     */
    public static void updateTokenDetailsInBackChannel(String correlationId, BackChannelUserDetails
            backChannelUserDetails) throws
            ConfigurationException, CommonAuthenticatorException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String updateUserDetailsQuery = null;

        updateUserDetailsQuery =
                "update backchannel_request_details set refresh_token=?,scope=?,id_token=?,token_type=?,expires_in=? " +
                        "where correlation_id=?;";

        try {
            connection = getConnectDBConnection();

            if (log.isDebugEnabled()) {
                log.debug("Executing the query " + updateUserDetailsQuery);
            }

            preparedStatement = connection.prepareStatement(updateUserDetailsQuery);
            preparedStatement.setString(1, backChannelUserDetails.getRefreshToken());
            preparedStatement.setString(2, backChannelUserDetails.getScope());
            preparedStatement.setString(3, backChannelUserDetails.getIdToken());
            preparedStatement.setString(4, backChannelUserDetails.getTokenType());
            preparedStatement.setInt(5, backChannelUserDetails.getExpiresIn());
            preparedStatement.setString(6, correlationId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            handleException(
                    "Error occurred while updating token details for : " + correlationId + "in " +
                            "BackChannel Scenario.",
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection);
        }
    }

    private static void closeAllConnections(PreparedStatement preparedStatement,
                                            Connection connection, ResultSet resultSet) {
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
        closeConnection(connection);
    }

    private static void closeAllConnections(PreparedStatement preparedStatement,
                                            Connection connection) {
        closeStatement(preparedStatement);
        closeConnection(connection);
    }

    /**
     * Close Connection
     *
     * @param dbConnection Connection
     */
    private static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close database connection. Continuing with others. - " + e
                        .getMessage(), e);
            }
        }
    }

    /**
     * Close ResultSet
     *
     * @param resultSet ResultSet
     */
    private static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close ResultSet  - " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close PreparedStatement
     *
     * @param preparedStatement PreparedStatement
     */
    private static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close PreparedStatement. Continuing with others. - " + e
                        .getMessage(), e);
            }
        }
    }

    /**
     * Handle exception.
     *
     * @param msg the msg
     * @param t   the t
     * @throws CommonAuthenticatorException the authenticator exception
     */
    private static void handleException(String msg, Throwable t) throws CommonAuthenticatorException {
        log.error(msg, t);
        throw new CommonAuthenticatorException(msg, t);
    }

}