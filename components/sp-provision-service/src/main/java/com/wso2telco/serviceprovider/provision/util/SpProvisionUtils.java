package com.wso2telco.serviceprovider.provision.util;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.KeyStore;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class SpProvisionUtils {
    private static final Logger log = Logger.getLogger(SpProvisionUtils.class);

    public static DefaultHttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SslSocketFactory sf = new SslSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams parameters = new BasicHttpParams();
            HttpProtocolParams.setVersion(parameters, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(parameters, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(parameters,
                    registry);

            return new DefaultHttpClient(ccm, parameters);
        } catch (Exception exception) {
            log.error("Exception" + exception.getMessage(),exception);
            return new DefaultHttpClient();
        }
    }

    public static String readRequestBody(BufferedReader br) throws IOException {
        String line = "";
        String requestBody = "";
        while ((line = br.readLine()) != null) {
            requestBody += line;
        }
        return requestBody;
    }

    public static JSONObject stringToJsonObject(String jsonStr) throws JSONException {
        return new JSONObject(jsonStr);
    }
}
