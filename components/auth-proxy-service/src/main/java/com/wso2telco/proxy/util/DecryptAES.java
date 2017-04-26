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

package com.wso2telco.proxy.util;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.ConfigurationException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class DecryptAES {

    private static MobileConnectConfig mobileConnectConfigs = null;
    private static String encryptionKey = null;

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    static {
        //Load mobile-connect.xml file.
        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
        if (mobileConnectConfigs != null) {
            encryptionKey = mobileConnectConfigs.getMsisdn().getEncryptionKey();
        }
    }

    public static String decrypt(String encryptedText)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException, ConfigurationException {
        if (encryptionKey != null) {
            byte[] encryptionKeyByteValue = encryptionKey.getBytes();
            SecretKey secretKey = new SecretKeySpec(encryptionKeyByteValue, AuthProxyConstants.ASE_KEY);
            String decryptedText = null;
            if (encryptedText != null) {
                byte[] encryptedTextByte = Base64.decodeBase64(encryptedText);
                Cipher cipher = Cipher.getInstance("AES");

                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
                decryptedText = new String(decryptedByte);
            }
            return decryptedText;
        } else {
            throw new ConfigurationException("MSISDN EncryptionKey could not be found in mobile-connect.xml");
        }
    }
}
