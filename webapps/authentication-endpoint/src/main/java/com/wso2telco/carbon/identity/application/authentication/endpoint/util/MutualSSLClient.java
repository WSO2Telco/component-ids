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
package com.wso2telco.carbon.identity.application.authentication.endpoint.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Map;


// TODO: Auto-generated Javadoc
/**
 * The Class MutualSSLClient.
 */
public class MutualSSLClient {

    /** The Constant log. */
    private static final Log log = LogFactory.getLog(MutualSSLClient.class);
    
    /** The key store. */
    private static KeyStore keyStore;
    
    /** The trust store. */
    private static KeyStore trustStore;
    
    /** The key store password. */
    private static String keyStorePassword;
    
    /** The key store type. */
    private static String KEY_STORE_TYPE = "JKS";
    
    /** The trust store type. */
    private static String TRUST_STORE_TYPE = "JKS";
    
    /** The key manager type. */
    private static String KEY_MANAGER_TYPE = "SunX509";
    
    /** The trust manager type. */
    private static String TRUST_MANAGER_TYPE = "SunX509";
    
    /** The protocol. */
    private static String PROTOCOL = "SSLv3";
    
    /** The https url connection. */
    private static HttpsURLConnection httpsURLConnection;
    
    /** The ssl socket factory. */
    private static SSLSocketFactory sslSocketFactory;

     
    /**
     * Load key store.
     *
     * @param keyStorePath the key store path
     * @param keyStorePassoword the key store passoword
     * @throws KeyStoreException the key store exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws CertificateException the certificate exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public static void loadKeyStore(String keyStorePath, String keyStorePassoword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        keyStorePassword = keyStorePassoword;
        keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
        keyStore.load(new FileInputStream(keyStorePath),
                keyStorePassoword.toCharArray());
    }

     
    /**
     * Load trust store.
     *
     * @param trustStorePath the trust store path
     * @param trustStorePassoword the trust store passoword
     * @throws KeyStoreException the key store exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws CertificateException the certificate exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public static void loadTrustStore(String trustStorePath, String trustStorePassoword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        trustStore = KeyStore.getInstance(TRUST_STORE_TYPE);
        trustStore.load(new FileInputStream(trustStorePath),
                trustStorePassoword.toCharArray());
    }

     
    /**
     * Inits the mutual ssl connection.
     *
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws KeyStoreException the key store exception
     * @throws KeyManagementException the key management exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws UnrecoverableKeyException the unrecoverable key exception
     */
    public static void initMutualSSLConnection()
            throws NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException, IOException, UnrecoverableKeyException {

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KEY_MANAGER_TYPE);
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
        trustManagerFactory.init(trustStore);
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        sslSocketFactory = sslContext.getSocketFactory();
    }

     
    /**
     * Send post request.
     *
     * @param backendURL the backend url
     * @param message the message
     * @param requestProps the request props
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String sendPostRequest(String backendURL, String message, Map<String, String> requestProps)
            throws IOException {
        URL url = new URL(backendURL);
        httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setRequestMethod("POST");
        if (requestProps != null && requestProps.size() > 0) {
            for (Map.Entry<String, String> entry : requestProps.entrySet()) {
                httpsURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        OutputStream outputStream = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        StringBuilder response = null;
        try {
            outputStream = httpsURLConnection.getOutputStream();
            outputStream.write(message.getBytes());
            inputStream = httpsURLConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            log.error("Calling url : " + url + "failed. ", e);
        } finally {
            reader.close();
            inputStream.close();
            outputStream.close();
        }
        return response.toString();
    }

     
    /**
     * Send get request.
     *
     * @param backendURL the backend url
     * @param message the message
     * @param requestProps the request props
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String sendGetRequest(String backendURL, String message, Map<String, String> requestProps)
            throws IOException {
        URL url = new URL(backendURL);
        httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setRequestMethod("GET");
        if (requestProps != null && requestProps.size() > 0) {
            for (Map.Entry<String, String> entry : requestProps.entrySet()) {
                httpsURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        OutputStream outputStream = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        StringBuilder response = null;
        try {
            outputStream = httpsURLConnection.getOutputStream();
            //outputStream.write(message.getBytes());
            inputStream = httpsURLConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            log.error("Calling url : " + url + "failed. ", e);
        } finally {
            reader.close();
            inputStream.close();
            outputStream.close();
        }
        return response.toString();
    }

    /**
     * Gets the key store type.
     *
     * @return the key store type
     */
    public static String getKeyStoreType() {
        return KEY_STORE_TYPE;
    }

    /**
     * Sets the key store type.
     *
     * @param KEY_STORE_TYPE the new key store type
     */
    public static void setKeyStoreType(String KEY_STORE_TYPE) {
        MutualSSLClient.KEY_STORE_TYPE = KEY_STORE_TYPE;
    }

    /**
     * Gets the trust store type.
     *
     * @return the trust store type
     */
    public static String getTrustStoreType() {
        return TRUST_STORE_TYPE;
    }

    /**
     * Sets the trust store type.
     *
     * @param TRUST_STORE_TYPE the new trust store type
     */
    public static void setTrustStoreType(String TRUST_STORE_TYPE) {
        MutualSSLClient.TRUST_STORE_TYPE = TRUST_STORE_TYPE;
    }

    /**
     * Gets the key manager type.
     *
     * @return the key manager type
     */
    public static String getKeyManagerType() {
        return KEY_MANAGER_TYPE;
    }

    /**
     * Sets the t key manager type.
     *
     * @param KEY_MANAGER_TYPE the new t key manager type
     */
    public static void settKeyManagerType(String KEY_MANAGER_TYPE) {
        MutualSSLClient.KEY_MANAGER_TYPE = KEY_MANAGER_TYPE;
    }

    /**
     * Gets the trust manager type.
     *
     * @return the trust manager type
     */
    public static String getTrustManagerType() {
        return TRUST_MANAGER_TYPE;
    }

    /**
     * Gets the trust manager type.
     *
     * @param TRUST_MANAGER_TYPE the trust manager type
     * @return the trust manager type
     */
    public static void getTrustManagerType(String TRUST_MANAGER_TYPE) {
        MutualSSLClient.TRUST_MANAGER_TYPE = TRUST_MANAGER_TYPE;
    }

    /**
     * Gets the https url connection.
     *
     * @return the https url connection
     */
    public static HttpsURLConnection getHttpsURLConnection() {
        return httpsURLConnection;
    }

    /**
     * Sets the protocol.
     *
     * @param PROTOCOL the new protocol
     */
    public static void setProtocol(String PROTOCOL) {
        MutualSSLClient.PROTOCOL = PROTOCOL;
    }
}
