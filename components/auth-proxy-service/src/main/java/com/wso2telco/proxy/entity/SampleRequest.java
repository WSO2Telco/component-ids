package com.wso2telco.proxy.entity;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;

@Path("/sample")
public class SampleRequest {
    private static Log log = LogFactory.getLog(SampleRequest.class);


       /*
    Sample Request:
curl -X POST \
 https://localhost:9443/authproxy/sample/oauth2/sign \
 -H 'content-type: application/json' \
 -d '{"response_type":"mc_si_async_code",
"scope":"openid",
"login_hint":"911111111110",
"client_notification_token":"ybyy",
"notification_uri":"https://localhost:9443/playground2/oauth2.jsp",
"acr_value":"11",
"state":"state123",
"nonce":"nonce123",
"client_id":"8DtnWHOza4SY2cSkqUi7QxGlQkIa",
"iss":"8DtnWHOza4SY2cSkqUi7QxGlQkIa",
"aud":"spark",
"version":"123"}
     */


    //sample request to create signed  jwe
    @POST
    @Path("/oauth2/sign")
    public Response sign(@Context HttpServletRequest httpServletRequest, @Context
            HttpServletResponse httpServletResponse, @Context HttpHeaders httpHeaders, @Context UriInfo uriInfo,
                         @PathParam("operatorName") String operatorName, String jsonBody) throws Exception {

        String sharedKey = readSignatureFile("./repository/conf/keys/jwsSigningPrivateKey.pem");
        String jwe = signJson(jsonBody, sharedKey);
        log.info("JWE: " + jwe);
        return Response.status(Response.Status.OK.getStatusCode()).entity("JWE: " + jwe).build();
    }

    private String signJson(String message, String sharedKey) throws JOSEException,
            GeneralSecurityException {
        Payload payload = new Payload(message);
        if (log.isDebugEnabled()) {
            log.debug("JWS payload message: " + message);
        }

        // Create JWS header with RS256 algorithm
        JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
        header.setContentType("text/plain");

        if (log.isDebugEnabled()) {
            log.debug("JWS header: " + header.toJSONObject());
        }

        // Create JWS object
        JWSObject jwsObject = new JWSObject(header, payload);

        if (log.isDebugEnabled()) {
            log.debug("HMAC key: " + sharedKey);
        }

        RSAPrivateKey loadPrivateKey = loadPrivateKey(sharedKey);
        JWSSigner signer = new RSASSASigner(loadPrivateKey);

        try {
            jwsObject.sign(signer);
        } catch (JOSEException e) {
            log.error("Couldn't sign JWS object: " + e.getMessage());
            throw e;
        }

        // Serialise JWS object to compact format
        String serializedJWE = jwsObject.serialize();

        if (log.isDebugEnabled()) {
            log.debug("Serialised JWS object: " + serializedJWE);
        }

        return serializedJWE;
    }

    //read JWS key file
    private String readSignatureFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    //Generate RSAPrivateKey private key
    public static RSAPrivateKey loadPrivateKey(String privateKeyContent) throws GeneralSecurityException {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        privateKeyContent = privateKeyContent.substring(0, privateKeyContent.length() - 1);
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        PrivateKey key = kf.generatePrivate(keySpecPKCS8);
        return (RSAPrivateKey) key;
    }

    //Sample notification endpoint.For testing purposes only
    @POST
    @Path("/sp/token-endpoint")
    public void spTokenEndpoint(@Context HttpServletRequest httpServletRequest, @Context
            HttpServletResponse httpServletResponse, @Context HttpHeaders httpHeaders, @Context UriInfo uriInfo,
                                String jsonBody) throws Exception {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sp_auth_header", httpHeaders.getRequestHeader("Authorization"));

        JSONObject body = new JSONObject(jsonBody);
        String token = "";
        if (body.has("access_token")) {
            token = body.getString("access_token");
        }
        jsonObject.put("access_token", token);

        log.info("Response in spTokenEndpoint:" + jsonObject.toString());

        //return Response.status(Response.Status.OK.getStatusCode()).entity(jsonObject.toString()).build();
    }
}
