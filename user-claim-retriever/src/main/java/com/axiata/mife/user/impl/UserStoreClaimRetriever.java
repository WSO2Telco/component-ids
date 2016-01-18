package com.axiata.mife.user.impl;

import org.wso2.carbon.identity.oauth.user.UserInfoClaimRetriever;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;

import java.util.HashMap;
import java.util.Map;

public class UserStoreClaimRetriever implements UserInfoClaimRetriever {

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
