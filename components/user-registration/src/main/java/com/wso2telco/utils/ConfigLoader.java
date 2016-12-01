package com.wso2telco.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class ConfigLoader {

    private Log log = LogFactory.getLog(ConfigLoader.class);

    private AuthenticationLevels authenticationLevels;
    private MobileConnectConfig mobileConnectConfig;
    private static ConfigLoader loader = new ConfigLoader();

    private ConfigLoader() {
        try {
            this.authenticationLevels = initLoaConfig();
            this.mobileConnectConfig = initMConnectConfig();
        } catch (JAXBException e) {
            log.error("Error while initiating custom config files", e);
        }
    }

    public static ConfigLoader getInstance() {
        return loader;
    }

    private AuthenticationLevels initLoaConfig() throws JAXBException {
        String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "LOA.xml";
        File file = new File(configPath);
        JAXBContext ctx = JAXBContext.newInstance(AuthenticationLevels.class);
        Unmarshaller um = ctx.createUnmarshaller();
        return  (AuthenticationLevels) um.unmarshal(file);
    }

    public AuthenticationLevels getAuthenticationLevels() {
        return authenticationLevels;
    }

    private MobileConnectConfig initMConnectConfig() throws JAXBException {
        String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "mobile-connect.xml";
        File file = new File(configPath);
        JAXBContext ctx = JAXBContext.newInstance(MobileConnectConfig.class);
        Unmarshaller um = ctx.createUnmarshaller();
        return (MobileConnectConfig) um.unmarshal(file);
    }

    public MobileConnectConfig getMobileConnectConfig(){
        return mobileConnectConfig;
    }

}
