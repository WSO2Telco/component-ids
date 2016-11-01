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
