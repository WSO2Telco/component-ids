package com.gsma.shorten;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyStore;


/**
 * this Class use for the get bitly shorten url
 */
public class BitlyUrlShorten implements UrlShorten {

    @Override
    public String getShortenURL(String longUrl,String accessToken,String shortServiceUrl) {
        longUrl="https://india.mconnect.wso2telco.com?id=1235666";
        String webserviceUrl=shortServiceUrl+"?access_token="+accessToken+"&longUrl="+longUrl;
        String shortUrl=null;

        try {

            org.apache.http.client.HttpClient httpClient =  getNewHttpClient();
            HttpGet getRequest = new HttpGet(webserviceUrl);
            getRequest.addHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            String output;
            String outLine=null;
            while ((output = br.readLine()) != null) {
               outLine = output;
            }

            JSONObject jsonObj = (JSONObject)new JSONParser().parse(outLine);
            String shortUrlObject = jsonObj.get("data").toString();
            JSONObject urlObject = (JSONObject)new JSONParser().parse(shortUrlObject);
            shortUrl = urlObject.get("url").toString();
            System.out.println(shortUrl);

        }catch(Exception ex){
            ex.printStackTrace();
        }

        return shortUrl;
    }

    @SuppressWarnings("deprecation")
    public CloseableHttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            org.apache.http.conn.ssl.SSLSocketFactory sf = new SSLSocket(trustStore);
            sf.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }


}
