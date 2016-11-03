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
package com.wso2telco.proxy.internal;

import com.wso2telco.proxy.MSISDNDecryption;
import com.wso2telco.proxy.util.AuthProxyConstants;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class DecryptMsisdn implements MSISDNDecryption {
    public String decryptMsisdn(String encryptedText, String decryptionKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
                   IllegalBlockSizeException {
        byte[] hexBinaryOfEncryptedText = DatatypeConverter.parseHexBinary(encryptedText);

        Cipher cipher = Cipher.getInstance(AuthProxyConstants.RC4_KEY);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptionKey.getBytes(), AuthProxyConstants.RC4_KEY));
        byte[] byteCodeOfDecryptedMsisdn = cipher.doFinal(hexBinaryOfEncryptedText);
        String decryptedMSISDN = new String(byteCodeOfDecryptedMsisdn);
        return decryptedMSISDN;
    }
}
