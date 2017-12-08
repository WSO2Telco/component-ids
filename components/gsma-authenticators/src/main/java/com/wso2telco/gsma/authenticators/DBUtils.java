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

import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.model.MSISDNHeader;
import com.wso2telco.gsma.authenticators.model.PromptData;
import com.wso2telco.gsma.authenticators.ussd.Pinresponse;
import com.wso2telco.gsma.authenticators.util.TableName;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: Auto-generated Javadoc

/**
 * The Class DBUtils.
 */
public class DBUtils {

    /**
     * The m connect datasource.
     */
    private static volatile DataSource mConnectDatasource = null;

    /**
     * The Constant log.
     */
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
            ConfigurationService configurationService = new ConfigurationServiceImpl();
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

        sql.append("SELECT status FROM ");
        sql.append(TableName.REG_STATUS);
        sql.append(" WHERE uuid=?");

        String userResponse = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql.toString());
            ps.setString(1, sessionDataKey);
            results = ps.executeQuery();
            while (results.next()) {
                userResponse = results.getString("status");
            }
        } catch (SQLException e) {
            handleException("Error occured while getting User Response for SessionDataKey: " + sessionDataKey
                    + " from the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, results, ps);
        }
        return userResponse;
    }

    /**
     * Gets the user response for login.
     *
     * @param sessionDataKey the session data key
     * @return the user response
     * @throws AuthenticatorException the authenticator exception
     */
    public static String getUserLoginResponse(String sessionDataKey) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet results = null;
        //String sql = "SELECT SessionID, Status FROM clientstatus WHERE SessionID=?";
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT SessionID, Status FROM ");
        sql.append(TableName.CLIENT_STATUS);
        sql.append(" WHERE SessionID=?");

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
            handleException("Error occured while getting User Response for SessionDataKey: " + sessionDataKey + " " +
                    "from the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, results, ps);
        }
        return userResponse;
    }


    /**
     * Gets the user response for registration.
     *
     * @param sessionDataKey the session data key
     * @return the user response
     * @throws AuthenticatorException the authenticator exception
     */
    public static String getAuthFlowStatus(String sessionDataKey) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet results = null;
        //String sql = "SELECT SessionID, Status FROM regstatus WHERE uuid=?";
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT uuid, status FROM ");
        sql.append(TableName.REG_STATUS);
        sql.append(" WHERE uuid=?");

        String userResponse = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql.toString());
            ps.setString(1, sessionDataKey);
            results = ps.executeQuery();
            while (results.next()) {
                userResponse = results.getString("status");
            }
        } catch (SQLException e) {
            handleException("Error occured while getting User Response for SessionDataKey: " + sessionDataKey + " " +
                    "from the database", e);
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
            handleException("Error occured while getting User Response for SessionDataKey: " + sessionDataKey + " " +
                    "from the database", e);
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
    public static String insertUserResponse(String sessionDataKey, String responseStatus) throws
            AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        //String sql = "INSERT INTO clientstatus (SessionID, Status) VALUES (?,?)";

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(TableName.CLIENT_STATUS);
        sql.append(" (SessionID, Status) VALUES (?,?)");

        String userResponse = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql.toString());
            ps.setString(1, sessionDataKey);
            ps.setString(2, responseStatus);
            ps.executeUpdate();
            //SessionExpire sessionExpire = new SessionExpire(sessionDataKey);
            //sessionExpire.start();
        } catch (SQLException e) {
            handleException("Error occured while inserting User Response for SessionDataKey: " + sessionDataKey + " " +
                    "to the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, null, ps);
        }
        return userResponse;
    }


    /**
     * Insert user response.
     *
     * @param sessionDataKey the session data key
     * @param responseStatus the response status
     * @return the string
     * @throws AuthenticatorException the authenticator exception
     */
    public static String insertLoginStatus(String sessionDataKey, String responseStatus) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        //String sql = "INSERT INTO clientstatus (SessionID, Status) VALUES (?,?)";

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(TableName.REG_STATUS);
        sql.append(" (uuid, status) VALUES (?,?)");

        String userResponse = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql.toString());
            ps.setString(1, sessionDataKey);
            ps.setString(2, responseStatus);
            ps.executeUpdate();
            SessionExpire sessionExpire = new SessionExpire(sessionDataKey);
            sessionExpire.start();
        } catch (SQLException e) {
            handleException("Error occured while inserting User Response for SessionDataKey: " + sessionDataKey + " " +
                    "to the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, null, ps);
        }
        return userResponse;
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

    /**
     * Update user response.
     *
     * @param sessionDataKey the session data key
     * @param responseStatus the response status
     * @return the string
     * @throws AuthenticatorException the authenticator exception
     */
    public static String updateUserResponse(String sessionDataKey, String responseStatus) throws
            AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        //String sql = "update  clientstatus set Status=? WHERE SessionID=?";
        String userResponse = null;

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE  ");
        sql.append(TableName.CLIENT_STATUS);
        sql.append(" set Status=? WHERE SessionID=?");

        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql.toString());
            ps.setString(1, responseStatus);
            ps.setString(2, sessionDataKey);
            ps.executeUpdate();
        } catch (SQLException e) {
            handleException("Error occured while inserting User Response for SessionDataKey: " + sessionDataKey
                    + " to the database", e);
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
    public static void deleteAuthenticateData(String tokenId) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        //String sql =
        //        "DELETE FROM authenticated_login " +
        //                "WHERE tokenID=?; " ;

        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ");
        sql.append(TableName.AUTHENTICATED_LOGIN);
        sql.append(" WHERE tokenID=?");

        try {
            try {
                connection = getConnectDBConnection();
            } catch (AuthenticatorException e) {
                log.error("Delete authenticate data Error" + e);
            }
            ps = connection.prepareStatement(sql.toString());
            ps.setString(1, tokenId);
            ps.execute();

        } catch (SQLException e) {
            log.error("Error " + e);
        } finally {
            connection.close();
        }
    }

    static int saveRequestType(String msisdn, Integer requestType) throws SQLException, NamingException,
            AuthenticatorException {
        Connection connection = null;
//        String sql = "insert into pendingussd (msisdn, requesttype) values (?,?)";
        String sql = "insert into pendingussd (msisdn, requesttype) values (?,?) ON DUPLICATE KEY UPDATE " +
                "requesttype=VALUES(requesttype)";
        try {
            connection = getConnectDBConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, msisdn);
            ps.setInt(2, requestType);
            ps.executeUpdate();
            return 1;
        } catch (SQLException e) {
            log.error("Error while saving request type ", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return -1;
    }

    public static String insertAuthFlowStatus(String username, String status, String uuid) throws SQLException,
            AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;
        String sql = "INSERT INTO `regstatus` (`uuid`,`username`, `status`) VALUES (?,?,?);";
        connection = getConnectDBConnection();
        ps = connection.prepareStatement(sql);
        ps.setString(1, uuid);
        ps.setString(2, username);
        ps.setString(3, status);
        log.info(ps.toString());
        ps.execute();

        if (connection != null) {
            connection.close();
        }
        return uuid;
    }

    public static void updateIdsRegStatus(String username, String status) throws SQLException, AuthenticatorException {
        Connection connection;
        PreparedStatement ps;

        String sql =
                "update `regstatus` set "
                        + "status=? where "
                        + "username=?;";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setString(1, status);
        ps.setString(2, username);
        log.info(ps.toString());
        ps.execute();

        if (connection != null) {
            connection.close();
        }
    }

    public static void updateAuthenticateData(String msisdn, String status) throws SQLException,
            AuthenticatorException {
        Connection connection = null;
        PreparedStatement ps = null;
        String sql =
                "update `authenticated_login` set "
                        + "status=? where "
                        + "msisdn=?;";

        connection = getConnectDBConnection();
        ps = connection.prepareStatement(sql);
        ps.setString(1, status);
        ps.setString(2, msisdn);
        log.info(ps.toString());
        ps.execute();

        if (connection != null) {
            connection.close();
        }
    }

    public static int readPinAttempts(String sessionId) throws SQLException, AuthenticatorException {

        Connection connection;
        PreparedStatement ps;
        int noOfAttempts = 0;
        ResultSet rs;

        String sql = "select attempts from `multiplepasswords` where " + "ussdsessionid=?;";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setString(1, sessionId);

        rs = ps.executeQuery();

        while (rs.next()) {
            noOfAttempts = rs.getInt("attempts");
        }
        if (connection != null) {
            connection.close();
        }

        return noOfAttempts;
    }

    public static void updateMultiplePasswordNoOfAttempts(String username, int attempts) throws SQLException,
            AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;

        String sql = "update `multiplepasswords` set  attempts=? where  username=?;";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setInt(1, attempts);
        ps.setString(2, username);
        log.info(ps.toString());
        ps.execute();

        if (connection != null) {
            connection.close();
        }
    }

    public static void incrementPinAttempts(String sessionId) throws SQLException, AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;

        String sql = "update multiplepasswords set attempts=attempts +1 where ussdsessionid = ?;";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setString(1, sessionId);
        log.info(ps.toString());
        ps.execute();

        if (connection != null) {
            connection.close();
        }
    }

    public static void updatePin(int pin, String sessionId) throws SQLException, AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;

        String sql = "update multiplepasswords set pin=? where ussdsessionid = ?;";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setInt(1, pin);
        ps.setString(2, sessionId);
        log.info(ps.toString());
        ps.execute();

        if (connection != null) {
            connection.close();
        }
    }

    public static void insertPinAttempt(String msisdn, int attempts, String sessionId) throws SQLException,
            AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;

        String sql = "insert into multiplepasswords(username, attempts, ussdsessionid) values  (?,?,?);";

        connection = getConnectDBConnection();

        ps = connection.prepareStatement(sql);

        ps.setString(1, msisdn);
        ps.setInt(2, attempts);
        ps.setString(3, sessionId);
        log.info(ps.toString());
        ps.execute();

        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Get MSISDN properties by operator Id.
     *
     * @param operatorId   operator Id.
     * @param operatorName operator Name.
     * @return MSISDN properties of given operator.
     * @throws SQLException
     * @throws NamingException
     */
    @Deprecated
    private static List<MSISDNHeader> getMSISDNPropertiesByOperatorId(int operatorId, String operatorName,
                                                                      Connection connection) throws
            SQLException,
            AuthenticatorException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<MSISDNHeader> msisdnHeaderList = new ArrayList<MSISDNHeader>();
        String queryToGetOperatorProperty = "SELECT  msisdnHeaderName, isHeaderEncrypted, encryptionImplementation, " +
                "msisdnEncryptionKey, priority FROM operators_msisdn_headers_properties WHERE operatorId = ? ORDER BY" +
                " priority ASC";
        try {
            preparedStatement = connection.prepareStatement(queryToGetOperatorProperty);
            preparedStatement.setInt(1, operatorId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                MSISDNHeader msisdnHeader = new MSISDNHeader();
                msisdnHeader.setMsisdnHeaderName(resultSet.getString("msisdnHeaderName"));
                msisdnHeader.setHeaderEncrypted(resultSet.getBoolean("isHeaderEncrypted"));
                msisdnHeader.setHeaderEncryptionMethod(resultSet.getString("encryptionImplementation"));
                msisdnHeader.setHeaderEncryptionKey(resultSet.getString("msisdnEncryptionKey"));
                msisdnHeader.setPriority(resultSet.getInt("priority"));
                msisdnHeaderList.add(msisdnHeader);
            }
        } catch (SQLException e) {
            log.error("Error occurred while retrieving operator MSISDN properties of operator : " + operatorName, e);
            throw e;
        } finally {
            IdentityDatabaseUtil.closeAllConnections(null, resultSet, preparedStatement);
        }
        return msisdnHeaderList;
    }

    public static Set<String> getAllowedAuthenticatorSetForMNO(String mobileNetworkOperator)
            throws AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT *");
        sql.append(" from ");
        sql.append(TableName.ALLOWED_AUTHENTICATORS_MNO);
        sql.append(" where mobile_network_operator=?");

        Set<String> authenticatorSet = new HashSet<>();
        try {
            connection = getConnectDBConnection();
            ps = connection.prepareStatement(sql.toString());
            ps.setString(1, mobileNetworkOperator);
            rs = ps.executeQuery();
            while (rs.next()) {
                authenticatorSet.add(rs.getString("allowed_authenticator"));
            }
        } catch (SQLException e) {
            handleException("Error occurred while retrieving allowed authenticators for " + mobileNetworkOperator, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, ps);
        }
        return authenticatorSet;
    }

    public static Set<String> getAllowedAuthenticatorSetForSP(String serviceProvider)
            throws AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT *");
        sql.append(" from ");
        sql.append(TableName.ALLOWED_AUTHENTICATORS_SP);
        sql.append(" where client_id=?");

        Set<String> authenticatorSet = new HashSet<>();
        try {
            connection = getConnectDBConnection();
            ps = connection.prepareStatement(sql.toString());
            ps.setString(1, serviceProvider);
            rs = ps.executeQuery();
            while (rs.next()) {
                authenticatorSet.add(rs.getString("allowed_authenticator"));
            }
        } catch (SQLException e) {
            handleException("Error occurred while retrieving allowed authenticators for " + serviceProvider, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, ps);
        }
        return authenticatorSet;
    }

    /*
    public static int saveStatusData(UserStatus userStatus) throws SQLException,AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnectDBConnection();

            String ADD_USER_STATUS =  " INSERT INTO USER_STATUS (Time, Status, Msisdn, State, Nonce, Scope, AcrValue,
             SessionId, IsMsisdnHeader, IpHeader," +
                    "IsNewUser, LoginHint, Operator, UserAgent, Comment, ConsumerKey) values (?,?,?,?,?,?,?,?,?,?,?,
                    ?,?,?,?,?)";

            ps = conn.prepareStatement(ADD_USER_STATUS);

            ps.setTimestamp(1, new java.sql.Timestamp( new java.util.Date().getTime()));
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
            handleException("Error occured while inserting User status for SessionDataKey: " + userStatus
            .getSessionId() + " to the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, null, ps);
        }
        return -1;
    }
*/


    public static void insertHashKeyContextIdentifierMapping(String hashKey, String contextIdentifier)
            throws AuthenticatorException {
        String sql = "insert into sms_hashkey_contextid_mapping(hashkey, contextid) values  (?,?);";

        if (log.isDebugEnabled()) {
            log.debug("Executing the query " + sql + " for hash key " + hashKey + " and context identifier "
                    + contextIdentifier);
        }

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = getConnectDBConnection();
            ps = connection.prepareStatement(sql.toString());
            ps.setString(1, hashKey);
            ps.setString(2, contextIdentifier);
            ps.executeUpdate();
        } catch (SQLException e) {
            handleException(e.getMessage(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, ps);
        }
    }


    /**
     * Get prompt data
     *
     * @param scope
     * @param prompt
     * @param isLoginHintExists
     * @return PromptData
     */
    public static PromptData getPromptData(String scope, String prompt, Boolean isLoginHintExists) {
        PromptData promptData = new PromptData();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sql = "SELECT * FROM prompt_configuration WHERE scope = ? AND prompt_value = ? AND is_login_hint_exists = ?";

        try {
            connection = getConnectDBConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, scope);
            ps.setString(2, prompt);
            ps.setBoolean(3, isLoginHintExists);
            rs = ps.executeQuery();
            while (rs.next()) {
                promptData.setScope(rs.getString("scope"));
                promptData.setLoginHintExists(rs.getBoolean("is_login_hint_exists"));
                promptData.setPromptValue(rs.getString("prompt_value"));
                promptData.setBehaviour(PromptData.behaviorTypes.valueOf(rs.getString("behaviour")));
            }
        } catch (SQLException ex) {
            handleException("Error while retrieving Propmt Data ", ex);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rs, ps);
            return promptData;
        }
    }

    /**
     * Insert otp for sms authentication.
     *
     * @param sessionDataKey the session data key
     * @param otp the smsotp
     * @param status the status
     * @return the string
     * @throws AuthenticatorException the authenticator exception
     */
    public static String insertOTPForSMS(String sessionDataKey, String otp,String status) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(TableName.SMS_OTP);
        sql.append(" (session_id, otp,status) VALUES (?,?,?)");
        String userResponse = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql.toString());
            ps.setString(1, sessionDataKey);
            ps.setString(2, otp);
            ps.setString(3,status);
            ps.executeUpdate();
        } catch (SQLException e) {
            handleException("Error occured while inserting SMS OTP for SessionDataKey: " + sessionDataKey + " " +
                    "to the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, null, ps);
        }
        return userResponse;
    }


    /**
     * Update otp for sms authentication.
     *
     * @param sessionDataKey the session data key
     * @param status the status
     * @return the string
     * @throws AuthenticatorException the authenticator exception
     */
    public static String updateOTPForSMS(String sessionDataKey, String status) throws AuthenticatorException {
        Connection conn = null;
        PreparedStatement ps = null;
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(TableName.SMS_OTP);
        sql.append(" SET status=? WHERE session_id=?");
        String userResponse = null;
        try {
            conn = getConnectDBConnection();
            ps = conn.prepareStatement(sql.toString());
            ps.setString(1, status);
            ps.setString(2, sessionDataKey);
            ps.executeUpdate();
        } catch (SQLException e) {
            handleException("Error occured while updating SMS OTP for SessionDataKey: " + sessionDataKey + " " +
                    "to the database", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(conn, null, ps);
        }
        return userResponse;
    }

    /**
     * Update reg status.
     *
     * @param sessionID the session id
     * @param status    the status
     * @throws AuthenticatorException the Authentication exception
     */
    public static void updateAuthFlowStatus(String sessionID, String status) throws AuthenticatorException {

        Connection connection = null;
        PreparedStatement ps = null;

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE  ");
        sql.append(TableName.REG_STATUS);
        sql.append(" SET status=? WHERE uuid=?");

        try {
            connection = getConnectDBConnection();
            ps = connection.prepareStatement(sql.toString());
            ps.setString(1, status);
            ps.setString(2, sessionID);
            log.info(ps.toString());
            ps.execute();
        } catch (SQLException e) {
            handleException("Error occured while updating Timeout Response for SessionDataKey: " + sessionID
                    + " to the database", e);
        }
        finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, ps);
        }
    }


}
