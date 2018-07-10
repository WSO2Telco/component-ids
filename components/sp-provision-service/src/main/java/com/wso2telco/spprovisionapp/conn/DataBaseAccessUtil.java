/** *****************************************************************************
 * Copyright  (c) 2015-2018, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
 ***************************************************************************** */
package com.wso2telco.spprovisionapp.conn;

import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.core.dbutils.DbUtils;
import com.wso2telco.core.dbutils.util.DataSourceNames;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class DataBaseAccessUtil {

    static final Logger logInstance = Logger.getLogger(DataBaseAccessUtil.class);

    public static Connection getConnectionToApimgtDb() throws SQLException {
        try {
            return DbUtils.getDbConnection(DataSourceNames.WSO2AM_DB);
        } catch (Exception e) {
            logInstance.error("SPProvisionAPI: Error in AM DB connection", e);
            throw new SQLException(e);
        }
    }

    public static Connection getConnectionToAxiataDb() throws SQLException {
        try {
            Connection con = DbUtils.getDbConnection(DataSourceNames.WSO2TELCO_DEP_DB);
            con.setAutoCommit(true);
            return con;
        } catch (Exception e) {
            logInstance.error("SPProvisionAPI: Error in DEP DB connection", e);
            throw new SQLException(e);
        }
    }

    public static Connection getConnectionToConnectDb()throws SQLException {
        try {
            Connection con = DbUtils.getConnectDbConnection();
            con.setAutoCommit(true);
            return con;
        } catch (DBUtilException e) {
            logInstance.error("SPProvisionAPI: Error in CONNECT DB connection", e);
            throw new SQLException(e);
        }
    }
}
