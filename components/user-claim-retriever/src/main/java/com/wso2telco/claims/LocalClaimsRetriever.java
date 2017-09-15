package com.wso2telco.claims;


import com.wso2telco.core.config.model.ScopeDetailsConfig;
import org.apache.commons.lang.ArrayUtils;
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
    private static Map<String, ScopeDetailsConfig.Scope> scopeConfigsMap = new HashMap<String, ScopeDetailsConfig.Scope>();

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
    public Map<String, Object> getRequestedClaims(String[] scopes, List<ScopeDetailsConfig.Scope> scopeConfigs, Map<String, Object>
            totalClaims) throws NoSuchAlgorithmException {
        Map<String, Object> requestedClaims = new HashMap<String, Object>();
        if (!scopeConfigsMap.isEmpty()) {
            for (ScopeDetailsConfig.Scope scope : scopeConfigs) {
                scopeConfigsMap.put(scope.getName(), scope);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Could not load user-info claims.");
            }
        }

        for (String scope : scopes) {
            if (scopeConfigsMap.containsKey(scope)) {
                Iterator<String> i = scopeConfigsMap.get(scope).getClaimSet().iterator();
                boolean isHashed = scopeConfigsMap.get(scope).isHashed();
                while (i.hasNext()) {
                    Object claimStr = totalClaims.get(i.next());
                    if (claimStr != null) {
                        String claimValue = (isHashed) ? getHashedClaimValue(totalClaims.get(i.next()).toString()) : totalClaims.get(i.next()).toString();
                        requestedClaims.put(i.next(), totalClaims.get(claimValue));
                    }

                }

            }
        }

        return requestedClaims;
    }

    private String getHashedClaimValue(String claimValue) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(claimValue.getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
