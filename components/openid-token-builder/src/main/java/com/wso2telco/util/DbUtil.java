/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 * 
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class DbUtil.
 */
public class DbUtil {

/** The Constant log. */
private static final Log log = LogFactory.getLog(DbUtil.class);
	
	/** The Constant connectDataSourceName. */
	private static final String connectDataSourceName = "jdbc/CONNECT_DB";
	
	/** The connect datasource. */
	private static volatile DataSource connectDatasource = null;
	
	/**
	 * Initialize data source.
	 *
	 * @throws Exception the exception
	 */
	public static void initializeDataSource() throws Exception {
		getConnectDataSource();
		
	}
	
	/**
	 * Gets the connect data source.
	 *
	 * @return the connect data source
	 * @throws Exception the exception
	 */
	public static void getConnectDataSource() throws Exception {
		if (connectDatasource != null) {
			return;
		}
		if (connectDataSourceName != null) {
			try {
				Context ctx = new InitialContext();
				connectDatasource = (DataSource) ctx
						.lookup(connectDataSourceName);
			} catch (NamingException e) {
				throw new Exception("Error while looking up the data " + "source: "
								+ connectDataSourceName);
			}
		}
	}
	
	/**
	 * Gets the connect db connection.
	 *
	 * @return the connect db connection
	 * @throws SQLException the SQL exception
	 * @throws Exception the exception
	 */
	public static Connection getConnectDBConnection() throws SQLException, Exception {
		initializeDataSource();
		if (connectDatasource != null) {
			return connectDatasource.getConnection();
		} else {
			throw new SQLException(
					"Connect Datasource not initialized properly.");
		}
	}
	
	/**
	 * Close all connections.
	 *
	 * @param preparedStatement the prepared statement
	 * @param connection the connection
	 * @param resultSet the result set
	 */
	public static void closeAllConnections(PreparedStatement preparedStatement, 
			Connection connection, ResultSet resultSet) {
		
		closeConnection(connection);
		closeStatement(preparedStatement);
		closeResultSet(resultSet);
	}
	
     
    /**
     * Close connection.
     *
     * @param dbConnection the db connection
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
     * Close result set.
     *
     * @param resultSet the result set
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
     * Close statement.
     *
     * @param preparedStatement the prepared statement
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
