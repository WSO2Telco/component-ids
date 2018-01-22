/*******************************************************************************
 * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
package com.wso2telco.claims;

import com.wso2telco.core.config.model.ScopeDetailsConfig;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;


public interface ClaimsRetriever {

    /**
     * Gets the requested claims.
     *
     * @param scopes       the scopes
     * @param scopeConfigs the scope configs
     * @param totalClaims  the total claims
     * @return the requested claims
     * ClaimsRetriever
     */
    public Map<String, Object> getRequestedClaims(String[] scopes, List<ScopeDetailsConfig.Scope> scopeConfigs,
                                                  Map<String, Object> totalClaims) throws NoSuchAlgorithmException;
}
