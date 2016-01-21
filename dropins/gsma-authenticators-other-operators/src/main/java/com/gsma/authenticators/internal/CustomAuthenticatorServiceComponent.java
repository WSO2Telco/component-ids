package com.gsma.authenticators.internal;

import com.gsma.authenticators.android.AndroidAuthenticatorOK;
import com.gsma.authenticators.android.AndroidAuthenticatorPIN;
import com.gsma.authenticators.headerenrich.HeaderEnrichmentAuthenticator;
import com.gsma.authenticators.*;
import com.gsma.authenticators.config.ConfigLoader;
import com.gsma.authenticators.config.LOAConfig;
import com.gsma.authenticators.mepin.MePinAuthenticatorFP;
import com.gsma.authenticators.mepin.MePinAuthenticatorPIN;
import com.gsma.authenticators.mepin.MePinAuthenticatorSWIPE;
import com.gsma.authenticators.mepin.MePinAuthenticatorTAP;
import com.gsma.authenticators.sms.SMSAuthenticator;
import com.gsma.authenticators.ussd.USSDAuthenticator;
import com.gsma.authenticators.ussd.USSDPinAuthenticator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.user.core.service.RealmService;

///**
// * @scr.component name="custom.authenticators.component" immediate="true"
// * @scr.reference name="user.realmservice.default"
// *                interface="org.wso2.carbon.user.core.service.RealmService"
// *                cardinality="1..1" policy="dynamic" bind="setRealmService"
// *                unbind="unsetRealmService"
// *
// */
@Component(name = "custom.authenticators.component")
@Reference(
        name = "user.realmservice.default",
        referenceInterface = org.wso2.carbon.user.core.service.RealmService.class,
        cardinality = ReferenceCardinality.MANDATORY_UNARY,
        policy = ReferencePolicy.DYNAMIC,
        bind = "setRealmService",
        unbind = "unsetRealmService"
)
public class CustomAuthenticatorServiceComponent {

    private static Log log = LogFactory.getLog(CustomAuthenticatorServiceComponent.class);

    private static RealmService realmService;

    protected void activate(ComponentContext ctxt) {

        /* Custom authenticators have to be registered first before we initialize the LOAConfig */
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new PinAuthenticator(), null);

        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new HeaderEnrichmentAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new LOACompositeAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new OpCoCompositeAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new MSISDNAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new GSMAMSISDNAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new USSDAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new USSDPinAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new SMSAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new MSSAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new MSSPinAuthenticator(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new MePinAuthenticatorTAP(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new MePinAuthenticatorPIN(), null);        
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new MePinAuthenticatorSWIPE(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new MePinAuthenticatorFP(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new AndroidAuthenticatorOK(), null);        
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new AndroidAuthenticatorPIN(), null);
        ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(),
                new WhiteListMSISDNAuthenticator(), null);


        LOAConfig config = ConfigLoader.getInstance().getLoaConfig();

        DataHolder.getInstance().setLOAConfig(config);

        DataHolder.getInstance().setMobileConnectConfig(ConfigLoader.getInstance().getMobileConnectConfig());

        if (log.isDebugEnabled()) {
            log.info("Custom Application Authenticator bundle is activated");
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Custom Application Authenticator bundle is deactivated");
        }
    }

    protected void setRealmService(RealmService realmService) {
        log.debug("Setting the Realm Service");
        CustomAuthenticatorServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        log.debug("UnSetting the Realm Service");
        CustomAuthenticatorServiceComponent.realmService = null;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

}
