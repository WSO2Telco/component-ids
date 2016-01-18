package org.wso2.carbon.identity.application.authentication.endpoint.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ScopeTest {

    @Test
    public void testPrintHelloWorld() {


        String outputString;
        String client_id="UGibF0qGR4Tfj_FoPXmfcVjDYEQa";
        String client_secret="EVoMEE9untkntAtfXKkvHA_0pIsa";
        String scope="openid";
        String resp = "";
        String isUrlHTTPS="https://localhost:9444";
        String path = "/home/nilan/Software/Axiata/ISAM/wso2is-5.0.0/repository/resources/security/wso2carbon.jks";

        String command = "curl -X POST -H Content-Type:application/x-www-form-urlencoded "+isUrlHTTPS+"/oauth2/token --insecure --data" +
                " client_id="+client_id+"&" +
                "client_secret="+client_secret+"&grant_type=client_credentials&scope="+scope;

        System.out.println(command);

        Process curlProc;
        try {
            curlProc = Runtime.getRuntime().exec(command);

            DataInputStream curlIn = new DataInputStream(curlProc.getInputStream());

            while ((outputString = curlIn.readLine()) != null) {
                JSONObject obj = new JSONObject(outputString);
                System.out.println(obj);



                System.setProperty("javax.net.ssl.trustStore", path);
                System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
                String access_token=obj.getString("access_token");

                System.out.println(access_token);

                try {
                    org.apache.http.client.HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet(isUrlHTTPS+"/oauth2/userinfo?schema="+scope);
                    request.addHeader("Authorization", "Bearer " + access_token);
                    HttpResponse httpResponse= client.execute(request);
                    System.out.println(httpResponse.getStatusLine());
                    if (httpResponse.getEntity() != null) {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                        String line;

                        while ((line = rd.readLine()) != null) {
                            resp += line;
                        }
                        System.out.println(resp);
                    }

                }catch(Exception ex){

                    ex.printStackTrace();
                }

            }


        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }




}

