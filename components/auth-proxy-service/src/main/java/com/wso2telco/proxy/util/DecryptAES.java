package com.wso2telco.proxy.util;

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

    private static MobileConnectConfig mobileConnectConfigs = null;
    private static String encryptionKey = null;

    static {
        //Load mobile-connect.xml file.
        mobileConnectConfigs = ConfigLoader.getInstance().getMobileConnectConfig();
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
            if (encryptedText != null){
                byte[] encryptedTextByte = Base64.decodeBase64(encryptedText);
                Cipher cipher = Cipher.getInstance("AES");

                cipher.init(Cipher.DECRYPT_MODE,secretKey);
                byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
                decryptedText = new String(decryptedByte);
            }
            return decryptedText;
        } else {
            throw new ConfigurationException("MSISDN EncryptionKey could not be found in mobile-connect.xml");
        }
    }
}
