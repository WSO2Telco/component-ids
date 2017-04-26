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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.model.Consent;
import com.wso2telco.gsma.authenticators.model.UserConsent;

/**
 * This class is used to read and update consent properties.
 */
public class DBUtil {
	private static final Log log = LogFactory.getLog(DBUtil.class);

	/**
	 * The m connect datasource.
	 */
	private static volatile DataSource mConnectDatasource = null;

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

	/**
	 * To get scope parameter details for scope
	 * 
	 * @param scope
	 * @return
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static ScopeParam getScopeDetails(String scope) throws SQLException, NamingException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		ScopeParam parameters = new ScopeParam();
		String queryToGetScopeDetails = "SELECT param_id,is_consent_page FROM scope_parameter WHERE scope=?";
		try {
			connection = getConnectDBConnection();
			preparedStatement = connection.prepareStatement(queryToGetScopeDetails);
			preparedStatement.setString(1, scope);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				parameters.setScope_id(resultSet.getInt("param_id"));
				parameters.setConsentPage(resultSet.getBoolean("is_consent_page"));
			}
		} catch (SQLException e) {
			throw new SQLException("Error occurred while retrieving scopeID for scope : " + scope, e);
		} catch (NamingException e) {
			throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
		} finally {
			closeAllConnections(preparedStatement, connection, resultSet);
		}
		return parameters;
	}

	/**
	 * To get consent details for the scope and client id
	 * 
	 * @param scope
	 * @param clientID
	 * @param operator
	 * @return
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static Consent getConsentDetails(String scope, String clientID, String operator)
			throws SQLException, NamingException {
		Consent consentProp = new Consent();
		ScopeParam params = getScopeDetails(scope);
		if (params.isConsentPage()) {
			Connection connection = null;
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			String queryToGetConsent = "SELECT operator,approve_status FROM consent WHERE scope_id=? AND client_id=?";
			try {
				connection = getConnectDBConnection();
				preparedStatement = connection.prepareStatement(queryToGetConsent);
				preparedStatement.setInt(1, params.getScope_id());
				preparedStatement.setString(2, clientID);
				resultSet = preparedStatement.executeQuery();
				while (resultSet.next()) {
					if (resultSet.getString("operator").equalsIgnoreCase("all")) {
						consentProp.setOperator(resultSet.getString("operator"));
						consentProp.setStatus(resultSet.getString("approve_status"));
						consentProp.setConsumerKey(clientID);
						consentProp.setScope(scope);
						return consentProp;
					} else if (resultSet.getString("operator").equalsIgnoreCase(operator)) {
						consentProp.setOperator(resultSet.getString("operator"));
						consentProp.setStatus(resultSet.getString("approve_status"));
						consentProp.setConsumerKey(clientID);
						consentProp.setScope(scope);
					}
				}
			} catch (SQLException e) {
				throw new SQLException("Error occurred while retrieving consent details for client_id : " + clientID,
						e);
			} catch (NamingException e) {
				throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
			} finally {
				closeAllConnections(preparedStatement, connection, resultSet);
			}
		}
		return consentProp;
	}

	public static UserConsent getUserConsentDetails(String msisdn, String scope, String clientID, String operator)
			throws SQLException, NamingException {
		UserConsent consentProp = new UserConsent();
		ScopeParam params = getScopeDetails(scope);
		if (params.isConsentPage()) {
			Connection connection = null;
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			String queryToGetUserConsent = "SELECT operator,approve FROM user_consent WHERE msisdn=? AND scope_id=? AND client_id=?";
			try {
				connection = getConnectDBConnection();
				preparedStatement = connection.prepareStatement(queryToGetUserConsent);
				preparedStatement.setString(1, msisdn);
				preparedStatement.setInt(2, params.getScope_id());
				preparedStatement.setString(3, clientID);
				resultSet = preparedStatement.executeQuery();
				while (resultSet.next()) {
					if (resultSet.getString("operator").equalsIgnoreCase("all")) {
						consentProp.setMsisdn(msisdn);
						consentProp.setScope(scope);
						consentProp.setConsumerKey(clientID);
						consentProp.setOperator(resultSet.getString("operator"));
						consentProp.setIs_approved(resultSet.getBoolean("approve"));
						return consentProp;
					} else if (resultSet.getString("operator").equalsIgnoreCase(operator)) {
						consentProp.setMsisdn(msisdn);
						consentProp.setScope(scope);
						consentProp.setConsumerKey(clientID);
						consentProp.setOperator(resultSet.getString("operator"));
						consentProp.setIs_approved(resultSet.getBoolean("approve"));
					}
				}
			} catch (SQLException e) {
				throw new SQLException("Error occurred while retrieving user consent details for msisdn :- " + msisdn
						+ ",operator :-" + operator + ",scope :-" + scope, e);
			} catch (NamingException e) {
				throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
			} finally {
				closeAllConnections(preparedStatement, connection, resultSet);
			}
		}
		return consentProp;
	}

	public static void insertUserConsentDetails(String msisdn, String scope, String clientID, String operator)
			throws SQLException, NamingException {
		UserConsent consentProp = getUserConsentDetails(msisdn, scope, clientID, operator);
		if (consentProp.getConsumerKey() == null && consentProp.getMsisdn() == null && consentProp.getOperator() == null
				&& consentProp.getScope() == null) {
			Connection connection = null;
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			String queryToInsertUserConsent = "INSERT INTO user_consent VALUES(?,?,?,?,true)";
			try {
				connection = getConnectDBConnection();
				preparedStatement = connection.prepareStatement(queryToInsertUserConsent);
				preparedStatement.setString(1, msisdn);
				preparedStatement.setString(2, clientID);
				preparedStatement.setInt(3, getScopeDetails(scope).getScope_id());
				preparedStatement.setString(4, operator);
				preparedStatement.execute();
			} catch (SQLException e) {
				throw new SQLException("Error occurred while inserting user consent details for msisdn :- " + msisdn
						+ ",operator :-" + operator + ",scope :-" + scope, e);
			} catch (NamingException e) {
				throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
			} finally {
				closeAllConnections(preparedStatement, connection, resultSet);
			}
		}
	}

	private static void closeAllConnections(PreparedStatement preparedStatement, Connection connection,
			ResultSet resultSet) {
		closeResultSet(resultSet);
		closeStatement(preparedStatement);
		closeConnection(connection);
	}

	/**
	 * Close Connection
	 *
	 * @param dbConnection
	 *            Connection
	 */
	private static void closeConnection(Connection dbConnection) {
		if (dbConnection != null) {
			try {
				dbConnection.close();
			} catch (SQLException e) {
				log.error("Database error. Could not close database connection. Continuing with others. - "
						+ e.getMessage(), e);
			}
		}
	}

	/**
	 * Close ResultSet
	 *
	 * @param resultSet
	 *            ResultSet
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
	 * @param preparedStatement
	 *            PreparedStatement
	 */
	private static void closeStatement(PreparedStatement preparedStatement) {
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				log.error("Database error. Could not close PreparedStatement. Continuing with others. - "
						+ e.getMessage(), e);
			}
		}
	}

}