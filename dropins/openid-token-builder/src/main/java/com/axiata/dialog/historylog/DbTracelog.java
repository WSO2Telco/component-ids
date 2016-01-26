package com.axiata.dialog.historylog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;


public class DbTracelog {

    private static volatile DataSource connectDatasource = null;
    private static final String CONNECT_DATA_SOURCE = "jdbc/CONNECT_DB";
    private static final Log log = LogFactory.getLog(DbTracelog.class);

    public static void initializeDatasources() throws LogHistoryException {
        if (connectDatasource != null) {
            return;
        }

        try {
            Context ctx = new InitialContext();
            connectDatasource = (DataSource) ctx.lookup(CONNECT_DATA_SOURCE);
        } catch (NamingException e) {
            handleException("Error while looking up the data source: " + CONNECT_DATA_SOURCE, e);
        }
    }


    public static Connection getMobileDBConnection() throws SQLException, LogHistoryException {
        initializeDatasources();

        if (connectDatasource != null) {
            return connectDatasource.getConnection();
        }
        throw new SQLException("Axiata Datasource not initialized properly");
    }

    public static void LogHistory(String Reqtype,boolean isauthenticated, String application, String authUser, String authenticators, String ipaddress) throws LogHistoryException {
            Connection con = null;
            PreparedStatement pst = null;
        try {
            con = DbTracelog.getMobileDBConnection();

            String sql = "INSERT INTO sp_login_history (reqtype, application_id, authenticated_user, isauthenticated, authenticators,ipaddress, created, created_date)"
                        + " VALUES "
                    + "(?, ?, ?, ?, ?, ?, ?, ?)";
                
                pst = con.prepareStatement(sql);
                
                pst.setString(1, Reqtype);
                pst.setString(2, application);
                pst.setString(3, authUser);               
                pst.setInt(4, (isauthenticated ? 1 : 0));                
                pst.setString(5, authenticators);                
                pst.setString(6, ipaddress);
                pst.setString(7, "authUser");                                
                pst.setTimestamp(8, new java.sql.Timestamp( new java.util.Date().getTime()));
                pst.executeUpdate();               
            
        } catch (SQLException e) {
            handleException("Error occured while Login SP LogHistory: " + application + " Service Provider: " +
                    authUser , e);
        } finally {
            APIMgtDBUtil.closeAllConnections(pst, con, null);
        }        
    }

    private static void handleException(String msg, Throwable t) throws LogHistoryException {
        log.error(msg, t);
        throw new LogHistoryException(msg, t);
    }

}
