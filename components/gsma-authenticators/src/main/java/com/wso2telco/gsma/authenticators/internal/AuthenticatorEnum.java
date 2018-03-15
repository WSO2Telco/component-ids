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
package com.wso2telco.gsma.authenticators.internal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AuthenticatorEnum {

    public enum TrustedStatus {

        FULLY_TRUSTED("fullytrusted"),
        TRUSTED("trusted"),
        UNTRUSTED("normal"),
        UNDEFINED("undefined");

        private final String trustedStatus;

        TrustedStatus(String trustedStatus) {
            this.trustedStatus = trustedStatus;
        }

        public String getTrustedStatus() {
            return this.trustedStatus;
        }
    }

    public enum AttributeShareScopeTypes {

        VERIFICATION_SCOPE("ATT_VERIFICATION"),
        PROVISIONING_SCOPE("ATT_SHARE");

        private final String attributeShareScopeType;

        AttributeShareScopeTypes(String attributeShareScopeTypes) {
            this.attributeShareScopeType = attributeShareScopeTypes;
        }

        public String getAttributeShareScopeType() {
            return this.attributeShareScopeType;
        }
    }

    public enum ConsentType {
        IMPLICIT("implicit"),
        EXPLICIT("explicit"),
        NOCONSENT("no consent"),
        UNDEFINED("undefined");

        private String consent;

        private ConsentType(String text) {
            this.consent = text;
        }

        public String getConsent() {
            return consent;

        }

        static Map<String, ConsentType> valueMap = new HashMap<>();

        static {
            Iterator<ConsentType> enumTy = EnumSet.allOf(ConsentType.class).iterator();
            while (enumTy.hasNext()) {
                ConsentType consentType = enumTy.next();
                valueMap.put(consentType.getConsent(), consentType);
            }
        }

        public static ConsentType get(final String code) {
            return valueMap.get(code);
        }
    }
}
