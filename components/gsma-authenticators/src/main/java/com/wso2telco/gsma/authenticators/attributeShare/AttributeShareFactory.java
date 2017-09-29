/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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

package com.wso2telco.gsma.authenticators.attributeShare;

import com.wso2telco.gsma.authenticators.internal.AuthenticatorEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*This factory class created because the user consent mechanism can be vary with the SP type.
* TSP1 consent mechanisms not included yet.
* Currently created one object for both TSP2 and Normal because it seems same same functionality.
* In future these funcionality can be changed
*/
public class AttributeShareFactory {

    static TrustedSP2 trustedSP2;
    static TrustedSP trustedSP;
    static NormalSP normalSP;
    private static Log log = LogFactory.getLog(AttributeShareFactory.class);

    private AttributeShareFactory(){}

    public static AttributeSharable getAttributeSharable(String trustedStatus) {

        AttributeSharable attributeSharable = null;

            if (trustedStatus != null && (trustedStatus.equalsIgnoreCase(AuthenticatorEnum.TrustedStatus.TRUSTED.name()))) {
                if (trustedSP2 == null) {
                    trustedSP2 = new TrustedSP2();
                }
                attributeSharable = trustedSP2;
            }
            if (trustedStatus != null && (trustedStatus.equalsIgnoreCase(AuthenticatorEnum.TrustedStatus.FULLY_TRUSTED.name()))) {
                if (trustedSP == null) {
                    trustedSP = new TrustedSP();
                }
                attributeSharable = trustedSP;
            }
            if (trustedStatus != null && (trustedStatus.equalsIgnoreCase(AuthenticatorEnum.TrustedStatus.UNTRUSTED.name()))) {
                if (normalSP == null) {
                    normalSP = new NormalSP();
                }
                attributeSharable = normalSP;

            }
        log.debug("SP type" + attributeSharable);
        return attributeSharable;
    }
}
