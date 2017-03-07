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
package com.wso2telco.gsma.utils;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import com.wso2telco.gsma.authenticators.dialog.sms.SendSMS;

// TODO: Auto-generated Javadoc

/**
 * The Class ReadMobileConnectConfig.
 */
public class ReadMobileConnectConfig {

    /**
     * Query.
     *
     * @param XpathExpression the xpath expression
     * @return the map
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException                 the SAX exception
     * @throws IOException                  Signals that an I/O exception has occurred.
     * @throws XPathExpressionException     the x path expression exception
     */
    public Map<String, String> query(String XpathExpression) throws ParserConfigurationException, SAXException,
            IOException, XPathExpressionException {
        // standard for reading an XML file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        XPathExpression expr = null;
        builder = factory.newDocumentBuilder();
        doc = builder.parse(CarbonUtils.getCarbonConfigDirPath() + File.separator + "mobile-connect.xml");

        // create an XPathFactory
        XPathFactory xFactory = XPathFactory.newInstance();

        // create an XPath object
        XPath xpath = xFactory.newXPath();

        // compile the XPath expression
        expr = xpath.compile("//" + XpathExpression + "/*");
        // run the query and get a nodeset
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
//        // cast the result to a DOM NodeList
        NodeList nodes = (NodeList) result;

        Map<String, String> ConfigfileAttributes = new Hashtable<String, String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            ConfigfileAttributes.put(nodes.item(i).getNodeName(), nodes.item(i).getTextContent());
        }

        return ConfigfileAttributes;
    }

}

