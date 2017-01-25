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
package com.wso2telco.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;
import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

 
// TODO: Auto-generated Javadoc
/**
 * The Class ConfigLoader.
 */
public class ConfigLoader {

    Log log = LogFactory.getLog(ConfigLoader.class);
    
    /** The scope configs. */
    private ScopeConfigs scopeConfigs;
    
    /** The loader. */
    private static ConfigLoader loader = new ConfigLoader();

    /**
     * Instantiates a new config loader.
     */
    private ConfigLoader() {
        try {
            this.scopeConfigs = initClaimsConfig();
        } catch (JAXBException e) {
            log.error(e);
        }
    }

    /**
     * Gets the single instance of ConfigLoader.
     *
     * @return single instance of ConfigLoader
     */
    public static ConfigLoader getInstance() {
        return loader;
    }

    /**
     * Inits the claims config.
     *
     * @return the scope configs
     * @throws JAXBException the JAXB exception
     */
    private ScopeConfigs initClaimsConfig() throws JAXBException {
        Unmarshaller um = null;
        ScopeConfigs userClaims = null;
        String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "scope-config.xml";
        File file = new File(configPath);
        try {
            JAXBContext ctx = JAXBContext.newInstance(ScopeConfigs.class);
            um = ctx.createUnmarshaller();
            userClaims = (ScopeConfigs) um.unmarshal(file);
        } catch (JAXBException e) {
            throw new JAXBException("Error unmarshalling file :"+configPath);
        }
        return userClaims;
    }

    /**
     * Gets the scope configs.
     *
     * @return the scope configs
     */
    public ScopeConfigs getScopeConfigs() {
        return scopeConfigs;
    }

    /**
     * Sets the scope configs.
     *
     * @param scopeConfigs the new scope configs
     */
    public void setScopeConfigs(ScopeConfigs scopeConfigs) {
        this.scopeConfigs = scopeConfigs;
    }
}
