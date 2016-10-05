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
package com.wso2telco.historylog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wso2telco.util.DbUtil;


// TODO: Auto-generated Javadoc
/**
 * The Class DbTracelog.
 */
public class DbTracelog {

    /** The connect datasource. */
    private static volatile DataSource connectDatasource = null;
    
    /** The Constant CONNECT_DATA_SOURCE. */
    private static final String CONNECT_DATA_SOURCE = "jdbc/CONNECT_DB";
    
    /** The Constant log. */
    private static final Log log = LogFactory.getLog(DbTracelog.class);

    /**
     * Initialize datasources.
     *
     * @throws LogHistoryException the log history exception
     */
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


    /**
     * Gets the mobile db connection.
     *
     * @return the mobile db connection
     * @throws SQLException the SQL exception
     * @throws LogHistoryException the log history exception
     */
    public static Connection getMobileDBConnection() throws SQLException, LogHistoryException {
        initializeDatasources();

        if (connectDatasource != null) {
            return connectDatasource.getConnection();
        }
        throw new SQLException("Axiata Datasource not initialized properly");
    }

    /**
     * Log history.
     *
     * @param Reqtype the reqtype
     * @param isauthenticated the isauthenticated
     * @param application the application
     * @param authUser the auth user
     * @param authenticators the authenticators
     * @param ipaddress the ipaddress
     * @throws LogHistoryException the log history exception
     */
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
           DbUtil.closeAllConnections(pst, con, null); 
        }        
    }

    /**
     * Handle exception.
     *
     * @param msg the msg
     * @param t the t
     * @throws LogHistoryException the log history exception
     */
    private static void handleException(String msg, Throwable t) throws LogHistoryException {
        log.error(msg, t);
        throw new LogHistoryException(msg, t);
    }

}
