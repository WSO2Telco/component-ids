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
import org.apache.commons.collections.map.HashedMap;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

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
    private boolean isLoginhintMandatory;
    private boolean showTnc;

    private ScopeParam.MsisdnMismatchResultTypes headerMismatchResult;
    private ScopeParam.HeFailureResults heFailureResult;
    private String transactionId;
    private String prompt;
    private boolean attributeSharingScope;
    private String trustedStatus;
    private String attributeSharingScopeType;
 
    private String correlationId;
    private String redirectUrl;
    private boolean isBackChannelAllowed;
 
    private boolean showConsent;
    private EnumSet<ScopeParam.scopeTypes> scopeTypesList;

    private Map<String, String> approveNeededScopes;
    private List<String> approvedScopes;
    private boolean enableapproveall;
    private boolean isAPIConsent;

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

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

    public void setHeaderMismatchResult(ScopeParam.MsisdnMismatchResultTypes headerMismatchResult) {
        this.headerMismatchResult = headerMismatchResult;
    }

    public ScopeParam.MsisdnMismatchResultTypes getHeaderMismatchResult() {
        return headerMismatchResult;
    }

    public void setHeFailureResult(ScopeParam.HeFailureResults heFailureResult) {
        this.heFailureResult = heFailureResult;
    }

    public ScopeParam.HeFailureResults getHeFailureResult() {
        return heFailureResult;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public boolean isAttributeSharingScope() {
        return attributeSharingScope;
    }

    public void setAttributeSharingScope(boolean attributeSharingScope) {
        this.attributeSharingScope = attributeSharingScope;
    }

    public String getTrustedStatus() {
        return trustedStatus;
    }

    public void setTrustedStatus(String trustedStatus) {
        this.trustedStatus = trustedStatus;
    }

    public String getAttributeSharingScopeType() {
        return attributeSharingScopeType;
    }

    public void setAttributeSharingScopeType(String attributeSharingScopeType) {
        this.attributeSharingScopeType = attributeSharingScopeType;
    }
 
    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public boolean isBackChannelAllowed() {
        return isBackChannelAllowed;
    }

    public void setBackChannelAllowed(boolean backChannelAllowed) {
        isBackChannelAllowed = backChannelAllowed;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
 
    public boolean isShowConsent() {
        return showConsent;
    }

    public void setShowConsent(boolean showConsent) {
        this.showConsent = showConsent;
    }

    public EnumSet<ScopeParam.scopeTypes> getScopeTypesList() { return scopeTypesList;  }

    public void setScopeTypesList(EnumSet<ScopeParam.scopeTypes> scopeTypesList) { this.scopeTypesList = scopeTypesList; }

    public Map<String, String> getApproveNeededScopes() {
        return approveNeededScopes;
    }

    public void setApproveNeededScopes(Map<String, String> approveNeededScopes) {
        this.approveNeededScopes = approveNeededScopes;
    }

    public List<String> getApprovedScopes() {
        return approvedScopes;
    }

    public void setApprovedScopes(List<String> approvedScopes) {
        this.approvedScopes = approvedScopes;
    }

    public boolean isEnableapproveall() {
        return enableapproveall;
    }

    public void setEnableapproveall(boolean enableapproveall) {
        this.enableapproveall = enableapproveall;
    }

    public boolean isAPIConsent() {
        return isAPIConsent;
    }

    public void setAPIConsent(boolean APIConsent) {
        isAPIConsent = APIConsent;
    }
}
