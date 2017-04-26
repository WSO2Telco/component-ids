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
package com.wso2telco;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.wso2telco.config.Scope;
import com.wso2telco.config.ScopeConfigs;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

// TODO: Auto-generated Javadoc

/**
 * The Class PremiumInfoScopeTest.
 */
@Ignore
public class PremiumInfoScopeTest {

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(PremiumInfoScopeTest.class);

    /**
     * The scope configs.
     */
    private ScopeConfigs scopeConfigs;

    /**
     * The client_id.
     */
    private String client_id = "izbtbCFWzsVNtXT8Sdg0QuP9Lg4a";

    /**
     * The client_secret.
     */
    private String client_secret = "KLm_CYBd93KWFowB3kb_y0Q5Iy8a";

    /**
     * The admin url.
     */
    private String adminUrl = "https://localhost:9443";

    /**
     * The jks file path.
     */
    private String jksFilePath = "/home/nipuni/Nipuni/dev-service/dialog-axiata/support/MIFE-470/wso2is-5.0.0" +
            "/repository/resources/security/wso2carbon.jks";

    /**
     * The scopes.
     */
    HashMap<String, Scope> scopes = new HashMap<String, Scope>();

    /**
     * Read scopes from file.
     */
    @Before
    public void readScopesFromFile() {
        try {
            scopeConfigs = readScopesConfig();
            readScopes();
            System.setProperty("javax.net.ssl.trustStore", jksFilePath);
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        } catch (JAXBException e) {
            log.error("Error reading scopes");
        }
    }

    /**
     * Test profile scope.
     */
    @Test
    public void testProfileScope() {
        String scope = "openid+profile";
        String access_token = getToken(scope);
        String response = getPremiumInfo(access_token);
        log.info("Response retrieved for scope:profile " + response);

        boolean isValid = isScopeValid("profile", response);
        Assert.assertTrue(isValid);
    }

    /**
     * Test email scope.
     */
    @Test
    public void testEmailScope() {
        String scope = "openid+email";
        String access_token = getToken(scope);
        String response = getPremiumInfo(access_token);
        log.info("Response retrieved for scope:email " + response);

        boolean isValid = isScopeValid("email", response);
        Assert.assertTrue(isValid);
    }

    /**
     * Test phone scope.
     */
    @Test
    public void testPhoneScope() {
        String scope = "openid+phone";
        String access_token = getToken(scope);
        String response = getPremiumInfo(access_token);
        log.info("Response retrieved for scope:phone " + response);

        boolean isValid = isScopeValid("phone", response);
        Assert.assertTrue(isValid);
    }

    /**
     * Test address scope.
     */
    @Test
    public void testAddressScope() {
        String scope = "openid+address";
        String access_token = getToken(scope);
        String response = getPremiumInfo(access_token);
        log.info("Response retrieved for scope:address " + response);

        boolean isValid = isScopeValid("address", response);
        Assert.assertTrue(isValid);
    }

    /**
     * Gets the token.
     *
     * @param scope the scope
     * @return the token
     */
    private String getToken(String scope) {
        Process curlProc;
        String outputString;
        DataInputStream curlIn = null;
        String access_token = null;
        String command = "curl -X POST -H Content-Type:application/x-www-form-urlencoded " + adminUrl
                + "/oauth2/token --insecure --data" + " client_id=" + client_id + "&" + "client_secret="
                + client_secret + "&grant_type=client_credentials&scope=" + scope;
        try {
            curlProc = Runtime.getRuntime().exec(command);

            curlIn = new DataInputStream(curlProc.getInputStream());

            while ((outputString = curlIn.readLine()) != null) {
                JSONObject obj = new JSONObject(outputString);
                access_token = obj.getString("access_token");
            }
        } catch (IOException e) {
            log.error(e);
        } catch (JSONException e) {
            log.error(e);
        }
        return access_token;

    }

    /**
     * Gets the premium info.
     *
     * @param access_token the access_token
     * @return the premium info
     */
    private String getPremiumInfo(String access_token) {
        String resp = "";
        try {
            org.apache.http.client.HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(adminUrl + "/premiuminfo/premiuminfo?schema=openid");
            request.addHeader("Authorization", "Bearer " + access_token);
            HttpResponse httpResponse = client.execute(request);
            if (httpResponse.getEntity() != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String line;

                while ((line = rd.readLine()) != null) {
                    resp += line;
                }
            }

        } catch (Exception ex) {

            log.error(ex);
        }
        return resp;
    }

    /**
     * Read scopes config.
     *
     * @return the scope configs
     * @throws JAXBException the JAXB exception
     */
    private ScopeConfigs readScopesConfig() throws JAXBException {
        Unmarshaller um = null;
        ScopeConfigs userClaims = null;
        String configPath = "config" + File.separator + "scope-config.xml";
        File file = new File(getClass().getClassLoader().getResource(configPath).getFile());
        try {
            JAXBContext ctx = JAXBContext.newInstance(ScopeConfigs.class);
            um = ctx.createUnmarshaller();
            userClaims = (ScopeConfigs) um.unmarshal(file);
        } catch (JAXBException e) {
            throw new JAXBException("Error unmarshalling file :" + configPath);
        }
        return userClaims;
    }

    /**
     * Read scopes.
     */
    private void readScopes() {
        for (Scope scope : scopeConfigs.getScopes().getScopeList()) {
            scopes.put(scope.getName(), scope);
        }
    }

    /**
     * Checks if is scope valid.
     *
     * @param scope    the scope
     * @param response the response
     * @return true, if is scope valid
     */
    private boolean isScopeValid(String scope, String response) {
        boolean isValid = true;
        JSONObject obj;
        try {
            obj = new JSONObject(response);

            if (obj.toString().equals("{}")) {
                return true;
            }

            Iterator keys = obj.keys();
            List xmlScopeList = scopes.get(scope).getClaims().getClaimValues();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (!xmlScopeList.contains(key)) {
                    log.info("Response contains a claim value that is not belongs to scope " + scope);
                    isValid = false;
                    break;
                }

            }
        } catch (JSONException e) {
            log.error(e);
            return false;
        }
        return isValid;

    }

}
