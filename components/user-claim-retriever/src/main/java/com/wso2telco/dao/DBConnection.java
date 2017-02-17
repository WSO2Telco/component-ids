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
package com.wso2telco.dao;

import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.core.dbutils.DbUtils;
import com.wso2telco.exception.EmptyResultSetException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DBConnection {

    private static Log logger = LogFactory.getLog(DBConnection.class);
    private static DBConnection instance = null;
    private Connection connection = null;
    private PreparedStatement statement;
    private ResultSet resultSet;


    protected DBConnection() throws SQLException, ClassNotFoundException, DBUtilException {
        connection = DbUtils.getConnectDbConnection();
    }

    public static DBConnection getInstance() throws ClassNotFoundException {
        if (instance == null) {
            try {
                instance = new DBConnection();
            } catch (SQLException | DBUtilException e) {
                logger.error("Error occurred while getting connection");
            }
        }
        return instance;
    }

    /**
     * Check the availability of the clients for a given clientID .
     *
     * @param acessToken access Token
     * @return int indicating the transaction is success ,failure or error
     */
    public ScopeDetails getScopeFromAcessToken(String acessToken)throws Exception{
        ScopeDetails  accessTokenRelatedscopeDetails = new ScopeDetails();
            String query = "SELECT sub FROM scope_log where access_token = ? "+
                    "order by created_time desc limit 1";
            statement = connection.prepareStatement(query);
            statement.setString(1, acessToken);
            resultSet = statement.executeQuery();


            if (resultSet.next()) {
                accessTokenRelatedscopeDetails.setAccessToken(acessToken);
                accessTokenRelatedscopeDetails.setPcr(resultSet.getString("sub"));
                return accessTokenRelatedscopeDetails;
            } else {
                throw new EmptyResultSetException("Result set is empty");
            }

    }


    /**
     * Close the database connection.
     */
    private void close() {
        try {
            if (resultSet != null)
                resultSet.close();
            if (statement != null)
                statement.close();
            if (connection != null)
                connection.close();
        } catch (Exception e) {
        }
    }
}
