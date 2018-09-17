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


import com.wso2telco.core.config.model.LoginHintFormatDetails;
import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.proxy.model.AuthenticatorException;
import com.wso2telco.proxy.model.MSISDNHeader;
import com.wso2telco.proxy.model.Operator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * This class is used to read operator, msisdn and login hint properties.
 */
public class DBUtils {
    private static final Log log = LogFactory.getLog(DBUtils.class);
    private static DataSource dataSource = null;

    /**
     * The m connect datasource.
     */
    private static volatile DataSource mConnectDatasource = null;

    /**
     * The WSO2 APIM datasource
     */
    private static volatile DataSource wso2APIMDatasource = null;

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    private static void initializeDatasource() throws NamingException {
        if (dataSource != null) {
            return;
        }

        String dataSourceName = null;
        try {
            Context ctx = new InitialContext();
            dataSourceName = configurationService.getDataHolder().getMobileConnectConfig().getAuthProxy()
                    .getDataSourceName();
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

    private static void initializeWSO2APIMDatasource() throws NamingException {
        if (wso2APIMDatasource != null) {
            return;
        }

        String dataSourceName = null;
        try {
            Context ctx = new InitialContext();
            ConfigurationService configurationService = new ConfigurationServiceImpl();
            dataSourceName = configurationService.getDataHolder().getMobileConnectConfig().getWso2APIMDataSourceName();
            wso2APIMDatasource = (DataSource) ctx.lookup(dataSourceName);
        } catch (NamingException e) {
            throw new NamingException("Error while looking up the data source : " + dataSourceName);
        }
    }


    /**
     * Gets the connect db connection.
     *
     * @return the connect db connection
     * @throws SQLException           the SQL exception
     * @throws AuthenticatorException the authenticator exception
     */
    private static Connection getConnectDBConnection() throws SQLException, NamingException {
        initializeConnectDatasource();

        if (mConnectDatasource != null) {
            return mConnectDatasource.getConnection();
        }
        throw new SQLException("Connect Datasource not initialized properly");
    }

    /**
     * Gets the WSO2 API Manager db connection.
     *
     * @return the WSO2 API Manager db connection
     * @throws SQLException           the SQL exception
     * @throws AuthenticatorException the authenticator exception
     */
    private static Connection getWSO2APIMDBConnection() throws SQLException, NamingException {
        initializeWSO2APIMDatasource();

        if (wso2APIMDatasource != null) {
            return wso2APIMDatasource.getConnection();
        }
        throw new SQLException("Connect Datasource not initialized properly");
    }

    /**
     * Get Operators' Properties.
     *
     * @return operators properties map.
     * @throws SQLException    on errors.
     * @throws NamingException on errors.
     */
    public static Map<String, Operator> getOperatorProperties() throws SQLException, NamingException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Map<String, Operator> operatorProperties = new HashMap<String, Operator>();
        String queryToGetOperatorProperties = "SELECT ID, operatorName, requiredIPValidation, ipHeader FROM operators";
        try {
            connection = getConnectDBConnection();
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
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }
        return operatorProperties;
    }

    /**
     * Get operators' MSISDN header properties.
     *
     * @return operators' MSISDN header properties map.
     * @throws SQLException    on errors
     * @throws NamingException on errors
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
            connection = getConnectDBConnection();
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
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }
        return operatorsMSISDNHeadersList;
    }

    /**
     * Get MSISDN properties by operator Id.
     *
     * @param operatorId   operator Id.
     * @param operatorName operator Name.
     * @return MSISDN properties of given operator.
     * @throws SQLException    on errors
     * @throws NamingException on errors
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
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(queryToGetOperatorProperty);
            preparedStatement.setInt(1, operatorId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                MSISDNHeader msisdnHeader = new MSISDNHeader();
                msisdnHeader.setMsisdnHeaderName(resultSet.getString(AuthProxyConstants.MSISDN_HEADER_NAME));
                msisdnHeader.setHeaderEncrypted(resultSet.getBoolean(AuthProxyConstants.IS_HEADER_ENCRYPTED));
                msisdnHeader.setHeaderEncryptionMethod(resultSet.getString(AuthProxyConstants
                        .ENCRYPTION_IMPLEMENTATION));
                msisdnHeader.setHeaderEncryptionKey(resultSet.getString(AuthProxyConstants.MSISDN_ENCRYPTION_KEY));
                msisdnHeader.setPriority(resultSet.getInt(AuthProxyConstants.PRIORITY));
                msisdnHeaderList.add(msisdnHeader);
            }
        } catch (SQLException e) {
            throw new SQLException("Error occurred while retrieving operator MSISDN properties of operator : " +
                    operatorName, e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }
        return msisdnHeaderList;
    }

    /**
     * Get a map of parameters mapped to a scope
     *
     * @return map of scope vs parameters
     * @throws AuthenticatorException on errors
     */
    public static Map<String, ScopeParam> getScopeParams(String scope) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet results = null;
        String[] scopeValues = scope.split("\\s+|\\+");
        StringBuilder params = new StringBuilder("?");
        for (int i = 1; i < scopeValues.length; i++) {
            params.append(",?");
        }
        String sql = "SELECT * FROM `scope_parameter` WHERE scope in (" + params + ")";

        if (log.isDebugEnabled()) {
            log.debug("Executing the query " + sql);
        }

        Map scopeParamsMap = new HashMap();
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < scopeValues.length; i++) {
                ps.setString(i + 1, scopeValues[i]);
            }
            results = ps.executeQuery();

            Boolean mainScopeFound = false;
            List<String> scopeValuesFromDatabase = new ArrayList<>();

            ScopeParam parameters = new ScopeParam();
            EnumSet<ScopeParam.scopeTypes> scopetypes=EnumSet.noneOf(ScopeParam.scopeTypes.class);
            boolean is_consent_page=false;
            while (results.next()) {
                is_consent_page=results.getBoolean("is_consent_page");
                Boolean isMultiscope = results.getBoolean("is_multiscope");
                scopeValuesFromDatabase.add(results.getString("scope"));

                if (!isMultiscope) {
                    //throw error if multiple main scopes found
                    if (mainScopeFound) {
                        throw new ConfigurationException("Multiple main scopes found");
                    }
                    //mark as main scope found
                    mainScopeFound = true;
                    scopeParamsMap.put("scope", results.getString("scope"));

                    parameters.setScope(results.getString("scope"));
                    parameters.setLoginHintMandatory(results.getBoolean("is_login_hint_mandatory"));
                    parameters.setHeaderMsisdnMandatory(results.getBoolean("is_header_msisdn_mandatory"));

                    parameters.setMsisdnMismatchResult(ScopeParam.MsisdnMismatchResultTypes.valueOf(results.getString(
                            "msisdn_mismatch_result")));
                    parameters.setHeFailureResult(ScopeParam.HeFailureResults.valueOf(results.getString(
                            "he_failure_result")));

                    parameters.setTncVisible(results.getBoolean("is_tnc_visible"));
                    parameters.setLoginHintFormat(getLoginHintFormatTypeDetails(results.getInt("param_id"), conn));
                }

                String scope_type=results.getString("scope_type");
                ScopeParam.scopeTypes scopeTypeFromDB=null;
                //// TODO: 8/31/17 add the scopetype retrival to a common method
                if(scope_type.equalsIgnoreCase(ScopeParam.scopeTypes.MAIN.name())){
                    scopeTypeFromDB=ScopeParam.scopeTypes.MAIN;
                }else if(scope_type.equalsIgnoreCase(ScopeParam.scopeTypes.APICONSENT.name())){
                    scopeTypeFromDB=ScopeParam.scopeTypes.APICONSENT;
                }
                if(scopeTypeFromDB!=null){
                    scopetypes.add(scopeTypeFromDB);
                }
            }
            parameters.setScopeTypesList(scopetypes);
            parameters.setConsentPage(is_consent_page);

            //validate all scopes and compare with scopes fetched from database
            for (String scopeToValidate : scopeValues) {
                if (!scopeValuesFromDatabase.contains(scopeToValidate)) {
                    throw new ConfigurationException("One or more scopes are not valid");
                }
            }
            scopeParamsMap.put("params", parameters);
        } catch (SQLException e) {
            handleException("Error occurred while getting scope parameters from the database", e);
        } catch (ConfigurationException e) {
            handleException(e.getMessage(), e);
        } catch (NamingException e) {
            log.error("Naming exception ", e);
        } finally {
            closeAllConnections(ps, conn, results);
        }
        return scopeParamsMap;
    }

    private static List<LoginHintFormatDetails> getLoginHintFormatTypeDetails(int paramId, Connection conn)
            throws AuthenticatorException, SQLException {
        PreparedStatement ps = null;
        ResultSet results = null;
        String sql =
                "SELECT * FROM `login_hint_format` WHERE `format_id` IN (SELECT `format_id` FROM " +
                        "`scope_supp_login_hint_format` WHERE `param_id` = ?);";

        if (log.isDebugEnabled()) {
            log.debug("Executing the query : " + sql);
        }

        List<LoginHintFormatDetails> loginHintFormatDetails = new ArrayList<LoginHintFormatDetails>();
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, paramId);
            results = ps.executeQuery();

            while (results.next()) {
                LoginHintFormatDetails loginHintFormat = new LoginHintFormatDetails();
                loginHintFormat.setFormatType(LoginHintFormatDetails.loginHintFormatTypes.valueOf(results.getString(
                        "type")));
                loginHintFormat.setEncrypted(results.getBoolean("is_encrypted"));
                loginHintFormat.setDecryptAlgorithm(results.getString("decrypt_algorithm"));
                loginHintFormatDetails.add(loginHintFormat);
            }
        } catch (SQLException e) {
            //using the same connection to avoid connection pool exhaust exception within the loop. SQL exception to
            // be handled in the parent function.
            log.error("Error occurred while getting login format details from the database", e);
            throw e;
        } finally {
            closeAllConnections(ps, null, results);
        }
        return loginHintFormatDetails;
    }

    public static long getAccountUnlockTime(String userName)
            throws ConfigurationException, AuthenticatorException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        long remainingTime = 0;
        String getAccountUnlockTimeQuery = "SELECT DATA_VALUE FROM idn_identity_user_data WHERE TENANT_ID=? AND " +
                "USER_NAME=? AND DATA_KEY='http://wso2.org/claims/identity/unlockTime'";

        try {
            connection = getWSO2APIMDBConnection();
            preparedStatement = connection.prepareStatement(getAccountUnlockTimeQuery);
            preparedStatement.setString(1, "-1234");
            preparedStatement.setString(2, userName);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                remainingTime = Long.parseLong(resultSet.getString("DATA_VALUE"));
            }
        } catch (SQLException e) {
            handleException(
                    "Error occurred while getting account unlock time for UserName - " + userName, e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }

        return remainingTime;
    }

    public static boolean isSPAllowedScope(String scopeName, String clientId)
            throws ConfigurationException, AuthenticatorException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String[] scopeValues = scopeName.split("\\s+|\\+");
        StringBuilder params = new StringBuilder("?");
        for (int i = 1; i < scopeValues.length; i++) {
            params.append(",?");
        }

        String queryToGetOperatorProperty =
                "select count(*) as record_count from " +
                        "( select client_id,config_key,config_value " +
                        "FROM `sp_configuration` " +
                        "WHERE `client_id`=? AND `config_key`=? AND  `config_value` in (" + params + ") " +
                        "group by client_id,config_key,config_value" +
                        ") as spscopes";
        boolean isSPAllowedScope = false;

        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(queryToGetOperatorProperty);
            preparedStatement.setString(1, clientId);
            preparedStatement.setString(2, "scope");

            for (int i = 0; i < scopeValues.length; i++) {
                preparedStatement.setString(i + 3, scopeValues[i]);
            }

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                isSPAllowedScope = resultSet.getInt("record_count") == scopeValues.length;
            }
        } catch (SQLException e) {
            handleException(
                    "Error occurred while SP Configurations for ClientId - " + clientId + " and Scope - " + scopeName,
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }
        return isSPAllowedScope;
    }

    /**
     * Get a list of notification URLs for a given SP
     *
     * @param clientId unique Client_id
     * @return list of Notification urls
     * @throws AuthenticatorException on errors
     * @throws ConfigurationException on errors
     */
    public static List<String> getNotificationUrls(String clientId) throws AuthenticatorException,
            ConfigurationException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<String> notificationUrls = null;

        String sql = "SELECT notification_url FROM sp_notification_url WHERE client_id=? ;";

        if (log.isDebugEnabled()) {
            log.debug("Executing the query to get Notification Urls: " + sql);
        }

        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, clientId);
            resultSet = preparedStatement.executeQuery();
            notificationUrls = new ArrayList<>();

            while (resultSet.next()) {
                notificationUrls.add(resultSet.getString("notification_url"));
            }
        } catch (SQLException e) {
            handleException(
                    "Error occurred while getting Notification Urls for ClientId - " + clientId,
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }
        return notificationUrls;
    }

    public static boolean isValidCallback(String redirectURL, String clientId)
            throws ConfigurationException, AuthenticatorException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String queryToGetOperatorProperty = "SELECT callback_url FROM idn_oauth_consumer_apps WHERE consumer_key=?";
        boolean isValidCallback = false;

        try {
            connection = getWSO2APIMDBConnection();
            preparedStatement = connection.prepareStatement(queryToGetOperatorProperty);
            preparedStatement.setString(1, clientId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                isValidCallback = resultSet.getString("callback_url").equalsIgnoreCase(redirectURL);
            }
        } catch (SQLException e) {
            handleException(
                    "Error occurred while getting call back infomation for ClientId - " + clientId, e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }
        return isValidCallback;
    }

    /**
     * Get request encrypted key for a given SP
     *
     * @param clientId unique Client_id
     * @return shared private key
     * @throws AuthenticatorException on errors
     * @throws ConfigurationException on errors
     */
    public static String getSpRequestEncryptedKey(String clientId) throws AuthenticatorException,
            ConfigurationException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sharedKey = null;

        String sql = "SELECT config_value FROM sp_configuration WHERE client_id=? and config_key='" +
                AuthProxyConstants.SP_REQUEST_ENCRYPTED_PUBLIC_KEY + "';";

        if (log.isDebugEnabled()) {
            log.debug("Executing the query to get Shared request Encrypted Key: " + sql);
        }

        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, clientId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                sharedKey = resultSet.getString("config_value");
            }
        } catch (SQLException e) {
            handleException(
                    "Error occurred while getting Shared Encrypted Key for ClientId - " + clientId,
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            closeAllConnections(preparedStatement, connection, resultSet);
        }
        return sharedKey;
    }

    public static String getSPConfigValue(String operator, String clientID, String key) throws AuthenticatorException, NamingException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String queryToGetConsent = "SELECT config_value FROM sp_configuration WHERE client_id=? AND operator=? AND config_key=?";
        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(queryToGetConsent);
            preparedStatement.setString(1,clientID);
            preparedStatement.setString(2,operator);
            preparedStatement.setString(3, key);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString("config_value");
            }
        } catch (SQLException e) {
            handleException("Error occurred while retrieving config_value for client_id : " + clientID,e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet,preparedStatement);
        }
        return null;
    }

    private static void closeAllConnections(PreparedStatement preparedStatement,
                                            Connection connection, ResultSet resultSet) {
        closeResultSet(resultSet);
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
     * @throws AuthenticatorException the authenticator exception
     */
    private static void handleException(String msg, Throwable t) throws AuthenticatorException {
        log.error(msg, t);
        throw new AuthenticatorException(msg, t);
    }

}