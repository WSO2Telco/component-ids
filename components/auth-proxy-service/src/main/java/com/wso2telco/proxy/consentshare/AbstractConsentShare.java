/*******************************************************************************
 * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.proxy.consentshare;

import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.proxy.dao.ConsentShareDao;
import com.wso2telco.proxy.dao.attsharedaoimpl.ConsentShareDaoImpl;
import com.wso2telco.proxy.util.AuthProxyEnum;
import com.wso2telco.proxy.util.AuthProxyConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

public abstract class AbstractConsentShare implements ConsentSharable {

    private static Log log = LogFactory.getLog(AbstractConsentShare.class);

    /**
     * Get trusted status of given clientId
     *
     * @param operatorName
     * @param clientId
     * @param loginHintMsisdn
     * @param msisdn
     * @return trusted status of SP
     * @throws AuthenticationFailedException
     */
    public String getTrustedStatus(String operatorName, String clientId, String loginHintMsisdn, String msisdn)
            throws AuthenticationFailedException {

        String trustedStatus = null;

        try {
            ConsentShareDao consentShareDAO = new ConsentShareDaoImpl();
            trustedStatus = consentShareDAO.getSpTypeConfigValue(operatorName, clientId, AuthProxyConstants.TRUSTED_STATUS);

            AuthProxyEnum.TRUSTEDSTATUS sp = AuthProxyEnum.TRUSTEDSTATUS.getStatus(trustedStatus);

            switch (sp) {
                case FULLY_TRUSTED:
                    trustedStatus = AuthProxyEnum.TRUSTEDSTATUS.FULLY_TRUSTED.name();
                    checkMsisdnAvailability(loginHintMsisdn, msisdn, trustedStatus);
                    break;

                case TRUSTED:
                    trustedStatus = AuthProxyEnum.TRUSTEDSTATUS.TRUSTED.name();
                    checkMsisdnAvailability(loginHintMsisdn, msisdn, trustedStatus);
                    break;

                case UNTRUSTED:
                    trustedStatus = AuthProxyEnum.TRUSTEDSTATUS.UNTRUSTED.name();
                    break;
                default:
                    trustedStatus = AuthProxyEnum.TRUSTEDSTATUS.UNDEFINED.name();
            }

            log.debug("Trusted Status of " + clientId + ":" + trustedStatus);

        } catch (DBUtilException e) {
            log.error("Error occurred in retrieving data from database :" + e.getMessage());
            throw new AuthenticationFailedException(e.getMessage(), e);
        }

        return trustedStatus;
    }

    /**
     * Check loginHint/headerEnrichment availability for Trusted SPs
     *
     * @param loginHintMsisdn
     * @param headerMsisdn
     * @param trustedStatus
     * @throws AuthenticationFailedException
     */
    private void checkMsisdnAvailability(String loginHintMsisdn, String headerMsisdn, String trustedStatus) throws
            AuthenticationFailedException {

        if (loginHintMsisdn.isEmpty() && headerMsisdn.isEmpty()) {
            log.error("MSISDN is not available for " + trustedStatus);
            throw new AuthenticationFailedException("Msisdn not available for " + trustedStatus);
        }

    }

    /**
     * Validate availability of mandatory parameters in verification requests
     */
    public abstract void mandatoryFieldValidation();

    public abstract void scopeAndClaimMatching();

    public abstract void shaAlgorithmValidation();

}
