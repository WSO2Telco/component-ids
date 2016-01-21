package com.gsma.authendictorselector;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * Created by paraparan on 5/13/15.
 */
public interface AuthenticatorSelector {
    public String invokeAuthendicator() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException;
}
