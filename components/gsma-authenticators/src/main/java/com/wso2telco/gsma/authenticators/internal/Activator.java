/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.wso2telco.gsma.authenticators.internal ;

import com.wso2telco.core.config.Authentication;
import com.wso2telco.core.config.AuthenticationLevel;
import com.wso2telco.core.config.AuthenticationLevels;
import com.wso2telco.core.config.Authenticator;
import com.wso2telco.core.config.Authenticators;
import com.wso2telco.core.config.ConfigLoader;
import com.wso2telco.core.config.DataHolder;
import com.wso2telco.core.config.MIFEAuthentication;
import com.wso2telco.gsma.authenticators.consent.ConsentAuthenticator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;

import com.wso2telco.gsma.authenticators.GSMAMSISDNAuthenticator;
import com.wso2telco.gsma.authenticators.LOACompositeAuthenticator;
import com.wso2telco.gsma.authenticators.MSISDNAuthenticator;
import com.wso2telco.gsma.authenticators.MSSAuthenticator;
import com.wso2telco.gsma.authenticators.MSSPinAuthenticator;
import com.wso2telco.gsma.authenticators.OpCoCompositeAuthenticator;
import com.wso2telco.gsma.authenticators.PinAuthenticator;
import com.wso2telco.gsma.authenticators.SelfAuthenticator;
import com.wso2telco.gsma.authenticators.headerenrich.HeaderEnrichmentAuthenticator;
import com.wso2telco.gsma.authenticators.sms.SMSAuthenticator;
import com.wso2telco.gsma.authenticators.ussd.USSDAuthenticator;
import com.wso2telco.gsma.authenticators.ussd.USSDPinAuthenticator;

import org.osgi.framework.BundleActivator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is one of the first bundles that start in Carbon.
 * <p/>
 * ServerConfiguration object is not available to this bundle.
 * Therefore we read properties but do not keep a reference to it.
 */
public class Activator implements BundleActivator {
    private static final Log log = LogFactory.getLog(Activator.class);

    public void startDeploy(BundleContext bundleContext) throws Exception {
    	
    	if(log.isDebugEnabled()){
    	log.debug("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH__________0001");
    	}
    	
    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new PinAuthenticator(), null);

    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new HeaderEnrichmentAuthenticator(), null);
    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new LOACompositeAuthenticator(), null);
    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new OpCoCompositeAuthenticator(), null);
    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new MSISDNAuthenticator(), null);
    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new GSMAMSISDNAuthenticator(), null);
    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new USSDAuthenticator(), null);
    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new USSDPinAuthenticator(), null);
    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new SMSAuthenticator(), null);
    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new MSSAuthenticator(), null);
    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new MSSPinAuthenticator(), null);
    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new SelfAuthenticator(), null);
    	bundleContext.registerService(ApplicationAuthenticator.class.getName(),
                 new ConsentAuthenticator(), null);

    	if (log.isDebugEnabled()) {
    	 log.debug("Authenticators registered");
    	}
    	
        AuthenticationLevels authenticationLevels = ConfigLoader.getInstance().getAuthenticationLevels();
        DataHolder.getInstance().setAuthenticationLevels(authenticationLevels);

        DataHolder.getInstance().setMobileConnectConfig(ConfigLoader.getInstance().getMobileConnectConfig());
        Map<String, MIFEAuthentication> authenticationMap = loadMIFEAuthenticatorMap(authenticationLevels);
        DataHolder.getInstance().setAuthenticationLevelMap(authenticationMap);
         if (log.isDebugEnabled()) {
         log.debug("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH__________0003");
         }
         
         if (log.isDebugEnabled()) {
             log.debug("Custom Application Authenticator bundle is activated");
         }
    }

    public String getName() {
        return "UserCore";
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }

	public void start(BundleContext context) throws Exception {
		startDeploy(context);
	}

	private Map<String, MIFEAuthentication> loadMIFEAuthenticatorMap(AuthenticationLevels authenticationLevels) {
		Map<String, MIFEAuthentication> authenticatorMap = new HashMap<>();
		List<AuthenticationLevel> authenticationLevelList = authenticationLevels.getAuthenticationLevelList();
		for (AuthenticationLevel authenticationLevel : authenticationLevelList) {
			MIFEAuthentication mifeAuthentication = new MIFEAuthentication();
			String authenticationLevelValue = authenticationLevel.getLevel();
			Authentication authentication = authenticationLevel.getAuthentication();
			Authenticators authenticators = authentication.getAuthenticators();
			String levelToFallBack = authentication.getLevelToFallback();
			List<Authenticator> authenticatorList = authenticators.getAuthenticators();
			List<MIFEAuthentication.MIFEAbstractAuthenticator> mifeAuthenticationList = new ArrayList<>();
			for (Authenticator authenticator : authenticatorList) {
				MIFEAuthentication.MIFEAbstractAuthenticator mifeAuthenticator = new MIFEAuthentication
						.MIFEAbstractAuthenticator();
				mifeAuthenticator.setAuthenticator(authenticator.getAuthenticatorName());
				mifeAuthenticator.setOnFailAction(authenticator.getOnfail());
				mifeAuthenticator.setSupportFlow(authenticator.getSupportiveFlow());
				mifeAuthenticationList.add(mifeAuthenticator);
			}
			mifeAuthentication.setLevelToFail(levelToFallBack);
			mifeAuthentication.setAuthenticatorList(mifeAuthenticationList);
			authenticatorMap.put(authenticationLevelValue, mifeAuthentication);
		}
		return  authenticatorMap;
	}

}
