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
package com.wso2telco.proxy.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AuthProxyEnum {

    public enum TRUSTEDSTATUS {
        FULLY_TRUSTED("fullytrusted"),
        TRUSTED("trusted"),
        UNTRUSTED("normal"),
        UNDEFINED("undefined");

        private String trustedStatus;

        private TRUSTEDSTATUS(String status) {
            this.trustedStatus = status;
        }

        public String getTrustedStatus() {
            return trustedStatus;

        }

        static Map<String, TRUSTEDSTATUS> statusMap = new HashMap<>();

        static {
            Iterator<TRUSTEDSTATUS> enumStatus = EnumSet.allOf(TRUSTEDSTATUS.class).iterator();
            while (enumStatus.hasNext()) {
                TRUSTEDSTATUS statusType = enumStatus.next();
                statusMap.put(statusType.getTrustedStatus(), statusType);
            }
        }

        public static TRUSTEDSTATUS getStatus(final String code) {
            return statusMap.get(code);

        }
    }

    public enum SCOPETYPE {
        ATT_VERIFICATION,
        ATT_SHARE,
        APICONSENT,
        MAIN,
        OTHER;
    }
}
