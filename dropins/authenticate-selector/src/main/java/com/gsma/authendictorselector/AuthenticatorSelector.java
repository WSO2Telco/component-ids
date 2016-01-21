package com.gsma.authendictorselector;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

 
public interface AuthenticatorSelector {
    public String invokeAuthendicator() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException;
}
