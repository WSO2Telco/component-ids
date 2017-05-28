package com.wso2telco.gsma.authenticators.model;

public class Consent {

	private String consumerKey;

	private int operatorID;

	private String scope;

	private String status;

	private String description;

	// private boolean is_approved;

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public int getOperatorID() {
		return operatorID;
	}

	public void setOperatorID(int operatorID) {
		this.operatorID = operatorID;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/*
	 * public boolean getIs_approved() { return is_approved; }
	 * 
	 * public void setIs_approved(boolean is_approved) { this.is_approved =
	 * is_approved; }
	 */

}
