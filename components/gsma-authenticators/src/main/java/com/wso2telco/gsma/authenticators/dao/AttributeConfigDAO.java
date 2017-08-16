package com.wso2telco.gsma.authenticators.dao;

import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.gsma.authenticators.model.SPConsent;
import com.wso2telco.gsma.authenticators.model.UserConsentDetails;
import com.wso2telco.gsma.authenticators.model.UserConsentHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by aushani on 7/26/17.
 */
public interface AttributeConfigDAO {

    List<SPConsent> getScopeExprieTime(String operator, String consumerKey, String scope) throws SQLException, NamingException;

    UserConsentDetails getUserConsentDetails(UserConsentDetails userConsentDetails) throws SQLException, NamingException;

    public String getSPConfigValue(String operator, String clientID, String key)
            throws SQLException, NamingException;

    public void saveUserConsentedAttributes(List<UserConsentHistory> userConsentHistory) throws SQLException, NamingException;

    public List<ScopeParam> getScopeParams(String scopes) throws SQLException, NamingException;


}
