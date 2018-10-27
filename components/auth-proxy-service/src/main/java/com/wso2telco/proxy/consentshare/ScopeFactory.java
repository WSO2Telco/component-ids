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

import com.wso2telco.proxy.util.AuthProxyEnum;

public class ScopeFactory {

    private static ProvisionScope provisionScope;
    private static VerificationScope verificationScope;
    private static APIConsentScope apiConsentScope;
    private static MainScope mainScope;
    private static OtherScope otherScope;

    private ScopeFactory() {
    }

    /***
     * Identifying the type of the request(Provision request/Verification request)
     * @param scopeType
     * @return ConsentSharable object
     */
    public static ConsentSharable getConsentSharable(String scopeType) {

        AbstractConsentShare consentShareScope = null;

        AuthProxyEnum.SCOPETYPE scopetype = AuthProxyEnum.SCOPETYPE.valueOf(scopeType);

        if (scopetype != null) {
            switch (scopetype) {
                case ATT_VERIFICATION:
                    if (verificationScope == null) {
                        verificationScope = new VerificationScope();
                    }
                    consentShareScope = verificationScope;
                    break;
                case ATT_SHARE:
                    if (provisionScope == null) {
                        provisionScope = new ProvisionScope();
                    }
                    consentShareScope = provisionScope;
                    break;
                case APICONSENT:
                    if (apiConsentScope == null) {
                        apiConsentScope = new APIConsentScope();
                    }
                    consentShareScope = apiConsentScope;
                    break;
                case MAIN:
                    if (mainScope == null){
                        mainScope = new MainScope();
                    }
                    consentShareScope = mainScope;
                    break;
                default:
                    break;
            }

        }

        return consentShareScope;
    }
}
