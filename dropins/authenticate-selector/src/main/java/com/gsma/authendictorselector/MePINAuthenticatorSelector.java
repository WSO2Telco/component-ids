package com.gsma.authendictorselector;

import com.google.gson.JsonObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * Created by paraparan on 5/22/15.
 */
public abstract class MePINAuthenticatorSelector implements AuthenticatorSelector {
    public abstract JsonObject getMePINPIN(String mepinID, String sessionID, String serviceProvider,String confirmation_policy) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException;
}
