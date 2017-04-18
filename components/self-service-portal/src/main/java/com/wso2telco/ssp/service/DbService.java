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
     * @throws NamingException
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
     * @throws NamingException
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
     * @throws SQLException           the SQL exception
     * @throws NamingException the authenticator exception
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
     * @throws DBUtilException
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

            String sql = "SELECT * FROM sp_login_history "
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
}
