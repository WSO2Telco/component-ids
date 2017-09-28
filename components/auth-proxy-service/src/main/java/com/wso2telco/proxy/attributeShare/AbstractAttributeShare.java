package com.wso2telco.proxy.attributeShare;

import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.proxy.dao.AttShareDAO;
import com.wso2telco.proxy.dao.attShareDAOImpl.AttShareDAOImpl;
import com.wso2telco.proxy.util.AuthProxyEnum;
import com.wso2telco.proxy.util.AuthProxyConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by aushani on 8/31/17.
 */
public abstract class AbstractAttributeShare implements AttrubteSharable {

    Log log = LogFactory.getLog(AbstractAttributeShare.class);

    public abstract void mandatoryFeildValidation();

    public String getTrsutedStatus(String operatorName, String clientId, String loginhintMsisdn, String msisdn) throws AuthenticationFailedException{

        String trustedStatus = null;

        try {
            AttShareDAO attShareDAO = new AttShareDAOImpl();
            trustedStatus = attShareDAO.getSPTypeConfigValue(operatorName, clientId, AuthProxyConstants.TRUSTED_STATUS);

            AuthProxyEnum.TRUSTEDSTATUS sp = AuthProxyEnum.TRUSTEDSTATUS.getStatus(trustedStatus);

            switch (sp) {
                case FULLY_TRUSTED:
                    trustedStatus = AuthProxyEnum.TRUSTEDSTATUS.FULLY_TRUSTED.name();
                    checkMSISDNAvailability(loginhintMsisdn,msisdn,trustedStatus);
                    break;

                case TRUSTED:
                    trustedStatus = AuthProxyEnum.TRUSTEDSTATUS.TRUSTED.name();
                    checkMSISDNAvailability(loginhintMsisdn,msisdn,trustedStatus);
                    break;

                case UNTRUSTED:
                    trustedStatus = AuthProxyEnum.TRUSTEDSTATUS.UNTRUSTED.name();
                    break;
                default:
                    trustedStatus = AuthProxyEnum.TRUSTEDSTATUS.UNDEFINED.name();
            }

        } catch (DBUtilException|SQLException e){
            log.debug("Error occurred retreiving data from database");
            throw new AuthenticationFailedException(e.getMessage(),e);
        }

        return trustedStatus;
   }

    private void checkMSISDNAvailability(String loginhintMsisdn, String headerMsisdn, String trustedStatus) throws AuthenticationFailedException{

       if(loginhintMsisdn.isEmpty() && headerMsisdn.isEmpty()){
           throw new AuthenticationFailedException("Msisdn not available for " + trustedStatus +"");
       }

    }


    public abstract void scopeNClaimMatching();

    public abstract void shaAlgortithemValidation();

}
