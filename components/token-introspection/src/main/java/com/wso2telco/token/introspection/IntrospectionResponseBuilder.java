/*
 * ******************************************************************************
 *  * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *  *
 *  * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package com.wso2telco.token.introspection;

import org.apache.oltu.oauth2.common.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * this class is responsible for building the introspection response.
 * 
 */
public class IntrospectionResponseBuilder {

    private Map<String, Object> parameters = new HashMap();
    private boolean isActive = false;

    /**
     * build the introspection response.
     * 
     * @return
     */
    public String build() {
	return JSONUtils.buildJSON(parameters);
    }

    /**
     * 
     * @param isActive
     * @return
     */
    public IntrospectionResponseBuilder setActive(boolean isActive) {
	parameters.put(IntrospectionResponse.ACTIVE, isActive);
	if (!isActive) {
	    // if the token is not active we do not want to return back the expiration time.
	    if (parameters.containsKey(IntrospectionResponse.EXP)) {
		parameters.remove(IntrospectionResponse.EXP);
	    }
	    // if the token is not active we do not want to return back the nbf time.
	    if (parameters.containsKey(IntrospectionResponse.NBF)) {
		parameters.remove(IntrospectionResponse.NBF);
	    }
	}
	this.isActive = isActive;
	return this;
    }

    /**
     * 
     * @param issuedAt
     * @return
     */
    public IntrospectionResponseBuilder setIssuedAt(long issuedAt) {
	if (issuedAt != 0) {
	    parameters.put(IntrospectionResponse.IAT, issuedAt);
	}
	return this;
    }

    /**
     * 
     * @param jwtId
     * @return
     */
    public IntrospectionResponseBuilder setJwtId(String jwtId) {
	if (jwtId != null && !jwtId.isEmpty()) {
	    parameters.put(IntrospectionResponse.JTI, jwtId);
	}
	return this;
    }

    /**
     * 
     * @param subject
     * @return
     */
    public IntrospectionResponseBuilder setSubject(String subject) {
	if (subject != null && !subject.isEmpty()) {
	    parameters.put(IntrospectionResponse.SUB, subject);
	}
	return this;
    }

    /**
     * 
     * @param expiration
     * @return
     */
    public IntrospectionResponseBuilder setExpiration(long expiration) {
	if (isActive && expiration != 0) {
	    // if the token is not active we do not want to return back the expiration time.
	    parameters.put(IntrospectionResponse.EXP, expiration);
	}
	return this;
    }

    /**
     * 
     * @param username
     * @return
     */
    public IntrospectionResponseBuilder setUsername(String username) {
	if (username != null && !username.isEmpty()) {
	    parameters.put(IntrospectionResponse.USERNAME, username);
	}
	return this;
    }

    /**
     * 
     * @param tokenType
     * @return
     */
    public IntrospectionResponseBuilder setTokenType(String tokenType) {
	if (tokenType != null && !tokenType.isEmpty()) {
	    parameters.put(IntrospectionResponse.TOKEN_TYPE, tokenType);
	}
	return this;
    }

    /**
     * 
     * @param notBefore
     * @return
     */
    public IntrospectionResponseBuilder setNotBefore(long notBefore) {
	if (isActive && notBefore != 0) {
	    // if the token is not active we do not want to return back the nbf time.
	    parameters.put(IntrospectionResponse.NBF, notBefore);
	}
	return this;
    }

    /**
     * 
     * @param audience
     * @return
     */
    public IntrospectionResponseBuilder setAudience(String audience) {
	if (audience != null && !audience.isEmpty()) {
	    parameters.put(IntrospectionResponse.AUD, audience);
	}
	return this;
    }

    /**
     * 
     * @param issuer
     * @return
     */
    public IntrospectionResponseBuilder setIssuer(String issuer) {
	if (issuer != null && !issuer.isEmpty()) {
	    parameters.put(IntrospectionResponse.ISS, issuer);
	}
	return this;
    }

    /**
     * 
     * @param scope
     * @return
     */
    public IntrospectionResponseBuilder setScope(String scope) {
	if (scope != null && !scope.isEmpty()) {
	    parameters.put(IntrospectionResponse.SCOPE, scope);
	}
	return this;
    }

    /**
     * 
     * @param consumerKey
     * @return
     */
    public IntrospectionResponseBuilder setClientId(String consumerKey) {
	if (consumerKey != null && !consumerKey.isEmpty()) {
	    parameters.put(IntrospectionResponse.CLIENT_ID, consumerKey);
	}
	return this;
    }

    /**
     * 
     * @param errorCode
     * @return
     */
    public IntrospectionResponseBuilder setErrorCode(String errorCode) {
	parameters.put(IntrospectionResponse.Error.ERROR_CODE, errorCode);
	return this;
    }

    /**
     * 
     * @param description
     * @return
     */
    public IntrospectionResponseBuilder setErrorDescription(String description) {
	parameters.put(IntrospectionResponse.Error.ERROR_DESCRIPTION, description);
	return this;
    }
}
