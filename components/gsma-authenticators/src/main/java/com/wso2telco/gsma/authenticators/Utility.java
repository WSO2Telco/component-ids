/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.wso2telco.gsma.authenticators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Utility {

    private static Log log = LogFactory.getLog(Utility.class);

    public static String getMultiScopeQueryParam(AuthenticationContext context) {
        List<String> availableMultiScopes = Arrays.asList("profile", "email",
                "address", "phone", "mc_identity_phonenumber_hashed");

        String originalScope = ((Map) context.getProperty("authMap")).get(
                "Scope").toString();

        String[] originalScopesRequested = originalScope.split(" ");

        String multiScopes = "";

        if (originalScopesRequested.length > 1) {

            StringBuilder filteredMultiScopesQuery = new StringBuilder();
            filteredMultiScopesQuery.append("&multiScopes=");

            for (String requestedScope : originalScopesRequested) {
                if (availableMultiScopes.contains(requestedScope)) {
                    filteredMultiScopesQuery.append(requestedScope);
                    filteredMultiScopesQuery.append(" ");
                }
            }

            multiScopes = filteredMultiScopesQuery.toString().trim().replace(" ", "+");
        }

        return multiScopes;
    }

    /**
     * Returns the OTP for defined length with minimum length should be 4
     * @param length for the otp
     */
    public static String genarateOTP(int length) {
        if (length < 4) {
            length = 4;
        }
        String numbers = "0123456789";
        // Using random method
        Random rndm_method = new Random();
        char[] password = new char[length];
        for (int i = 0; i < length; i++) {
            // Use of charAt() method : to get character value
            // Use of nextInt() as it is scanning the value as int
            password[i] = numbers.charAt(rndm_method.nextInt(numbers.length()));
        }
        return String.valueOf(password);
    }

    /**
     * Returns the sha256 digest for a given string
     * @param input for the retrieving the sha256 hash
     */
    public static String generateSHA256Hash(String input) throws AuthenticationFailedException {
        String returnValue=null;
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(input.getBytes());
            byte byteData[] = md.digest();
            //convert the byte to hex format
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            returnValue=sb.toString();
        }catch (Exception e){
            throw new AuthenticationFailedException("Failure while hashing the input value",e);
        }
        return returnValue;
    }


}