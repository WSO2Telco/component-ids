/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 * 
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.claimhandler;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.openidconnect.as.messages.IDToken;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl.DefaultClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class MIFEClaimHandler.
 */
public class MIFEClaimHandler extends DefaultClaimHandler {

	private static Log log = LogFactory.getLog(MIFEClaimHandler.class);
    private static boolean DEBUG = log.isDebugEnabled();

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl.DefaultClaimHandler#handleClaimMappings(org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext, java.util.Map, boolean)
	 */
	@Override
	public Map<String, String> handleClaimMappings(StepConfig stepConfig,
			AuthenticationContext context, Map<String, String> remoteAttributes,
			boolean isFederatedClaims) throws FrameworkException {

		Map<String, String> localClaims = super.handleClaimMappings(stepConfig, context,
				remoteAttributes, isFederatedClaims);
		// Map<String, String> localClaims = new HashMap<String, String>();

		try {
			String sdk = context.getCallerSessionKey();
			SessionDataCacheKey sessionDataCacheKey=new SessionDataCacheKey(sdk);
			SessionDataCacheEntry sdce = (SessionDataCacheEntry) SessionDataCache.getInstance()
					.getValueFromCache(sessionDataCacheKey);

			// Add acr to claims map
			LinkedHashSet<?> acrValues = sdce.getoAuth2Parameters().getACRValues();
			Iterator<?> it = acrValues.iterator();

			if (null != acrValues && it.hasNext()) {
				String loaLevel;
				if (null != (loaLevel = (String) it.next())) {
					localClaims.put("acr", loaLevel);
				}
			}

			// Add amr to claims map
			Object amrValue = context.getProperty("amr");
			if (null != amrValue && amrValue instanceof ArrayList<?>) {
				@SuppressWarnings("unchecked")
				List<String> amr = (ArrayList<String>) amrValue;
				if(!amr.isEmpty()) {
					localClaims.put("amr", amr.get(amr.size() - 1));
				}else{
					localClaims.put("amr", "");
				}
			}
			
			//this becomes null for scenario where user is already authenticated
			//ex: accessing tokens from two SPs from a same browser. user is already
			//authenticated for the first SP
			// todo: confirm this scenario
			if(null == amrValue){
				Map<Integer, StepConfig> stepMap = context.getSequenceConfig().getStepMap();
				List<String> amr = new ArrayList<String>();
				for (Map.Entry<Integer, StepConfig> entry : stepMap.entrySet()){
					StepConfig stpConf = entry.getValue();
					if(stpConf != null && stepConfig.getAuthenticatedAutenticator().getName() != null) {
						amr.add(stepConfig.getAuthenticatedAutenticator().getName());
					}					
				}
				localClaims.put("amr", StringUtils.join(amr, ','));
			}
			
			
			// Adding nonce to claim map
            String nonce = sdce.getoAuth2Parameters().getNonce();

            if (DEBUG) {
                log.debug("nonce values from  getoAuth2Parameters " + nonce);
            }

            AuthenticationRequest authRequest = context.getAuthenticationRequest();

            if (authRequest.getRequestQueryParams().containsKey(IDToken.NONCE)) {
                if (authRequest.getRequestQueryParams().get(IDToken.NONCE).length != 0) {
                    nonce = authRequest.getRequestQueryParams().get(IDToken.NONCE)[0];
                }
            }
           
            if (authRequest.getRequestQueryParams().get("state").length != 0){
                String state = authRequest.getRequestQueryParams().get("state")[0];
                
                localClaims.put("state", state);
                if(DEBUG) {
                    log.debug("state : " + state);
                }
                
            } else {
                if(DEBUG) {
                    log.debug("state is empty");
                }
            }
            
            
            
            
            if (DEBUG) {
                log.debug(" nonce values from  getoAuth2Parameters " + nonce);
            }
            if (null != nonce) {
                localClaims.put(IDToken.NONCE, nonce);
            }

		} catch (NullPointerException e) {
			// Possible exception during dashboard login
			// Should continue even if NPE is thrown
		}

		return localClaims;
	}
}
