/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 * 
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.gsma.authenticators.cryptosystem;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import com.wso2telco.core.config.ReadMobileConnectConfig;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AESencrp.
 */
public class AESencrp {

    /** The log. */
    private static Log log = LogFactory.getLog(AESencrp.class);
	
    /** The Constant ALGO. */
    private static final String ALGO = "AES";
    
    /** The key. */
    private static String key;
    
    /** The key value. */
    private static byte[] keyValue ;


    /**
     * Encrypt.
     *
     * @param Data the data
     * @return the string
     * @throws Exception the exception
     */
    public static String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        //String encryptedValue = new BASE64Encoder().encode(encVal);
        byte[] encryptedValue = Base64.encodeBase64(encVal);
        return new String(encryptedValue);
    }

    /**
     * Decrypt.
     *
     * @param encryptedData the encrypted data
     * @return the string
     * @throws Exception the exception
     */
    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        //byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decordedValue = Base64.decodeBase64(encryptedData.getBytes());
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    /**
     * Generate key.
     *
     * @return the key
     * @throws Exception the exception
     */
    private static Key generateKey() throws Exception {
        Map<String, String> readMobileConnectConfigResult= null;
        readMobileConnectConfigResult = ReadMobileConnectConfig.query("SMS");

        key = readMobileConnectConfigResult.get("AesKey");
        keyValue = key.getBytes(Charset.forName("UTF-8"));
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }

}

