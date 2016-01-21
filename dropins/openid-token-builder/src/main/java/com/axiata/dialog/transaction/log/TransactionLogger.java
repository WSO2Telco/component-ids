package com.axiata.dialog.transaction.log;

import com.axiata.dialog.model.Transactions;

public interface TransactionLogger {

	int logTransaction(Transactions Transactions, String contextId);
	boolean logTransactionConnect(Transactions transactions, String contextId, int statusCode);
	Transactions prepareTransactionData(String clientKey, boolean transactionSuccessState, long transactionStartTime, long transactionEndTime, String mCXClientAppState, String contextId);
}
