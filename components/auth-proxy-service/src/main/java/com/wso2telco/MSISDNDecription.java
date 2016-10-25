package com.wso2telco;

import com.google.gdata.util.common.util.Base64DecoderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface MSISDNDecription {
    String decryptMsisdn(String encryptedText, String decryptionKey)
            throws Base64DecoderException, UnsupportedEncodingException, NoSuchAlgorithmException,
                   NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException;
}
