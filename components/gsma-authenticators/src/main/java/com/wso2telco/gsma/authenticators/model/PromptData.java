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


package com.wso2telco.gsma.authenticators.model;

public class PromptData {
    public enum behaviorTypes {
        OFFNET_TRUST_LOGIN_HINT,
        ONNET,
        OFFNET
    }

    ;
    private String scope;
    private String promptValue;
    private boolean isLoginHintExists;
    private behaviorTypes behaviour;

    public boolean isLoginHintExists() {
        return isLoginHintExists;
    }

    public void setLoginHintExists(boolean loginHintExists) {
        isLoginHintExists = loginHintExists;
    }

    public String getPromptValue() {
        return promptValue;
    }

    public void setPromptValue(String promptValue) {
        this.promptValue = promptValue;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public behaviorTypes getBehaviour() {
        return behaviour;
    }

    public void setBehaviour(behaviorTypes behaviour) {
        this.behaviour = behaviour;
    }
}
