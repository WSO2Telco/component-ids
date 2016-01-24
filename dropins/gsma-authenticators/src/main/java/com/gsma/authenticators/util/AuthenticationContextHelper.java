package com.gsma.authenticators.util;

import java.util.LinkedHashSet;

import javax.servlet.http.HttpServletRequest;

import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;

public class AuthenticationContextHelper {

	public static AuthenticationContext setSubject(AuthenticationContext context, String msisdn) {
		context.setSubject(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(msisdn));
		return context;
	}

	public static StepConfig setSubject(StepConfig context, String msisdn) {
		context.setAuthenticatedUser(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(msisdn));
		return context;
	}

	
	public static LinkedHashSet<?> getACRValues(HttpServletRequest request) {
		String sdk = request.getParameter(OAuthConstants.SESSION_DATA_KEY);
		CacheKey ck = new SessionDataCacheKey(sdk);
		SessionDataCacheEntry sdce = (SessionDataCacheEntry) SessionDataCache.getInstance()
				.getValueFromCache(ck);
		LinkedHashSet<?> acrValues = sdce.getoAuth2Parameters().getACRValues();
		return null;
	}
	
	public static String getUser(AuthenticatedIdPData authenticatedIdPData  ){
		return authenticatedIdPData.getUser().getAuthenticatedSubjectIdentifier();
		
	}
}
