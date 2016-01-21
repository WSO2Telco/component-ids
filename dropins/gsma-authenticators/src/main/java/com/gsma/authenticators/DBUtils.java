package com.gsma.authenticators;


import com.gsma.authenticators.ussd.Pinresponse;
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

public class DBUtils {

    private static volatile DataSource mConnectDatasource = null;
    private static final Log log = LogFactory.getLog(DBUtils.class);

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

    private static Connection getConnectDBConnection() throws SQLException, AuthenticatorException {
        initializeDatasources();

        if (mConnectDatasource != null) {
            return mConnectDatasource.getConnection();
        }
        throw new SQLException("Sessions Datasource not initialized properly");
    }

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
            SessionExpire sessionExpire=new SessionExpire(sessionDataKey);
            sessionExpire.start();
        } catch (SQLException e) {
            handleException("Error occured while inserting User Response for SessionDataKey: " + sessionDataKey + " to the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, null, ps);
        }
        return userResponse;
    }

    private static void handleException(String msg, Throwable t) throws AuthenticatorException {
        log.error(msg, t);
        throw new AuthenticatorException(msg, t);
    }

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


    public static AuthenticationData getAuthenticateData(String tokenID) {

        Connection connection = null;
        PreparedStatement ps = null;
        String userStatus = null;
        ResultSet rs = null;
        AuthenticationData authenticationData = new AuthenticationData();

        String sql = "select *"
                + " from `authenticated_login` where tokenID=?;";

        try {
            connection = getConnectDBConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, tokenID);
            rs = ps.executeQuery();
            while (rs.next()) {
                authenticationData.setTokenID(tokenID);
                authenticationData.setScope(rs.getString("scope"));
                authenticationData.setRedirectUri(rs.getString("redirect_uri"));
                authenticationData.setClientId(rs.getString("client_id"));
                authenticationData.setResponseType(rs.getString("response_type"));
                authenticationData.setAcrValues(rs.getInt("acr_value"));
                authenticationData.setStatus(rs.getInt("status"));

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return authenticationData;
        }
    }


    public static void deleteAuthenticateData(String tokenId) throws SQLException{
        Connection connection = null;
        PreparedStatement ps = null;
        String sql =
                "DELETE FROM authenticated_login " +
                        "WHERE tokenID=?; " ;

        try {
            try {
                connection = getConnectDBConnection();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            }
            ps = connection.prepareStatement(sql);
            ps.setString(1, tokenId);
            ps.execute();

        }
        catch (SQLException e) {
            System.out.print(e.getMessage());
        } finally {
            connection.close();
        }
    }


}
