package com.axiata.mife;

import com.axiata.mife.config.Scope;
import com.axiata.mife.config.ScopeConfigs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This test case test the response retrieved from userinfo endpoint against a config file scope-config.xml
 * This test cases need a IS server to up and running in order to pass the tests. Please change  the client_id,
 * client_secret, adminUrl and jksFilePath with valid values
 */

@Ignore
public class UserInfoScopeTest {

    private static Log log = LogFactory.getLog(UserInfoScopeTest.class);
    private ScopeConfigs scopeConfigs;
    private String client_id = "izbtbCFWzsVNtXT8Sdg0QuP9Lg4a";
    private String client_secret = "KLm_CYBd93KWFowB3kb_y0Q5Iy8a";
    private String adminUrl = "https://localhost:9443";
    private String jksFilePath = "/home/nipuni/Nipuni/dev-service/dialog-axiata/support/MIFE-470/wso2is-5.0.0/repository/resources/security/wso2carbon.jks";


    HashMap<String, Scope> scopes = new HashMap<String, Scope>();

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

    @Test
    public void testProfileScope() {
        String scope = "openid+profile";
        String access_token = getToken(scope);
        String response = getUserInfo(access_token);
        System.out.println("Response retrieved for scope:profile " + response);

        boolean isValid = isScopeValid("profile", response);
        Assert.assertTrue(isValid);
    }

    @Test
    public void testEmailScope() {
        String scope = "openid+email";
        String access_token = getToken(scope);
        String response = getUserInfo(access_token);
        System.out.println("Response retrieved for scope:email " + response);

        boolean isValid = isScopeValid("email", response);
        Assert.assertTrue(isValid);
    }

    @Test
    public void testPhoneScope() {
        String scope = "openid+phone";
        String access_token = getToken(scope);
        String response = getUserInfo(access_token);
        System.out.println("Response retrieved for scope:phone " + response);

        boolean isValid = isScopeValid("phone", response);
        Assert.assertTrue(isValid);
    }

    @Test
    public void testAddressScope() {
        String scope = "openid+address";
        String access_token = getToken(scope);
        String response = getUserInfo(access_token);
        System.out.println("Response retrieved for scope:address " + response);

        boolean isValid = isScopeValid("address", response);
        Assert.assertTrue(isValid);
    }


    private String getToken(String scope) {
        Process curlProc;
        String outputString;
        DataInputStream curlIn = null;
        String access_token = null;
        String command = "curl -X POST -H Content-Type:application/x-www-form-urlencoded " + adminUrl + "/oauth2/token --insecure --data" +
                " client_id=" + client_id + "&" +
                "client_secret=" + client_secret + "&grant_type=client_credentials&scope=" + scope;
        try {
            curlProc = Runtime.getRuntime().exec(command);

            curlIn = new DataInputStream(curlProc.getInputStream());

            while ((outputString = curlIn.readLine()) != null) {
                JSONObject obj = new JSONObject(outputString);
                access_token = obj.getString("access_token");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return access_token;

    }

    private String getUserInfo(String access_token) {
        String resp = "";
        try {
            org.apache.http.client.HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(adminUrl + "/oauth2/userinfo?schema=openid");
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

            ex.printStackTrace();
        }
        return resp;
    }

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

    private void readScopes() {
        for (Scope scope : scopeConfigs.getScopes().getScopeList()) {
            scopes.put(scope.getName(), scope);
        }
    }

    private boolean isScopeValid(String scope, String response) {
        boolean isValid = true;
        JSONObject obj = new JSONObject(response);
        if (obj.toString().equals("{}")) {
            System.out.println("No claims have not null values in the scope : " + scope);
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
        return isValid;

    }

}

