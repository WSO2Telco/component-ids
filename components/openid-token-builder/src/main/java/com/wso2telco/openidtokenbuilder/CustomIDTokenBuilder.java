package com.wso2telco.openidtokenbuilder;


import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.JWTException;
import org.apache.oltu.openidconnect.as.messages.IDToken;
import org.apache.oltu.openidconnect.as.messages.IDTokenException;
import org.codehaus.jettison.json.JSONArray;

public class CustomIDTokenBuilder {

    private static Log log = LogFactory.getLog(CustomIDTokenBuilder.class);
    private static boolean debug = log.isDebugEnabled();

    // extensions
    private Map<String, Object> claims = new HashMap<String, Object>();
    private Map<String, Object> header = new HashMap<String, Object>();
    // Configurations
    private Key sigKey = null;
    private Key encKey = null;
    private String sigAlg = "none";
    private String encAlg = null;

    /**
     * @param iss the iss to set
     */
    public void setIssuer(String iss) {
        claims.put(IDToken.ISS, iss);
    }

    /**
     * @param sub the sub to set
     */
    public void setSubject(String sub) {
        claims.put(IDToken.SUB, sub);
    }

    /**
     * @param aud the aud to set
     */
    public void setAudience(String aud) {
        claims.put(IDToken.AUD, aud);
    }

    /**
     * @param exp the exp to set
     */
    public void setExpiration(int exp) {
        claims.put(IDToken.EXP, exp);
    }

    /**
     * @param iat the iat to set
     */
    public void setIssuedAt(int iat) {
        claims.put(IDToken.IAT, iat);
    }

    /**
     * @param nonce the nonce to set
     */
    public void setNonce(String nonce) {
        claims.put(IDToken.NONCE, nonce);
    }

    /**
     * @param azp the azp to set
     */
    public void setAuthorizedParty(String azp) {
        claims.put(IDToken.AZP, azp);
    }

    /**
     * @param acr the acr to set
     */
    public void setAuthenticationContextClassReference(String acr) {
        claims.put(IDToken.ACR, acr);
    }

    /**
     * @param authTime the auth_time to set
     */
    public void setAuthTime(String authTime) {
        claims.put(IDToken.AUTH_TIME, authTime);
    }

    /**
     * @param at_hash the at_hash to set
     */
    public void setAtHash(String at_hash) {
        claims.put(IDToken.AT_HASH, at_hash);
    }

    /**
     * @param c_hash the c_hash to set
     */
    public void setCHash(String c_hash) {
        claims.put(IDToken.C_HASH, c_hash);
    }

    /**
     * Use this method to set custom claims
     *
     * @param claimKey   claim key
     * @param claimValue claim value
     */
    public void setClaim(String claimKey, String claimValue) {
        if (claimKey == null || claimValue == null) {
            log.error("Key or Value cannot be null");
        }
        claims.put(claimKey, claimValue);
    }

    /**
     * Use this method to set custom AMR
     *
     * @param amr custom amr
     */
    public void setAmr(JSONArray amr) {
        if (amr == null) {
            log.error("AMR cannot be null");
        }
        claims.put("amr", amr);
    }


    /**
     * Use this method to set custom JWT headers
     *
     * @param key   key parameter
     * @param value value parameter
     */
    public void setHeaderParam(String key, String value) {
        if (key == null || value == null) {
            log.error("Key or Value cannot be null");
        }
        header.put(key, value);
    }

    /**
     * @param sigKey the sigKey to set
     * @param sigAlg sign algorithm
     */
    public void setSigKey(Key sigKey, String sigAlg) {
        this.sigKey = sigKey;
        this.sigAlg = sigAlg;
    }

    /**
     * @param encKey the encKey to set
     * @param encAlg encrypt algorithm
     */
    public void setEncKey(Key encKey, String encAlg) {
        this.encKey = encKey;
        this.encAlg = encAlg;
    }

    /**
     * @return returns id token
     * @throws IDTokenException token exception
     */
    public String buildIDToken() throws IDTokenException {
        checkSpecCompliance();
        // setting algorithm parameter
        header.put(JWT.HeaderParam.ALGORITHM, sigAlg);

        try {
            CustomJWTBuilder customJWTBuilder = new CustomJWTBuilder();
            customJWTBuilder.setClaims(claims);
            customJWTBuilder.setHeaderParams(header);
            customJWTBuilder.signJWT(sigKey, sigAlg);
            customJWTBuilder.encryptJWT(encKey, encAlg);
            return customJWTBuilder.buildJWT();

        } catch (JWTException e) {
            throw new IDTokenException("Error while building IDToken", e);
        }
    }

    /**
     * Check for spec compliance
     *
     * @throws IDTokenException
     */
    private void checkSpecCompliance() throws IDTokenException {
        if (debug) {
            if (claims.get(IDToken.ISS) == null) {
                log.error("iss claim not set");
            }
            if (claims.get(IDToken.SUB) == null) {
                log.error("sub claim not set");
            }
            if (claims.get(IDToken.AUD) == null) {
                log.error("aud claim not set");
            }
            if (claims.get(IDToken.EXP) == null) {
                log.error("exp claim not set");
            }
            if (claims.get(IDToken.IAT) == null) {
                log.error("iat claim not set");
            }
        }
        if (claims.get(IDToken.ISS) == null || claims.get(IDToken.SUB) == null ||
                claims.get(IDToken.AUD) == null || claims.get(IDToken.EXP) == null ||
                claims.get(IDToken.IAT) == null) {
            throw new IDTokenException("One or more required claims missing");
        }
    }
}
