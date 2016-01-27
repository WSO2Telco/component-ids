package com.gsma.authenticators.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Application {
    private static Log log = LogFactory.getLog(Application.class);

    public String changeApplicationName(String applicationName){
        try {
            applicationName = applicationName.substring(applicationName.indexOf("_") + 1);
        }catch(Exception ex){
            log.info("application name not convert");
        }
        return applicationName;
    }
}
