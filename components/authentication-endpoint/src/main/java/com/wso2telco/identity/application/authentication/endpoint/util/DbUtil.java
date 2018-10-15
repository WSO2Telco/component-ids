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
package com.wso2telco.identity.application.authentication.endpoint.util;

import com.wso2telco.core.config.AuthenticatorException;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
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
import java.util.HashMap;
import java.util.Map;


public class DbUtil {

    /**
     * The m connect datasource.
     */
    private static volatile DataSource mConnectDatasource = null;

    /**
     * The WSO2 APIM datasource
     */
    private static volatile DataSource wso2APIMDatasource = null;

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

    public static Map<String, String> getScopeDescforSession (String hashKey) throws AuthenticatorException {
        Map<String,String> scopeDescription= new HashMap<String,String>();
        String scopes = null;
        Boolean newUser = false;
        String operator = null;
        String spName = null;
        Boolean enable_approve_all = false;
        StringBuilder sql = new StringBuilder();
        sql.append("select isNewUser, scopes, operator, spName, isLongLive from backchannel_request_details brd INNER JOIN sms_hashkey_contextid_mapping shcm ")
           .append("on shcm.contextid = brd.session_id where hashkey=?");

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnectDBConnection();
            ps = connection.prepareStatement(sql.toString());
            ps.setString(1, hashKey);
            rs = ps.executeQuery();
            while (rs.next()) {
                scopes = rs.getString("scopes");
                newUser = rs.getBoolean("isNewUser");
                operator = rs.getString("operator");
                spName = rs.getString("spName");
                enable_approve_all = rs.getBoolean("isLongLive");
            }
            String[] scopeList = scopes.split(" ");
            ps.close();
            rs.close();
            sql = new StringBuilder();
            sql.append("select scope, description from scope_parameter where scope in (");
            for (int i = 0; i < scopeList.length; i++){
                sql.append("?,");
            }
            sql.deleteCharAt(sql.length()-1);
            sql.append(");");
            ps = connection.prepareStatement(sql.toString());
            for (int i = 0; i < scopeList.length; i++){
                ps.setString(i+1, scopeList[i]);
            }
            rs = ps.executeQuery();
            while (rs.next()){
                scopeDescription.put(rs.getString("scope"), rs.getString("description"));
            }
            scopeDescription.put("isNewUser", String.valueOf(newUser));
            scopeDescription.put("operator", operator);
            scopeDescription.put("spName", spName);
            scopeDescription.put("approve_all_enable", String.valueOf(enable_approve_all));
        } catch (SQLException e) {
            handleException(e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, ps);
        }
        return scopeDescription;
    }

    public static Boolean getSessionStatus(String sessionID) throws SQLException, AuthenticatorException {
        Connection connection = null;
        PreparedStatement ps = null;
        String userStatus = null;
        ResultSet rs = null;
        Boolean status = false;

        String sql = "select status from regstatus inner join sms_hashkey_contextid_mapping shcm " +
                "on regstatus.uuid = shcm.contextid where shcm.hashkey=?";
        try {
            connection = getConnectDBConnection();

            ps = connection.prepareStatement(sql);

            ps.setString(1, sessionID);

            rs = ps.executeQuery();

            while (rs.next()) {
                userStatus = rs.getString("status");
            }
            if(userStatus.equalsIgnoreCase("PENDING")){
                status = false;
            }else{
                status = true;
            }
        } catch (SQLException e) {
            log.error("Error while retrieving user status", e);
        } finally {
            connection.close();
            ps.close();
            rs.close();
        }
        return status;
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
