/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com)
 * <p>
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package com.wso2telco.util;


import com.wso2telco.exception.AuthProxyServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class is used to read operator properties.
 */
public class DBUtils {
    private static final Log log = LogFactory.getLog(DBUtils.class);
    private static DataSource dataSource = null;

    private static void initializeDatasource() throws JAXBException, NamingException, AuthProxyServiceException {
        if (dataSource != null) {
            return;
        }

        String dataSourceName = null;
        MobileConnectConfig mobileConnectConfigs = ConfigLoader.getInstance().getMobileConnectConfig();
        Context ctx = new InitialContext();
        dataSourceName = mobileConnectConfigs.getAuthProxy().getDataSourceName();
        if (dataSourceName != null) {
            dataSource = (DataSource) ctx.lookup(dataSourceName);
        } else {
            throw new AuthProxyServiceException("DataSource could not be found in mobile-connect.xml");
        }
    }

    private static Connection getConnection() throws AuthProxyServiceException, JAXBException, NamingException,
                                                     SQLException {
        initializeDatasource();
        if (dataSource != null) {
            return dataSource.getConnection();
        } else {
            throw new AuthProxyServiceException("DataSource can't be null.");
        }
    }

    /**
     * Get Operator Property by operator name and property key.
     * @param operatorName Operator Name.
     * @param propertyKey operator property key.
     * @return operator property value.
     * @throws SQLException
     * @throws NamingException
     */
    public static String getOperatorProperty(String operatorName, String propertyKey)
            throws AuthProxyServiceException, SQLException, JAXBException, NamingException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String propertyValue = null;
        String queryToGetOperatorProperty = "SELECT propertyValue FROM operators_properties prop LEFT JOIN operators" +
                " op ON op.ID=prop.operatorId AND LOWER(op.operatorName)=? AND prop.propertyKey=?";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(queryToGetOperatorProperty);
            preparedStatement.setString(1, operatorName.toLowerCase());
            preparedStatement.setString(2, propertyKey);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                propertyValue = resultSet.getString(AuthProxyConstants.PROPERTY_VALUE);
            }
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }
        return propertyValue;
    }

    /**
     * Utility method to close the connection streams.
     *
     * @param preparedStatement PreparedStatement.
     * @param connection        Connection.
     * @param resultSet         ResultSet.
     */
    private static void closeAllConnections(PreparedStatement preparedStatement,
                                           Connection connection, ResultSet resultSet) {
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
        closeConnection(connection);
    }

    /**
     * Close Connection.
     * @param dbConnection Connection.
     */
    private static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close database connection. Continuing with " +
                                 "others. - " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close ResultSet.
     * @param resultSet ResultSet.
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
     * Close PreparedStatement.
     * @param preparedStatement PreparedStatement.
     */
    private static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close PreparedStatement. Continuing with" +
                                 " others. - " + e.getMessage(), e);
            }
        }
    }
}