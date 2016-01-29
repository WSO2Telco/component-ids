/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 * 
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.gsma.authenticators;


// TODO: Auto-generated Javadoc
/**
 * The Class AuthenticationData.
 */
public class AuthenticationData {

    /** The token id. */
    private  String tokenID;
    
    /** The client id. */
    private  String clientId;
    
    /** The redirect uri. */
    private  String redirectUri;
    
    /** The scope. */
    private  String scope;
    
    /** The authenticators. */
    private  String authenticators;
    
    /** The acr values. */
    private int acrValues;
    
    /** The response type. */
    private  String responseType;
    
    /** The status. */
    private int status;

    /**
     * Gets the status.
     *
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Gets the response type.
     *
     * @return the response type
     */
    public String getResponseType() {
        return responseType;
    }

    /**
     * Sets the response type.
     *
     * @param responseType the new response type
     */
    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }
    
    /**
     * Gets the token id.
     *
     * @return the token id
     */
    public String getTokenID() {
        return tokenID;
    }

    /**
     * Sets the token id.
     *
     * @param tokenID the new token id
     */
    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    /**
     * Gets the client id.
     *
     * @return the client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the client id.
     *
     * @param clientId the new client id
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets the redirect uri.
     *
     * @return the redirect uri
     */
    public String getRedirectUri() {
        return redirectUri;
    }

    /**
     * Sets the redirect uri.
     *
     * @param redirectUri the new redirect uri
     */
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    /**
     * Gets the scope.
     *
     * @return the scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the scope.
     *
     * @param scope the new scope
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Gets the authenticators.
     *
     * @return the authenticators
     */
    public String getAuthenticators() {
        return authenticators;
    }

    /**
     * Sets the authenticators.
     *
     * @param authenticators the new authenticators
     */
    public void setAuthenticators(String authenticators) {
        this.authenticators = authenticators;
    }

    /**
     * Gets the acr values.
     *
     * @return the acr values
     */
    public int getAcrValues() {
        return acrValues;
    }

    /**
     * Sets the acr values.
     *
     * @param acrValues the new acr values
     */
    public void setAcrValues(int acrValues) {
        this.acrValues = acrValues;
    }

}
