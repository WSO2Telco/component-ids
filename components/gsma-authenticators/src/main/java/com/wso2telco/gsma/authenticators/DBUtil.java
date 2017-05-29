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
import com.wso2telco.gsma.authenticators.model.Operator;
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

	public static ScopeParam getScopeDetails(String scope) throws SQLException, NamingException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		ScopeParam parameters = new ScopeParam();
		String queryToGetScopeDetails = "SELECT param_id,is_consent_page,description FROM scope_parameter WHERE scope=?";
		try {
			connection = getConnectDBConnection();
			preparedStatement = connection.prepareStatement(queryToGetScopeDetails);
			preparedStatement.setString(1, scope);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				parameters.setScope_id(resultSet.getInt("param_id"));
				parameters.setConsentPage(resultSet.getBoolean("is_consent_page"));
				parameters.setDescription(resultSet.getString("description"));
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

	public static Consent getConsentDetails(String scope, String clientID, int operatorID)
			throws SQLException, NamingException {
		Consent consentProp = new Consent();
		ScopeParam params = getScopeDetails(scope);
		if (params.isConsentPage()) {
			Connection connection = null;
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			String queryToGetConsent = "SELECT approve_status FROM consent WHERE scope_id=? AND client_id=? AND operator_id=?";
			try {
				connection = getConnectDBConnection();
				preparedStatement = connection.prepareStatement(queryToGetConsent);
				preparedStatement.setInt(1, params.getScope_id());
				preparedStatement.setString(2, clientID);
				preparedStatement.setInt(3, operatorID);
				resultSet = preparedStatement.executeQuery();
				while (resultSet.next()) {
						consentProp.setOperatorID(operatorID);
						consentProp.setStatus(resultSet.getString("approve_status"));
						consentProp.setConsumerKey(clientID);
						consentProp.setScope(scope);
						consentProp.setDescription(params.getDescription());
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

	public static UserConsent getUserConsentDetails(String msisdn, String scope, String clientID, int operatorID)
			throws SQLException, NamingException {
		UserConsent consentProp = new UserConsent();
		ScopeParam params = getScopeDetails(scope);
		if (params.isConsentPage()) {
			Connection connection = null;
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			String queryToGetUserConsent = "SELECT approve FROM user_consent WHERE msisdn=? AND scope_id=? AND client_id=? AND operator_id=?";
			try {
				connection = getConnectDBConnection();
				preparedStatement = connection.prepareStatement(queryToGetUserConsent);
				preparedStatement.setString(1, msisdn);
				preparedStatement.setInt(2, params.getScope_id());
				preparedStatement.setString(3, clientID);
				preparedStatement.setInt(4, operatorID);
				resultSet = preparedStatement.executeQuery();
				while (resultSet.next()) {
						consentProp.setMsisdn(msisdn);
						consentProp.setScope(scope);
						consentProp.setConsumerKey(clientID);
						consentProp.setOperatorID(operatorID);
						consentProp.setIs_approved(resultSet.getBoolean("approve"));
				}
			} catch (SQLException e) {
				throw new SQLException("Error occurred while retrieving user consent details for msisdn :- " + msisdn
						+ ",operator :-" + operatorID + ",scope :-" + scope, e);
			} catch (NamingException e) {
				throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
			} finally {
				closeAllConnections(preparedStatement, connection, resultSet);
			}
		}
		return consentProp;
	}
	
	public static UserConsent getUserConsentDenyDetails(String msisdn, String scope, String clientID, int operatorID)
			throws SQLException, NamingException {
		UserConsent consentProp = new UserConsent();
		ScopeParam params = getScopeDetails(scope);
		if (params.isConsentPage()) {
			Connection connection = null;
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			String queryToGetUserConsentDeny = "SELECT deny FROM user_consent_deny WHERE msisdn=? AND scope_id=? AND client_id=? AND operator_id=?";
			try {
				connection = getConnectDBConnection();
				preparedStatement = connection.prepareStatement(queryToGetUserConsentDeny);
				preparedStatement.setString(1, msisdn);
				preparedStatement.setInt(2, params.getScope_id());
				preparedStatement.setString(3, clientID);
				preparedStatement.setInt(4, operatorID);
				resultSet = preparedStatement.executeQuery();
				while (resultSet.next()) {
						consentProp.setMsisdn(msisdn);
						consentProp.setScope(scope);
						consentProp.setConsumerKey(clientID);
						consentProp.setOperatorID(operatorID);
						consentProp.setIs_approved(resultSet.getBoolean("deny"));
				}
			} catch (SQLException e) {
				throw new SQLException("Error occurred while retrieving user consent deny details for msisdn :- " + msisdn
						+ ",operator :-" + operatorID + ",scope :-" + scope, e);
			} catch (NamingException e) {
				throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
			} finally {
				closeAllConnections(preparedStatement, connection, resultSet);
			}
		}
		return consentProp;
	}

	public static void insertUserConsentDetails(String msisdn, String scope, String clientID, int operatorID)
			throws SQLException, NamingException {
		UserConsent consentProp = getUserConsentDetails(msisdn, scope, clientID, operatorID);
		if (consentProp.getConsumerKey() == null && consentProp.getMsisdn() == null && consentProp.getScope() == null) {
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
				preparedStatement.setInt(4, operatorID);
				preparedStatement.execute();
			} catch (SQLException e) {
				throw new SQLException("Error occurred while inserting user consent details for msisdn :- " + msisdn
						+ ",operator :-" + operatorID + ",scope :-" + scope, e);
			} catch (NamingException e) {
				throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
			} finally {
				closeAllConnections(preparedStatement, connection, resultSet);
			}
		}
	}
	
	public static void insertConsentHistoryDetails(String msisdn, String scope, String clientID, int operatorID,String status)
			throws SQLException, NamingException {
			Connection connection = null;
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			String queryToInsertConsentHistory = "INSERT INTO consent_history (msisdn, client_id, scope_id, operator_id,approve_status,consent_date) VALUES(?,?,?,?,?,?)";
			try {
				connection = getConnectDBConnection();
				preparedStatement = connection.prepareStatement(queryToInsertConsentHistory);
				preparedStatement.setString(1, msisdn);
				preparedStatement.setString(2, clientID);
				preparedStatement.setInt(3, getScopeDetails(scope).getScope_id());
				preparedStatement.setInt(4, operatorID);
				preparedStatement.setString(5, status);
				preparedStatement.setTimestamp(6, new java.sql.Timestamp(new java.util.Date().getTime()));
				preparedStatement.execute();
			} catch (SQLException e) {
				throw new SQLException("Error occurred while inserting consent history details for msisdn :- " + msisdn
						+ ",operator :-" + operatorID + ",scope :-" + scope, e);
			} catch (NamingException e) {
				throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
			} finally {
				closeAllConnections(preparedStatement, connection, resultSet);
			}
	}
	
	public static Operator getOperatorDetails(String operator) throws SQLException, NamingException {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Operator parameters = new Operator();
		String queryToGetOperatorDetails = "SELECT ID FROM operators WHERE operatorname=?";
		try {
			connection = getConnectDBConnection();
			preparedStatement = connection.prepareStatement(queryToGetOperatorDetails);
			preparedStatement.setString(1, operator);
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				parameters.setOperatorId(resultSet.getInt("ID"));
			}
		} catch (SQLException e) {
			throw new SQLException("Error occurred while retrieving operator details for operator : " + operator, e);
		} catch (NamingException e) {
			throw new ConfigurationException("DataSource could not be found in mobile-connect.xml");
		} finally {
			closeAllConnections(preparedStatement, connection, resultSet);
		}
		return parameters;
	}
	
	public static String getSPConfigValue(String operator, String clientID, String key)
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
				closeAllConnections(preparedStatement, connection, resultSet);
			}
		return null;
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