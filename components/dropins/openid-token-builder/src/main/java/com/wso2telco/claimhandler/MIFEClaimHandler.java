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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl.DefaultClaimHandler;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;

// TODO: Auto-generated Javadoc
/**
 * The Class MIFEClaimHandler.
 */
public class MIFEClaimHandler extends DefaultClaimHandler {

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
				localClaims.put("amr", StringUtils.join(amr, ','));
			}
			
			//this becomes null for scenario where user is already authenticated
			//ex: accessing tokens from two SPs from a same browser. user is already
			//authenticated for the first SP
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
		} catch (NullPointerException e) {
			// Possible exception during dashboard login
			// Should continue even if NPE is thrown
		}

		return localClaims;
	}
}
