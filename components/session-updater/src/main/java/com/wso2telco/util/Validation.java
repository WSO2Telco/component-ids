package com.wso2telco.util;


import com.wso2telco.core.config.ConfigLoader;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;

import java.util.List;

public class Validation {

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    public boolean validateOperator(String operator) {
        boolean validOperator = false;
        List<MobileConnectConfig.OPERATOR> operatorList = configurationService.getDataHolder().getMobileConnectConfig().getHEADERENRICH().getOperators();
        for (MobileConnectConfig.OPERATOR configuredOperator : operatorList) {
            if (operator.equalsIgnoreCase(configuredOperator.getOperatorName())) {
                validOperator = true;
                break;
            }
        }
        return validOperator;
    }

}
