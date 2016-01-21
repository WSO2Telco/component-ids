package com.gsma.authendictorselector;

import com.gsma.authenticators.dialog.sms.SendSMS;
import com.gsma.authenticators.dialog.ussd.SendUSSD;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * Created by paraparan on 5/15/15.
 */
public class DialogAuthenticatorSelectorImpl extends DialogAuthenticatorSelector{

    @Override
    public String getSMS(String msisdn, String message) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        SendSMS sendSMS = new SendSMS(msisdn, message);
        return sendSMS.invokeAuthendicator();
    }

    @Override
    public void getSMSandURLNotifiyURL() {

    }

    @Override
    public String getUSSD(String msisdn, String sessionID, String serviceProvider) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        SendUSSD sendUSSD = new SendUSSD(msisdn, sessionID, serviceProvider);
        return sendUSSD.invokeAuthendicator();
    }

    @Override
    public String getUSSDPIN(String msisdn, String sessionID, String serviceProvider) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        SendUSSD sendUSSD = new SendUSSD(msisdn, sessionID, serviceProvider);
        return sendUSSD.invokeAuthendicatorUSSDPIN();
    }

    @Override
    public String invokeAuthendicator() {
        return "";
    }
}
