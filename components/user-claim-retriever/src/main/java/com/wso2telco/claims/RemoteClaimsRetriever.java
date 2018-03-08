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

    private static Map<String, Object[]> remoteClaimsMap = new HashMap();

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
                                                  Map<String, Object> totalClaims) throws NoSuchAlgorithmException,
            IllegalAccessException, ClassNotFoundException, InstantiationException {
        Map<String, Object> requestedClaims = new HashMap();
        CustomerInfo customerInfo = new CustomerInfo();

        populateScopeConfigs(scopeConfigs);
        String operatorName = (String) totalClaims.get(operator);
        customerInfo.setMsisdn((String) totalClaims.get(phoneNumberClaim));
        customerInfo.setScopes(scopes);

        Map<String, Object> totalClaimsValues = null;

        totalClaimsValues = getTotalClaims(operatorName, customerInfo);

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

    public Map<String, Object> getTotalClaims(String operatorName, CustomerInfo customerInfo) throws
            IllegalAccessException, InstantiationException, ClassNotFoundException {

        Map<String, Object> totalClaims;

        try {
            totalClaims = getRemoteTotalClaims(operatorName, customerInfo);
        } catch (ClassNotFoundException e) {
            log.error("Class Not Found Exception occurred when calling operator'd endpoint - " + operatorName + ":" +
                    e);
            throw new ClassNotFoundException(e.getMessage(), e);
        } catch (InstantiationException e) {
            log.error("Instantiation Exception occurred when calling operator'd endpoint - " + operatorName + ":" + e);
            throw new InstantiationException(e.getMessage());
        } catch (IllegalAccessException e) {
            log.error("Illegal Access Exception occurred when calling operator'd endpoint - " + operatorName + ":" + e);
            throw new IllegalAccessException(e.getMessage());
        }
        return totalClaims;
    }

    private Map<String, Object> getRemoteTotalClaims(String operatorName, CustomerInfo customerInfo) throws
            ClassNotFoundException,
            IllegalAccessException, InstantiationException {

        RemoteClaims remoteClaims;
        Object[] claimValueObject = new Object[3];

        if (remoteClaimsMap.containsKey(operatorName)) {
            claimValueObject = remoteClaimsMap.get(operatorName);
        } else {
            for (MobileConnectConfig.OperatorData operator : operators) {
                if (operator.getOperatorName().equalsIgnoreCase(operatorName)) {
                    String className = operator.getClassName();
                    claimValueObject[0] = className;
                    claimValueObject[1] = operator.getUserInfoEndPointURL();
                    Class c = Class.forName(className);
                    claimValueObject[2] = (RemoteClaims) c.newInstance();
                    remoteClaimsMap.put(operatorName, claimValueObject);
                    break;
                }
            }
        }

        remoteClaims = (RemoteClaims) claimValueObject[2];
        return remoteClaims.getTotalClaims(claimValueObject[1].toString(), customerInfo);

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
