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
package com.wso2telco.proxy.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * This file is used to load mobile-connect.xml file.
 */
public class ConfigLoader {
    private Log log = LogFactory.getLog(ConfigLoader.class);

    private MobileConnectConfig mobileConnectConfig;
    private static ConfigLoader loader;

    private ConfigLoader() {
        try {
            this.mobileConnectConfig = initMConnectConfig();
        } catch (JAXBException e) {
            log.error("Error occurred while initiating mobile-connector config file", e);
        }
    }

    /**
     * Get instance of the ConfigLoader class.
     * @return
     */
    public static ConfigLoader getInstance() {
        if(loader == null) {
            loader = new ConfigLoader();
        }
        return loader;
    }

    /**
     * Initiate mobile-connect.xml file.
     * @return the configuration of mobile-connect.xml file.
     * @throws JAXBException
     */
    private MobileConnectConfig initMConnectConfig() throws JAXBException {
        String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "mobile-connect.xml";
        File file = new File(configPath);
        JAXBContext jaxbContext = JAXBContext.newInstance(MobileConnectConfig.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MobileConnectConfig) unmarshaller.unmarshal(file);
    }

    /**
     * Get configurations from mobile-connect.xml.
     * @return the configuration of mobile-connect.xml file.
     */
    public MobileConnectConfig getMobileConnectConfig(){
        return mobileConnectConfig;
    }
}