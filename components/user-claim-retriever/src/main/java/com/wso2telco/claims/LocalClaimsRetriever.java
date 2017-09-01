package com.wso2telco.claims;


import com.wso2telco.core.config.model.ScopeDetailsConfig;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalClaimsRetriever implements ClaimsRetriever {

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(LocalClaimsRetriever.class);

    private final String hashPhoneScope = "mc_identity_phonenumber_hashed";

    private final String phone_number_claim = "phone_number";

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
        if (scopeConfigs != null) {
            if (ArrayUtils.contains(scopes, hashPhoneScope)) {
                if (totalClaims.get(phone_number_claim) == null) {
                    requestedClaims.put(phone_number_claim, "");
                } else {
                    String hashed_msisdn = getHashedClaimValue((String) totalClaims.get(phone_number_claim));
                    requestedClaims.put(phone_number_claim, hashed_msisdn);
                }
            } else {
                for (ScopeDetailsConfig.Scope scope : scopeConfigs) {
                    if (ArrayUtils.contains(scopes, scope.getName())) {
                        for (String claims : scope.getClaimSet()) {
                            if (totalClaims.get(claims) == null) {
                                requestedClaims.put(claims, "");
                            } else {
                                requestedClaims.put(claims, totalClaims.get(claims));
                            }
                        }
                    }
                }
            }

        } else {
            if (log.isDebugEnabled()) {
                log.debug("Could not load user-info claims.");
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
