package com.axiata.dialog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.axiata.dialog.model.Transaction;
import com.axiata.dialog.util.DbUtil;

public class TransactionDAO {
	
	private static final Log log = LogFactory.getLog(TransactionDAO.class);
	
	public static void insertTransactionLog(Transaction transaction, String contextId, int statusCode) throws Exception {

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DbUtil.getConnectDBConnection();
			String query = "INSERT INTO mcx_cross_operator_transaction_log (tx_id, tx_status, batch_id, api_id, client_id,"
					+ " application_state, sub_op_mcc, sub_op_mnc, timestamp_start, timestamp_end, exchange_response_code)"
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
	 * Handle exception.
	 * 
	 * @param msg
	 * @param t
	 * @throws Exception
	 */
	private static void handleException(String msg, Throwable t)
			throws Exception {
		log.error(msg, t);
		throw new Exception(msg, t);
	}

}
