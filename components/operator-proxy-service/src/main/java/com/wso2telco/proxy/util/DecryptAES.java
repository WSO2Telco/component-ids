/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wso2telco.proxy.util;

//import com.google.gdata.util.common.util.Base64;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
/**
 *
 * @author WSO2.Telco
 */


public class DecryptAES {
    
    private Cipher cipher;
    private static byte[] keyValue;
    
    static final String key = "cY4L3dBf@mifenew";
    
    static{
          keyValue = key.getBytes();
    }
    
    public String decrypt(String encryptedText)
			throws Exception {
		
               String decryptedText =null ;
               if (encryptedText != null){
                    SecretKey key = new SecretKeySpec(keyValue, "AES");
                    
                    
                    byte[] encryptedTextByte = Base64.decodeBase64(encryptedText);
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
    


