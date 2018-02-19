/*******************************************************************************
 * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.claims;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.model.ScopeDetailsConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RemoteClaimsRetriever implements ClaimsRetriever {

    /**
     * The log.
     */

    private static Log log = LogFactory.getLog(RemoteClaimsRetriever.class);

    private static Map<String, ScopeDetailsConfig.Scope> scopeConfigsMap = new HashMap();

    private static List<MobileConnectConfig.OperatorData> operators = com.wso2telco.core.config.ConfigLoader
            .getInstance().getMobileConnectConfig().getOperatorsList().getOperatorData();

    private static String phoneNumberClaim = "phone_number";

    private static String operator = "operator";


    private void populateScopeConfigs(List<ScopeDetailsConfig.Scope> scopeConfigs) {
        if (scopeConfigsMap.size() == 0) {
            for (ScopeDetailsConfig.Scope scope : scopeConfigs) {
                scopeConfigsMap.put(scope.getName(), scope);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Could not load user-info claims.");
            }
        }
    }

    private String getclaimValue(Iterator<String> i, boolean isHashed, Map<String, Object> totalClaims) throws
            NoSuchAlgorithmException {
        String claimValue = "";
        while (i.hasNext()) {
            Object claimStr = totalClaims.get(i.next());
            if (claimStr != null) {
                claimValue = (isHashed) ? getHashedClaimValue(claimStr.toString()) : claimStr.toString();

            }
        }
        return claimValue;
    }

    @Override
    public Map<String, Object> getRequestedClaims(String[] scopes, List<ScopeDetailsConfig.Scope> scopeConfigs,
                                                  Map<String, Object> totalClaims) throws NoSuchAlgorithmException {
        Map<String, Object> requestedClaims = new HashMap();
        CustomerInfo customerInfo = new CustomerInfo();

        populateScopeConfigs(scopeConfigs);
        String operatorName = (String) totalClaims.get(operator);
        customerInfo.setMsisdn((String) totalClaims.get(phoneNumberClaim));
        customerInfo.setScope(scopes);

        Map<String, Object> totalClaimsValues = getTotalClaims(operatorName, customerInfo);

        for (String scope : scopes) {
            if (scopeConfigsMap.containsKey(scope) && totalClaimsValues != null) {
                List<String> claimSet = scopeConfigsMap.get(scope).getClaimSet();
                boolean isHashed = scopeConfigsMap.get(scope).isHashed();
                for (String claim : claimSet) {
                    requestedClaims.put(claim, (isHashed ? getHashedClaimValue(totalClaimsValues.get(claim).toString
                            ()) : totalClaimsValues.get(claim)));
                }
            }
        }

        return requestedClaims;
    }

    public static Map<String, Object> getTotalClaims(String operatorName, CustomerInfo customerInfo) {

        Map<String, Object> totalClaims = null;
        String className = null;
        String operatorEndPoint = null;
        for (MobileConnectConfig.OperatorData operator : operators) {
            if (operator.getOperatorName().equalsIgnoreCase(operatorName)) {
                className = operator.getClassName();
                operatorEndPoint = operator.getUserInfoEndPointURL();
                break;
            }
        }

        try {
            Class c = Class.forName(className);
            RemoteClaims remoteClaims = (RemoteClaims) c.newInstance();
            totalClaims = remoteClaims.getTotalClaims(operatorEndPoint, customerInfo);
        } catch (ClassNotFoundException e) {
            log.error(className + " Class Not Found Exception");
        } catch (InstantiationException e) {
            log.error(className + " Instantiation Exception");
        } catch (IllegalAccessException e) {
            log.error(className + " Illegal Access Exception");
        }
        return totalClaims;
    }


    private String getHashedClaimValue(String claimValue) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(claimValue.getBytes());

        byte[] byteData = md.digest();
        //convert the byte to hex format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
