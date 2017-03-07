package com.wso2telco.openidtokenbuilder;


import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.JWTBuilder;
import org.apache.oltu.oauth2.jwt.JWTException;
import org.apache.oltu.oauth2.jwt.JWTUtil;
import org.codehaus.jettison.json.JSONException;

public class CustomJWTBuilder {

    private static Log log = LogFactory.getLog(CustomJWTBuilder.class);
    private static boolean debug = log.isDebugEnabled();

    private Map<String, Object> headerParams = new HashMap<String, Object>();
    private Map<String, Object> payloadClaims = new HashMap<String, Object>();
    private String headerJson = null;
    private String payloadJson = null;
    private String signatureJson = null;
    private String encodedHeader = null;
    private String encodedPayload = "";
    private String encodedSignature = "";
    private Key sigKey = null;
    private String sigAlg = null;
    private Key encKey = null;
    private String encAlg = null;
    private boolean isSignAndEncrypt = false;

    /**
     * This methods is used to add header parameters to the JWT header. Custom
     * JWT headers can be defined. The builder does not evaluate the semantic
     * meanings of the parameters. The JWT receiver should process the semantic
     * meanings of those parameters. However builder does not allow empty valued
     * parameters and will throw a {@link JWTException} in such an encounter. In
     * case of duplicate params, the older value will be replaced by the new
     * value.
     *
     * @param headerParamName param name
     * @param headeParamValue param value
     * @return {@link JWTBuilder}
     * @throws JWTException
     */
    public CustomJWTBuilder setHeaderParam(String headerParamName, String headeParamValue)
            throws JWTException {
        if (headeParamValue == null || headeParamValue.equals("")) {
            throw new JWTException("Empty JWT header parameters NOT allowed");
        }
        headerParams.put(headerParamName, headeParamValue);
        return this;
    }

    /**
     * This method is set to add header parameters to the JWT header.
     *
     * @param headerParams
     */
    public void setHeaderParams(Map<String, Object> headerParams) {
        this.headerParams = headerParams;
    }

    /**
     * This method is used to add claims to the JWT. In case of duplicate
     * claims, the older value will be replaced by the new value. Empty claim
     * values are not allowed.
     *
     * @param claimName  name of the claim
     * @param claimvalue value of the claim
     * @throws JWTException JWT exception
     */
    public void setClaim(String claimName, String claimvalue) throws JWTException {
        if (claimvalue == null || claimvalue.equals("")) {
            throw new JWTException("Empty JWT claims NOT allowed");
        }
        payloadClaims.put(claimName, claimvalue);
    }

    /**
     * This method is used to set claim values to the JWT
     *
     * @param claims
     * @return
     */
    public CustomJWTBuilder setClaims(Map<String, Object> claims) {
        payloadClaims = claims;
        return this;
    }

    /**
     * Sign the JWT headerParams and payloadClaims with the key provided using
     * the signature Algorithm provided.
     *
     * @param sigKey sign key
     * @param sigAlg sign algorithm
     */
    public void signJWT(Key sigKey, String sigAlg) {
        this.sigKey = sigKey;
        this.sigAlg = sigAlg;
    }

    /**
     * Encrypt the JWT headerParams and payloadClaims with the key provided
     * using
     * the encryption Algorithm provided
     *
     * @param encKey encrypt key
     * @param encAlg encrypt algorithm
     */
    public void encryptJWT(Key encKey, String encAlg) {
        this.encKey = encKey;
        this.encAlg = encAlg;
    }

    public CustomJWTBuilder doSignAndEnctypt(boolean signAndEncrypt) {
        this.isSignAndEncrypt = signAndEncrypt;
        return this;
    }

    /**
     * This method returns the completed JWT which is the concatenation of the
     * base 64 encoded header JSON, payload JSON and signature with the period
     * (".") between them.
     *
     * @return gets the JWT
     * @throws JWTException
     */
    public String buildJWT() throws JWTException {
        buildJWTHeader();
        buildJWTPayload();
        return concatenateParts();
    }

    /**
     * This method builds the JWT payload. The JWT payload is a JSON with
     * claims. The payload cannot be NULL. For null payloads the Builder throws
     * a {@linkJWTException}.
     *
     * @throws JWTException
     */
    private void buildJWTPayload() throws JWTException {
        if (!payloadClaims.isEmpty()) {
            try {
                payloadJson = CustomJSONUtils.buildJSON(payloadClaims);
                encodedPayload = JWTUtil.encodeJSON(payloadJson);
                if (debug) {
                    log.debug("JWT payload : " + payloadJson);
                    log.debug("Encoded JWT payload : " + encodedPayload);
                }
            } catch (JSONException e) {
                log.debug(e);
                throw new JWTException("Error while building JWTPayload", e);
            }
        } else {
            throw new JWTException("JWT Payload cannot be NULL");
        }
    }

    /**
     * This method builds the JWT Header. JWT must have a header and the 'alg'
     * parameter must be in the HWT Header. This method throws a
     * {@linkJWTException} if the JWT Header doesn't meet those requirements.
     *
     * @throws JWTException
     */
    private void buildJWTHeader() throws JWTException {
        // The alg parameter MUST have a value
        if (sigAlg == null && !headerParams.containsKey(JWT.HeaderParam.ALGORITHM)) {
            log.warn("No signature algorithm defined. Building a plain-text JWT");
            headerParams.put(JWT.HeaderParam.ALGORITHM, JWT.HeaderParamValue.ALG_NONE);
        }
        // The type parameter MUST have the value JWT
        if (!headerParams.containsKey(JWT.HeaderParam.TYPE)) {
            headerParams.put(JWT.HeaderParam.TYPE, JWT.HeaderParamValue.TYPE_JWT);
        } else if (headerParams.get(JWT.HeaderParam.CONTENT_TYPE) != JWT.HeaderParamValue.TYPE_JWT) {
            headerParams.put(JWT.HeaderParam.CONTENT_TYPE, JWT.HeaderParamValue.TYPE_JWT);
        }
        try {
            headerJson = CustomJSONUtils.buildJSON(headerParams);
            encodedHeader = JWTUtil.encodeJSON(headerJson);
            if (debug) {
                log.debug("JWT header :" + headerJson);
                log.debug("Encoded JWT header" + encodedHeader);
            }
        } catch (JSONException e) {
            log.debug(e);
            throw new JWTException("Error while building JWTHeader", e);
        }
    }

    /**
     * This method concatenates the headerParams, payloadClaims and signature
     * according to the JWT specification.
     *
     * @return
     */
    private String concatenateParts() {
        StringBuilder jwt = new StringBuilder();
        jwt.append(encodedHeader + ".");
        jwt.append(encodedPayload + ".");
        jwt.append(encodedSignature);
        return jwt.toString();
    }
}
