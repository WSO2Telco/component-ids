package com.wso2telco.gsma.authenticators.attributeShare;

import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtil;
import com.wso2telco.gsma.authenticators.attributeShare.internal.ConsentType;
import com.wso2telco.gsma.authenticators.attributeShare.internal.UserConsentStatus;
import com.wso2telco.gsma.authenticators.attributeShare.internal.ValidityType;
import com.wso2telco.gsma.authenticators.dao.SpconfigDAO;
import com.wso2telco.gsma.authenticators.dao.impl.SpconfigDAOimpl;
import com.wso2telco.gsma.authenticators.model.SPConsent;
import com.wso2telco.gsma.authenticators.model.UserConsentDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
public abstract class AbstractAttributeShare implements AttributeSharable {

    private static Log log = LogFactory.getLog(AbstractAttributeShare.class);

    public Map<String,List<String>> getAttributeMap(AuthenticationContext context) throws Exception {

        List<String> explicitScopes = new ArrayList();
        List<String> implicitScopes = new ArrayList();
        Map<String,List<String>> scopesList= new HashMap();

        String[] scopes = context.getProperty(Constants.SCOPE).toString().split("\\s+");

        for(String scope:scopes){

            SPConsent spConsent = getConsentType(context);
            String consentType= spConsent.getConsentType();
            String validityType = spConsent.getValidityType();

            if(consentType.equalsIgnoreCase(ConsentType.EXPLICIT.name()) && isValidited(context, validityType)) {
                explicitScopes.add(scope);
            } else if (consentType.equalsIgnoreCase(ConsentType.IMPLICIT.name()) && isValidited(context,validityType))
                implicitScopes.add(scope);

        }

        scopesList.put("explicitScopes",explicitScopes);
        scopesList.put("implicitScopes",implicitScopes);

        return scopesList;
    }


    private SPConsent getConsentType(AuthenticationContext context) throws Exception {


        SpconfigDAO spconfigDAO = new SpconfigDAOimpl();

        SPConsent spConsent = new SPConsent();
        spConsent.setOperatorID((DBUtil.getOperatorDetails(context.getProperty(Constants.OPERATOR).toString())).getOperatorId());
        spConsent.setConsumerKey(context.getProperty(Constants.CLIENT_ID).toString());
        spConsent.setScope(context.getProperty(Constants.SCOPE).toString());
        spConsent = spconfigDAO.getSpConsentDetails(spConsent);

        return spConsent;
    }

    private UserConsentDetails getUserConsentDetails(AuthenticationContext context) throws Exception {
        SpconfigDAO spconfigDAO = new SpconfigDAOimpl();
        UserConsentDetails userConsentDetails = new UserConsentDetails();
        userConsentDetails.setConsumerKey(context.getProperty(Constants.CLIENT_ID).toString());
        userConsentDetails.setOperatorID((DBUtil.getOperatorDetails(context.getProperty(Constants.OPERATOR).toString())).getOperatorId());
        userConsentDetails.setScope(context.getProperty(Constants.SCOPE).toString());
        userConsentDetails.setMsisdn(context.getProperty(Constants.MSISDN).toString());
        return spconfigDAO.getUserConsentDetails(userConsentDetails);

    }

    private boolean isValidited(AuthenticationContext context, String validityTyp) throws Exception {

        ValidityType validityType = ValidityType.get(validityTyp);
        switch (validityType) {
            case TRANSACTIONAL:
                return true;

            case LONG_LIVE:
                return isLongLiveConsent(context);

            default:
                break;

        }

        return false;
    }

    private boolean isLongLiveConsent(AuthenticationContext context) throws Exception {

        boolean isConsent=false;

        try {

            UserConsentDetails userConsentDetails = getUserConsentDetails(context);
            if (userConsentDetails.getRevokeStatus().equalsIgnoreCase(UserConsentStatus.ACTIVE.name())) {
                Date today = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                    if (today.after(dateFormat.parse(userConsentDetails.getConsentExpireDatetime()))) {
                        isConsent= true;
                    }

            } else if (userConsentDetails.getRevokeStatus().equalsIgnoreCase(UserConsentStatus.REVOKED.name())) {
                isConsent= true;
            }
        } catch (SQLException e) {
            log.debug("error occurred while accessing the database table" + e);

        } catch (NamingException ex) {


        } catch (ParseException e) {
            log.debug("error occurred while formatting the date");
        } catch (Exception e) {

        }
        return isConsent;

    }


}
