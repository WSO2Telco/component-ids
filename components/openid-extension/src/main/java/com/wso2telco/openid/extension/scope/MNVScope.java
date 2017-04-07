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
package com.wso2telco.openid.extension.scope;

import com.wso2telco.openid.extension.dto.ScopeDTO;

import java.util.HashMap;
import java.util.Map;

public class MNVScope extends Scope {

    public MNVScope(ScopeDTO scopeDTO) {
        super(scopeDTO);
    }

    @Override
    public ScopeValidationResult validate(String callBackURL, String state) {
        String loginHint = getScopeDTO().getLoginHint();
        ScopeValidationResult scopeValidationResult;
        Map<String, String> queryParameters;

        if (getScopeDTO().getMsisdn() == null && (loginHint != null || loginHint.isEmpty())) {
            isOffNet = true;
            scopeValidationResult = new ScopeValidationResult(false);
        } else if (loginHint == null || loginHint.isEmpty()) {
            queryParameters = new HashMap<>();
            queryParameters.put("error", "access_denied");
            queryParameters.put("state", state);
            scopeValidationResult = new ScopeValidationResult(true, createRedirectURL(callBackURL, queryParameters));
        } else if (!loginHint.equals(getScopeDTO().getMsisdn())) {
            queryParameters = new HashMap<>();
            queryParameters.put("error", "access_denied");
            queryParameters.put("state", state);
            queryParameters.put("error_description", "MSISDN_mismatch");
            scopeValidationResult = new ScopeValidationResult(true, createRedirectURL(callBackURL, queryParameters));
        } else {
            scopeValidationResult = new ScopeValidationResult(false);
        }

        return scopeValidationResult;
    }

    @Override
    public String createRedirectURL(String baseURL, Map<String, String> queryParameters) {
        return super.createRedirectURL(baseURL, queryParameters);

    }

    @Override
    public void executeAuthenticationFlow() {
        super.executeAuthenticationFlow();
    }
}
