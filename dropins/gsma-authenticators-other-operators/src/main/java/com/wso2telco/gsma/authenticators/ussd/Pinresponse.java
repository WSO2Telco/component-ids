/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
package com.wso2telco.gsma.authenticators.ussd;

 
// TODO: Auto-generated Javadoc
/**
 * The Class Pinresponse.
 */
public class Pinresponse {

    /** The user response. */
    private String userResponse;
    
    /** The user pin. */
    private String userPin;

    /**
     * Gets the user response.
     *
     * @return the user response
     */
    public String getUserResponse() {
        return userResponse;
    }

    /**
     * Sets the user response.
     *
     * @param userResponse the new user response
     */
    public void setUserResponse(String userResponse) {
        this.userResponse = userResponse;
    }

    /**
     * Gets the user pin.
     *
     * @return the user pin
     */
    public String getUserPin() {
        return userPin;
    }

    /**
     * Sets the user pin.
     *
     * @param userPin the new user pin
     */
    public void setUserPin(String userPin) {
        this.userPin = userPin;
    }
    
}
