/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com)
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
package com.wso2telco.entity;


public class ValidationResponse {

    private String status;

    private String sessionId;

    private boolean isAnswer1Valid;

    private boolean isAnswer2Valid;

    public ValidationResponse(String status, String sessionId) {
        this.status = status;
        this.sessionId = sessionId;
    }

    public ValidationResponse(String status, String sessionId, boolean isAnswer1Valid, boolean isAnswer2Valid) {
        this.status = status;
        this.sessionId = sessionId;
        this.isAnswer1Valid = isAnswer1Valid;
        this.isAnswer2Valid = isAnswer2Valid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isAnswer1Valid() {
        return isAnswer1Valid;
    }

    public void setAnswer1Valid(boolean answer1Valid) {
        isAnswer1Valid = answer1Valid;
    }

    public boolean isAnswer2Valid() {
        return isAnswer2Valid;
    }

    public void setAnswer2Valid(boolean answer2Valid) {
        isAnswer2Valid = answer2Valid;
    }
}
