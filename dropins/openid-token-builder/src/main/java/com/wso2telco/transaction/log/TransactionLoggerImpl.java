/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
 ******************************************************************************/
package com.wso2telco.transaction.log;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wso2telco.dao.TransactionDAO;
import com.wso2telco.gsma.authenticators.DataHolder;
import com.wso2telco.gsma.authenticators.config.MobileConnectConfig;
import com.wso2telco.model.API;
import com.wso2telco.model.LogRequest;
import com.wso2telco.model.ServingOperator;
import com.wso2telco.model.Transaction;
import com.wso2telco.model.TransactionTimeStamp;
import com.wso2telco.model.Transactions;
import com.wso2telco.util.GSMAAuthenticatorConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class TransactionLoggerImpl.
 */
public class TransactionLoggerImpl implements TransactionLogger {
	
	/** The log. */
	private static Log log = LogFactory.getLog(TransactionLoggerImpl.class);

	/* (non-Javadoc)
	 * @see com.wso2telco.transaction.log.TransactionLogger#logTransaction(com.wso2telco.model.Transactions, java.lang.String)
	 */
	public int logTransaction(Transactions transactions, String contextId) {
		
//		boolean status = false;
		
		MobileConnectConfig.GSMAExchangeConfig gsmaExchangeConfig = DataHolder.getInstance().getMobileConnectConfig()
                .getGsmaExchangeConfig();
		
		String batchId = contextId;
		String path = "/v1/exchange/organizations/" + gsmaExchangeConfig.getOrganization() + "/transactions/" + batchId;
		String requestValidationExchangeEndpoint = "http://" + gsmaExchangeConfig.getServingOperatorHost() + path;
		
		HttpClient client = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(requestValidationExchangeEndpoint);
        
        postRequest.addHeader("Authorization", "Basic " + gsmaExchangeConfig.getAuthToken());
        postRequest.addHeader("Content-Type", "application/json");
        postRequest.addHeader("Accept", "application/json");
        
        // Prepare json input.
        LogRequest request = new LogRequest();
        request.setTransactions(transactions);
        
        Gson gson = new GsonBuilder().serializeNulls().create();
        String reqString = gson.toJson(request);
        
        int statusCode = -1;
		try {
			StringEntity input = new StringEntity(reqString);
	        input.setContentType("application/json");
	        postRequest.setEntity(input);
	        
			HttpResponse response = client.execute(postRequest);
			statusCode = response.getStatusLine().getStatusCode();
//	        status = (statusCode == HttpStatus.SC_OK);
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return statusCode;
			
		} catch (IOException e) {
			e.printStackTrace();
			return statusCode;
		}
		
//		return status;
		return statusCode;
	}

	/* (non-Javadoc)
	 * @see com.wso2telco.transaction.log.TransactionLogger#logTransactionConnect(com.wso2telco.model.Transactions, java.lang.String, int)
	 */
	public boolean logTransactionConnect(Transactions transactions, String contextId, int statusCode) {
		
		boolean status = false;
		
		try {
			TransactionDAO.insertTransactionLog(transactions.getTransaction()[0], contextId, statusCode);
			
		} catch (Exception e) {
			log.error("Error in inserting transaction log record : " + e.getMessage(), e);
		}
		
		return status;
	}

	/* (non-Javadoc)
	 * @see com.wso2telco.transaction.log.TransactionLogger#prepareTransactionData(java.lang.String, boolean, long, long, java.lang.String, java.lang.String)
	 */
	public Transactions prepareTransactionData(String clientKey, boolean transactionSuccessState, long transactionStartTime, long transactionEndTime, String mCXClientAppState, String contextId) {
		
		MobileConnectConfig.GSMAExchangeConfig gsmaExchangeConfig = DataHolder.getInstance().getMobileConnectConfig()
                .getGsmaExchangeConfig();
		
		Transaction transaction = new Transaction();
        API api = new API();
        api.setId(GSMAAuthenticatorConstants.API_ID_OPERATORID);
        
        ServingOperator servingOp = new ServingOperator();
        servingOp.setMcc(gsmaExchangeConfig.getServingOperator().getMcc());
        servingOp.setMnc(gsmaExchangeConfig.getServingOperator().getMnc());
        
        TransactionTimeStamp timestamp = new TransactionTimeStamp();
        timestamp.setStart(Long.toString(transactionStartTime));
        timestamp.setEnd(Long.toString(transactionEndTime));
        
        String transactionStatus = "";
        if (transactionSuccessState) {
        	transactionStatus = GSMAAuthenticatorConstants.SUCCESS;
        } else {
        	transactionStatus = GSMAAuthenticatorConstants.FAILED;
        }
        
        transaction.setApi(api);
        transaction.setApplication_state(mCXClientAppState);
        transaction.setClient_id(clientKey);
        transaction.setSubscriber_operator(servingOp);
        transaction.setTimestamp(timestamp);
        transaction.setTx_id(GSMAAuthenticatorConstants.API_ID_OPERATORID + "-" + contextId);
        transaction.setTx_status(transactionStatus);
        
        Transactions transactions = new Transactions();
        Transaction[] transactionArr = new Transaction[1];
        transactionArr[0] = transaction;
        transactions.setTransaction(transactionArr);
        transactions.setCount(1);
        
		return transactions;
	}
	
	 
	/**
	 * Handle exception.
	 *
	 * @param msg the msg
	 * @param t the t
	 * @throws Exception the exception
	 */
	private static void handleException(String msg, Throwable t)
			throws Exception {
		log.error(msg, t);
		throw new Exception(msg, t);
	}

}
