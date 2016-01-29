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
package com.wso2telco.gsma.authendictorselector;

import com.google.gson.JsonObject;
import com.wso2telco.gsma.authenticators.mepin.MePinQuery;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

 
// TODO: Auto-generated Javadoc
/**
 * The Class MePINAuthenticatorSelectorImpl.
 */
public class MePINAuthenticatorSelectorImpl extends MePINAuthenticatorSelector {

    /* (non-Javadoc)
     * @see com.wso2telco.gsma.authendictorselector.MePINAuthenticatorSelector#getMePINPIN(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public JsonObject getMePINPIN(String mepinID, String sessionID, String serviceProvider,String confirmation_policy) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        MePinQuery mePinQuery = new MePinQuery();
        return mePinQuery.createTransaction(mepinID, sessionID, serviceProvider, confirmation_policy);
    }

    /* (non-Javadoc)
     * @see com.wso2telco.gsma.authendictorselector.AuthenticatorSelector#invokeAuthendicator()
     */
    @Override
    public String invokeAuthendicator() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        return null;
    }
}
