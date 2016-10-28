package com.wso2telco.genz.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.ConfigurationException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class DecryptAES {

    public static String decrypt(String encryptedText)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
                   IllegalBlockSizeException, ConfigurationException {
        MobileConnectConfig mobileConnectConfigs = ConfigLoader.getInstance().getMobileConnectConfig();
        byte[] keyValue;
        Cipher cipher;
        if (mobileConnectConfigs != null) {
            keyValue = mobileConnectConfigs.getMsisdn().getEncryptionKey().getBytes();
            SecretKey key = new SecretKeySpec(keyValue, AuthProxyConstants.ASE_KEY);
            String decryptedText = null;
            if (encryptedText != null){
                byte[] encryptedTextByte = Base64.decodeBase64(encryptedText);
                cipher = Cipher.getInstance("AES");

                cipher.init(Cipher.DECRYPT_MODE,key);
                byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
                decryptedText = new String(decryptedByte);
            }
            return decryptedText;
        } else {
            throw new ConfigurationException("MSISDN EncryptionKey could not be found in mobile-connect.xml");
        }
    }
}
