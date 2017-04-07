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

import com.wso2telco.gsma.authenticators.dialog.sms.SendSMS;
import com.wso2telco.gsma.authenticators.dialog.ussd.SendUSSD;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;


// TODO: Auto-generated Javadoc

/**
 * The Class DialogAuthenticatorSelectorImpl.
 */
public class DialogAuthenticatorSelectorImpl extends DialogAuthenticatorSelector {

    /* (non-Javadoc)
     * @see com.wso2telco.gsma.authendictorselector.DialogAuthenticatorSelector#getSMS(java.lang.String, java.lang
     * .String)
     */
    @Override
    public String getSMS(String msisdn, String message) throws SAXException, ParserConfigurationException,
            XPathExpressionException, IOException {
        SendSMS sendSMS = new SendSMS(msisdn, message);
        return sendSMS.invokeAuthendicator();
    }

    /* (non-Javadoc)
     * @see com.wso2telco.gsma.authendictorselector.DialogAuthenticatorSelector#getSMSandURLNotifiyURL()
     */
    @Override
    public void getSMSandURLNotifiyURL() {

    }

    /* (non-Javadoc)
     * @see com.wso2telco.gsma.authendictorselector.DialogAuthenticatorSelector#getUSSD(java.lang.String, java.lang
     * .String, java.lang.String)
     */
    @Override
    public String getUSSD(String msisdn, String sessionID, String serviceProvider) throws SAXException,
            ParserConfigurationException, XPathExpressionException, IOException {
        SendUSSD sendUSSD = new SendUSSD(msisdn, sessionID, serviceProvider);
        return sendUSSD.invokeAuthendicator();
    }

    /* (non-Javadoc)
     * @see com.wso2telco.gsma.authendictorselector.DialogAuthenticatorSelector#getUSSDPIN(java.lang.String, java
     * .lang.String, java.lang.String)
     */
    @Override
    public String getUSSDPIN(String msisdn, String sessionID, String serviceProvider) throws SAXException,
            ParserConfigurationException, XPathExpressionException, IOException {
        SendUSSD sendUSSD = new SendUSSD(msisdn, sessionID, serviceProvider);
        return sendUSSD.invokeAuthendicatorUSSDPIN();
    }

    /* (non-Javadoc)
     * @see com.wso2telco.gsma.authendictorselector.AuthenticatorSelector#invokeAuthendicator()
     */
    @Override
    public String invokeAuthendicator() {
        return "";
    }
}
