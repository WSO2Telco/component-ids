package com.wso2telco.spprovisionapp.utils;

import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;

public class MIGUtils {
    private static final int MIG_BASE_PORT = 9443;
    private static final String MIG_PORT_OFFSET_KEY = "portOffset";
    private static final String HTTPS_PREFIX = "https://";


    public static String getMigHostName() throws SocketException {
        String host = NetworkUtils.getLocalHostname();
        if (!host.startsWith("http")) {
            host = HTTPS_PREFIX + host;
        }
        return host;
    }

    public static int getMigPort() {
        String portOffset = System.getProperty(MIG_PORT_OFFSET_KEY);
        if (null == portOffset || "".equals(portOffset)) {
            return MIG_BASE_PORT;
        }

        return MIG_BASE_PORT + Integer.parseInt(portOffset);
    }
}
