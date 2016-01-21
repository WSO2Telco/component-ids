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
package com.wso2telco.carbon.identity.application.authentication.endpoint.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class TenantDataManager.
 */
public class TenantDataManager {

    /** The Constant log. */
    private static final Log log = LogFactory.getLog(TenantDataManager.class);
    
    /** The Constant USERNAME. */
    private static final String USERNAME = "mutual.ssl.username";
    
    /** The Constant USERNAME_HEADER. */
    private static final String USERNAME_HEADER = "username.header";
    
    /** The Constant HOST. */
    private static final String HOST = "identity.server.host";
    
    /** The Constant PORT. */
    private static final String PORT = "identity.server.port";
    
    /** The Constant CLIENT_KEY_STORE. */
    private static final String CLIENT_KEY_STORE = "client.keyStore";
    
    /** The Constant CLIENT_TRUST_STORE. */
    private static final String CLIENT_TRUST_STORE = "client.trustStore";
    
    /** The Constant CLIENT_KEY_STORE_PASSWORD. */
    private static final String CLIENT_KEY_STORE_PASSWORD = "client.keyStore.password";
    
    /** The Constant CLIENT_TRUST_STORE_PASSWORD. */
    private static final String CLIENT_TRUST_STORE_PASSWORD = "client.trustStore.password";
    
    /** The Constant RETURN. */
    private static final String RETURN = "return";
    
    /** The Constant RETRIEVE_TENANTS_RESPONSE. */
    private static final String RETRIEVE_TENANTS_RESPONSE = "retrieveTenantsResponse";
    
    /** The Constant TENANT_DOMAIN. */
    private static final String TENANT_DOMAIN = "tenantDomain";
    
    /** The Constant ACTIVE. */
    private static final String ACTIVE = "active";
    
    /** The Constant TENANT_LIST_ENABLED. */
    private static final String TENANT_LIST_ENABLED = "tenantListEnabled";
    
    /** The prop. */
    private static Properties prop;
    
    /** The carbon login. */
    private static String carbonLogin = "";
    
    /** The username header name. */
    private static String usernameHeaderName = "";
    
    /** The tenant domain list. */
    private static List<String> tenantDomainList;;
    
    /** The is initialized. */
    private static boolean isInitialized = false;

    /**
     * Inits the.
     */
    private static synchronized void init() {

        try {
            if (!isInitialized) {
                prop = new Properties();

                InputStream inputStream = TenantDataManager.class.getClassLoader().getResourceAsStream("TenantConfig.properties");

                if (inputStream != null) {
                    prop.load(inputStream);

                    usernameHeaderName = getPropertyValue(USERNAME_HEADER);

                    carbonLogin = getPropertyValue(USERNAME);

                    byte[] base64EncodedByteArray =
                            new Base64().encode(carbonLogin.getBytes("UTF-8"));

                    carbonLogin = new String(base64EncodedByteArray); //Base64 encode username

                    String clientKeyStorePath = buildFilePath(getPropertyValue(CLIENT_KEY_STORE));
                    String clientTrustStorePath = buildFilePath(getPropertyValue(CLIENT_TRUST_STORE));

                    MutualSSLClient.loadKeyStore(clientKeyStorePath, getPropertyValue(CLIENT_KEY_STORE_PASSWORD));
                    MutualSSLClient.loadTrustStore(clientTrustStorePath, getPropertyValue(CLIENT_TRUST_STORE_PASSWORD));
                    MutualSSLClient.initMutualSSLConnection();

                    tenantDomainList = new ArrayList<String>();

                    isInitialized = true;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Configuration file TenantConfig.properties not found");
                    }
                }
            }

        } catch (Exception e) {
            log.error("Initialization failed : ", e);
        }
    }

    /**
     * Builds the file path.
     *
     * @param path the path
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static String buildFilePath(String path) throws IOException {

        if(path != null && path.startsWith(".")){ //relative path is given
            File currentDirectory = new File(new File(".").getAbsolutePath());
            path = currentDirectory.getCanonicalPath() + File.separator + path;
        }

        if(log.isDebugEnabled()){
            log.debug("File path for KeyStore/TrustStore : " + path);
        }
        return path;
    }

    /**
     * Gets the property value.
     *
     * @param key the key
     * @return the property value
     */
    private static String getPropertyValue(String key) {

        return prop.getProperty(key);
    }

    /**
     * Gets the service response.
     *
     * @param url the url
     * @return the service response
     */
    private static String getServiceResponse(String url) {

        try {
            Map<String, String> headerParams = new HashMap<String, String>();
            headerParams.put(usernameHeaderName, carbonLogin);
            return MutualSSLClient.sendGetRequest(url, null, headerParams);
        } catch (Exception e) {
            log.error("Processing request for " + url + " Failed : " , e);
            return null;
        }
    }

    /**
     * Gets the all active tenant domains.
     *
     * @return the all active tenant domains
     */
    public static List<String> getAllActiveTenantDomains() {

        if (!isInitialized) {
            init();
        }
        if (tenantDomainList == null || tenantDomainList.size() == 0) {
            refreshActiveTenantDomainsList();
        }
        return tenantDomainList;
    }

    /**
     * Sets the tenant data list.
     *
     * @param dataList the new tenant data list
     */
    public static void setTenantDataList(String dataList) {

        if (!isInitialized) {
            init();
        }

        if (dataList != null && dataList.trim().length() > 0) {

            synchronized (tenantDomainList) {
                String[] domains = dataList.split(",");
                tenantDomainList = new ArrayList<String>();
                for (String domain : domains) {
                    tenantDomainList.add(domain);
                }

                Collections.sort(tenantDomainList);
            }
        } else {
            tenantDomainList = null;
        }
    }

    /**
     * Refresh active tenant domains list.
     */
    private static void refreshActiveTenantDomainsList() {

        try {
            String url = "https://" + getPropertyValue(HOST) + ":" + getPropertyValue(PORT) +
                    "/services/TenantMgtAdminService/retrieveTenants";

            String xmlString = getServiceResponse(url);

            if (xmlString != null && !"".equals(xmlString)) {

                XPathFactory xpf = XPathFactory.newInstance();
                XPath xpath = xpf.newXPath();

                InputSource inputSource = new InputSource(new StringReader(xmlString));
                String xPathExpression =
                        "/*[local-name() = '" + RETRIEVE_TENANTS_RESPONSE +
                                "']/*[local-name() = '" +
                                RETURN + "']";
                NodeList nodeList =
                        (NodeList) xpath
                                .evaluate(xPathExpression, inputSource, XPathConstants.NODESET);
                tenantDomainList = new ArrayList<String>();

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        NodeList tenantData = element.getChildNodes();
                        boolean activeChecked = false;
                        boolean domainChecked = false;
                        boolean isActive = false;
                        String tenantDomain = null;

                        for (int j = 0; j < tenantData.getLength(); j++) {

                            Node dataItem = tenantData.item(j);
                            String localName = dataItem.getLocalName();

                            if (ACTIVE.equals(localName)) {
                                activeChecked = true;
                                if ("true".equals(dataItem.getTextContent())) {
                                    isActive = true;
                                }
                            }

                            if (TENANT_DOMAIN.equals(localName)) {
                                domainChecked = true;
                                tenantDomain = dataItem.getTextContent();
                            }

                            if (activeChecked && domainChecked) {
                                if (isActive) {
                                    tenantDomainList.add(tenantDomain);
                                }
                                break;
                            }
                        }
                    }
                }

                Collections.sort(tenantDomainList);
            }

        } catch (Exception e) {
            if(log.isDebugEnabled()) {
                log.debug("Retrieving Active Tenant Domains Failed. Ignore this if there are no tenants : ", e);
            }
        }
    }

    /**
     * Checks if is tenant list enabled.
     *
     * @return true, if is tenant list enabled
     */
    public static boolean isTenantListEnabled() {
        if (!isInitialized) {
            init();
        }
        return "true".equals(getPropertyValue(TENANT_LIST_ENABLED));
    }
}
