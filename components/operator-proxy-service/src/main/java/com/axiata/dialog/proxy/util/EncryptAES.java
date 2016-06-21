/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.axiata.dialog.proxy.util;

import com.google.gdata.util.common.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author WSO2Telco
 */
public class EncryptAES {
    
    private Cipher cipher;

    private static Log log = LogFactory.getLog(EncryptAES.class);
    
    private static byte[] keyValue;
    
    static{
        keyValue = FileUtil.getApplicationProperty("key").getBytes();
    }
    
    public String encrypt(String plainText)
			 {
                 String encryptedText = "";

                 try {
                SecretKey key = new SecretKeySpec(keyValue, "AES");

                     cipher = Cipher.getInstance("AES");


                 if (plainText != null) {
                    byte[] plainTextByte = plainText.getBytes();
                    cipher.init(Cipher.ENCRYPT_MODE, key);
                    byte[] encryptedByte = cipher.doFinal(plainTextByte);
                    encryptedText = Base64.encode(encryptedByte);
                }
                else{
                    //nop
                }

                 } catch (NoSuchAlgorithmException e) {
                   log.error(" NoSuchAlgorithmException  : " +e.getMessage());
                 } catch (NoSuchPaddingException e) {
                     log.error("NoSuchPaddingException  : " + e.getMessage());
                 } catch (IllegalBlockSizeException e) {
                     log.error("IllegalBlockSizeException  : " + e.getMessage());
                 } catch (BadPaddingException e) {
                     log.error("BadPaddingException  : " + e.getMessage());
                 } catch (InvalidKeyException e) {
                     log.error("InvalidKeyException  : " + e.getMessage());
                 }

                 return encryptedText;
	}

}
