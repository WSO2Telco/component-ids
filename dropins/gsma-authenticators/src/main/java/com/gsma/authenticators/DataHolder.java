package com.gsma.authenticators;

import com.gsma.authenticators.config.LOAConfig;
import com.gsma.authenticators.config.MobileConnectConfig;

public class DataHolder {

    private LOAConfig loaConfig;
    private MobileConnectConfig mobileConnectConfig;

    private static DataHolder thisInstance = new DataHolder();

    private DataHolder() {}

    public static DataHolder getInstance() {
        return thisInstance;
    }

    public void setLOAConfig(LOAConfig config) {
        this.loaConfig = config;
    }

    public LOAConfig getLOAConfig() {
        return loaConfig;
    }

    public MobileConnectConfig getMobileConnectConfig() {
        return mobileConnectConfig;
    }

    public void setMobileConnectConfig(MobileConnectConfig mobileConnectConfig) {
        this.mobileConnectConfig = mobileConnectConfig;
    }
}
