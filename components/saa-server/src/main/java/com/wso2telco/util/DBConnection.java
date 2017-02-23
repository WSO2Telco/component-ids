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

    protected DBConnection() throws SQLException, ClassNotFoundException, DBUtilException {

    }

    public Connection getConnection() throws SQLException, DBUtilException {
        Connection connection = DbUtils.getConnectDbConnection();
        return connection;
    }

    public static DBConnection getInstance() throws ClassNotFoundException {
        if (instance == null) {
            try {
                instance = new DBConnection();
            } catch (SQLException | DBUtilException e) {
                logger.error("Error occurred while getting connection");
            }
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
     * @return int indicating the transaction is success or failure
     */
    public boolean addClient(String clientDeviceId, String platform, String pushToken, String msisdn) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String query = "INSERT INTO clients (client_device_id,platform,push_token,date_time,msisdn) VALUES ('" + clientDeviceId + "','" + platform + "','" + pushToken + "','" + timeStamp + "','" + msisdn + "');";
        return executeUpdate(query);
    }

    /**
     * Check the availability of the clients for a given clientID .
     *
     * @param msisdn mobile number
     * @return int indicating the transaction is success ,failure or error
     */
    public int isExist(String msisdn) {
        final int UNREGISTEREDCLIENT = 1;
        final int REGISTEREDCLIENT = 0;
        final int ERROR = 2;

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String query = "SELECT * FROM clients where msisdn=" + msisdn + ";";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery(query);

            if (!resultSet.next()) {
                return UNREGISTEREDCLIENT;
            } else {
                return REGISTEREDCLIENT;
            }

        } catch (SQLException e) {
            logger.info("SQLException occurred " + e);
            return ERROR;
        } catch (DBUtilException e) {
            logger.info("DBUtilException occurred " + e);
            return ERROR;
        } finally {
            close(connection, statement, resultSet);
        }
    }

    /**
     * get the clientID for the given msisdn .
     *
     * @param msisdn mobile number
     * @return clientID
     */
    public String[] getClientDetails(String msisdn) {

        logger.info("getclientdetails");
        String[] clientDetails = new String[3];
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        String query = "SELECT client_device_id,platform,push_token FROM clients WHERE msisdn='" + msisdn + "';";
        try {
            connection = getConnection();
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery(query);

            if(connection !=null)
                logger.error("connection:not null");
            else
                logger.error("connection:null ");

            if(statement  !=null)
                logger.error("statement :not null");
            else
                logger.error("statement :null ");

            if(resultSet  !=null)
                logger.error("resultSet :not null");
            else
                logger.error("resultSet :null ");

            if (resultSet != null) {
                logger.info("Result set: "+resultSet.toString());
                if (resultSet.next()) {
                    logger.info(resultSet.getString(1));
                    logger.info(resultSet.getString(2));
                    logger.info(resultSet.getString(3));

                    clientDetails[0] = resultSet.getString(1);
                    logger.info(clientDetails[0]);
                    clientDetails[1] = resultSet.getString(2);
                    logger.info(clientDetails[1]);
                    clientDetails[2] = resultSet.getString(3);
                    logger.info(clientDetails[2]);
//                    clientDetails[0] = resultSet.getString(1);
//                    logger.info(clientDetails[0]);
//                    clientDetails[1] = resultSet.getString(2);
//                    clientDetails[2] = resultSet.getString(3);
                    return clientDetails;
                } else {
                    logger.error("Error occurred Result Set is null!!!!");
                    //throw new EmptyResultSetException("Result set is empty");
                    return clientDetails;
                }
            } else {
                logger.error("Error occurred Result Set is null!!!!");
                return clientDetails;
            }
        } catch (SQLException e) {
            logger.error("SQLException occurred !!!" + e);
            return clientDetails;
        } catch (DBUtilException e) {
            logger.error("DBUtilException occurred !!!" + e);
            return clientDetails;
        } catch (Exception e){
            logger.error("Exception occurred !!!" + e);
            return clientDetails;
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
     * @return int indicating the transaction is success ,failure or
     */
    public boolean authenticateClient(String refID, String clientDeviceId, String message) {

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

        String query = "INSERT INTO messages (ref_id,client_device_id,message,req_date_time,status)VALUES ('" + refID + "','" + clientDeviceId + "','" + message + "','" + timeStamp + "','P');";
        return executeUpdate(query);
    }

    /**
     * Update message table after send the push notification
     *
     * @param refID  ref id
     * @param status status
     * @return int indicating the transaction is success ,failure or
     */
    public boolean updateMessageTable(String refID, char status) {

        String query = "UPDATE messages SET status='" + status + "' where ref_id='" + refID + "';";
        return executeUpdate(query);
    }

    /**
     * Delete already registered clients form the db
     *
     * @param msisdn mobile number
     * @return int indicating the deletion is success ,failure or
     */
    public boolean removeClient(String msisdn) {
        String query = "DELETE FROM clients WHERE msisdn='" + msisdn + "';";
        return executeUpdate(query);
    }

    private boolean executeUpdate(String query) {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(query);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("SQLException occurred " + e);
            return false;
        } catch (DBUtilException e) {
            logger.error("DBUtilException occurred " + e);
            return false;
        } finally {
            close(connection, statement, resultSet);
        }
    }

    /**
     * Close the database connection.
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


