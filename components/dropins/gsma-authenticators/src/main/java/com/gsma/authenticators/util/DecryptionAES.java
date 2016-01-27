/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gsma.authenticators.util;

import com.google.gdata.util.common.util.Base64;
import com.gsma.authenticators.DataHolder;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
/**
 *
 * @author tharanga_07219
 */


public class DecryptionAES {
    
    private static Cipher cipher;
    private static byte[] keyValue;
    
    static{
          keyValue = DataHolder.getInstance().getMobileConnectConfig().getHEADERENRICH().getKey().getBytes();
    }
    
    public static String decrypt(String encryptedText)
			throws Exception {
		
               String decryptedText =null ;
               if (encryptedText != null){
                    SecretKey key = new SecretKeySpec(keyValue, "AES");

                    byte[] encryptedTextByte = Base64.decode(encryptedText);
                    cipher = Cipher.getInstance("AES");

                    cipher.init(Cipher.DECRYPT_MODE,key);
                    byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
                    decryptedText = new String(decryptedByte);
               }
               else{
                  //nop 
               }
		return decryptedText;
	}
    
     
}
    

