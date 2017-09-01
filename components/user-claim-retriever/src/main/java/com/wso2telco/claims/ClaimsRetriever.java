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
    public Map<String, Object> getRequestedClaims(String[] scopes, List<ScopeDetailsConfig.Scope> scopeConfigs, Map<String, Object>totalClaims) throws NoSuchAlgorithmException;
}
