/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
package com.wso2telco.gsma.authenticators;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import com.wso2telco.gsma.authenticators.ussd.Pinresponse;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: Auto-generated Javadoc
/**
 * The Class DBUtils.
 */
public class DBUtils {

    /** The m connect datasource. */
    private static volatile DataSource mConnectDatasource = null;
    
    /** The stat connect datasource. */
    private static volatile DataSource statConnectDatasource = null;
    
    /** The Constant log. */
    private static final Log log = LogFactory.getLog(DBUtils.class);

    /**
     * Initialize datasources.
     *
     * @throws AuthenticatorException the authenticator exception
     */
    private static void initializeDatasources() throws AuthenticatorException {
        if (mConnectDatasource != null) {
            return;
        }

        String dataSourceName = null;
        try {
            Context ctx = new InitialContext();
            dataSourceName = DataHolder.getInstance().getMobileConnectConfig().getDataSourceName();
            mConnectDatasource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            handleException("Error while looking up the data source: " + dataSourceName, e);
        }
    }

    /**
     * Gets the stat db connection.
     *
     * @return the stat db connection
     * @throws SQLException the SQL exception
     * @throws AuthenticatorException the authenticator exception
     */
    private static Connection getStatDBConnection() throws SQLException, AuthenticatorException {
        if (statConnectDatasource != null) {
            return statConnectDatasource.getConnection();
        }
        String dataSourceName = "jdbc/WSO2AM_STATS_DB";
        try {
            Context ctx = new InitialContext();
            statConnectDatasource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            handleException("Error while looking up the data source: " + dataSourceName, e);
        }
        return statConnectDatasource.getConnection();
    }

    /**
     * Gets the connect db connection.
     *
     * @return the connect db connection
     * @throws SQLException the SQL exception
     * @throws AuthenticatorException the authenticator exception
     */
    private static Connection getConnectDBConnection() throws SQLException, AuthenticatorException {
        initializeDatasources();

        if (mConnectDatasource != null) {
            return mConnectDatasource.getConnection();
        }
        throw new SQLException("Sessions Datasource not initialized properly");
    }

    /**
     * Gets the user response.
     *
     * @param sessionDataKey the session data key
     * @return the user response
     * @throws AuthenticatorException the authenticator exception
     */
    public static String getUserResponse(String sessionDataKey) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet results = null;
        String sql = "SELECT SessionID, Status FROM clientstatus WHERE SessionID=?";
        String userResponse = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, sessionDataKey);
            results = ps.executeQuery();
            while (results.next()) {
                userResponse = results.getString("Status");
            }
        } catch (SQLException e) {
            handleException("Error occured while getting User Response for SessionDataKey: " + sessionDataKey + " from the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, results, ps);
        }
        return userResponse;
    }
    
    /**
     * Gets the user pin response.
     *
     * @param sessionDataKey the session data key
     * @return the user pin response
     * @throws AuthenticatorException the authenticator exception
     */
    public static Pinresponse getUserPinResponse(String sessionDataKey) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet results = null;
        String sql = "SELECT SessionID, pin, Status FROM clientstatus WHERE SessionID=?";
        Pinresponse pinresponse = new Pinresponse();
                
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, sessionDataKey);
            results = ps.executeQuery();
            while (results.next()) {
                pinresponse.setUserResponse(results.getString("Status"));
                pinresponse.setUserPin(results.getString("pin"));
            }
        } catch (SQLException e) {
            handleException("Error occured while getting User Response for SessionDataKey: " + sessionDataKey + " from the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, results, ps);
        }
        return pinresponse;
    }
    

    /**
     * Insert user response.
     *
     * @param sessionDataKey the session data key
     * @param responseStatus the response status
     * @return the string
     * @throws AuthenticatorException the authenticator exception
     */
    public static String insertUserResponse(String sessionDataKey, String responseStatus) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "INSERT INTO clientstatus (SessionID, Status) VALUES (?,?)";
        String userResponse = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, sessionDataKey);
            ps.setString(2, responseStatus);
            ps.executeUpdate();
        } catch (SQLException e) {
            handleException("Error occured while inserting User Response for SessionDataKey: " + sessionDataKey + " to the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, null, ps);
        }
        return userResponse;
    }

    /**
     * Handle exception.
     *
     * @param msg the msg
     * @param t the t
     * @throws AuthenticatorException the authenticator exception
     */
    private static void handleException(String msg, Throwable t) throws AuthenticatorException {
        log.error(msg, t);
        throw new AuthenticatorException(msg, t);
    }

    /**
     * Update user response.
     *
     * @param sessionDataKey the session data key
     * @param responseStatus the response status
     * @return the string
     * @throws AuthenticatorException the authenticator exception
     */
    public static String updateUserResponse(String sessionDataKey, String responseStatus) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "update  clientstatus set Status=? WHERE SessionID=?";
        String userResponse = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, responseStatus);
            ps.setString(2, sessionDataKey);
            ps.executeUpdate();
        } catch (SQLException e) {
            handleException("Error occured while inserting User Response for SessionDataKey: " + sessionDataKey + " to the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, null, ps);
        }
        return userResponse;
    }

    /**
     * Gets the me pin id.
     *
     * @param userId the user id
     * @return the me pin id
     * @throws AuthenticatorException the authenticator exception
     */
    public static String getMePinId(String userId) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet results = null;
        String sql = "SELECT mepin_id FROM mepin_accounts WHERE user_id=?";
        String mePinId = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            results = ps.executeQuery();
            while (results.next()) {
                mePinId = results.getString("mepin_id");
            }
        } catch (SQLException e) {
            handleException("Error occured while getting MePIN ID for User: " + userId + " from the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, results, ps);
        }
        return mePinId;
    }

    /**
     * Insert me pin transaction.
     *
     * @param sessionDataKey the session data key
     * @param transactionId the transaction id
     * @param mepinId the mepin id
     * @return the string
     * @throws AuthenticatorException the authenticator exception
     */
    public static String insertMePinTransaction(String sessionDataKey, String transactionId, String mepinId) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "INSERT INTO mepin_transactions (session_id, transaction_id, mepin_id) VALUES (?,?,?)";
        String userResponse = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, sessionDataKey);
            ps.setString(2, transactionId);
            ps.setString(3, mepinId);
            ps.executeUpdate();
        } catch (SQLException e) {
            handleException("Error occured while inserting MePIN transaction Response for SessionDataKey: " + sessionDataKey + " to the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, null, ps);
        }
        return userResponse;
    }

    /**
     * Gets the white listed numbers.
     *
     * @param msisdn the msisdn
     * @return the white listed numbers
     * @throws AuthenticatorException the authenticator exception
     */
    public static String getWhiteListedNumbers(String msisdn) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        String sql = "select api_id from subscription_WhiteList WHERE msisdn=?";
        String userResponse = null;
        try {
            conn = getStatDBConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, msisdn);
            ResultSet results = ps.executeQuery();
            while (results.next()) { //msisdn is listed in table subscription_WhiteList
                userResponse = results.getString("api_id");
                break;
            }
        } catch (SQLException e) {
            handleException("Error occured while retrieving whiteListed numbers ", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, null, ps);
        }
        return userResponse;
    }
}
