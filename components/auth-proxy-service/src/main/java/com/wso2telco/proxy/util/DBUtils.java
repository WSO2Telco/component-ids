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
package com.wso2telco.proxy.util;


import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.proxy.model.MSISDNHeader;
import com.wso2telco.proxy.model.Operator;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to read operator, msisdn and login hint properties.
 */
public class DBUtils {
    private static final Log log = LogFactory.getLog(DBUtils.class);
    private static DataSource dataSource = null;

    /** The Configuration service */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    private static void initializeDatasource() throws NamingException {
        if (dataSource != null) {
            return;
        }

        String dataSourceName = null;
        MobileConnectConfig mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
        try {
            Context ctx = new InitialContext();
            dataSourceName = mobileConnectConfigs.getAuthProxy().getDataSourceName();
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

    private static Connection getConnection() throws SQLException, NamingException {
        initializeDatasource();
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        throw new SQLException("Sessions Datasource not initialized properly");
    }

    /**
     * Get Operators' Properties.
     * @return operators properties map.
     * @throws SQLException on errors.
     * @throws NamingException on errors.
     */
    public static Map<String, Operator> getOperatorProperties() throws SQLException, NamingException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Map<String, Operator> operatorProperties = new HashMap<String, Operator>();
        String queryToGetOperatorProperties = "SELECT ID, operatorName, requiredIPValidation, ipHeader FROM operators";
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(queryToGetOperatorProperties);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Operator operator = new Operator();
                int operatorId = resultSet.getInt(AuthProxyConstants.ID);
                String operatorName = resultSet.getString(AuthProxyConstants.OPERATOR_NAME);
                boolean requiredIPValidation = resultSet.getBoolean(AuthProxyConstants.REQUIRED_IP_VALIDATION);
                String ipHeader = resultSet.getString(AuthProxyConstants.IP_HEADER);
                operator.setOperatorId(operatorId);
                operator.setOperatorName(operatorName);
                operator.setRequiredIpValidation(requiredIPValidation);
                operator.setIpHeader(ipHeader);
                operatorProperties.put(operatorName, operator);
            }
        } catch (SQLException e) {
            throw new SQLException("Error occurred while retrieving operator properties.", e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        }
        finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }
        return operatorProperties;
    }

    /**
     * Get operators' MSISDN header properties.
     * @return operators' MSISDN header properties map.
     * @throws SQLException
     * @throws NamingException
     */
    public static Map<String, List<MSISDNHeader>> getOperatorsMSISDNHeaderProperties() throws SQLException,
                                                                                              NamingException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Map<String, List<MSISDNHeader>> operatorsMSISDNHeadersList = new HashMap<String, List<MSISDNHeader>>();
        String queryToGetOperatorProperty = "SELECT DISTINCT operatorId, LOWER(operatorName) AS operatorName FROM " +
                "operators_msisdn_headers_properties prop LEFT JOIN operators op ON op.ID=prop.operatorId";
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(queryToGetOperatorProperty);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int operatorId = resultSet.getInt(AuthProxyConstants.OPERATOR_ID);
                String operatorName = resultSet.getString(AuthProxyConstants.OPERATOR_NAME);
                //Get msisdn properties of the operator.
                List<MSISDNHeader> msisdnHeaderList = getMSISDNPropertiesByOperatorId(operatorId, operatorName);
                operatorsMSISDNHeadersList.put(operatorName, msisdnHeaderList);
            }
        } catch (SQLException e) {
            throw new SQLException("Error occurred while retrieving operator MSISDN properties of operators : ", e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        }
        finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }
        return operatorsMSISDNHeadersList;
    }

    /**
     * Get MSISDN properties by operator Id.
     * @param operatorId operator Id.
     * @param operatorName operator Name.
     * @return MSISDN properties of given operator.
     * @throws SQLException
     * @throws NamingException
     */
    public static List<MSISDNHeader> getMSISDNPropertiesByOperatorId(int operatorId, String operatorName) throws
                                                                                                     SQLException,
                                                                                            NamingException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<MSISDNHeader> msisdnHeaderList = new ArrayList<MSISDNHeader>();
        String queryToGetOperatorProperty = "SELECT  msisdnHeaderName, isHeaderEncrypted, encryptionImplementation, " +
                "msisdnEncryptionKey, priority FROM operators_msisdn_headers_properties WHERE operatorId = ? ORDER BY" +
                " priority ASC";
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(queryToGetOperatorProperty);
            preparedStatement.setInt(1, operatorId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                MSISDNHeader msisdnHeader = new MSISDNHeader();
                msisdnHeader.setMsisdnHeaderName(resultSet.getString(AuthProxyConstants.MSISDN_HEADER_NAME));
                msisdnHeader.setHeaderEncrypted(resultSet.getBoolean(AuthProxyConstants.IS_HEADER_ENCRYPTED));
                msisdnHeader.setHeaderEncryptionMethod(resultSet.getString(AuthProxyConstants.ENCRYPTION_IMPLEMENTATION));
                msisdnHeader.setHeaderEncryptionKey(resultSet.getString(AuthProxyConstants.MSISDN_ENCRYPTION_KEY));
                msisdnHeader.setPriority(resultSet.getInt(AuthProxyConstants.PRIORITY));
                msisdnHeaderList.add(msisdnHeader);
            }
        } catch (SQLException e) {
            throw new SQLException("Error occurred while retrieving operator MSISDN properties of operator : " +
                                           operatorName, e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        }
        finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }
        return msisdnHeaderList;
    }

    private static void closeAllConnections(PreparedStatement preparedStatement,
                                           Connection connection, ResultSet resultSet) {
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
        closeConnection(connection);
    }

    /**
     * Close Connection
     * @param dbConnection Connection
     */
    private static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close database connection. Continuing with others. - " + e
                        .getMessage(), e);
            }
        }
    }

    /**
     * Close ResultSet
     * @param resultSet ResultSet
     */
    private static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close ResultSet  - " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close PreparedStatement
     * @param preparedStatement PreparedStatement
     */
    private static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close PreparedStatement. Continuing with others. - " + e
                        .getMessage(), e);
            }
        }
    }

}