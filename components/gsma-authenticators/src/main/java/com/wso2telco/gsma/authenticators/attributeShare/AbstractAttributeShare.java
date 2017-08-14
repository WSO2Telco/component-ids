package com.wso2telco.gsma.authenticators.attributeShare;

import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtil;
import com.wso2telco.gsma.authenticators.attributeShare.internal.ConsentType;
import com.wso2telco.gsma.authenticators.attributeShare.internal.UserConsentStatus;
import com.wso2telco.gsma.authenticators.attributeShare.internal.ValidityType;
import com.wso2telco.gsma.authenticators.dao.AttributeConfigDAO;
import com.wso2telco.gsma.authenticators.dao.impl.AttributeConfigDAOimpl;
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

        AttributeConfigDAO attributeConfigDAO = new AttributeConfigDAOimpl();
        List<ScopeParam>  scopeParamList = attributeConfigDAO.getScopeParams(context.getProperty(Constants.TELCO_SCOPE).toString());

        for (ScopeParam scopeParam: scopeParamList){
            String consentType= scopeParam.getConsentType();
            String validityType = scopeParam.getConsent_validity_type();
            String scope = scopeParam.getScope();

            if(consentType.equalsIgnoreCase(ConsentType.EXPLICIT.name()) && isValidited(context, validityType,scope)) {
                explicitScopes.add(scope);
            } else if (consentType.equalsIgnoreCase(ConsentType.IMPLICIT.name()) && isValidited(context,validityType,scope))
                implicitScopes.add(scope);

        }

        scopesList.put("explicitScopes",explicitScopes);
        scopesList.put("implicitScopes",implicitScopes);

        return scopesList;
        }


    private UserConsentDetails getUserConsentDetails(AuthenticationContext context) throws Exception {
        AttributeConfigDAO attributeConfigDAO = new AttributeConfigDAOimpl();
        UserConsentDetails userConsentDetails = new UserConsentDetails();
        userConsentDetails.setConsumerKey(context.getProperty(Constants.CLIENT_ID).toString());
        userConsentDetails.setOperatorID((DBUtil.getOperatorDetails(context.getProperty(Constants.OPERATOR).toString())).getOperatorId());
        userConsentDetails.setScope(context.getProperty(Constants.TELCO_SCOPE).toString());
        userConsentDetails.setMsisdn(context.getProperty(Constants.MSISDN).toString());
        return attributeConfigDAO.getUserConsentDetails(userConsentDetails);

    }

    private boolean isValidited(AuthenticationContext context, String validityTyp,String scope) throws Exception {

        ValidityType validityType = ValidityType.get(validityTyp);
        switch (validityType) {
            case TRANSACTIONAL:
                return true;

            case LONG_LIVE:
                return isLongLiveConsent(context,scope);

            default:
                break;

        }

        return false;
    }

    private boolean isLongLiveConsent(AuthenticationContext context,String scope) throws Exception {

        boolean isConsent=false;


        try {
            List<String> longlivedScopes = new ArrayList();
            UserConsentDetails userConsentDetails = getUserConsentDetails(context);
            if(userConsentDetails == null){
                isConsent = true;
                longlivedScopes.add(scope);

            } else {

                if (userConsentDetails.getRevokeStatus().equalsIgnoreCase(UserConsentStatus.ACTIVE.name())) {
                    Date today = new Date();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                    if (today.after(dateFormat.parse(userConsentDetails.getConsentExpireDatetime()))) {
                        isConsent= true;
                        longlivedScopes.add(scope);
                    }

                } else if (userConsentDetails.getRevokeStatus().equalsIgnoreCase(UserConsentStatus.REVOKED.name())) {
                    isConsent= true;
                    longlivedScopes.add(scope);
                }

            }

            if(!longlivedScopes.isEmpty()){
                context.setProperty("longlivedScopes",longlivedScopes.toArray());
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
