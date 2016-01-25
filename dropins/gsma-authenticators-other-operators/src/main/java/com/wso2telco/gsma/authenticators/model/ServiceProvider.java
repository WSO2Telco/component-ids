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
package com.wso2telco.gsma.authenticators.model;

 
// TODO: Auto-generated Javadoc
/**
 * The Class ServiceProvider.
 */
public class ServiceProvider {
    
    /** The service provider name. */
    String serviceProviderName;
    
    /** The logo url. */
    String logoURL;
    
    /** The message. */
    String message;

    /**
     * Gets the service provider name.
     *
     * @return the service provider name
     */
    public String getServiceProviderName() {
        return serviceProviderName;
    }

    /**
     * Sets the service provider name.
     *
     * @param serviceProviderName the new service provider name
     */
    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    /**
     * Gets the logo url.
     *
     * @return the logo url
     */
    public String getLogoURL() {
        return logoURL;
    }

    /**
     * Sets the logo url.
     *
     * @param logoURL the new logo url
     */
    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     *
     * @param message the new message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
