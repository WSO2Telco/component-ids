/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 * 
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

// TODO: Auto-generated Javadoc
/**
 * The Class AuthenticationContextHelper.
 */
public class AuthenticationContextHelper {

	/**
	 * Sets the subject.
	 *
	 * @param context the context
	 * @param msisdn the msisdn
	 * @return the authentication context
	 */
	public static AuthenticationContext setSubject(AuthenticationContext context, String msisdn) {
		context.setSubject(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(msisdn));
		return context;
	}

	/**
	 * Sets the subject.
	 *
	 * @param context the context
	 * @param msisdn the msisdn
	 * @return the step config
	 */
	public static StepConfig setSubject(StepConfig context, String msisdn) {
		context.setAuthenticatedUser(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(msisdn));
		return context;
	}

	
	/**
	 * Gets the ACR values.
	 *
	 * @param request the request
	 * @return the ACR values
	 */
	public static LinkedHashSet<?> getACRValues(HttpServletRequest request) {
		String sdk = request.getParameter(OAuthConstants.SESSION_DATA_KEY);
		CacheKey ck = new SessionDataCacheKey(sdk);
		SessionDataCacheEntry sdce = (SessionDataCacheEntry) SessionDataCache.getInstance()
				.getValueFromCache(ck);
		LinkedHashSet<?> acrValues = sdce.getoAuth2Parameters().getACRValues();
		return null;
	}
	
	/**
	 * Gets the user.
	 *
	 * @param authenticatedIdPData the authenticated id p data
	 * @return the user
	 */
	public static String getUser(AuthenticatedIdPData authenticatedIdPData  ){
		return authenticatedIdPData.getUser().getAuthenticatedSubjectIdentifier();
		
	}
}
