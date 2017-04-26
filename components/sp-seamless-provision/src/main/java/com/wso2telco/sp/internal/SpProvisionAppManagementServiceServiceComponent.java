package com.wso2telco.sp.internal;

import com.wso2telco.core.spprovisionservice.external.admin.service.OauthAdminService;
import com.wso2telco.core.spprovisionservice.external.admin.service.SpAppManagementService;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.apache.felix.scr.annotations.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Component(name = "com.wso2telco.sp.internal.SpProvisionAppManagementServiceServiceComponent", immediate = true)
@Reference(
        name = "com.wso2telco.core.spprovisionservice.internal.SpAppManagementServiceComponent",
        referenceInterface = com.wso2telco.core.spprovisionservice.external.admin.service.SpAppManagementService.class,
        cardinality = ReferenceCardinality.MANDATORY_UNARY,
        policy = ReferencePolicy.DYNAMIC,
        bind = "setAppManagementService",
        unbind = "unsetAppManagementService"
)
public class SpProvisionAppManagementServiceServiceComponent {

    private SpAppManagementService appManagementService;
    private static Log log = LogFactory.getLog(SpProvisionPcrServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        log.debug("SpProvisionOauthServiceServiceComponent Bundle Activated");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    protected void setAppManagementService(SpAppManagementService appManagementService) {
        SpProvisionAppManagementServiceDataHolder.getInstance().setAppManagementService(appManagementService);
    }

    protected void unsetAppManagementService(SpAppManagementService appManagementService) {
        SpProvisionAppManagementServiceDataHolder.getInstance().setAppManagementService(null);
    }
}
