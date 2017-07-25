package com.wso2telco.sp.internal;

import com.wso2telco.core.spprovisionservice.external.admin.service.OauthAdminService;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.apache.felix.scr.annotations.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Component(name = "com.wso2telco.sp.internal.SpProvisionOauthServiceServiceComponent", immediate = true)
@Reference(
        name = "com.wso2telco.core.spprovisionservice.internal.OAuthAdminServiceComponent",
        referenceInterface = com.wso2telco.core.spprovisionservice.external.admin.service.OauthAdminService.class,
        cardinality = ReferenceCardinality.MANDATORY_UNARY,
        policy = ReferencePolicy.DYNAMIC,
        bind = "setOauthService",
        unbind = "unsetOauthService"
)
public class SpProvisionOauthServiceServiceComponent {

    private OauthAdminService authAdminService;
    private static Log log = LogFactory.getLog(SpProvisionPcrServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        log.debug("SpProvisionOauthServiceServiceComponent Bundle Activated");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    protected void setOauthService(OauthAdminService authAdminService) {
        SpProvisionOauthServiceDataHolder.getInstance().setOauthAdminService(authAdminService);
    }

    protected void unsetOauthService(OauthAdminService authAdminService) {
        SpProvisionOauthServiceDataHolder.getInstance().setOauthAdminService(null);
    }
}
