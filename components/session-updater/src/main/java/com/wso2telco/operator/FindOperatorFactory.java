package com.wso2telco.operator;


import com.wso2telco.core.config.ConfigLoader;
import com.wso2telco.util.Constants;


public class FindOperatorFactory {

    public FindOperator getRecoveryOption() {
        FindOperator findOperator=null;
        String recoveryOption=ConfigLoader.getInstance().getMobileConnectConfig().getOperatorRecovery().getRecoveryOption();
        if(recoveryOption.equalsIgnoreCase(Constants.DISCOVERY)){
            findOperator=new OperatorDiscovery();
        }else{
            findOperator=new OperatorNumberRange();
        }
        return findOperator;
    }

 
}
