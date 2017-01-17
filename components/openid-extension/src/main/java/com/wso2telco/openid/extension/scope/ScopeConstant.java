/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com)
 * <p>
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.openid.extension.scope;

public final class ScopeConstant {

    public static final String OAUTH20_PARAM_SCOPE = "scope";
    public static final String OAUTH20_VALUE_SCOPE = "openid";
    public static final String CUSTOM_PARAM_TELCO_SCOPE = "telco_scope";

    public static class CustomScopeType {
        public static final String MNV = "mnv";
        public static final String MC_MNV_VALIDATE = "mc_mnv_validate";
        public static final String MC_MNV_VALIDATE_PLUS = "mc_mnv_validate_plus";
        public static final String MC_INDIA_TC = "mc_india_tc";
        public static final String CPI = "cpi";

        //Void creating object from this class
        private CustomScopeType() {
        }
    }
}
