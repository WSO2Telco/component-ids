/** *****************************************************************************
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
 ***************************************************************************** */
package com.wso2telco.serviceprovider.provision.util;

import com.wso2telco.serviceprovider.provision.util.conn.ApimgtConnectionUtil;
import com.wso2telco.serviceprovider.provision.util.conn.ConnectdbConnectionUtil;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbUtils {

    private static int appId = -1;

    public static String activateApplication(Connection conn, String appName) throws SQLException {

        CallableStatement callablestatement = null;
        String message;

        try {
            String SQL = "{call populate_am_database_procedure (?,?)}";
            callablestatement = conn.prepareCall(SQL);
            callablestatement.setString(1, appName);
            callablestatement.registerOutParameter(2, java.sql.Types.INTEGER);
            callablestatement.execute();
            appId = callablestatement.getInt(2);
            message = "{error: false, message: 'success'}";

        } catch (SQLException e) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE,
                    "SQL Exception occurred when activating the application:" + e.getMessage(), e);
            message = "{error: true, message: \"Failed to call activate application procedure - " +
                    e.getMessage() + "\"}";
        } finally {
            conn.close();
        }
        return message;

    }

    public static String updateApplication(Connection conn, String appName) throws SQLException {

        CallableStatement callablestatement = null;
        String message;

        try {
            String SQL = "{call populate_axiata_database_procedure (?)}";
            callablestatement = conn.prepareCall(SQL);

            if (appId == -1) {
                getAppIdfromAmDatabase(appName);
            }
            callablestatement.setInt(1, appId);
            callablestatement.execute();
            message = "{error: false, message: 'success'}";

        } catch (SQLException e) {
            message = "{error: true, message: \"Failed to update application - " + e.getMessage() + "\"}";

        } finally {
            conn.close();
        }
        return message;
    }

    public static String updateSubscriptions(Connection conn) throws SQLException {

        CallableStatement callablestatement = null;
        String message;

        try {
            String SQL = "{call update_subscription_procedure (?)}";
            callablestatement = conn.prepareCall(SQL);
            callablestatement.setInt(1, appId);
            callablestatement.execute();
            message = "{error: false, message: 'success'}";

        } catch (SQLException e) {
            message = "{error: true, message: \"Failed to update subscriptions - " + e.getMessage() + "\"}";

        } finally {
            conn.close();
        }
        return message;
    }

    public static String populateSubscriptionValidator(Connection conn) throws SQLException {

        CallableStatement callablestatement = null;
        String message;

        try {
            String SQL = "{call populate_subscription_validator_procedure (?)}";
            callablestatement = conn.prepareCall(SQL);
            callablestatement.setInt(1, appId);
            callablestatement.execute();
            message = "{error: false, message: 'success'}";

        } catch (SQLException e) {
            message = "{error: true, message: \"Failed to populate subscription validator - " + e.getMessage() + "\"}";

        } finally {
            conn.close();
        }
        return message;
    }

    public static String scopeConfiguration(Connection conn, String consumerKey, String[] scopeList) throws SQLException {

        CallableStatement callablestatement = null;
        String message = "Failure";

        try {
            for (int i = 0; i < scopeList.length; i++) {
                String SQL = "{call populate_sp_config_procedure(?,?)}";
                callablestatement = conn.prepareCall(SQL);
                callablestatement.setString(1, consumerKey);
                callablestatement.setString(2, scopeList[i]);
                callablestatement.execute();
                message = "Success";
            }

        } catch (SQLException e) {
            message = "Failure:" + e.toString();

        } finally {
            conn.close();
        }
        return message;
    }

    public static String trustedStatusCofiguration(Connection conn, String consumerKey) throws SQLException {

        CallableStatement callablestatement = null;
        String message;

        try {
            String SQL = "{call populate_trusted_status_procedure(?)}";
            callablestatement = conn.prepareCall(SQL);
            callablestatement.setString(1, consumerKey);
            callablestatement.execute();
            message = "Success";

        } catch (SQLException e) {
            message = "Failure:" + e.toString();

        } finally {
            conn.close();
        }
        return message;
    }

    public static String updateClientAndSecretKeys(String consumerKeyOld, String consumerKeyNew, String secretKeyOld, String secretKeyNew, String accessToken) throws SQLException {

        String message;
        String resultsetValueOld, resultsetValueNew;

        resultsetValueOld = check_for_consumer_key_availability(consumerKeyOld);
        resultsetValueNew = check_for_consumer_key_availability(consumerKeyNew);

        if (resultsetValueOld.equals(consumerKeyOld) && resultsetValueNew == null) {
            if (update_SP_INBOUND_AUTH_table(consumerKeyOld, consumerKeyNew, secretKeyOld, secretKeyNew) > 0) {
                if (update_AM_APPLICATION_KEY_MAPPING_table(consumerKeyOld, consumerKeyNew) > 0) {
                    if (update_AM_APP_KEY_DOMAIN_MAPPING_table(consumerKeyOld) > 0) {
                        if (update_IDN_OAUTH2_ACCESS_TOKEN_table(consumerKeyOld) > 0) {
                            if (update_IDN_OAUTH2_AUTHORIZATION_CODE_table(consumerKeyOld) > 0) {
                                if (update_IDN_OAUTH_CONSUMER_APPS_table(consumerKeyOld, consumerKeyNew, secretKeyNew) > 0) {
                                    if (update_tempkey_in_AM_APP_KEY_DOMAIN_MAPPING_table(consumerKeyNew) > 0) {
                                        if (update_tempkey_in_IDN_OAUTH2_ACCESS_TOKEN_table(consumerKeyNew) > 0) {
                                            if (update_tempkey_in_IDN_OAUTH2_AUTHORIZATION_CODE_table(consumerKeyNew) > 0) {
                                                if (update_sp_token_table(accessToken, consumerKeyOld, consumerKeyNew) > 0) {
                                                    if (update_sp_configuration_table(consumerKeyOld, consumerKeyNew) > 0) {
                                                        message = "Success";
                                                    } else {
                                                        message = "Failure in updating Sp Configuration Table";
                                                    }
                                                } else {
                                                    message = "Failure in updating Sp Token Table";
                                                }
                                            } else {
                                                message = "Failure in updating IDN_OAUTH2_AUTHORIZATION_CODE_table";
                                            }
                                        } else {
                                            message = "Failure in updating IDN_OAUTH2_ACCESS_TOKEN_table";
                                        }
                                    } else {
                                        message = "Failure in updating AM_APP_KEY_DOMAIN_MAPPING_table";
                                    }
                                } else {
                                    message = "Failure in updating IDN_OAUTH_CONSUMER_APPS_table";
                                }

                            } else {
                                message = "Failure in updating IDN_OAUTH2_AUTHORIZATION_CODE_table";
                            }
                        } else {
                            message = "Failure in updating IDN_OAUTH2_ACCESS_TOKEN_table";
                        }
                    } else {
                        message = "Failure in updating AM_APP_KEY_DOMAIN_MAPPING_table";
                    }
                } else {
                    message = "Failure in updating AM_APPLICATION_KEY_MAPPING_table";
                }
            } else {
                message = "Failure in updating SP_INBOUND_AUTH_table";
            }
        } else {
            message = "Failure :Old Consumer key is not available or requested new Consumer Key is already available";
        }

        return message;
    }

    private static String check_for_consumer_key_availability(String consumerKey) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String outputValue = null;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            String sqlQuery = "select * from IDN_OAUTH_CONSUMER_APPS where IDN_OAUTH_CONSUMER_APPS.CONSUMER_KEY=?;";

            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKey);
            resultSet = statement.executeQuery();

            if (resultSet.wasNull()) {
                outputValue = null;
            } else {
                if (resultSet.next()) {
                    outputValue = resultSet.getString("CONSUMER_KEY");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, "SQL Exception occured when check for consumer key availability:" + ex.getMessage(), ex);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, "Class Not Found Error occured when check for consumer key availability:" + ex.getMessage(), ex);
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }

        return outputValue;
    }

    private static int update_SP_INBOUND_AUTH_table(String consumerKeyOld, String consumerKeyNew, String secretKeyOld, String secretKeyNew) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery, tempValue = null;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from SP_INBOUND_AUTH where SP_INBOUND_AUTH.INBOUND_AUTH_KEY=? and SP_INBOUND_AUTH.PROP_VALUE=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            statement.setString(2, secretKeyOld);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                tempValue = resultSet.getString("INBOUND_AUTH_KEY");
            }

            if (tempValue.equals(consumerKeyOld)) {

                sqlQuery = "update SP_INBOUND_AUTH set SP_INBOUND_AUTH.INBOUND_AUTH_KEY=?, SP_INBOUND_AUTH.PROP_VALUE=? where SP_INBOUND_AUTH.INBOUND_AUTH_KEY=? and SP_INBOUND_AUTH.PROP_VALUE=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                statement.setString(2, secretKeyNew);
                statement.setString(3, consumerKeyOld);
                statement.setString(4, secretKeyOld);
                status = statement.executeUpdate();

                conn.commit();

            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return status;
    }

    private static int update_AM_APPLICATION_KEY_MAPPING_table(String consumerKeyOld, String consumerKeyNew) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from AM_APPLICATION_KEY_MAPPING where AM_APPLICATION_KEY_MAPPING.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update AM_APPLICATION_KEY_MAPPING set AM_APPLICATION_KEY_MAPPING.CONSUMER_KEY=? where AM_APPLICATION_KEY_MAPPING.CONSUMER_KEY=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                statement.setString(2, consumerKeyOld);
                status = statement.executeUpdate();
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return status;
    }

    private static int update_AM_APP_KEY_DOMAIN_MAPPING_table(String consumerKeyOld) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from AM_APP_KEY_DOMAIN_MAPPING where AM_APP_KEY_DOMAIN_MAPPING.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update AM_APP_KEY_DOMAIN_MAPPING set AM_APP_KEY_DOMAIN_MAPPING.CONSUMER_KEY='tempConsumerKey' where AM_APP_KEY_DOMAIN_MAPPING.CONSUMER_KEY=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyOld);
                status = statement.executeUpdate();
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return status;
    }

    private static int update_IDN_OAUTH2_ACCESS_TOKEN_table(String consumerKeyOld) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from IDN_OAUTH2_ACCESS_TOKEN where IDN_OAUTH2_ACCESS_TOKEN.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update IDN_OAUTH2_ACCESS_TOKEN set IDN_OAUTH2_ACCESS_TOKEN.CONSUMER_KEY='tempConsumerKey' where IDN_OAUTH2_ACCESS_TOKEN.CONSUMER_KEY=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyOld);
                status = statement.executeUpdate();
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }

        return status;
    }

    private static int update_IDN_OAUTH2_AUTHORIZATION_CODE_table(String consumerKeyOld) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from IDN_OAUTH2_AUTHORIZATION_CODE where IDN_OAUTH2_AUTHORIZATION_CODE.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update IDN_OAUTH2_AUTHORIZATION_CODE set IDN_OAUTH2_AUTHORIZATION_CODE.CONSUMER_KEY='tempConsumerKey' where IDN_OAUTH2_AUTHORIZATION_CODE.CONSUMER_KEY=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyOld);
                status = statement.executeUpdate();
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return status;
    }

    private static int update_IDN_OAUTH_CONSUMER_APPS_table(String consumerKeyOld, String consumerKeyNew, String consumerSecretNew) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from IDN_OAUTH_CONSUMER_APPS where IDN_OAUTH_CONSUMER_APPS.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update IDN_OAUTH_CONSUMER_APPS set IDN_OAUTH_CONSUMER_APPS.CONSUMER_KEY=? , IDN_OAUTH_CONSUMER_APPS.CONSUMER_SECRET=? where IDN_OAUTH_CONSUMER_APPS.CONSUMER_KEY=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                statement.setString(2, consumerSecretNew);
                statement.setString(3, consumerKeyOld);
                status = statement.executeUpdate();
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return status;
    }

    private static int update_tempkey_in_AM_APP_KEY_DOMAIN_MAPPING_table(String consumerKeyNew) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from AM_APP_KEY_DOMAIN_MAPPING where AM_APP_KEY_DOMAIN_MAPPING.CONSUMER_KEY='tempConsumerKey'";
            statement = conn.prepareStatement(sqlQuery);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update AM_APP_KEY_DOMAIN_MAPPING set AM_APP_KEY_DOMAIN_MAPPING.CONSUMER_KEY=? where AM_APP_KEY_DOMAIN_MAPPING.CONSUMER_KEY='tempConsumerKey'";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                status = statement.executeUpdate();
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return status;
    }

    private static int update_tempkey_in_IDN_OAUTH2_ACCESS_TOKEN_table(String consumerKeyNew) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from IDN_OAUTH2_ACCESS_TOKEN where IDN_OAUTH2_ACCESS_TOKEN.CONSUMER_KEY='tempConsumerKey'";
            statement = conn.prepareStatement(sqlQuery);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update IDN_OAUTH2_ACCESS_TOKEN set IDN_OAUTH2_ACCESS_TOKEN.CONSUMER_KEY=? where IDN_OAUTH2_ACCESS_TOKEN.CONSUMER_KEY='tempConsumerKey'";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                status = statement.executeUpdate();
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return status;
    }

    private static int update_tempkey_in_IDN_OAUTH2_AUTHORIZATION_CODE_table(String consumerKeyNew) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from IDN_OAUTH2_AUTHORIZATION_CODE where IDN_OAUTH2_AUTHORIZATION_CODE.CONSUMER_KEY='tempConsumerKey'";

            statement = conn.prepareStatement(sqlQuery);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update IDN_OAUTH2_AUTHORIZATION_CODE set IDN_OAUTH2_AUTHORIZATION_CODE.CONSUMER_KEY=? where IDN_OAUTH2_AUTHORIZATION_CODE.CONSUMER_KEY='tempConsumerKey'";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                status = statement.executeUpdate();
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return status;
    }

    private static int update_sp_token_table(String accessToken, String consumerKeyOld, String consumerKeyNew) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection(); //AxiatadbConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from sp_token where sp_token.consumer_key=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update sp_token set sp_token.consumer_key=? where sp_token.consumer_key=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                statement.setString(2, consumerKeyOld);
                status = statement.executeUpdate();
                conn.commit();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }

        return status;
    }

    private static int update_sp_configuration_table(String consumerKeyOld, String consumerKeyNew) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ConnectdbConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "SELECT * from sp_configuration where client_id = ?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update sp_configuration set sp_configuration.client_id=? where sp_configuration.client_id=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                statement.setString(2, consumerKeyOld);
                status = statement.executeUpdate();
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return status;
    }

    private static int getAppIdfromAmDatabase(String appName) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            sqlQuery = "select APPLICATION_ID from AM_APPLICATION where NAME= ?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, appName);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                appId = resultSet.getInt("APPLICATION_ID");
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            appId = -1;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                appId = -1;
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return appId;
    }

    public static void insertValuesToAmDatabases(String appName, String consumerKey, String secretKey, String accessToken) {
        try {
            insert_AM_APPLICATION_KEY_MAPPING_table(appName, consumerKey);
            insert_AM_APP_KEY_DOMAIN_MAPPING_table(consumerKey);
            insert_sp_token_table(accessToken, consumerKey);

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static int insert_AM_APPLICATION_KEY_MAPPING_table(String appName, String consumerKey) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1, appId;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from AM_APPLICATION_KEY_MAPPING where AM_APPLICATION_KEY_MAPPING.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKey);
            resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                appId = getAppIdfromAmDatabase(appName);
                sqlQuery = "insert into AM_APPLICATION_KEY_MAPPING(APPLICATION_ID,CONSUMER_KEY,KEY_TYPE,STATE) values(?,?,'PRODUCTION','COMPLETED');";
                statement = conn.prepareStatement(sqlQuery);
                statement.setInt(1, appId);
                statement.setString(2, consumerKey);
                status = statement.executeUpdate();
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return status;
    }

    private static int insert_AM_APP_KEY_DOMAIN_MAPPING_table(String consumerKey) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from AM_APP_KEY_DOMAIN_MAPPING where AM_APP_KEY_DOMAIN_MAPPING.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKey);
            resultSet = statement.executeQuery();

            if (!resultSet.next()) {

                sqlQuery = "insert into rs_dbApiMgt.AM_APP_KEY_DOMAIN_MAPPING(CONSUMER_KEY,AUTHZ_DOMAIN) values(?,'ALL');";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKey);
                status = statement.executeUpdate();
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return status;
    }

    private static int insert_sp_token_table(String accessToken, String consumerKey) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from sp_token where sp_token.consumer_key=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKey);
            resultSet = statement.executeQuery();

            if (!resultSet.next()) {

                sqlQuery = "insert into sp_token(consumer_key, token) values (?, ?)";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKey);
                statement.setString(2, accessToken);
                status = statement.executeUpdate();
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbUtils.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return status;
    }
}
