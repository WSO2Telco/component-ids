package com.axiata.dialog.util;

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

public class DbUtil {

private static final Log log = LogFactory.getLog(DbUtil.class);
	
	private static final String connectDataSourceName = "jdbc/CONNECT_DB";
	private static volatile DataSource connectDatasource = null;
	
	public static void initializeDataSource() throws Exception {
		getConnectDataSource();
		
	}
	
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
	
	public static Connection getConnectDBConnection() throws SQLException, Exception {
		initializeDataSource();
		if (connectDatasource != null) {
			return connectDatasource.getConnection();
		} else {
			throw new SQLException(
					"Connect Datasource not initialized properly.");
		}
	}
	
	public static void closeAllConnections(PreparedStatement preparedStatement, 
			Connection connection, ResultSet resultSet) {
		
		closeConnection(connection);
		closeStatement(preparedStatement);
		closeResultSet(resultSet);
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
                log.warn("Database error. Could not close database connection. Continuing with " +
                        "others. - " + e.getMessage(), e);
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
                log.warn("Database error. Could not close PreparedStatement. Continuing with" +
                        " others. - " + e.getMessage(), e);
            }
        }

    }
}
