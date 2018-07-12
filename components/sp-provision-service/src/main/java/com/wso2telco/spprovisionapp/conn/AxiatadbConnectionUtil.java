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

import java.sql.Connection;
import java.sql.SQLException;

public class AxiatadbConnectionUtil {

    public static Connection getConnection() throws SQLException {

        return DataBaseAccessUtil.getConnectionToAxiataDb();

    }

    public static void closeQuietly(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
        }
    }

    public static void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
        }
    }

}