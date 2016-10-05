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
package com.wso2telco.gsma.shorten;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyStore;

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


 
// TODO: Auto-generated Javadoc
/**
 * The Class BitlyUrlShorten.
 */
public class BitlyUrlShorten implements UrlShorten {

    /* (non-Javadoc)
     * @see com.wso2telco.gsma.shorten.UrlShorten#getShortenURL(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String getShortenURL(String longUrl,String accessToken,String shortServiceUrl) {

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

    /**
     * Gets the new http client.
     *
     * @return the new http client
     */
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
