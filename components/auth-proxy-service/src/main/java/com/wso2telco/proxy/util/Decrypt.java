package com.wso2telco.proxy.util;


import com.wso2telco.core.config.DataHolder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class Decrypt {


    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(Decrypt.class);

    /**
     * Decrypt data.
     *
     * @param data                the data
     * @param encryptionAlgorithm encryption algorithmFileUtil
     * @return the string
     * @throws Exception the exception
     */
    public static String decryptData(String data, String encryptionAlgorithm) throws Exception {
        byte[] bytes = hexStringToByteArray(data);
        String filename = DataHolder.getInstance().getMobileConnectConfig().getKeyfile();
        PrivateKey key = getPrivateKey(filename, encryptionAlgorithm);
        return decrypt(bytes, key, encryptionAlgorithm);
    }


    /**
     * Hex string to byte array.
     *
     * @param s the s
     * @return the byte[]
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Decrypt.
     *
     * @param text                the text
     * @param key                 the key
     * @param encryptionAlgorithm encryption algorithm
     * @return the string
     */
    public static String decrypt(byte[] text, PrivateKey key, String encryptionAlgorithm) {
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance(encryptionAlgorithm);

            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] dectyptedText = cipher.doFinal(text);

            return new String(dectyptedText);

        } catch (Exception ex) {
            log.error("Exception encrypting data " + ex.getClass().getName() + ": " + ex.getMessage());
            return null;
        }
    }

    /**
     * Gets the private key.
     *
     * @param filename            the filename
     * @param encryptionAlgorithm encryption algorithm
     * @return the private key
     * @throws Exception the exception
     */
    public static PrivateKey getPrivateKey(String filename, String encryptionAlgorithm) throws Exception {

        try {

            String publicK = readStringKey(filename);
            //byte[] keyBytes = new BASE64Decoder().decodeBuffer(publicK);
            byte[] keyBytes = Base64.decodeBase64(publicK.getBytes());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory fact = KeyFactory.getInstance("RSA");

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(encryptionAlgorithm);
            return kf.generatePrivate(spec);

        } catch (Exception ex) {
            log.error("Exception reading private key:" + ex.getMessage());
            return null;
        }
    }

    /**
     * Read string key.
     *
     * @param fileName the file name
     * @return the string
     */
    public static String readStringKey(String fileName) {

        BufferedReader reader = null;
        StringBuffer fileData = null;
        try {

            fileData = new StringBuffer(2048);
            reader = new BufferedReader(new FileReader(fileName));
            char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }

            reader.close();

        } catch (Exception e) {
        } finally {
            if (reader != null) {
                reader = null;
            }
        }
        return fileData.toString();

    }
}
