/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Utility {

    private static Log log = LogFactory.getLog(Utility.class);

    public static String getMultiScopeQueryParam(AuthenticationContext context) {
        List<String> availableMultiScopes = Arrays.asList("profile", "email",
                "address", "phone", "mc_identity_phonenumber_hashed");

        String originalScope = ((Map) context.getProperty("authMap")).get(
                "Scope").toString();

        String[] originalScopesRequested = originalScope.split(" ");

        String multiScopes = "";

        if (originalScopesRequested.length > 1) {

            StringBuilder filteredMultiScopesQuery = new StringBuilder();
            filteredMultiScopesQuery.append("&multiScopes=");

            for (String requestedScope : originalScopesRequested) {
                if (availableMultiScopes.contains(requestedScope)) {
                    filteredMultiScopesQuery.append(requestedScope);
                    filteredMultiScopesQuery.append(" ");
                }
            }

            multiScopes = filteredMultiScopesQuery.toString().trim().replace(" ", "+");
        }

        return multiScopes;
    }
}