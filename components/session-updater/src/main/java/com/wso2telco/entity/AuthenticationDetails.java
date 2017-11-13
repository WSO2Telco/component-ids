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


public class AuthenticationDetails {

    private String sessionId;

    private String challengeQuestion1;

    private String challengeQuestion2;

    private String challengeAnswer1;

    private String challengeAnswer2;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getChallengeQuestion1() {
        return challengeQuestion1;
    }

    public void setChallengeQuestion1(String challengeQuestion1) {
        this.challengeQuestion1 = challengeQuestion1;
    }

    public String getChallengeQuestion2() {
        return challengeQuestion2;
    }

    public void setChallengeQuestion2(String challengeQuestion2) {
        this.challengeQuestion2 = challengeQuestion2;
    }

    public String getChallengeAnswer1() {
        return challengeAnswer1;
    }

    public void setChallengeAnswer1(String challengeAnswer1) {
        this.challengeAnswer1 = challengeAnswer1;
    }

    public String getChallengeAnswer2() {
        return challengeAnswer2;
    }

    public void setChallengeAnswer2(String challengeAnswer2) {
        this.challengeAnswer2 = challengeAnswer2;
    }
}
