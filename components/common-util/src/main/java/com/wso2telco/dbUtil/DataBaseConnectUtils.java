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
import com.wso2telco.model.BackChannelRequestDetails;
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
     * @param backChannelUserDetails BackChannelRequestDetails
     */
    public static void addBackChannelRequestDetails(BackChannelRequestDetails backChannelUserDetails) throws
            ConfigurationException, CommonAuthenticatorException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String addUserDetailsQuery =
                "insert into backchannel_request_details(correlation_id,msisdn,notification_bearer_token," +
                        "notification_url,request_initiated_time,client_id,redirect_url,auth_requested_id) values(?," +
                        "?,?,?,NOW(),?,?,?);";

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
            preparedStatement.setString(5, backChannelUserDetails.getClientId());
            preparedStatement.setString(6, backChannelUserDetails.getRedirectUrl());
            preparedStatement.setString(7, backChannelUserDetails.getAuthRequestId());

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
     * Update user details in Back Channeling Scenario : update oauth code
     *
     * @param code          Auth code
     * @param correlationId unique ID of the request
     */
    public static void updateCodeInBackChannel(String correlationId, String code) throws
            ConfigurationException, CommonAuthenticatorException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String updateUserDetailsQuery = null;

        updateUserDetailsQuery =
                "update backchannel_request_details set auth_code=? where correlation_id=?";

        try {
            connection = getConnectDBConnection();

            if (log.isDebugEnabled()) {
                log.debug("Executing the query " + updateUserDetailsQuery);
            }

            preparedStatement = connection.prepareStatement(updateUserDetailsQuery);
            preparedStatement.setString(1, code);
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
     * Update user details in Back Channeling Scenario : update oauth code
     *
     * @param correlationId unique ID of the request
     * @param token         access token
     */
    public static void updateTokenInBackChannel(String correlationId, String token) throws
            ConfigurationException, CommonAuthenticatorException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String updateUserDetailsQuery = "update backchannel_request_details set access_token=? where correlation_id=?";

        try {
            connection = getConnectDBConnection();

            if (log.isDebugEnabled()) {
                log.debug("Executing the query " + updateUserDetailsQuery);
            }

            preparedStatement = connection.prepareStatement(updateUserDetailsQuery);
            preparedStatement.setString(1, token);
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
     * Get user details in Back Channeling Scenario using sessionID
     *
     * @param sessionId Id of the session
     */
    public static BackChannelRequestDetails getBackChannelUserDetails(String sessionId) throws ConfigurationException,
            CommonAuthenticatorException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        BackChannelRequestDetails backChannelRequestDetails = null;
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

                backChannelRequestDetails = new BackChannelRequestDetails();
                backChannelRequestDetails.setCorrelationId(resultSet.getString("correlation_id"));
                backChannelRequestDetails.setSessionId(resultSet.getString("session_id"));
                backChannelRequestDetails.setNotificationUrl(resultSet.getString("notification_url"));
                backChannelRequestDetails.setNotificationBearerToken(resultSet.getString("notification_bearer_token"));
                backChannelRequestDetails.setAuthCode(resultSet.getString("auth_code"));
                backChannelRequestDetails.setMsisdn(resultSet.getString("msisdn"));
                backChannelRequestDetails.setRequestIniticatedTime(resultSet.getString("request_initiated_time"));
                backChannelRequestDetails.setAuthRequestId(resultSet.getString("auth_requested_id"));
                backChannelRequestDetails.setClientId(resultSet.getString("client_id"));
                backChannelRequestDetails.setRedirectUrl(resultSet.getString("redirect_url"));
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

        return backChannelRequestDetails;
    }
    
    /**
     * Get SP related configurations
     *
     * @param correlationId
     * @return
     * @throws ConfigurationException
     * @throws CommonAuthenticatorException
     */
    public static BackChannelRequestDetails getRequestDetailsById(String correlationId) throws ConfigurationException,
            CommonAuthenticatorException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        BackChannelRequestDetails backchannelRequestDetails = null;
        ResultSet resultSet = null;

        String getUserDetailsQuery = "select * FROM backchannel_request_details where correlation_id=?";

        try {
            connection = getConnectDBConnection();

            if (log.isDebugEnabled()) {
                log.debug("Executing the query " + getUserDetailsQuery);
            }

            preparedStatement = connection.prepareStatement(getUserDetailsQuery);
            preparedStatement.setString(1, correlationId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                backchannelRequestDetails = new BackChannelRequestDetails();
                backchannelRequestDetails.setSessionId(resultSet.getString("session_id"));
                backchannelRequestDetails.setAuthCode(resultSet.getString("auth_code"));
                backchannelRequestDetails.setCorrelationId(resultSet.getString("correlation_id"));
                backchannelRequestDetails.setMsisdn(resultSet.getString("msisdn"));
                backchannelRequestDetails.setNotificationBearerToken(resultSet.getString("notification_bearer_token"));
                backchannelRequestDetails.setNotificationUrl(resultSet.getString("notification_url"));
                backchannelRequestDetails.setClientId(resultSet.getString("client_id"));
            }
        } catch (SQLException e) {
            handleException(
                    "Error occurred while fetching SP related data for the Correlation Id: " + correlationId,
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }

        return backchannelRequestDetails;
    }


    /**
     * Get SP related configurations
     *
     * @param sessionId
     * @return
     * @throws ConfigurationException
     * @throws CommonAuthenticatorException
     */
    public static BackChannelRequestDetails getRequestDetailsBySessionId(String sessionId) throws ConfigurationException,
            CommonAuthenticatorException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        BackChannelRequestDetails backchannelRequestDetails = null;
        ResultSet resultSet = null;

        String getUserDetailsQuery = "select * FROM backchannel_request_details where session_id=?";

        try {
            connection = getConnectDBConnection();

            if (log.isDebugEnabled()) {
                log.debug("Executing the query " + getUserDetailsQuery);
            }

            preparedStatement = connection.prepareStatement(getUserDetailsQuery);
            preparedStatement.setString(1, sessionId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                backchannelRequestDetails = new BackChannelRequestDetails();
                backchannelRequestDetails.setSessionId(resultSet.getString("session_id"));
                backchannelRequestDetails.setAuthCode(resultSet.getString("auth_code"));
                backchannelRequestDetails.setCorrelationId(resultSet.getString("correlation_id"));
                backchannelRequestDetails.setMsisdn(resultSet.getString("msisdn"));
                backchannelRequestDetails.setNotificationBearerToken(resultSet.getString("notification_bearer_token"));
                backchannelRequestDetails.setNotificationUrl(resultSet.getString("notification_url"));
                backchannelRequestDetails.setClientId(resultSet.getString("client_id"));
            }
        } catch (SQLException e) {
            handleException(
                    "Error occurred while fetching SP related data for the Session Id: " + sessionId,
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }

        return backchannelRequestDetails;
    }



    /**
     * Update token details in Back Channeling Scenario : update access_token,refresh_token,scope etc
     *
     * @param correlationId          unique ID of the user
     * @param backChannelUserDetails Access Token
     */
    public static void updateTokenDetailsInBackChannel(String correlationId, BackChannelRequestDetails
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

    /**
     * Check the scope is backChannel allowed
     *
     * @param scope scope value
     * @return allowed or not allowed
     * @throws ConfigurationException on errors
     */
    public static boolean isBackChannelAllowedScope(String scope) throws
            ConfigurationException, CommonAuthenticatorException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        boolean isBackChannelAllowed = false;

        String[] scopeValues = scope.split("\\s+|\\+");
        StringBuilder params = new StringBuilder("?");
        for (int i = 1; i < scopeValues.length; i++) {
            params.append(",?");
        }

        String sql = "SELECT is_backchannel_allowed FROM scope_parameter WHERE scope in (" + params + ") ;";

        if (log.isDebugEnabled()) {
            log.debug("Executing the query to check the scope is backChannel allowed: " + sql);
        }

        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < scopeValues.length; i++) {
                preparedStatement.setString(i + 1, scopeValues[i]);
            }

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                isBackChannelAllowed = resultSet.getBoolean("is_backchannel_allowed");

                if (!isBackChannelAllowed) {
                    isBackChannelAllowed = false;
                }
            }

        } catch (SQLException e) {
            handleException(
                    "Error occurred while checking the scope is back channel allowed - " + scope,
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }
        return isBackChannelAllowed;
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