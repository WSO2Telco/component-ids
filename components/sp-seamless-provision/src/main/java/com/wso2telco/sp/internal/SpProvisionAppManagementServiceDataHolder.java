package com.wso2telco.sp.internal;

import com.wso2telco.core.spprovisionservice.external.admin.service.SpAppManagementService;
import com.wso2telco.core.spprovisionservice.external.admin.service.impl.SpAppManagementServiceImpl;

public class SpProvisionAppManagementServiceDataHolder {

    private SpAppManagementService appManagementService;

    private static SpProvisionAppManagementServiceDataHolder dataHolder = new SpProvisionAppManagementServiceDataHolder();

    public static SpProvisionAppManagementServiceDataHolder getInstance() {
        return dataHolder;
    }

    public SpAppManagementService getAppManagementService() {
        return new SpAppManagementServiceImpl();
    }

    public void setAppManagementService(SpAppManagementService appManagementService) {
        this.appManagementService = appManagementService;
    }

}
