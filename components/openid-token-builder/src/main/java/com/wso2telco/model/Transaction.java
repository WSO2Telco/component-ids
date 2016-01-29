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
package com.wso2telco.model;

// TODO: Auto-generated Javadoc
/**
 * The Class Transaction.
 */
public class Transaction {

	/** The api. */
	private API api = null;
	
	/** The application_state. */
	private String application_state = "";
	
	/** The client_id. */
	private String client_id = "";
	
	/** The subscriber_operator. */
	private ServingOperator subscriber_operator = null;
	
	/** The timestamp. */
	private TransactionTimeStamp timestamp = null;
	
	/** The tx_id. */
	private String tx_id = "";
	
	/** The tx_status. */
	private String tx_status = "";
	
	/**
	 * Gets the api.
	 *
	 * @return the api
	 */
	public API getApi() {
		return api;
	}
	
	/**
	 * Sets the api.
	 *
	 * @param api the new api
	 */
	public void setApi(API api) {
		this.api = api;
	}
	
	/**
	 * Gets the application_state.
	 *
	 * @return the application_state
	 */
	public String getApplication_state() {
		return application_state;
	}
	
	/**
	 * Sets the application_state.
	 *
	 * @param application_state the new application_state
	 */
	public void setApplication_state(String application_state) {
		this.application_state = application_state;
	}
	
	/**
	 * Gets the client_id.
	 *
	 * @return the client_id
	 */
	public String getClient_id() {
		return client_id;
	}
	
	/**
	 * Sets the client_id.
	 *
	 * @param client_id the new client_id
	 */
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	
	/**
	 * Gets the subscriber_operator.
	 *
	 * @return the subscriber_operator
	 */
	public ServingOperator getSubscriber_operator() {
		return subscriber_operator;
	}
	
	/**
	 * Sets the subscriber_operator.
	 *
	 * @param subscriber_operator the new subscriber_operator
	 */
	public void setSubscriber_operator(ServingOperator subscriber_operator) {
		this.subscriber_operator = subscriber_operator;
	}
	
	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public TransactionTimeStamp getTimestamp() {
		return timestamp;
	}
	
	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp the new timestamp
	 */
	public void setTimestamp(TransactionTimeStamp timestamp) {
		this.timestamp = timestamp;
	}
	
	/**
	 * Gets the tx_id.
	 *
	 * @return the tx_id
	 */
	public String getTx_id() {
		return tx_id;
	}
	
	/**
	 * Sets the tx_id.
	 *
	 * @param tx_id the new tx_id
	 */
	public void setTx_id(String tx_id) {
		this.tx_id = tx_id;
	}
	
	/**
	 * Gets the tx_status.
	 *
	 * @return the tx_status
	 */
	public String getTx_status() {
		return tx_status;
	}
	
	/**
	 * Sets the tx_status.
	 *
	 * @param tx_status the new tx_status
	 */
	public void setTx_status(String tx_status) {
		this.tx_status = tx_status;
	}
	
}
