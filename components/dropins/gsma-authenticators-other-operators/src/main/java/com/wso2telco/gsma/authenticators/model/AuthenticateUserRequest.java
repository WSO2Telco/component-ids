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
 * The Class AuthenticateUserRequest.
 */
public class AuthenticateUserRequest {
    
    /** The address. */
    String address;
    
    /** The requested loa. */
    String requestedLOA;
    
    /** The service provider. */
    ServiceProvider serviceProvider;
    
    /** The client correlator. */
    String clientCorrelator;

    /**
     * Gets the address.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address.
     *
     * @param address the new address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the requested loa.
     *
     * @return the requested loa
     */
    public String getRequestedLOA() {
        return requestedLOA;
    }

    /**
     * Sets the requested loa.
     *
     * @param requestedLOA the new requested loa
     */
    public void setRequestedLOA(String requestedLOA) {
        this.requestedLOA = requestedLOA;
    }

    /**
     * Gets the service provider.
     *
     * @return the service provider
     */
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    /**
     * Sets the service provider.
     *
     * @param serviceProvider the new service provider
     */
    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    /**
     * Gets the client correlator.
     *
     * @return the client correlator
     */
    public String getClientCorrelator() {
        return clientCorrelator;
    }

    /**
     * Sets the client correlator.
     *
     * @param clientCorrelator the new client correlator
     */
    public void setClientCorrelator(String clientCorrelator) {
        this.clientCorrelator = clientCorrelator;
    }
}
