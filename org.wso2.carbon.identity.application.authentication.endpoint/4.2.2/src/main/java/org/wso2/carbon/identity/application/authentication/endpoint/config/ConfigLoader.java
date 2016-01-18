package org.wso2.carbon.identity.application.authentication.endpoint.config;

import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * This class reads the configuration file claims.xml to read scopes and claims of relevant scope
 */
public class ConfigLoader {
    private ScopeConfigs scopeConfigs;
    private static ConfigLoader loader = new ConfigLoader();

    private ConfigLoader() {
        try {
            this.scopeConfigs = initClaimsConfig();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static ConfigLoader getInstance() {
        return loader;
    }

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

    public ScopeConfigs getScopeConfigs() {
        return scopeConfigs;
    }

    public void setScopeConfigs(ScopeConfigs scopeConfigs) {
        this.scopeConfigs = scopeConfigs;
    }
}
