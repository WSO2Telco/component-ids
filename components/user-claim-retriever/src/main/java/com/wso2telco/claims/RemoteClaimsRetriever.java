package com.wso2telco.claims;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.model.ScopeDetailsConfig;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
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

    private static Map<String, ScopeDetailsConfig.Scope> scopeConfigsMap = new HashMap<String, ScopeDetailsConfig.Scope>();

    private final static String PHONE_NUMBER = "phone_number";

    private final static String OPERATOR = "operator1";

    private static List<MobileConnectConfig.OperatorData> operators = com.wso2telco.core.config.ConfigLoader.getInstance().getMobileConnectConfig().getOperatorsList().getOperatorData();


    private void populateScopeConfigs(List<ScopeDetailsConfig.Scope> scopeConfigs) {
        if (!scopeConfigsMap.isEmpty()) {
            for (ScopeDetailsConfig.Scope scope : scopeConfigs) {
                scopeConfigsMap.put(scope.getName(), scope);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Could not load user-info claims.");
            }
        }
    }

    private String getclaimValue(Iterator<String> i, boolean isHashed, Map<String, Object> totalClaims) throws NoSuchAlgorithmException {
        String claimValue = "";
        while (i.hasNext()) {
            Object claimStr = totalClaims.get(i.next());
            if (claimStr != null) {
                claimValue = (isHashed) ? getHashedClaimValue(totalClaims.get(i.next()).toString()) : totalClaims.get(i.next()).toString();

            }
        }
        return claimValue;
    }

    @Override
    public Map<String, Object> getRequestedClaims(String[] scopes, List<ScopeDetailsConfig.Scope> scopeConfigs, Map<String, Object> totalClaims) throws NoSuchAlgorithmException {
        Map<String, Object> requestedClaims = new HashMap<String, Object>();

        populateScopeConfigs(scopeConfigs);
        String operatorName = (String) totalClaims.get(OPERATOR);
        String phoneNumber = (String) totalClaims.get(PHONE_NUMBER);
        totalClaims = getTotalClaims(operatorName, phoneNumber);

        for (String scope : scopes) {
            if (scopeConfigsMap.containsKey(scope) && totalClaims != null) {
                Iterator<String> i = scopeConfigsMap.get(scope).getClaimSet().iterator();
                boolean isHashed = scopeConfigsMap.get(scope).isHashed();
                requestedClaims.put(i.next(), totalClaims.get(getclaimValue(i, isHashed, totalClaims)));

            }
        }

        return requestedClaims;
    }

    public static Map<String, Object> getTotalClaims(String operatorName, String msisdn) {

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
            totalClaims = remoteClaims.getTotalClaims(operatorEndPoint, msisdn);
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

        byte byteData[] = md.digest();
        //convert the byte to hex format
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
