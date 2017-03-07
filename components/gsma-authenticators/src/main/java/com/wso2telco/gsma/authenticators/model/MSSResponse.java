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
package com.wso2telco.gsma.authenticators.model;

import java.io.Serializable;


// TODO: Auto-generated Javadoc

/**
 * The Class MSSResponse.
 */
public class MSSResponse implements Serializable {

    /**
     * The msisdn no.
     */
    public String msisdnNo;

    /**
     * The response status.
     */
    public String responseStatus;

    /**
     * Gets the msisdn no.
     *
     * @return the msisdn no
     */
    public String getMsisdnNo() {
        return msisdnNo;
    }

    /**
     * Sets the msisdn no.
     *
     * @param msisdnNo the new msisdn no
     */
    public void setMsisdnNo(String msisdnNo) {
        this.msisdnNo = msisdnNo;
    }

    /**
     * Gets the response status.
     *
     * @return the response status
     */
    public String getResponseStatus() {
        return responseStatus;
    }

    /**
     * Sets the response status.
     *
     * @param responseStatus the new response status
     */
    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }


}
