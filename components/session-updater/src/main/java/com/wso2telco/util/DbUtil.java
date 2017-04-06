package com.wso2telco.util;

import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.exception.AuthenticatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by isuru on 12/29/16.
 */


public class DbUtil {

    /**
     * The m connect datasource.
     */
    private static volatile DataSource mConnectDatasource = null;

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

    public static String getMePinId(String msisdn) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet results = null;
        String sql = "SELECT mepin_id FROM mepin_accounts WHERE user_id=?";

        String mePinId = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, msisdn);
            results = ps.executeQuery();
            while (results.next()) {
                mePinId = results.getString("mepin_id");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting me pin response", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, results, ps);
        }
        return mePinId;
    }

    public static String getSessionId(String transactionId) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet results = null;
        String sql = "SELECT session_id FROM mepin_transactions WHERE transaction_id=?";

        String mePinId = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, transactionId);
            results = ps.executeQuery();
            while (results.next()) {
                mePinId = results.getString("session_id");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting me pin response", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, results, ps);
        }
        return mePinId;

    }

    public static void insertMePinData(String msisdn, String mePinId) throws SQLException,
            AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;

        String sql = "insert into mepin_accounts(user_id, mepin_id) values  (?,?);";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setString(1, msisdn);
        ps.setString(2, mePinId);
        ps.execute();

        if (ps != null) {
            ps.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    public static void updateMePinData(String msisdn, String mePinId) throws SQLException,
            AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;

        String sql = "update mepin_accounts set mepin_id = ? where user_id = ?;";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setString(1, mePinId);
        ps.setString(2, msisdn);
        ps.execute();

        if (ps != null) {
            ps.close();
        }
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
