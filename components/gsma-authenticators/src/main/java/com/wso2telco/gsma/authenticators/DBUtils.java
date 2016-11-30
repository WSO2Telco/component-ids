/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 * 
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
import com.wso2telco.gsma.authenticators.util.TableName;
import com.wso2telco.core.config.DataHolder;

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
        //String sql = "SELECT SessionID, Status FROM clientstatus WHERE SessionID=?";
        StringBuilder sql = new StringBuilder();
        
        sql.append("SELECT SessionID, Status FROM ");
        sql.append(TableName.CLIENT_STATUS);
        sql.append(" WHERE SessionID=?");
        
        if(log.isDebugEnabled()){
        	log.debug("Executing the query " + sql +" for sessionDataKey : " +sessionDataKey);
        }
        
        String userResponse = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql.toString());
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
        //String sql = "SELECT SessionID, pin, Status FROM clientstatus WHERE SessionID=?";
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SessionID, pin, Status FROM ");
        sql.append(TableName.CLIENT_STATUS);
        sql.append(" WHERE SessionID=?");
        
        if(log.isDebugEnabled()){
        	log.debug("Executing the query " + sql +" for sessionDataKey : " +sessionDataKey);
        }
        
        Pinresponse pinresponse = new Pinresponse();
                
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql.toString());
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
        //String sql = "INSERT INTO clientstatus (SessionID, Status) VALUES (?,?)";
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(TableName.CLIENT_STATUS);
        sql.append(" (SessionID, Status) VALUES (?,?)");
        
        if(log.isDebugEnabled()){
        	log.debug("Executing the query " + sql +" to Insert sessionDataKey : " +sessionDataKey 
        			+ "and responseStatus " + responseStatus);
        }
        
        String userResponse = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql.toString());
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
        //String sql = "update  clientstatus set Status=? WHERE SessionID=?";
        String userResponse = null;
        
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE  ");
        sql.append(TableName.CLIENT_STATUS);
        sql.append(" set Status=? WHERE SessionID=?");
        
        if(log.isDebugEnabled()){
        	log.debug("Executing the query " + sql +" to Update sessionDataKey : " +sessionDataKey 
        			+ "and responseStatus " + responseStatus);
        }
        
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql.toString());
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
     * Gets the authenticate data.
     *
     * @param tokenID the token id
     * @return the authenticate data
     */
    public static AuthenticationData getAuthenticateData(String tokenID) {

        Connection connection = null;
        PreparedStatement ps = null;
        String userStatus = null;
        ResultSet rs = null;
        AuthenticationData authenticationData = new AuthenticationData();

        //String sql = "select *"
        //        + " from `authenticated_login` where tokenID=?;";
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT *");
        sql.append(" from ");
        sql.append(TableName.AUTHENTICATED_LOGIN);
        sql.append(" where tokenID=?");
        
        if(log.isDebugEnabled()){
        	log.debug("Executing the query "+ sql + " for TokenId " + tokenID);
        }

        try {
            connection = getConnectDBConnection();
            ps = connection.prepareStatement(sql.toString());
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
        	log.error("authenticationData Error " + ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error " + e);
                }
            }

            return authenticationData;
        }
    }


    /**
     * Delete authenticate data.
     *
     * @param tokenId the token id
     * @throws SQLException the SQL exception
     */
    public static void deleteAuthenticateData(String tokenId) throws SQLException{
        Connection connection = null;
        PreparedStatement ps = null;
        //String sql =
        //        "DELETE FROM authenticated_login " +
        //                "WHERE tokenID=?; " ;
        
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ");
        sql.append(TableName.AUTHENTICATED_LOGIN);
        sql.append(" WHERE tokenID=?");
        
        if(log.isDebugEnabled()){
        	log.debug("Executing the query " + sql + "to Delete tokenId " + tokenId);
        }
        
        try {
            try {
                connection = getConnectDBConnection();
            } catch (AuthenticatorException e) {
                log.error("Delete authenticate data Error" + e);
            }
            ps = connection.prepareStatement(sql.toString());
            ps.setString(1, tokenId);
            ps.execute();

        }
        catch (SQLException e) {
            log.error("Error " + e);
        } finally {
            connection.close();
        }
    }


}
