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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DBConnection {

    private static Log logger = LogFactory.getLog(DBConnection.class);
    private static DBConnection instance = null;

    String connectionURL;
    String dbUser;
    String dbPassword;
    private Log log = LogFactory.getLog(DBConnection.class);
    private Connection connection = null;
    private PreparedStatement statement;
    private ResultSet resultSet;


    protected DBConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        connectionURL = PropertyReader.getProperty("saa.server.jdbcUrl");
        dbUser = PropertyReader.getProperty("saa.server.dbUser");
        dbPassword = PropertyReader.getProperty("saa.server.dbPassword");
        connection = DriverManager.getConnection(connectionURL, dbUser, dbPassword);
    }

    public static DBConnection getInstance() throws ClassNotFoundException {
        if (instance == null) {
            try {
                instance = new DBConnection();
            } catch (SQLException e) {
                logger.info(e.getMessage());
            }
        }
        return instance;
    }

    /**
     * Register clients in the Database.
     *
     * @param clientDeviceId device id
     * @param platform platform
     * @param pushToken push token
     * @param msisdn mobile number
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

        String query = "SELECT * FROM clients where msisdn=" + msisdn + ";";
        try {
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery(query);

            if (!resultSet.next()) {
                return UNREGISTEREDCLIENT;
            } else {
                return REGISTEREDCLIENT;
            }

        } catch (SQLException ex) {
            log.info(ex.getMessage());
            return ERROR;
        }
    }

    /**
     * get the clientID for the given msisdn .
     *
     * @param msisdn mobile number
     * @return clientID
     */
    public String[] getClientDetails(String msisdn) {
        String clientDetails[] = new String[3];

        String query = "SELECT client_device_id,platform,push_token FROM clients WHERE msisdn='" + msisdn + "';";
        try {
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                clientDetails[0] = resultSet.getString(1);
                clientDetails[1] = resultSet.getString(2);
                clientDetails[2] = resultSet.getString(3);
                return clientDetails;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            log.info(ex.getMessage());
            return null;
        }
    }

    /**
     * Check the authentication of the client and get the message from the client.
     *
     * @param clientDeviceId device id of the client
     * @param refID ref id
     * @param message message
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
     * @param refID ref id
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
        try {
            statement = connection.prepareStatement(query);
            statement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Close the database connection.
     */
    private void close() {
        try {
            if (resultSet != null)
                resultSet.close();
            if (statement != null)
                statement.close();
            if (connection != null)
                connection.close();
        } catch (Exception e) {
        }
    }
}
