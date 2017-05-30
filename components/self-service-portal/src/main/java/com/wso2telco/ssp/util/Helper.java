package com.wso2telco.ssp.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helper util methods
 */
public class Helper {

    /**
     *
     * @param value Input string
     * @return Hashed value of the input
     * @throws NoSuchAlgorithmException Algorithm error
     * @throws UnsupportedEncodingException Encoding error
     */
    public static String GetHashValue(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value.getBytes("UTF-8"));

        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
