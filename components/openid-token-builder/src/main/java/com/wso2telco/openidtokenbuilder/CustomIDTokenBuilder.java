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
	 * @param iss
	 *            the iss to set
	 * 
	 */
	public CustomIDTokenBuilder setIssuer(String iss) {
		claims.put(IDToken.ISS, iss);
		return this;
	}

	/**
	 * @param sub
	 *            the sub to set
	 */
	public CustomIDTokenBuilder setSubject(String sub) {
		claims.put(IDToken.SUB, sub);
		return this;
	}

	/**
	 * @param aud
	 *            the aud to set
	 */
	public CustomIDTokenBuilder setAudience(String aud) {
		claims.put(IDToken.AUD, aud);
		return this;
	}

	/**
	 * @param exp
	 *            the exp to set
	 */
	public CustomIDTokenBuilder setExpiration(int exp) {
		claims.put(IDToken.EXP, exp);
		return this;
	}

	/**
	 * @param iat
	 *            the iat to set
	 */
	public CustomIDTokenBuilder setIssuedAt(int iat) {
		claims.put(IDToken.IAT, iat);
		return this;
	}

	/**
	 * @param nonce
	 *            the nonce to set
	 */
	public CustomIDTokenBuilder setNonce(String nonce) {
		claims.put(IDToken.NONCE, nonce);
		return this;
	}

	/**
	 * @param azp
	 *            the azp to set
	 */
	public CustomIDTokenBuilder setAuthorizedParty(String azp) {
		claims.put(IDToken.AZP, azp);
		return this;
	}

	/**
	 * @param acr
	 *            the acr to set
	 */
	public CustomIDTokenBuilder setAuthenticationContextClassReference(String acr) {
		claims.put(IDToken.ACR, acr);
		return this;
	}

	/**
	 * @param auth_time
	 *            the auth_time to set
	 */
	public CustomIDTokenBuilder setAuthTime(String authTime) {
		claims.put(IDToken.AUTH_TIME, authTime);
		return this;
	}

	/**
	 * @param at_hash
	 *            the at_hash to set
	 */
	public CustomIDTokenBuilder setAtHash(String at_hash) {
		claims.put(IDToken.AT_HASH, at_hash);
		return this;
	}

	/**
	 * @param c_hash
	 *            the c_hash to set
	 */
	public CustomIDTokenBuilder setCHash(String c_hash) {
		claims.put(IDToken.C_HASH, c_hash);
		return this;
	}

	/**
	 * Use this method to set custom claims
	 * 
	 * @param claimKey
	 * @param claimValue
	 * @return
	 */
	public CustomIDTokenBuilder setClaim(String claimKey, String claimValue) {
		if (claimKey == null || claimValue == null) {
			log.error("Key or Value cannot be null");
		}
		claims.put(claimKey, claimValue);
		return this;
	}

	/**
	 * Use this method to set custom AMR
	 * 
	 * @param amr
	 * @return
	 */
	public CustomIDTokenBuilder setAmr(JSONArray amr) {
		if (amr == null) {
			log.error("AMR cannot be null");
		}
		claims.put("amr", amr );			
		return this;
	}
	
	

	/**
	 * Use this method to set custom JWT headers
	 */
	public CustomIDTokenBuilder setHeaderParam(String key, String value) {
		if (key == null || value == null) {
			log.error("Key or Value cannot be null");
		}
		header.put(key, value);
		return this;
	}

	/**
	 * @param sigKey
	 *            the sigKey to set
	 * @param sigAlg
	 *            TODO
	 */
	public CustomIDTokenBuilder setSigKey(Key sigKey, String sigAlg) {
		this.sigKey = sigKey;
		this.sigAlg = sigAlg;
		return this;
	}

	/**
	 * @param encKey
	 *            the encKey to set
	 * @param encAlg
	 *            TODO
	 */
	public CustomIDTokenBuilder setEncKey(Key encKey, String encAlg) {
		this.encKey = encKey;
		this.encAlg = encAlg;
		return this;
	}

	/**
	 * 
	 * @return
	 * @throws IDTokenException
	 */
	public String buildIDToken() throws IDTokenException {
		checkSpecCompliance();
		// setting algorithm parameter
		header.put(JWT.HeaderParam.ALGORITHM, sigAlg);

		try {
			return new CustomJWTBuilder().setClaims(claims).setHeaderParams(header)
			                       .signJWT(sigKey, sigAlg).encryptJWT(encKey, encAlg).buildJWT();
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
