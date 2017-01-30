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
package com.wso2telco.ids.datapublisher.util;

// TODO: Auto-generated Javadoc

import com.wso2telco.core.config.AuthenticatorException;
import com.wso2telco.core.config.DataHolder;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * The Class DBUtils.
 */
public class DBUtil {

    private static volatile DataSource mConnectDatasource = null;
    private static final Log log = LogFactory.getLog(DBUtil.class);
    private final static String ADD_USER_STATUS =
            " INSERT INTO USER_STATUS (Time, Status, Msisdn, State, Nonce, Scope, AcrValue, SessionId, " +
                    "IsMsisdnHeader, IpHeader," +
                    "IsNewUser, LoginHint, Operator, UserAgent, Comment, ConsumerKey) values (?,?,?,?,?,?,?,?,?,?,?," +
                    "?,?,?,?,?)";


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

    public static int saveStatusData(UserStatus userStatus) throws SQLException, AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnectDBConnection();

            ps = conn.prepareStatement(ADD_USER_STATUS);

            ps.setTimestamp(1, new java.sql.Timestamp(new java.util.Date().getTime()));
            ps.setString(2, userStatus.getStatus());
            ps.setString(3, userStatus.getMsisdn());
            ps.setString(4, userStatus.getState());
            ps.setString(5, userStatus.getNonce());
            ps.setString(6, userStatus.getScope());
            ps.setString(7, userStatus.getAcrValue());
            ps.setString(8, userStatus.getSessionId());
            ps.setInt(9, userStatus.getIsMsisdnHeader());
            ps.setString(10, userStatus.getIpHeader());
            ps.setInt(11, userStatus.getIsNewUser());
            ps.setString(12, userStatus.getLoginHint());
            ps.setString(13, userStatus.getOperator());
            ps.setString(14, userStatus.getUserAgent());
            ps.setString(15, userStatus.getComment());
            ps.setString(16, userStatus.getConsumerKey());


            ps.executeUpdate();
            return 1;
        } catch (SQLException e) {
            handleException(
                    "Error occured while inserting User status for SessionDataKey: " + userStatus.getSessionId() +
                            " to the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, null, ps);
        }
        return -1;
    }

    private static void handleException(String msg, Throwable t) throws AuthenticatorException {
        log.error(msg, t);
        throw new AuthenticatorException(msg, t);
    }

}
