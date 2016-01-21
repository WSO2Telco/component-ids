package com.gsma.authendictorselector;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

 
public abstract  class DialogAuthenticatorSelector implements  AuthenticatorSelector{
    public abstract String getSMS(String msisdn, String message) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException;
    public abstract void getSMSandURLNotifiyURL();
    public abstract String getUSSD(String msisdn, String sessionID, String serviceProvider) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException;
    public abstract String getUSSDPIN(String msisdn, String sessionID, String serviceProvider) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException;
}
