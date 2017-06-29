package com.wso2telco.entity;

import java.util.List;

public class UserRegistrationResponse {
	
	List<RegisterUserStatusInfo> statusInfo;

	public List<RegisterUserStatusInfo> getStatusInfo() {
		return statusInfo;
	}

	public void setStatusInfo(List<RegisterUserStatusInfo> statusInfo) {
		this.statusInfo = statusInfo;
	}
}
