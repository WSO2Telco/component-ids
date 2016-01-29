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
package com.wso2telco.transaction.log;

import com.wso2telco.model.Transactions;

// TODO: Auto-generated Javadoc
/**
 * The Interface TransactionLogger.
 */
public interface TransactionLogger {

	/**
	 * Log transaction.
	 *
	 * @param Transactions the transactions
	 * @param contextId the context id
	 * @return the int
	 */
	int logTransaction(Transactions Transactions, String contextId);
	
	/**
	 * Log transaction connect.
	 *
	 * @param transactions the transactions
	 * @param contextId the context id
	 * @param statusCode the status code
	 * @return true, if successful
	 */
	boolean logTransactionConnect(Transactions transactions, String contextId, int statusCode);
	
	/**
	 * Prepare transaction data.
	 *
	 * @param clientKey the client key
	 * @param transactionSuccessState the transaction success state
	 * @param transactionStartTime the transaction start time
	 * @param transactionEndTime the transaction end time
	 * @param mCXClientAppState the m cx client app state
	 * @param contextId the context id
	 * @return the transactions
	 */
	Transactions prepareTransactionData(String clientKey, boolean transactionSuccessState, long transactionStartTime, long transactionEndTime, String mCXClientAppState, String contextId);
}
