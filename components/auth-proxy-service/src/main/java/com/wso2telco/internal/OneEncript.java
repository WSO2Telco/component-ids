package com.wso2telco.internal;

import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;
import com.wso2telco.MSISDNDecription;
import com.wso2telco.util.AuthProxyConstants;
import com.wso2telco.util.FileUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class OneEncript implements MSISDNDecription{
    public String decryptMsisdn(String encryptedText, String decryptionKey)
            throws Base64DecoderException, UnsupportedEncodingException, NoSuchAlgorithmException,
                   NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] decoded = Base64.decode((String) encryptedText);
        byte[] bytesofkey = decryptionKey.getBytes(AuthProxyConstants.UTF_ENCODER);
        MessageDigest md5digest = MessageDigest.getInstance(AuthProxyConstants.MD5_DIGEST);
        byte[] hashval = md5digest.digest(bytesofkey);
        SecretKeySpec rc4Key = new SecretKeySpec(hashval, AuthProxyConstants.RC4_KEY);
        Cipher rc4Decrypt = Cipher.getInstance(AuthProxyConstants.RC4_KEY);
        rc4Decrypt.init(2, rc4Key);
        byte[] decryptedText = rc4Decrypt.doFinal(decoded);
        String decryptedMSISDN = new String(decryptedText, AuthProxyConstants.UTF_ENCODER);
        return decryptedMSISDN;
    }
}
