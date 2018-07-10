/**
 * ****************************************************************************
 * Copyright  (c) 2015-2018, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 * <p>
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package com.wso2telco.spprovisionapp.utils;

import com.wso2telco.spprovisionapp.conn.ApimgtConnectionUtil;
import com.wso2telco.spprovisionapp.conn.AxiatadbConnectionUtil;
import com.wso2telco.spprovisionapp.conn.ConnectdbConnectionUtil;
import com.wso2telco.spprovisionapp.entity.Endpoints;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataBaseFunction {

    private static int appId = -1;
    private static final Log log = LogFactory.getLog(Endpoints.class);

    public static String activateApplication(Connection conn, String appName) throws SQLException {

        CallableStatement callablestatement = null;
        String message;
        try {
            String SQL = "{call populate_am_database_procedure (?,?)}";
            callablestatement = conn.prepareCall(SQL);
            callablestatement.setString(1, appName);
            callablestatement.registerOutParameter(2, Types.INTEGER);
            callablestatement.execute();
            System.out.println(callablestatement);
            if (log.isDebugEnabled()) {
                log.debug("activateApplication sql procedure call: " + callablestatement);
            }
            appId = callablestatement.getInt(2);
            conn.commit();
            message = "{error: false, message: \"success\"}";

        } catch (SQLException e) {
            log.error("SPProvisionAPI: Error occurred while activating AM application", e);
            conn.rollback();
            message = "{error: true, message: \"" + e.getMessage() + "\"}";
        } finally {
            conn.close();
        }
        return message;

    }

    public static String updateApplication(String appName, Connection conn) throws SQLException {

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

            if (log.isDebugEnabled()) {
                log.debug("updateApplication sql procedure call: " + callablestatement);
            }
            conn.commit();
            message = "{error: false, message: \"success\"}";

        } catch (SQLException e) {
            conn.rollback();
            log.error("SPProvisionAPI: Error occurred while updating AM application", e);
            message = "{error: true, message: \"" + e.getMessage() + "\"}";
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

            if (log.isDebugEnabled()) {
                log.debug("updateSubscriptions sql procedure call: " + callablestatement);
            }
            conn.commit();
            message = "{error: false, message: \"success\"}";

        } catch (SQLException e) {
            conn.rollback();
            log.error("SPProvisionAPI: Error occurred while updating AM subscriptions", e);
            message = "{error: true, message: \"" + e.getMessage() + "\"}";
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

            if (log.isDebugEnabled()) {
                log.debug("populateSubscriptionValidator sql procedure call: " + callablestatement);
            }
            conn.commit();
            message = "{error: false, message: \"success\"}";

        } catch (SQLException e) {
            conn.rollback();
            log.error("SPProvisionAPI: Error occurred in Populate Subscription Validator", e);
            message = "{error: true, message: \"" + e.getMessage() + "\"}";
        } finally {
            conn.close();
        }
        return message;
    }

    public static String scopeConfiguration(Connection conn, String consumerKey) throws SQLException {

        CallableStatement callablestatement = null;
        String message;

        try {
            String SQL = "{call populate_sp_config_procedure(?)}";
            callablestatement = conn.prepareCall(SQL);
            callablestatement.setString(1, consumerKey);
            callablestatement.execute();

            if (log.isDebugEnabled()) {
                log.debug("scopeConfiguration sql procedure call: " + callablestatement);
            }
            conn.commit();
            message = "{error: false, message: \"success\"}";
        } catch (SQLException e) {
            conn.rollback();
            log.error("SPProvisionAPI: Error occurred in Scope Configuration", e);
            message = "{error: true, message: \"" + e.getMessage() + "\"}";
        } finally {
            conn.close();
        }
        return message;
    }

    public static String updateClientAndSecretKeys(String consumerKeyOld, String consumerKeyNew, String secretKeyOld,
                                                   String secretKeyNew, String accessToken) throws SQLException {

        String message = "{error: true, message: null}";
        String resultsetValueOld, resultsetValueNew;

        resultsetValueOld = check_for_consumer_key_availability(consumerKeyOld);
        resultsetValueNew = check_for_consumer_key_availability(consumerKeyNew);

        if (resultsetValueOld == null ) {
            return "{error: true, message: \"Supplied old consumer key does not exist\"}";
        } else if (!resultsetValueOld.equals(consumerKeyOld)) {
            return "{error: true, message: \"Supplied old consumer key does match with the key in the database\"}";
        } else if (resultsetValueNew != null) {
            return "{error: true, message: \"Supplied new consumer already exists\"}";
        }

        if (resultsetValueOld != null && resultsetValueOld.equals(consumerKeyOld) && resultsetValueNew == null) {
            update_sp_inbound_auth_table(consumerKeyOld, consumerKeyNew, secretKeyOld, secretKeyNew);
            update_am_application_key_mapping_table(consumerKeyOld, consumerKeyNew);
            update_idn_oauth_consumer_apps_table(consumerKeyOld, consumerKeyNew, secretKeyNew);
            update_sp_token_table(accessToken, consumerKeyOld, consumerKeyNew);
            update_sp_configuration_table(consumerKeyOld, consumerKeyNew);
            message = "{error: false, message: \"success\"}";
        }
        return message;
    }

    public static String trustedStatusConfiguration(Connection conn, String consumerKey) throws SQLException {

        CallableStatement callablestatement = null;
        String message;

        try {
            String SQL = "{call populate_trusted_status_procedure(?)}";
            callablestatement = conn.prepareCall(SQL);
            callablestatement.setString(1, consumerKey);
            callablestatement.execute();
            conn.commit();
            message = "{error: false, message: \"success\"}";
        } catch (SQLException e) {
            conn.rollback();
            log.error("SPProvisionAPI: Error occurred in Trusted status configuration", e);
            message = "{error: true, message: \"" + e.getMessage() + "\"}";
        } finally {
            conn.close();
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
            String sqlQuery = "select * from idn_oauth_consumer_apps where idn_oauth_consumer_apps.CONSUMER_KEY=?;";

            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKey);
            resultSet = statement.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("check_for_consumer_key_availability sql statement: " + statement);
            }

            if (resultSet.wasNull()) {
                outputValue = null;
            } else {
                if (resultSet.next()) {
                    outputValue = resultSet.getString("CONSUMER_KEY");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE,
                    "SQL Exception occured when check for consumer key availability:" + ex.getMessage(), ex);

        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }

        return outputValue;
    }

    private static void update_sp_inbound_auth_table(String consumerKeyOld, String consumerKeyNew,
                                                     String secretKeyOld, String secretKeyNew) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);

            sqlQuery = "update sp_inbound_auth set sp_inbound_auth.INBOUND_AUTH_KEY= ? "
                    + "where sp_inbound_auth.INBOUND_AUTH_KEY=? and INBOUND_AUTH_TYPE= ?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyNew);
            statement.setString(2, consumerKeyOld);
            statement.setString(3, "oauth2");
            status = statement.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("update_sp_inbound_auth_table sql statement: " + statement + ", status: " + status);
            }

            conn.commit();

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                conn.rollback();
            }

        } finally {

            if (conn != null) {
                conn.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }

    private static void update_am_application_key_mapping_table(String consumerKeyOld, String consumerKeyNew)
            throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from am_application_key_mapping where am_application_key_mapping.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            resultSet = statement.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("update_am_application_key_mapping_table sql statement: " + statement);
            }

            if (resultSet.next()) {

                sqlQuery = "update am_application_key_mapping set am_application_key_mapping.CONSUMER_KEY=? "
                        + "where am_application_key_mapping.CONSUMER_KEY=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                statement.setString(2, consumerKeyOld);
                status = statement.executeUpdate();

                if (log.isDebugEnabled()) {
                    log.debug("am_application_key_mapping sql statement: " + statement + ", status: " + status);
                }

                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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
    }

    private static void update_am_app_key_domain_mapping_table(String consumerKeyOld) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from am_app_key_domain_mapping where am_app_key_domain_mapping.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update am_app_key_domain_mapping "
                        + "set am_app_key_domain_mapping.CONSUMER_KEY='tempConsumerKey' "
                        + "where am_app_key_domain_mapping.CONSUMER_KEY=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyOld);
                status = statement.executeUpdate();

                if (log.isDebugEnabled()) {
                    log.debug("update_am_app_key_domain_mapping_table sql statement: " + statement
                            + ", status: " + status);
                }

                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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
    }

    private static void update_idn_oauth2_access_token_table(String consumerKeyOld) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from idn_oauth2_access_token where idn_oauth2_access_token.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update idn_oauth2_access_token set idn_oauth2_access_token.CONSUMER_KEY='tempConsumerKey' "
                        + "where idn_oauth2_access_token.CONSUMER_KEY=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyOld);
                status = statement.executeUpdate();
                System.out.println(statement);
                System.out.println("update_idn_oauth2_access_token_table:" + status);
                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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

    }

    private static void update_idn_oauth2_authorization_code_table(String consumerKeyOld) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from idn_oauth2_authorization_code where idn_oauth2_authorization_code.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update idn_oauth2_authorization_code " +
                        "set idn_oauth2_authorization_code.CONSUMER_KEY='tempConsumerKey' " +
                        "where idn_oauth2_authorization_code.CONSUMER_KEY=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyOld);
                status = statement.executeUpdate();

                if (log.isDebugEnabled()) {
                    log.debug("update_idn_oauth2_authorization_code_table sql statement: " + statement
                            + ", status: " + status);
                }

                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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
    }

    private static void update_idn_oauth_consumer_apps_table(String consumerKeyOld, String consumerKeyNew,
                                                             String consumerSecretNew) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from idn_oauth_consumer_apps where idn_oauth_consumer_apps.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKeyOld);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update idn_oauth_consumer_apps set idn_oauth_consumer_apps.CONSUMER_KEY=? , " +
                        "idn_oauth_consumer_apps.CONSUMER_SECRET=? where idn_oauth_consumer_apps.CONSUMER_KEY=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                statement.setString(2, consumerSecretNew);
                statement.setString(3, consumerKeyOld);
                status = statement.executeUpdate();

                if (log.isDebugEnabled()) {
                    log.debug("update_idn_oauth_consumer_apps_table sql statement: " + statement
                            + ", status: " + status);
                }

                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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
    }

    private static void update_tempkey_in_am_app_key_domain_mapping_table(String consumerKeyNew) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from am_app_key_domain_mapping " +
                    "where am_app_key_domain_mapping.CONSUMER_KEY='tempConsumerKey'";
            statement = conn.prepareStatement(sqlQuery);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update am_app_key_domain_mapping set am_app_key_domain_mapping.CONSUMER_KEY=? " +
                        "where am_app_key_domain_mapping.CONSUMER_KEY='tempConsumerKey'";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                status = statement.executeUpdate();

                if (log.isDebugEnabled()) {
                    log.debug("update_tempkey_in_am_app_key_domain_mapping_table sql statement: " + statement
                            + ", status: " + status);
                }

                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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
    }

    private static void update_tempkey_in_idn_oauth2_access_token_table(String consumerKeyNew) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from idn_oauth2_access_token " +
                    "where idn_oauth2_access_token.CONSUMER_KEY='tempConsumerKey'";
            statement = conn.prepareStatement(sqlQuery);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update idn_oauth2_access_token set idn_oauth2_access_token.CONSUMER_KEY=? " +
                        "where idn_oauth2_access_token.CONSUMER_KEY='tempConsumerKey'";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                status = statement.executeUpdate();

                if (log.isDebugEnabled()) {
                    log.debug("update_tempkey_in_idn_oauth2_access_token_table sql statement: " + statement
                            + ", status: " + status);
                }

                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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
    }

    private static void update_tempkey_in_idn_oauth2_authorization_code_table(String consumerKeyNew)
            throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from idn_oauth2_authorization_code " +
                    "where idn_oauth2_authorization_code.CONSUMER_KEY='tempConsumerKey'";

            statement = conn.prepareStatement(sqlQuery);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                sqlQuery = "update idn_oauth2_authorization_code set idn_oauth2_authorization_code.CONSUMER_KEY=? " +
                        "where idn_oauth2_authorization_code.CONSUMER_KEY='tempConsumerKey'";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                status = statement.executeUpdate();

                if (log.isDebugEnabled()) {
                    log.debug("update_tempkey_in_idn_oauth2_authorization_code_table sql statement: " + statement
                            + ", status: " + status);
                }

                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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
    }

    private static void update_sp_token_table(String accessToken, String consumerKeyOld, String consumerKeyNew)
            throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = AxiatadbConnectionUtil.getConnection();
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

                if (log.isDebugEnabled()) {
                    log.debug("update_sp_token_table sql statement: " + statement + ", status: " + status);
                }

                conn.commit();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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

    }

    private static void update_sp_configuration_table(String consumerKeyOld, String consumerKeyNew)
            throws SQLException {

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

                sqlQuery = "update sp_configuration set sp_configuration.client_id=? " +
                        "where sp_configuration.client_id=?";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKeyNew);
                statement.setString(2, consumerKeyOld);
                status = statement.executeUpdate();

                if (log.isDebugEnabled()) {
                    log.debug("update_sp_configuration_table sql statement: " + statement + ", status: " + status);
                }

                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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
    }

    private static int getAppIdfromAmDatabase(String appName) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            sqlQuery = "select APPLICATION_ID from am_application where NAME= ?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, appName);
            resultSet = statement.executeQuery();

            if (log.isDebugEnabled()) {
                log.debug("getAppIdfromAmDatabase sql statement: " + statement);
            }

            if (resultSet.next()) {
                appId = resultSet.getInt("APPLICATION_ID");
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
            appId = -1;
        } finally {

            if (conn != null && resultSet != null && statement != null) {
                conn.close();
                resultSet.close();
                statement.close();
            }
        }
        return appId;
    }

    public static void insertValuesToAmDatabases(String appName, String consumerKey, String secretKey,
                                                 String accessToken) {
        try {
            insert_am_application_key_mapping_table(appName, consumerKey);
            insert_am_app_key_domain_mapping_table(consumerKey);
            insert_sp_token_table(accessToken, consumerKey);

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static int insert_am_application_key_mapping_table(String appName, String consumerKey) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1, appId;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from am_application_key_mapping where am_application_key_mapping.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKey);
            resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                appId = getAppIdfromAmDatabase(appName);
                sqlQuery = "insert into am_application_key_mapping(APPLICATION_ID,CONSUMER_KEY,KEY_TYPE,STATE) " +
                        "values(?,?,'PRODUCTION','COMPLETED');";
                statement = conn.prepareStatement(sqlQuery);
                statement.setInt(1, appId);
                statement.setString(2, consumerKey);
                status = statement.executeUpdate();

                if (log.isDebugEnabled()) {
                    log.debug("insert_am_application_key_mapping_table sql statement: " + statement
                            + ", status: " + status);
                }

                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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

    private static int insert_am_app_key_domain_mapping_table(String consumerKey) throws SQLException {

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String sqlQuery;
        int status = 1;
        try {
            conn = ApimgtConnectionUtil.getConnection();
            conn.setAutoCommit(false);
            sqlQuery = "select * from am_app_key_domain_mapping where am_app_key_domain_mapping.CONSUMER_KEY=?";
            statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, consumerKey);
            resultSet = statement.executeQuery();

            if (!resultSet.next()) {

                sqlQuery = "insert into am_app_key_domain_mapping(CONSUMER_KEY,AUTHZ_DOMAIN) values(?,'ALL');";
                statement = conn.prepareStatement(sqlQuery);
                statement.setString(1, consumerKey);
                status = statement.executeUpdate();

                if (log.isDebugEnabled()) {
                    log.debug("insert_am_app_key_domain_mapping_table sql statement: " + statement
                            + ", status: " + status);
                }

                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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
            conn = AxiatadbConnectionUtil.getConnection();
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

                if (log.isDebugEnabled()) {
                    log.debug("insert_sp_token_table sql statement: " + statement + ", status: " + status);
                }

                conn.commit();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseFunction.class.getName()).log(Level.SEVERE, null, ex);
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
