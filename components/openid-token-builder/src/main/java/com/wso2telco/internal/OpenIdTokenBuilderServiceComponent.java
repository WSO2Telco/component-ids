package com.wso2telco.internal;

import com.wso2telco.core.pcrservice.PCRGeneratable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;

/**
 * @scr.component name="com.wso2telco.internal.OpenIdTokenBuilderServiceComponent" immediate="true"
 * @scr.reference name="com.wso2telco.core.pcrservice.internal.PCRServiceComponent"
 * interface="com.wso2telco.core.pcrservice.PCRGeneratable"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setPcrService"
 * unbind="unsetPcrService"
 */
public class OpenIdTokenBuilderServiceComponent {

    private static Log log = LogFactory.getLog(OpenIdTokenBuilderServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        log.debug("OpenIdTokenBuilder Bundle Activated");
    }


    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    protected void setPcrService(PCRGeneratable pcrGeneratable) {
        OpenIdTokenBuilderDataHolder.getInstance().setPcrGeneratable(pcrGeneratable);
    }

    protected void unsetPcrService(PCRGeneratable pcrGeneratable) {
        OpenIdTokenBuilderDataHolder.getInstance().setPcrGeneratable(null);
    }
}
