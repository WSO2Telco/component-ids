/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com)
 * <p>
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.openid.extension.scope;

import com.wso2telco.openid.extension.dto.ScopeDTO;

import java.util.Map;

public abstract class Scope {

    private ScopeDTO scopeDTO;
    protected boolean consent;
    protected boolean isOnNet;
    protected boolean isOffNet;

    public Scope(ScopeDTO scopeDTO) {
        this.scopeDTO = scopeDTO;
    }

    /**
     * Scope validation method
     */
    public ScopeValidationResult validate(String callBackURL, String state) {
        return null;
    }

    /**
     * Create redirect URL with query parameters which are needed in the authentication flow
     *
     * @param baseURL         base URL (authorize endpoint URL)
     * @param queryParameters query parameter name and value
     * @return redirect URL
     */
    public String createRedirectURL(String baseURL, Map<String, String> queryParameters) {
        String redirectURL;
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> queryParam : queryParameters.entrySet()) {
            //if there is a result, separator will add
            if (result.length() > 0) {
                result.append("&");
            }
            result.append(queryParam.getKey());
            if (queryParam.getValue() != null) {
                result.append("=");
                result.append(queryParam.getValue());
            }
        }

        redirectURL = baseURL + "?" + result.toString();
        return redirectURL;
    }

    /**
     *
     */
    public void executeAuthenticationFlow() {
    }

    public ScopeDTO getScopeDTO() {
        return scopeDTO;
    }

    public void setScopeDTO(ScopeDTO scopeDTO) {
        this.scopeDTO = scopeDTO;
    }


}
