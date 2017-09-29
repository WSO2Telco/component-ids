package com.wso2telco.gsma.authenticators.dao.impl;

import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.dao.AttributeConfigDAO;
import com.wso2telco.gsma.authenticators.model.SPConsent;
import com.wso2telco.gsma.authenticators.model.UserConsentDetails;
import com.wso2telco.gsma.authenticators.model.UserConsentHistory;
import com.wso2telco.gsma.authenticators.util.TableName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

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

/**
 * Created by  on 7/26/17.
 */
public class AttributeConfigDAOimpl implements AttributeConfigDAO {


    /**
     * The m connect datasource.
     */
    private static volatile DataSource mConnectDatasource = null;
    private static final Log log = LogFactory.getLog(AttributeConfigDAOimpl.class);
    private static final String SELECT = "SELECT";
    private static final String FROM = "FROM";
    private static final String ERR_MSG = "Error occurred while retrieving consent details for client_id : ";

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
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
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

    public List<SPConsent> getScopeExprieTime(String operator, String consumerKey, String scope)
            throws SQLException, NamingException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String[] scopeValues =(scope.toString().substring(1,scope.toString().length()-1)).split(",") ;

        StringBuilder params = new StringBuilder("(select param_id from scope_parameter where scope= ?)");
        for (int i = 1; i < scopeValues.length; i++) {
            params.append(",(select param_id from scope_parameter where scope= ?)");
        }

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SELECT);
        sqlBuilder.append("con.scope_id,con.exp_period,con.operator_id ");
        sqlBuilder.append(FROM);
        sqlBuilder.append("consent con");
        sqlBuilder.append(" where ");
        sqlBuilder.append(" con.operator_id=? ");
        sqlBuilder.append(" AND con.client_id=? AND ");
        sqlBuilder.append(" con.scope_id in (" + params + ")");

        List<SPConsent> spConsentList = new ArrayList();

        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(sqlBuilder.toString());
            preparedStatement.setInt(1, 1);
            preparedStatement.setString(2, consumerKey);
            for (int i = 0; i < scopeValues.length; i++) {
                preparedStatement.setString(i + 3, scopeValues[i].trim());
            }


            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                SPConsent spConsent = new SPConsent();
                spConsent.setScope(resultSet.getInt("scope_id"));
                spConsent.setExpPeriod(resultSet.getInt(Constants.EXP_PERIOD));
                spConsent.setOperatorID(resultSet.getInt("operator_id"));
                spConsentList.add(spConsent);
            }
        } catch (SQLException e) {
            throw new SQLException(ERR_MSG,
                    e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return spConsentList;
    }

    public List<ScopeParam> getScopeParams(String scopes) throws SQLException, NamingException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String[] scopeValues = scopes.split("\\s+|\\+");
        StringBuilder params = new StringBuilder("?");

        for (int i = 1; i < scopeValues.length; i++) {
            params.append(",?");
        }

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SELECT);
        sqlBuilder.append("scp.scope,con.consent_type,vt.validity_type ");
        sqlBuilder.append(FROM);
        sqlBuilder.append("scope_parameter scp ");
        sqlBuilder.append("INNER JOIN consent_type con ON scp.consent_type=con.consent_typeID ");
        sqlBuilder.append("INNER JOIN consent_validity_type vt ON scp.consent_validity_type=vt.validity_id ");
        sqlBuilder.append(" where ");
        sqlBuilder.append(" scp.scope in (" + params + ")");

        List<ScopeParam> scopeParams = new ArrayList();
        try {

            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(sqlBuilder.toString());
            for (int i = 0; i < scopeValues.length; i++) {
                preparedStatement.setString(i + 1, scopeValues[i]);
            }
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ScopeParam scopeParam = new ScopeParam();
                scopeParam.setScope(resultSet.getString("scope"));
                scopeParam.setConsentValidityType(resultSet.getString("validity_type"));
                scopeParam.setConsentType(resultSet.getString("consent_type"));
                scopeParams.add(scopeParam);
            }


        } catch (SQLException e) {
            throw new SQLException(ERR_MSG,
                    e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return scopeParams;

    }


    public UserConsentDetails getUserConsentDetails(UserConsentDetails userConsentDetails) throws SQLException, NamingException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        UserConsentDetails userConsent=null;

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SELECT);
        sqlBuilder.append("usercon.consent_status as revokeStatus ,usercon.consent_expire_time as consent_expire_time ");
        sqlBuilder.append(FROM);
        sqlBuilder.append(TableName.CONSENT_HISTORY + " usercon ");
        sqlBuilder.append("INNER JOIN scope_parameter scp ON scp.param_id=usercon.scope_id");
        sqlBuilder.append(" where");
        sqlBuilder.append(" scp.scope=? ");
        sqlBuilder.append(" AND usercon.operator_id=? ");
        sqlBuilder.append(" AND usercon.client_id=? ");
        sqlBuilder.append("AND usercon.msisdn=?");

        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(sqlBuilder.toString());
            preparedStatement.setString(1, userConsentDetails.getScope());
            preparedStatement.setInt(2, userConsentDetails.getOperatorID());
            preparedStatement.setString(3, userConsentDetails.getConsumerKey());
            preparedStatement.setString(4, userConsentDetails.getMsisdn());

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                userConsent = new UserConsentDetails();
                userConsent.setConsumerKey(userConsentDetails.getConsumerKey());
                userConsent.setScope(userConsentDetails.getScope());
                userConsent.setOperatorID(userConsentDetails.getOperatorID());
                userConsent.setMsisdn(userConsentDetails.getMsisdn());
                userConsent.setRevokeStatus(resultSet.getString("revokeStatus"));
                userConsent.setConsentExpireDatetime(resultSet.getString("consent_expire_time"));

            }
        } catch (SQLException e) {
            throw new SQLException(ERR_MSG,
                    e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return userConsent;


    }

    @Override
    public void saveUserConsentedAttributes(List<UserConsentHistory> userConsentHistory) throws SQLException, NamingException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO ");
        sqlBuilder.append(TableName.CONSENT_HISTORY + " (");
        sqlBuilder.append("msisdn,client_id,scope_id,operator_id,consent_date,consent_expire_time,consent_status )");
        sqlBuilder.append("VALUES ");
        sqlBuilder.append("( ?,?,?,?,?,?,? )");


        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(sqlBuilder.toString());
            for (UserConsentHistory userConsentHistory1 : userConsentHistory) {

                preparedStatement.setString(1, userConsentHistory1.getMsisdn());
                preparedStatement.setString(2, userConsentHistory1.getClient_id());
                preparedStatement.setInt(3, userConsentHistory1.getScope_id());
                preparedStatement.setInt(4, userConsentHistory1.getOperator_id());
                preparedStatement.setString(5, userConsentHistory1.getConsent_date());
                preparedStatement.setString(6, userConsentHistory1.getConsent_expire_time());
                preparedStatement.setString(7, userConsentHistory1.getConsent_status());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();

        } catch (SQLException e) {
            throw new SQLException(ERR_MSG,
                    e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

    }

    public String getSPConfigValue(String operator, String clientID, String key)
            throws SQLException, NamingException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(SELECT);
        sqlBuilder.append("sp_config.config_value ");
        sqlBuilder.append(FROM);
        sqlBuilder.append(TableName.SP_CONFIGURATION + " sp_config");
        sqlBuilder.append(" where");
        sqlBuilder.append(" sp_config.client_id=? ");
        sqlBuilder.append(" AND sp_config.operator=? ");
        sqlBuilder.append(" AND sp_config.config_key=? ");

        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(sqlBuilder.toString());
            preparedStatement.setString(1, clientID);
            preparedStatement.setString(2, operator);
            preparedStatement.setString(3, key);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString("config_value");
            }
        } catch (SQLException e) {
            throw new SQLException(ERR_MSG,
                    e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return null;
    }

}
