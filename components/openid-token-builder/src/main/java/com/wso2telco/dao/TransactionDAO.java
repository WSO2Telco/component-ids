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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wso2telco.util.DbUtil;
import com.wso2telco.model.Transaction;

// TODO: Auto-generated Javadoc

/**
 * The Class TransactionDAO.
 */
public class TransactionDAO {

    /**
     * The Constant log.
     */
    private static final Log log = LogFactory.getLog(TransactionDAO.class);

    /**
     * Insert transaction log.
     *
     * @param transaction the transaction
     * @param contextId   the context id
     * @param statusCode  the status code
     * @throws Exception the exception
     */
    public static void insertTransactionLog(Transaction transaction, String contextId, int statusCode) throws
            Exception {

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnectDBConnection();
            String query = "INSERT INTO mcx_cross_operator_transaction_log (tx_id, tx_status, batch_id, api_id, " +
                    "client_id,"
                    + " application_state, sub_op_mcc, sub_op_mnc, timestamp_start, timestamp_end, " +
                    "exchange_response_code)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


            ps = conn.prepareStatement(query);
            ps.setString(1, transaction.getTx_id());
            ps.setString(2, transaction.getTx_status());
            ps.setString(3, contextId);
            ps.setString(4, transaction.getApi().getId());
            ps.setString(5, transaction.getClient_id());
            ps.setString(6, transaction.getApplication_state());
            ps.setString(7, transaction.getSubscriber_operator().getMcc());
            ps.setString(8, transaction.getSubscriber_operator().getMnc());
            ps.setString(9, transaction.getTimestamp().getStart());
            ps.setString(10, transaction.getTimestamp().getEnd());
            ps.setInt(11, statusCode);
            ps.execute();

        } catch (SQLException e) {
            handleException(
                    "Error in inserting transaction log record : "
                            + e.getMessage(), e);
        } finally {
            DbUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * Insert sub value.
     *
     * @param token the context id
     * @param sub   the status code
     * @throws Exception the exception
     */
    public static void insertTokenScopeLog(String token, String sub) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DbUtil.getConnectDBConnection();
            String query = "INSERT INTO scope_log ( access_token,sub) VALUES " +
                    "(? ,?);";
            ps = conn.prepareStatement(query);
            ps.setString(1, token);
            ps.setString(2, sub);
            ps.execute();
            log.debug("Sub value inserted successfully");
        } catch (SQLException e) {
            handleException("Error in inserting transaction log record : " + e.getMessage(), e);
        } finally {
            DbUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * Handle exception.
     *
     * @param msg the msg
     * @param t   the t
     * @throws Exception the exception
     */
    private static void handleException(String msg, Throwable t)
            throws Exception {
        log.error(msg, t);
        throw new Exception(msg, t);
    }

}
