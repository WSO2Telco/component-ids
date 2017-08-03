package com.wso2telco.gsma.authenticators.dao;

import com.wso2telco.gsma.authenticators.model.Consent;
import com.wso2telco.gsma.authenticators.model.SPConsent;
import com.wso2telco.gsma.authenticators.model.UserConsentDetails;

import javax.naming.NamingException;
import java.sql.SQLException;

/**
 * Created by aushani on 7/26/17.
 */
public interface SpconfigDAO {

     SPConsent getSpConsentDetails(SPConsent spConsent) throws SQLException, NamingException;

     UserConsentDetails getUserConsentDetails(UserConsentDetails userConsentDetails) throws SQLException, NamingException;

     public String getSPConfigValue(String operator, String clientID, String key)
             throws SQLException, NamingException;
}
