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
package com.wso2telco;


// TODO: Auto-generated Javadoc

/**
 * The Class USSDRequest.
 */
public class USSDRequest {

    /**
     * The outbound ussd message request.
     */
    private OutboundUSSDMessageRequest outboundUSSDMessageRequest = null;

    /**
     * Gets the outbound ussd message request.
     *
     * @return the outbound ussd message request
     */
    public OutboundUSSDMessageRequest getOutboundUSSDMessageRequest() {
        return outboundUSSDMessageRequest;
    }

    /**
     * Sets the outbound ussd message request.
     *
     * @param outboundUSSDMessageRequest the new outbound ussd message request
     */
    public void setOutboundUSSDMessageRequest(OutboundUSSDMessageRequest outboundUSSDMessageRequest) {
        this.outboundUSSDMessageRequest = outboundUSSDMessageRequest;
    }
}
