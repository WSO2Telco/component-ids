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
package com.wso2telco.gsma.authenticators.util;

import com.google.gdata.util.common.util.Base64;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
 


// TODO: Auto-generated Javadoc
/**
 * The Class DecryptionAES.
 */
public class DecryptionAES {
    
    /** The cipher. */
    private static Cipher cipher;
    
    /** The key value. */
    private static byte[] keyValue;

    /** The Configuration service */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    static{
        keyValue = configurationService.getDataHolder().getMobileConnectConfig().getMsisdn().getEncryptionKey().getBytes();
    }
    
    /**
     * Decrypt.
     *
     * @param encryptedText the encrypted text
     * @return the string
     * @throws Exception the exception
     */
    public static String decrypt(String encryptedText)
			throws Exception {
		
               String decryptedText =null ;
               if (encryptedText != null){
                    SecretKey key = new SecretKeySpec(keyValue, "AES");

                    byte[] encryptedTextByte = Base64.decode(encryptedText);
                    cipher = Cipher.getInstance("AES");

                    cipher.init(Cipher.DECRYPT_MODE,key);
                    byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
                    decryptedText = new String(decryptedByte);
               }
               else{
                  //nop 
               }
		return decryptedText;
	}
    
     
}
    

