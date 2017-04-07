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

import com.google.gdata.util.common.util.Base64;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.ConfigurationException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * This class used to encrypt the msisdn number.
 */
public class EncryptAES {
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

    /**
     * Encrypt MSISDN.
     *
     * @param plainText the plain text.
     * @return encrypted value of input parameter.
     * @throws ConfigurationException    on errors
     * @throws NoSuchPaddingException    on errors
     * @throws NoSuchAlgorithmException  on errors
     * @throws InvalidKeyException       on errors
     * @throws BadPaddingException       on errors
     * @throws IllegalBlockSizeException on errors
     */
    public static String encrypt(String plainText)
            throws ConfigurationException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        if (encryptionKey != null) {
            byte[] encryptionKeyByteValue = encryptionKey.getBytes();
            SecretKey secretKey = new SecretKeySpec(encryptionKeyByteValue, AuthProxyConstants.ASE_KEY);
            String encryptedText = null;
            Cipher cipher = Cipher.getInstance(AuthProxyConstants.ASE_KEY);

            if (plainText != null) {
                byte[] plainTextByte = plainText.getBytes();
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] encryptedByte = cipher.doFinal(plainTextByte);
                encryptedText = Base64.encode(encryptedByte);
            }
            return encryptedText;
        } else {
            throw new ConfigurationException("MSISDN EncryptionKey could not be found in mobile-connect.xml");
        }
    }
}