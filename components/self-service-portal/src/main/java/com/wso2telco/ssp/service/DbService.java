/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
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
package com.wso2telco.ssp.service;

import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.ssp.model.*;
import com.wso2telco.ssp.util.Pagination;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DbService {
    private static Log logger = LogFactory.getLog(DbService.class);

    private static DataSource dataSource = null;

    /**
     * The m connect datasource.
     */
    private static volatile DataSource mConnectDatasource = null;

    /**
     * The Configuration api
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /**
     * Initializes the datasource
     * @throws NamingException Datasource naming error
     */
    private static void initializeDatasource() throws NamingException {
        if (dataSource != null) {
            return;
        }

        String dataSourceName = null;
        try {
            Context ctx = new InitialContext();
            dataSourceName = configurationService.getDataHolder().getMobileConnectConfig().getDataSourceName();
            if (dataSourceName != null) {
                dataSource = (DataSource) ctx.lookup(dataSourceName);
            } else {
                throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
            }
        } catch (ConfigurationException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } catch (NamingException e) {
            throw new NamingException("Exception occurred while initiating data source : " + dataSourceName);
        }
    }

    /**
     * Initializes the datasource
     * @throws NamingException Datasource naming error
     */
    private static void initializeConnectDatasource() throws NamingException {
        if (mConnectDatasource != null) {
            return;
        }

        String dataSourceName = null;
        try {
            Context ctx = new InitialContext();
            ConfigurationService configurationService = new ConfigurationServiceImpl();
            dataSourceName = configurationService.getDataHolder().getMobileConnectConfig().getDataSourceName();
            mConnectDatasource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            throw new NamingException("Error while looking up the data source : " + dataSourceName);
        }
    }

    private static Connection getConnection() throws SQLException, NamingException {
        initializeDatasource();
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        throw new SQLException("Sessions Datasource not initialized properly");
    }

    /**
     * Gets the connect db connection.
     *
     * @return the connect db connection
     * @throws SQLException the SQL com.wso2telco.ssp.exception
     * @throws NamingException the authenticator com.wso2telco.ssp.exception
     */
    private static Connection getConnectDBConnection() throws SQLException, NamingException {
        initializeConnectDatasource();

        if (mConnectDatasource != null) {
            return mConnectDatasource.getConnection();
        }
        throw new SQLException("Connect Datasource not initialized properly");
    }

    /**
     * Gets paged result set for login history
     * @param msisdn msisdn
     * @param order sort order
     * @param orderType sort order type
     * @param pagination pagination object
     * @return paged result set
     * @throws DBUtilException Database access fail
     */
    public static PagedResults getLoginHistoryByMsisdn(String msisdn, String order,
                                                       OrderByType orderType, Pagination pagination)
            throws DBUtilException {

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        PagedResults loginHistoryResults = new PagedResults();
        ResultsMeta meta = new ResultsMeta();
        List<IDataItem> loginHistories = new ArrayList<>();

        try {
            con = getConnectDBConnection();

            String sql = "SELECT *, UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(created_date) as duration FROM sp_login_history "
                    + "WHERE authenticated_user=? ORDER BY ? " + orderType.toString() + " LIMIT ?,? ";

            ps = con.prepareStatement(sql);
            ps.setString(1, msisdn);
            ps.setString(2, order);
            ps.setInt(3, pagination.getOffset());
            ps.setInt(4, pagination.getLimit());

            rs = ps.executeQuery();

            while (rs.next()) {
                LoginHistory loginHistory = new LoginHistory();
                loginHistory.setId(rs.getInt("id"));
                loginHistory.setApplication_id(rs.getString("application_id"));
                loginHistory.setAuthenticated_user(rs.getString("authenticated_user"));
                loginHistory.setAuthenticators(rs.getString("authenticators"));
                loginHistory.setCreated(rs.getString("created"));
                loginHistory.setCreated_date(rs.getTimestamp("created_date"));
                loginHistory.setIpaddress(rs.getString("ipaddress"));
                loginHistory.setIsauthenticated(rs.getBoolean("isauthenticated"));
                loginHistory.setLastupdated(rs.getString("lastupdated"));
                loginHistory.setLastupdated_date(rs.getTimestamp("lastupdated_date"));
                loginHistory.setReqtype(rs.getString("reqtype"));
                loginHistory.setDuration(rs.getLong("duration"));

                loginHistories.add(loginHistory);
            }

            String countsql = "SELECT COUNT(*) AS c "
                    + "FROM sp_login_history "
                    + "USE INDEX(PRIMARY) "
                    + "WHERE authenticated_user=? ";

            ps = con.prepareStatement(countsql);
            ps.setString(1, msisdn);

            rs = ps.executeQuery();
            if (rs.next()) {
                meta.setTotal_count(rs.getInt("c"));
            }
        } catch (Exception e) {
            com.wso2telco.core.dbutils.DbUtils.handleException("Error while loading login histories. ", e);
        } finally {
            com.wso2telco.core.dbutils.DbUtils.closeAllConnections(ps, con, rs);
        }

        meta.setPage(pagination.getPage());
        meta.setPerPage(pagination.getLimit());

        loginHistoryResults.setItems(loginHistories);
        loginHistoryResults.setMeta(meta);
        return loginHistoryResults;
    }

    /**
     * Gets application login counts.
     * @param msisdn msisdn
     * @param order sort order
     * @param orderType sort order type
     * @return Application login details
     * @throws DBUtilException Database fails
     */
    public static PagedResults  getLoginApplicationsByMsisdn(String msisdn, String order, OrderByType orderType)
            throws DBUtilException {

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        PagedResults applicationLoginsResults = new PagedResults();
        List<IDataItem> applicationLogins = new ArrayList<>();

        try {
            con = getConnectDBConnection();

            String sql = "SELECT application_id,  max(created_date) as l, count(*) as count FROM mig_connectdb.sp_login_history "
                    + "WHERE authenticated_user=? GROUP BY application_id ORDER BY ? " + orderType.toString();

            ps = con.prepareStatement(sql);
            ps.setString(1, msisdn);
            ps.setString(2, order);

            rs = ps.executeQuery();

            while (rs.next()) {
                ApplicationLogin applicationLogin = new ApplicationLogin();
                applicationLogin.setApplication_id(rs.getString("application_id"));
                applicationLogin.setLogin_count(rs.getLong("count"));
                applicationLogin.setCreated_date(rs.getTimestamp("l"));

                applicationLogins.add(applicationLogin);
            }

        } catch (Exception e) {
            com.wso2telco.core.dbutils.DbUtils.handleException("Error while loading app login list. ", e);
        } finally {
            com.wso2telco.core.dbutils.DbUtils.closeAllConnections(ps, con, rs);
        }

        applicationLoginsResults.setItems(applicationLogins);
        applicationLoginsResults.setMeta(null);
        return applicationLoginsResults;
    }
}
