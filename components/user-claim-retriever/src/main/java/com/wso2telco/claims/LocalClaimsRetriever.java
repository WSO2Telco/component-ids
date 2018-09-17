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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LocalClaimsRetriever implements ClaimsRetriever {

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(LocalClaimsRetriever.class);
    private static Map<String, ScopeDetailsConfig.Scope> scopeConfigsMap = new HashMap();


    private void populateScopeConfigs(List<ScopeDetailsConfig.Scope> scopeConfigs) {
        if (scopeConfigsMap.size() == 0) {
            for (ScopeDetailsConfig.Scope scope : scopeConfigs) {
                scopeConfigsMap.put(scope.getName(), scope);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Could not load user-info claims.");
            }
        }
    }

    /**
     * Gets the requested claims.
     *
     * @param scopes       the scopes
     * @param scopeConfigs the scope configs
     * @param totalClaims  the total claims
     * @return the requested claims
     * ClaimsRetriever
     */
    @Override
    public Map<String, Object> getRequestedClaims(String[] scopes, List<ScopeDetailsConfig.Scope> scopeConfigs,
                                                  Map<String, Object>
                                                          totalClaims) throws NoSuchAlgorithmException {
        Map<String, Object> requestedClaims = new HashMap();

        populateScopeConfigs(scopeConfigs);

        for (String scope : scopes) {
            if (scopeConfigsMap.containsKey(scope)) {
                Iterator<String> i = scopeConfigsMap.get(scope).getClaimSet().iterator();
                boolean isHashed = scopeConfigsMap.get(scope).isHashed();
                while (i.hasNext()) {
                    String key = i.next();
                    Object claimStr = totalClaims.get(key);
                    if (claimStr != null) {
                        String claimValue = (isHashed) ? getHashedClaimValue(totalClaims.get(key).toString()) :
                                claimStr.toString();
                        requestedClaims.put(key, claimValue);
                    }
                }
            }
        }

        return requestedClaims;
    }

    private String getHashedClaimValue(String claimValue) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(claimValue.getBytes());

        byte[] byteData = md.digest();

        //convert the byte to hex format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
