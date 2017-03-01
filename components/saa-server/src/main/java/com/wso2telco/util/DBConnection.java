/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.util;

import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.core.dbutils.DbUtils;
import com.wso2telco.entity.ClientDetails;
import com.wso2telco.exception.EmptyResultSetException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DBConnection {

    private static Log logger = LogFactory.getLog(DBConnection.class);
    private static DBConnection instance = null;

    protected DBConnection() {

    }

    public Connection getConnection() throws SQLException, DBUtilException {
        Connection connection = DbUtils.getConnectDbConnection();
        return connection;
    }

    public static DBConnection getInstance() throws ClassNotFoundException {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Register clients in the Database.
     *
     * @param clientDeviceId device id
     * @param platform       platform
     * @param pushToken      push token
     * @param msisdn         mobile number
     * @throws SQLException SQLException
     * @throws DBUtilException DbUtilException
     */
    public void addClient(String clientDeviceId, String platform, String pushToken, String msisdn) throws SQLException, DBUtilException {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String query = "INSERT INTO clients (client_device_id,platform,push_token,date_time,msisdn) VALUES ('" + clientDeviceId + "','" + platform + "','" + pushToken + "','" + timeStamp + "','" + msisdn + "');";
        executeUpdate(query);
    }

    /**
     * Check the availability of the clients for a given clientID .
     *
     * @param msisdn mobile number
     * @return boolean indicating the transaction is success ,failure or error
     * @throws SQLException SQLException
     * @throws DBUtilException DBUtilsException
     */
    public boolean isExist(String msisdn) throws SQLException, DBUtilException {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String query = "SELECT * FROM clients where msisdn=" + msisdn + ";";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery(query);

            if (!resultSet.next()) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            logger.info("SQLException occurred " + e);
            throw new SQLException(e.getMessage(), e);
        } catch (DBUtilException e) {
            logger.info("DBUtilException occurred " + e);
            throw new DBUtilException(e.getMessage(), e);
        } finally {
            close(connection, statement, resultSet);
        }
    }

    /**
     * get the clientID for the given msisdn .
     *
     * @param msisdn mobile number
     * @return clientID
     * @throws EmptyResultSetException EmptyResultSetException
     * @throws SQLException SQLException
     * @throws DBUtilException DBUtilException
     */
    public ClientDetails getClientDetails(String msisdn) throws EmptyResultSetException, SQLException, DBUtilException {

        logger.info("getting client details");
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        ClientDetails clientDetails = new ClientDetails();

        String query = "SELECT client_device_id,platform,push_token FROM clients WHERE msisdn='" + msisdn + "';";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery(query);

            logger.info("Result set: " + resultSet.toString());
            if (resultSet.next()) {
                logger.info(resultSet.getString(1));
                logger.info(resultSet.getString(2));
                logger.info(resultSet.getString(3));

                clientDetails.setDeviceId(resultSet.getString("client_device_id"));
                clientDetails.setPlatform(resultSet.getString("platform"));
                clientDetails.setPushToken(resultSet.getString("push_token"));
                return clientDetails;
            } else {
                logger.error("Error occurred Result Set is null!!!!");
                throw new EmptyResultSetException("Result Set is empty");
            }
        } catch (SQLException e) {
            logger.error("SQLException occurred !!!" + e);
            throw new SQLException(e.getMessage(), e);
        } catch (DBUtilException e) {
            logger.error("DBUtilException occurred !!!" + e);
            throw new DBUtilException(e.getMessage(), e);
        } finally {
            close(connection, statement, resultSet);
        }
    }

    /**
     * Check the authentication of the client and get the message from the client.
     *
     * @param clientDeviceId device id of the client
     * @param refID          ref id
     * @param message        message
     * @throws SQLException SQLException
     * @throws DBUtilException DBUtilException
     */
    public void authenticateClient(String refID, String clientDeviceId, String message) throws SQLException, DBUtilException {

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        String query = "INSERT INTO messages (ref_id,client_device_id,message,req_date_time,status)VALUES ('" + refID + "','" + clientDeviceId + "','" + message + "','" + timeStamp + "','P');";
        executeUpdate(query);
    }

    /**
     * Update message table after send the push notification
     *
     * @param refID  ref id
     * @param status status
     * @throws SQLException SQLException
     * @throws DBUtilException DBUtilException
     */
    public void updateMessageTable(String refID, char status) throws SQLException, DBUtilException {
        String query = "UPDATE messages SET status='" + status + "' where ref_id='" + refID + "';";
        executeUpdate(query);
    }

    /**
     * Delete already registered clients form the db
     *
     * @param msisdn mobile number
     * @throws SQLException SQLException
     * @throws DBUtilException DBUtilException
     */
    public void removeClient(String msisdn) throws SQLException, DBUtilException {
        String query = "DELETE FROM clients WHERE msisdn='" + msisdn + "';";
        executeUpdate(query);
    }

    /**
     * Execute the query
     *
     * @param query query to execute
     * @throws SQLException SQLException
     * @throws DBUtilException DBUtilException
     */
    private void executeUpdate(String query) throws SQLException, DBUtilException {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(query);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQLException occurred " + e);
            throw new SQLException(e);
        } catch (DBUtilException e) {
            logger.error("DBUtilException occurred " + e);
            throw new DBUtilException(e);
        } finally {
            close(connection, statement, resultSet);
        }
    }

    /**
     * Close the database connection.
     * @param connection Connection instance used by the method call
     * @param statement prepared Statement used by the method call
     * @param resultSet result set which is used by the method call
     */
    public void close(Connection connection, PreparedStatement statement, ResultSet resultSet) {

        try {
            if (resultSet != null)
                resultSet.close();
            if (statement != null)
                statement.close();
            if (connection != null)
                connection.close();
        } catch (Exception e) {
            logger.error("Error occurred " + e);
        }
    }
}


