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
package com.wso2telco.proxy.model;

import com.wso2telco.core.config.model.ScopeParam;

/**
 * RedirectUrlInfo is using to construct redirect url.
 */
public class RedirectUrlInfo {
    private String queryString;
    private String authorizeUrl;
    private String operatorName;
    private String msisdnHeader;
    private String loginhintMsisdn;
    private String ipAddress;
    private String telcoScope;
    private String parentScope;
    private boolean isLoginhintMandatory;
    private boolean showTnc;
    private ScopeParam.msisdnMismatchResultTypes headerMismatchResult;
    private ScopeParam.heFailureResults heFailureResult;
    private String transactionId;
    private String prompt;

    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getPrompt() { return prompt; }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getTelcoScope() {
        return telcoScope;
    }

    public void setTelcoScope(String telcoScope) {
        this.telcoScope = telcoScope;
    }

    public String getParentScope() {
		return parentScope;
	}

	public void setParentScope(String parentScope) {
		this.parentScope = parentScope;
	}

	public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMsisdnHeader() {
        return msisdnHeader;
    }

    public void setMsisdnHeader(String msisdnHeader) {
        this.msisdnHeader = msisdnHeader;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public void setAuthorizeUrl(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    public void setLoginhintMandatory(boolean isLoginhintMandatory) {
        this.isLoginhintMandatory = isLoginhintMandatory;
    }

    public boolean isLoginhintMandatory() {
        return isLoginhintMandatory;
    }

    public String getLoginhintMsisdn() {
        return loginhintMsisdn;
    }

    public void setLoginhintMsisdn(String loginhintMsisdn) {
        this.loginhintMsisdn = loginhintMsisdn;
    }

    public void setShowTnc(boolean showTnc) {
        this.showTnc = showTnc;
    }

    public boolean isShowTnc() {
        return showTnc;
    }

    public void setHeaderMismatchResult(ScopeParam.msisdnMismatchResultTypes headerMismatchResult) {
        this.headerMismatchResult = headerMismatchResult;
    }

    public ScopeParam.msisdnMismatchResultTypes getHeaderMismatchResult() {
        return headerMismatchResult;
    }

    public void setHeFailureResult(ScopeParam.heFailureResults heFailureResult) {
        this.heFailureResult = heFailureResult;
    }

    public ScopeParam.heFailureResults getHeFailureResult() {
        return heFailureResult;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
