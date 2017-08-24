package com.wso2telco.sp.internal;

import com.wso2telco.core.spprovisionservice.external.admin.service.OauthAdminService;
import com.wso2telco.core.spprovisionservice.external.admin.service.impl.OauthAdminServiceImpl;

public class SpProvisionOauthServiceDataHolder {

    private OauthAdminService oauthAdminService;

    private static SpProvisionOauthServiceDataHolder dataHolder = new SpProvisionOauthServiceDataHolder();

    public static SpProvisionOauthServiceDataHolder getInstance() {
        return dataHolder;
    }

    public OauthAdminService getOauthAdminService() {
        return new OauthAdminServiceImpl();
    }

    public void setOauthAdminService(OauthAdminService oauthAdminService) {
        this.oauthAdminService = oauthAdminService;
    }

}
