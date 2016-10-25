package com.wso2telco.internal;

import com.wso2telco.MSISDNDecription;
import com.wso2telco.util.AuthProxyConstants;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TwoEncript implements MSISDNDecription {
    public String decryptMsisdn(String encryptedText, String decryptionKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
                   IllegalBlockSizeException {
        byte[] keyArray = DatatypeConverter.parseHexBinary(encryptedText);

        Cipher c = Cipher.getInstance(AuthProxyConstants.RC4_KEY);
        c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptionKey.getBytes(), AuthProxyConstants.RC4_KEY));
        byte[] decrypted = c.doFinal(keyArray);
        String decryptedMSISDN = new String(decrypted);
        return decryptedMSISDN;
    }
}
