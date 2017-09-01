package com.wso2telco.claims;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.model.ScopeDetailsConfig;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RemoteClaimsRetriever implements ClaimsRetriever{

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(RemoteClaimsRetriever.class);

    private final String hashPhoneScope = "mc_identity_phonenumber_hashed";

    private final String phone_number_claim = "phone_number";

    private final String operator="operator1";

    private static List<MobileConnectConfig.OperatorData> operators= com.wso2telco.core.config.ConfigLoader.getInstance().getMobileConnectConfig().getOperatorsList().getOperatorData();



    @Override
    public Map<String, Object> getRequestedClaims(String[] scopes, List<ScopeDetailsConfig.Scope> scopeConfigs, Map<String, Object> totalClaims) throws NoSuchAlgorithmException
        {
            Map<String, Object> requestedClaims = new HashMap<String, Object>();
            if (scopeConfigs != null) {
                if (ArrayUtils.contains(scopes, hashPhoneScope)) {
                    if (totalClaims.get(phone_number_claim) == null) {
                        requestedClaims.put(phone_number_claim, "");
                    } else {
                        String hashed_msisdn = getHashedClaimValue((String) totalClaims.get(phone_number_claim));
                        requestedClaims.put(phone_number_claim, hashed_msisdn);
                    }
                } else {
                    String  operatorName =(String)totalClaims.get(operator);
                    String  phoneNumber =(String)totalClaims.get(phone_number_claim);
                    totalClaims=getTotalClaims(operatorName,phoneNumber);
                    for (ScopeDetailsConfig.Scope scope : scopeConfigs) {
                        if (ArrayUtils.contains(scopes, scope.getName())) {
                            for (String claims : scope.getClaimSet()) {
                                if (totalClaims.get(claims) == null) {
                                    requestedClaims.put(claims, "");
                                } else {
                                    requestedClaims.put(claims, totalClaims.get(claims));
                                }
                            }
                        }
                    }
                }

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Could not load user-info claims.");
                }
            }
            return requestedClaims;
        }

        public static Map<String, Object> getTotalClaims(String operatorName,String msisdn){

            Map<String, Object> totalClaims=null;
            String className=null;
            String operatorEndPoint=null;
            for (MobileConnectConfig.OperatorData operator : operators) {
                if(operator.getOperatorName().equalsIgnoreCase(operatorName)){
                    className=operator.getClassName();
                    operatorEndPoint=operator.getUserInfoEndPointURL();
                    break;
                }
            }

            try {
                Class c   = Class.forName(className);
                RemoteClaims remoteClaims = (RemoteClaims)c.newInstance();
                totalClaims=remoteClaims.getTotalClaims(operatorEndPoint,msisdn);
            } catch (ClassNotFoundException e) {
                log.error(className+" Class Not Found Exception");
            } catch (InstantiationException e) {
                log.error(className +" Instantiation Exception");
            } catch (IllegalAccessException e) {
                log.error(className +" Illegal Access Exception");
            }
            return totalClaims;
         }


    private String getHashedClaimValue(String claimValue) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(claimValue.getBytes());

        byte byteData[] = md.digest();
        //convert the byte to hex format
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
