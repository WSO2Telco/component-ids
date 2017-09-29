/*
 * ******************************************************************************
 *  * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *  *
 *  * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

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


public abstract class AbstractAttributeShare implements AttrubteSharable {

    Log log = LogFactory.getLog(AbstractAttributeShare.class);

    public abstract void mandatoryFeildValidation();

    /**
     *
     * @param operatorName
     * @param clientId
     * @param loginhintMsisdn
     * @param msisdn
     * @return
     * @throws AuthenticationFailedException
     */
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

    /**
     *
     * @param loginhintMsisdn
     * @param headerMsisdn
     * @param trustedStatus
     * @throws AuthenticationFailedException
     */
    private void checkMSISDNAvailability(String loginhintMsisdn, String headerMsisdn, String trustedStatus) throws AuthenticationFailedException{

       if(loginhintMsisdn.isEmpty() && headerMsisdn.isEmpty()){
           throw new AuthenticationFailedException("Msisdn not available for " + trustedStatus +"");
       }

    }


    public abstract void scopeNClaimMatching();

    public abstract void shaAlgortithemValidation();

}
