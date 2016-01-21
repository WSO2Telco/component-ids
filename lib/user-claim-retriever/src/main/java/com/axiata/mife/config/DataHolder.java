package com.axiata.mife.config;

 
public class DataHolder {


    private ScopeConfigs scopeConfigs;

    private static DataHolder thisInstance = new DataHolder();

    private DataHolder() {}

    public static DataHolder getInstance() {
        return thisInstance;
    }


    public ScopeConfigs getScopeConfigs() {
        return scopeConfigs;
    }

    public void setScopeConfigs(ScopeConfigs scopeConfigs) {
        this.scopeConfigs = scopeConfigs;
    }
}