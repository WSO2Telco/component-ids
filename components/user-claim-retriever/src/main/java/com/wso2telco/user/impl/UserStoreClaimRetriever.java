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
package com.wso2telco.user.impl;

import org.wso2.carbon.identity.oauth.user.UserInfoClaimRetriever;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class UserStoreClaimRetriever.
 */
public class UserStoreClaimRetriever implements UserInfoClaimRetriever {

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.oauth.user.UserInfoClaimRetriever#getClaimsMap(java.util.Map)
     */
    public Map<String, Object> getClaimsMap(Map<ClaimMapping, String> userAttributes) {
        Map<String, Object> claims = new HashMap<String, Object>();
        if (userAttributes != null && userAttributes.size() > 0) {
            for (ClaimMapping claimMapping : userAttributes.keySet()) {
                claims.put(claimMapping.getRemoteClaim().getClaimUri(), userAttributes.get(claimMapping));
            }
        }
        return claims;
    }
}
