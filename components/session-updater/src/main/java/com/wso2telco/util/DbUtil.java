package com.wso2telco.util;

import com.wso2telco.exception.AuthenticatorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
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

    private static void initializeDatasources() throws AuthenticatorException {
        if (mConnectDatasource != null) {
            return;
        }

        String dataSourceName = null;
        try {
            Context ctx = new InitialContext();
            dataSourceName = ReadMobileConnectConfig.query(Constants.MC_CONFIG).get(Constants.DATA_SOURCE_NAME);
            mConnectDatasource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            handleException("Error while looking up the data source: " + dataSourceName, e);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    public static void updateMultiplePasswordNoOfAttempts(String username, int attempts) throws SQLException, AuthenticatorException {

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

    public static String updateRegistrationStatus(String uuid, String status) throws SQLException, AuthenticatorException {

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

    public static void insertPinAttempt(String msisdn, int attempts, String sessionId) throws SQLException, AuthenticatorException {

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
