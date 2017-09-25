package com.wso2telco.proxy.attributeShare;

import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.proxy.dao.AttShareDAO;
import com.wso2telco.proxy.dao.attShareDAOImpl.AttShareDAOImpl;
import com.wso2telco.proxy.util.AuthProxyConstants;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aushani on 8/31/17.
 */
public class AttributeShare {

    private static Log log = LogFactory.getLog(AttributeShare.class);

    private static AttShareDAO attShareDAO;
    private static Map<String, String> scopeTypes;

    private AttributeShare(){}
    static {

        try {
            attShareDAO = new AttShareDAOImpl();
            scopeTypes  =attShareDAO.getScopeParams();
        } catch (SQLException |DBUtilException e) {
             log.error("error occurred while scope retreiving the data from database");
        }
    }


    public static Map<String,String> validateAttShareScopes(String scopeName, String operatorName, String clientId, String loginhintMsisdn, String msisdn) throws AuthenticationFailedException {

        List<String> scopeList = new ArrayList(Arrays.asList(scopeName.split(" ")));
        String trustedStatus = null;
        boolean isAttributeShare = false;
        Map<String,String> attShareScoprDetails = new HashMap<>();
        try {
            if(!scopeTypes.isEmpty()){
                for (String scope : scopeList) {
                    String attrSharetype = scopeTypes.get(scope);
                    if (attrSharetype != null) {
                        isAttributeShare = true;
                        trustedStatus = ScopeFactory.getAttribAttrubteSharable(attrSharetype).attShareDetails(operatorName, clientId,loginhintMsisdn, msisdn);
                    }
                }
            }

        } catch (AuthenticationFailedException e){
            log.debug(e.getMessage());
            throw new AuthenticationFailedException(e.getMessage(),e);

        }
       attShareScoprDetails.put(AuthProxyConstants.ATTR_SHARE_SCOPE, Boolean.toString(isAttributeShare));
       attShareScoprDetails.put(AuthProxyConstants.TRUSTED_STATUS,trustedStatus);

       return  attShareScoprDetails;

    }
}
