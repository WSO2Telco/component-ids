package com.wso2telco.util;


import com.wso2telco.core.config.ConfigLoader;
import com.wso2telco.core.config.model.MobileConnectConfig;

import java.util.List;

public class Validation {

    public boolean validateOperator(String operator) {

        boolean validOperator = false;
        List<MobileConnectConfig.OPERATOR> operatorList = ConfigLoader.getInstance().getMobileConnectConfig().getHEADERENRICH().getOperators();
        for (MobileConnectConfig.OPERATOR configuredOperator : operatorList) {
            if (operator.equalsIgnoreCase(configuredOperator.getOperatorName())) {
                validOperator = true;
                break;
            }
        }

        return validOperator;
    }

}
