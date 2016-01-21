package com.axiata.dialog.transaction.log;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.axiata.dialog.dao.TransactionDAO;
import com.axiata.dialog.model.API;
import com.axiata.dialog.model.LogRequest;
import com.axiata.dialog.model.ServingOperator;
import com.axiata.dialog.model.Transaction;
import com.axiata.dialog.model.TransactionTimeStamp;
import com.axiata.dialog.model.Transactions;
import com.axiata.dialog.util.GSMAAuthenticatorConstants;
import com.gsma.authenticators.DataHolder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.StringEntity;

import com.gsma.authenticators.config.MobileConnectConfig;

public class TransactionLoggerImpl implements TransactionLogger {
	
	private static Log log = LogFactory.getLog(TransactionLoggerImpl.class);

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

	public boolean logTransactionConnect(Transactions transactions, String contextId, int statusCode) {
		
		boolean status = false;
		
		try {
			TransactionDAO.insertTransactionLog(transactions.getTransaction()[0], contextId, statusCode);
			
		} catch (Exception e) {
			log.error("Error in inserting transaction log record : " + e.getMessage(), e);
		}
		
		return status;
	}

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
