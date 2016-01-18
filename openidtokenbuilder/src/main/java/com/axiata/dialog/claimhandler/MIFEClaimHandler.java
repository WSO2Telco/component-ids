package com.axiata.dialog.claimhandler;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl.DefaultClaimHandler;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;

public class MIFEClaimHandler extends DefaultClaimHandler {

	@Override
	public Map<String, String> handleClaimMappings(StepConfig stepConfig,
			AuthenticationContext context, Map<String, String> remoteAttributes,
			boolean isFederatedClaims) throws FrameworkException {

		Map<String, String> localClaims = super.handleClaimMappings(stepConfig, context,
				remoteAttributes, isFederatedClaims);
		// Map<String, String> localClaims = new HashMap<String, String>();

		try {
			String sdk = context.getCallerSessionKey();
			CacheKey ck = new SessionDataCacheKey(sdk);
			SessionDataCacheEntry sdce = (SessionDataCacheEntry) SessionDataCache.getInstance()
					.getValueFromCache(ck);

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
			if (null != amrValue ) {
				@SuppressWarnings("unchecked")
				List<String> amr = new ArrayList<String>(Arrays.asList(StringUtils.split(amrValue.toString(), ',')));
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
