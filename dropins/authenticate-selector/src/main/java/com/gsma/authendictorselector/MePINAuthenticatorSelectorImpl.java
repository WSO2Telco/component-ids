package com.gsma.authendictorselector;

import com.google.gson.JsonObject;
import com.gsma.authenticators.mepin.MePinQuery;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * Created by paraparan on 5/22/15.
 */
public class MePINAuthenticatorSelectorImpl extends MePINAuthenticatorSelector {

    @Override
    public JsonObject getMePINPIN(String mepinID, String sessionID, String serviceProvider,String confirmation_policy) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        MePinQuery mePinQuery = new MePinQuery();
        return mePinQuery.createTransaction(mepinID, sessionID, serviceProvider, confirmation_policy);
    }

    @Override
    public String invokeAuthendicator() throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {
        return null;
    }
}
