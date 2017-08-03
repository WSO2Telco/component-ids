package com.wso2telco.gsma.authenticators.dao.impl;

import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtil;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.dao.SpconfigDAO;
import com.wso2telco.gsma.authenticators.model.SPConsent;
import com.wso2telco.gsma.authenticators.model.UserConsent;
import com.wso2telco.gsma.authenticators.model.UserConsentDetails;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by  on 7/26/17.
 */
public class SpconfigDAOimpl implements SpconfigDAO {


    /**
     * The m connect datasource.
     */
    private static volatile DataSource mConnectDatasource = null;
    private static final Log log = LogFactory.getLog(SpconfigDAOimpl.class);

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
     * Gets the connect db connection.
     *
     * @return the connect db connection
     * @throws SQLException
     *             the SQL exception
     * @throws AuthenticatorException
     *             the authenticator exception
     */
    private static Connection getConnectDBConnection() throws SQLException, NamingException {
        initializeConnectDatasource();

        if (mConnectDatasource != null) {
            return mConnectDatasource.getConnection();
        }
        throw new SQLException("Connect Datasource not initialized properly");
    }

    public SPConsent getSpConsentDetails(SPConsent spConsent)
			throws SQLException, NamingException {

			Connection connection = null;
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT");
        sqlBuilder.append("con.exp_period as exp_period ,scp_param.consent_type,scp_param.consent_validity_type ");
        sqlBuilder.append("FROM ");
        sqlBuilder.append(TableName.CONSENT + "con,");
        sqlBuilder.append(TableName.SCOPE_PARAMETER + "scp_param");
        sqlBuilder.append(" where");
        sqlBuilder.append(" con.scope_id = scp_param.scope_id ");
        sqlBuilder.append(" AND con.scope=? ");
        sqlBuilder.append(" AND con.operator_id=? ");
        sqlBuilder.append(" AND con.client_id=? ");

			try {
				connection = getConnectDBConnection();
				preparedStatement = connection.prepareStatement(sqlBuilder.toString());
				preparedStatement.setString(1, spConsent.getScope());
				preparedStatement.setInt(2, spConsent.getOperatorID());
				preparedStatement.setString(3, spConsent.getConsumerKey());

				resultSet = preparedStatement.executeQuery();
				while (resultSet.next()) {
                    spConsent.setConsumerKey(spConsent.getConsumerKey());
                    spConsent.setScope(spConsent.getScope());
                    spConsent.setOperatorID(spConsent.getOperatorID());
                    spConsent.setExpPeriod(resultSet.getInt(Constants.EXP_PERIOD));
                    spConsent.setValidityType(resultSet.getString(Constants.CONSENT_VALITITY_TYPE));
                    spConsent.setConsentType(resultSet.getString(Constants.CONSENT_TYPE));

				}
			} catch (SQLException e) {
				throw new SQLException("Error occurred while retrieving consent details for client_id : ",
						e);
			}   finally {
                IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
			}
		//}
		return spConsent;
	}

	public UserConsentDetails getUserConsentDetails(UserConsentDetails userConsentDetails) throws SQLException,NamingException {

	    Connection connection = null;
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT");
        sqlBuilder.append("usercon.revokeStatus as revokeStatus ,usercon.consent_expire_time as consent_expire_time,usercon.consentRevokeDatetime as consentRevokeDatetime ");
        sqlBuilder.append("FROM ");
        sqlBuilder.append(TableName.CONSENT + "usercon,");
        sqlBuilder.append(TableName.SCOPE_PARAMETER + "scp_param");
        sqlBuilder.append(" where");
        sqlBuilder.append(" con.scope_id = scp_param.scope_id ");
        sqlBuilder.append(" AND con.scope_id=? ");
        sqlBuilder.append(" AND con.operator_id=? ");
        sqlBuilder.append(" AND con.client_id=? ");





			try {
				connection = getConnectDBConnection();
				preparedStatement = connection.prepareStatement(sqlBuilder.toString());
				preparedStatement.setString(1, userConsentDetails.getScope());
				preparedStatement.setInt(2, userConsentDetails.getOperatorID());
				preparedStatement.setString(3, userConsentDetails.getConsumerKey());

				resultSet = preparedStatement.executeQuery();
				while (resultSet.next()) {
                    userConsentDetails.setConsumerKey(userConsentDetails.getConsumerKey());
                    userConsentDetails.setScope(userConsentDetails.getScope());
                    userConsentDetails.setOperatorID(userConsentDetails.getOperatorID());
                    userConsentDetails.setMsisdn(userConsentDetails.getMsisdn());
                    userConsentDetails.setRevokeStatus(resultSet.getString("revokeStatus"));
                   // userConsentDetails.getConsentExpireDatetime(resultSet.getString("consent_expire_time"));
                   // userConsentDetails.getRevokeStatus(resultSet.getString("consentRevokeDatetime"));

				}
			} catch (SQLException e) {
				throw new SQLException("Error occurred while retrieving consent details for client_id : ",
						e);
			}   finally {
                IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
			}
		//}
		return userConsentDetails;


    }

    public String getSPConfigValue(String operator, String clientID, String key)
            throws SQLException, NamingException {
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
            throw new SQLException("Error occurred while retrieving config_value for client_id : " + clientID,
                    e);
        } catch (NamingException e) {
            throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection,resultSet,preparedStatement);
        }
        return null;
    }



   /* public static Map<String, String> getScopeParams(String scope) throws SQLException, NamingException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String[] scopeValues = scope.split("\\s+|\\+");
        StringBuilder params = new StringBuilder("?");
        for (int i = 1; i < scopeValues.length; i++) {
            params.append(",?");
        }
        String sql = "SELECT * FROM `scope_parameter` WHERE scope in (" + params + ")";
        Map scopeParamsMap = new HashMap();
        try {
            connection = getConnectDBConnection();
            preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < scopeValues.length; i++) {
                preparedStatement.setString(i + 1, scopeValues[i]);
            }
            resultSet = preparedStatement.executeQuery();

            while (results.next()) {


            }


    }}


*/



}
