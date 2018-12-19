/*******************************************************************************
 * Copyright (c) 2015-2018, WSO2.Telco Inc. (http://www.wso2telco.com)
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
package com.wso2telco.dbutils;

import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.exception.CommonAuthenticatorException;
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


/**
 * This class is used to read operator, msisdn and login hint properties.
 */
public class DataBaseConnectUtils {
    private static final Log log = LogFactory.getLog(DataBaseConnectUtils.class);

    /**
     * The m connect datasource.
     */
    private static volatile DataSource mConnectDatasource = null;

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

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


    /**
     * Gets the connect db connection.
     *
     * @return the connect db connection
     * @throws SQLException                 the SQL exception
     */
    private static Connection getConnectDBConnection() throws SQLException, NamingException {
        initializeConnectDatasource();

        if (mConnectDatasource != null) {
            return mConnectDatasource.getConnection();
        }
        throw new SQLException("Connect Datasource not initialized properly");
    }

    /**
     * Get trusted status from consumer key
     *
     * @param consumerKey String
     */
    public static String getTrustedStatus(String consumerKey) throws
            ConfigurationException, CommonAuthenticatorException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String trustedStatus = "";

        String query =
                "select status from trustedstatus ts inner join "+
                        "(select * from sp_configuration where config_key = ? and client_id = ?)spc " +
                        "on ts.id_Trusted_type = spc.config_value ;";

        try {
            connection = getConnectDBConnection();

            if (log.isDebugEnabled()) {
                log.debug("Executing the query " + query);
            }

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "trustedstatus");
            preparedStatement.setString(2, consumerKey);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                trustedStatus = resultSet.getString("status");
            }
        } catch (SQLException e) {
            handleException(
                    "Error occurred while getting trusted status",
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection);
        }
        return trustedStatus;
    }

    private static void closeAllConnections(PreparedStatement preparedStatement,
                                            Connection connection, ResultSet resultSet) {
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
        closeConnection(connection);
    }

    private static void closeAllConnections(PreparedStatement preparedStatement,
                                            Connection connection) {
        closeStatement(preparedStatement);
        closeConnection(connection);
    }

    /**
     * Close Connection
     *
     * @param dbConnection Connection
     */
    private static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close database connection. Continuing with others. - " + e
                        .getMessage(), e);
            }
        }
    }

    /**
     * Close ResultSet
     *
     * @param resultSet ResultSet
     */
    private static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close ResultSet  - " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close PreparedStatement
     *
     * @param preparedStatement PreparedStatement
     */
    private static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close PreparedStatement. Continuing with others. - " + e
                        .getMessage(), e);
            }
        }
    }

    /**
     * Handle exception.
     *
     * @param msg the msg
     * @param t   the t
     * @throws CommonAuthenticatorException the authenticator exception
     */
    private static void handleException(String msg, Throwable t) throws CommonAuthenticatorException {
        log.error(msg, t);
        throw new CommonAuthenticatorException(msg, t);
    }

}