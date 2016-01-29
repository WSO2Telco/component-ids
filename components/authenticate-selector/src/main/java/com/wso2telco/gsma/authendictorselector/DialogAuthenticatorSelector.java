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

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

 
// TODO: Auto-generated Javadoc
/**
 * The Class DialogAuthenticatorSelector.
 */
public abstract  class DialogAuthenticatorSelector implements  AuthenticatorSelector{
    
    /**
     * Gets the sms.
     *
     * @param msisdn the msisdn
     * @param message the message
     * @return the sms
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     * @throws XPathExpressionException the x path expression exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public abstract String getSMS(String msisdn, String message) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException;
    
    /**
     * Gets the SM sand url notifiy url.
     *
     * @return the SM sand url notifiy url
     */
    public abstract void getSMSandURLNotifiyURL();
    
    /**
     * Gets the ussd.
     *
     * @param msisdn the msisdn
     * @param sessionID the session id
     * @param serviceProvider the service provider
     * @return the ussd
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     * @throws XPathExpressionException the x path expression exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public abstract String getUSSD(String msisdn, String sessionID, String serviceProvider) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException;
    
    /**
     * Gets the ussdpin.
     *
     * @param msisdn the msisdn
     * @param sessionID the session id
     * @param serviceProvider the service provider
     * @return the ussdpin
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     * @throws XPathExpressionException the x path expression exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public abstract String getUSSDPIN(String msisdn, String sessionID, String serviceProvider) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException;
}
