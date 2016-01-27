package com.axiata.dialog.model;

public class Transaction {

	private API api = null;
	private String application_state = "";
	private String client_id = "";
	private ServingOperator subscriber_operator = null;
	private TransactionTimeStamp timestamp = null;
	private String tx_id = "";
	private String tx_status = "";
	
	public API getApi() {
		return api;
	}
	
	public void setApi(API api) {
		this.api = api;
	}
	
	public String getApplication_state() {
		return application_state;
	}
	
	public void setApplication_state(String application_state) {
		this.application_state = application_state;
	}
	
	public String getClient_id() {
		return client_id;
	}
	
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	
	public ServingOperator getSubscriber_operator() {
		return subscriber_operator;
	}
	
	public void setSubscriber_operator(ServingOperator subscriber_operator) {
		this.subscriber_operator = subscriber_operator;
	}
	
	public TransactionTimeStamp getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(TransactionTimeStamp timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getTx_id() {
		return tx_id;
	}
	
	public void setTx_id(String tx_id) {
		this.tx_id = tx_id;
	}
	
	public String getTx_status() {
		return tx_status;
	}
	
	public void setTx_status(String tx_status) {
		this.tx_status = tx_status;
	}
	
}
