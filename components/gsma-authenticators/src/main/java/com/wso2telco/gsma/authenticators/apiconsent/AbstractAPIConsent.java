package com.wso2telco.gsma.authenticators.apiconsent;

import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.attributeshare.internal.ValidityType;
import com.wso2telco.gsma.authenticators.internal.AuthenticatorEnum;
import org.apache.commons.collections.map.HashedMap;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class AbstractAPIConsent {

    public static void setApproveNeededScope(AuthenticationContext context) throws AuthenticationFailedException {
        Map<String, String> approveNeededScopes = new HashedMap();
        List<String> approvedScopes = new ArrayList<>();
        String[] scopeList = context.getProperty(Constants.SCOPE).toString().split(" ");
        StringBuilder apiScopeList = new StringBuilder();
        try{
            if (scopeList != null) {
                if (scopeList != null && scopeList.length > 0) {
                    boolean enableapproveall = true;
                    for (String scope : scopeList) {
                        String consent[] = DBUtils.getConsentStatus(scope, context.getProperty(Constants.CLIENT_ID).toString(), context.getProperty(Constants.OPERATOR).toString());
                        if (consent != null && consent.length == 3 && !consent[0].isEmpty() && (consent[0].equalsIgnoreCase(ValidityType.TRANSACTIONAL.name()) || consent[0].equalsIgnoreCase(ValidityType.LONG_LIVE.name()))) {
                            apiScopeList.append(scope).append(",");
                            if(consent[2].equalsIgnoreCase(AuthenticatorEnum.ConsentType.IMPLICIT.name())){
                                continue;
                            }

                            String expireDate = DBUtils.getUserConsentScopeApproval(context.getProperty(Constants.MSISDN).toString(), scope, context.getProperty(Constants.CLIENT_ID).toString(), context.getProperty(Constants.OPERATOR).toString());
                            boolean approved = false;
                            Date today = new Date();
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                            if (expireDate != null && !today.after(dateFormat.parse(expireDate))) {
                                approved = true;
                            }
                            if (approved) {
                                approvedScopes.add(scope);
                            } else {
                                approveNeededScopes.put(scope, consent[1]);
                            }
                            if (consent[0].equalsIgnoreCase(ValidityType.TRANSACTIONAL.name())) {
                                enableapproveall = false;
                            }
                        }else if (consent != null && consent.length == 2 && !consent[0].isEmpty() && consent[0].equalsIgnoreCase(ValidityType.DENY.name())){
                            context.setProperty(Constants.DENIED_SCOPE, true);
                            throw new AuthenticationFailedException("Authenticator failed- Denied scopes are found");
                        }
                    }
                    apiScopeList.deleteCharAt(apiScopeList.length()-1);
                    context.setProperty(Constants.ALREADY_APPROVED, approveNeededScopes.isEmpty());
                    context.setProperty(Constants.APPROVE_NEEDED_SCOPES, approveNeededScopes);
                    context.setProperty(Constants.APPROVED_SCOPES, approvedScopes);
                    context.setProperty(Constants.APPROVE_ALL_ENABLE, enableapproveall);
                    context.setProperty(Constants.API_SCOPES, apiScopeList.toString());

                    String logoPath = DBUtils.getSPConfigValue(context.getProperty(Constants.OPERATOR).toString(), context.getProperty(Constants.CLIENT_ID).toString(), Constants.SP_LOGO);
                    if (logoPath != null && !logoPath.isEmpty()) {
                        context.setProperty(Constants.SP_LOGO, logoPath);
                    }
                } else {
                    throw new AuthenticationFailedException("Authenticator failed- Approval needed scopes not found");
                }
            } else {
                throw new AuthenticationFailedException("Authenticator failed- Approval needed scopes not found");
            }
        }catch (AuthenticatorException e){
            throw new AuthenticationFailedException("Authenticator failed- Approval needed scopes not found");
        } catch (ParseException e) {
            throw new AuthenticationFailedException("Error occurred while formatting the date : " + e.getMessage());
        }
    }
}
