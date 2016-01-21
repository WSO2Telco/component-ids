package com.gsma.authenticators.cryptosystem;

import org.xml.sax.SAXException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.Map;

public class AESencrp {


    private static final String ALGO = "AES";
    private static String key;
    private static byte[] keyValue ;


    public static String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = new BASE64Encoder().encode(encVal);
        return encryptedValue;
    }

    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    private static Key generateKey() throws Exception {
        com.gsma.utils.ReadMobileConnectConfig readMobileConnectConfig = new com.gsma.utils.ReadMobileConnectConfig();
        Map<String, String> readMobileConnectConfigResult= null;
        try {
            readMobileConnectConfigResult = readMobileConnectConfig.query("dialog/SMS");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        key = readMobileConnectConfigResult.get("AesKey");
        keyValue = key.getBytes(Charset.forName("UTF-8"));
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }

}

