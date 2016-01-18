package com.axiata.dialog.model;

public class Transactions {
	
	private Transaction[] transaction = null;
	private int count = -1;

	public Transaction[] getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction[] transaction) {
		this.transaction = transaction;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
