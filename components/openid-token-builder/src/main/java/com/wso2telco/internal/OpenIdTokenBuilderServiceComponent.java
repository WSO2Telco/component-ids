package com.wso2telco.internal;

import com.wso2telco.core.pcrservice.PCRGeneratable;
import com.wso2telco.core.pcrservice.persistable.UUIDPCRGenarator;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.apache.felix.scr.annotations.*;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Component(name = "com.wso2telco.internal.OpenIdTokenBuilderServiceComponent", immediate = true)
@Reference(
        name = "com.wso2telco.core.pcrservice.internal.PCRServiceComponent",
        referenceInterface = com.wso2telco.core.pcrservice.PCRGeneratable.class,
        cardinality = ReferenceCardinality.MANDATORY_UNARY,
        policy = ReferencePolicy.DYNAMIC,
        bind = "setPcrService",
        unbind = "unsetPcrService"
)
public class OpenIdTokenBuilderServiceComponent {

    private static Log log = LogFactory.getLog(OpenIdTokenBuilderServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        log.debug("OpenIdTokenBuilder Bundle Activated");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        // do nothing
    }
    
    protected void setPcrService(PCRGeneratable pcrGeneratable) {
        OpenIdTokenBuilderDataHolder.getInstance().setPcrGeneratable(pcrGeneratable);
    }

    protected void unsetPcrService(PCRGeneratable pcrGeneratable) {
        OpenIdTokenBuilderDataHolder.getInstance().setPcrGeneratable(null);
    }
}
