/*
 * FileUtil.java
 * Apr 2, 2013  11:20:38 AM
 * Tharanga Ranaweera
 *
 * Copyright (C) Dialog Axiata PLC. All Rights Reserved.
 */

package com.wso2telco.ids.datapublisher.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


public class FileUtil {

    private static Log log = LogFactory.getLog(FileUtil.class);

    private static Properties props = new Properties();

    static {
        try {
            props.load(FileUtil.class.getResourceAsStream("application.properties"));

        } catch (FileNotFoundException e) {
           log.error(
                    "Check your Property file, it should be in application home dir, Error:"
                    + e.getCause() + "Cant load APPLICATION.properties");
        } catch (IOException e) {
            log.error(
                    "Check your Property file, it should be in application home dir, Error:"
                    + e.getCause() + "Cant load APPLICATION.properties");
        }
    }

    /**
     * This method return value from property file of corresponding key passed.
     *
     * @return String
     */
    public static String getApplicationProperty(String key) {
        return props.getProperty(key);
    }

}
