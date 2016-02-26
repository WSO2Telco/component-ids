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
package com.wso2telco.util;

import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

// TODO: Auto-generated Javadoc
 
/**
 * The Class AuthenticationHealper.
 */
public class AuthenticationHealper {

	 
	/**
	 * Creates the authenticated id p data.
	 *
	 * @param context the context
	 * @return the authenticated id p data
	 */
	public static AuthenticatedIdPData createAuthenticatedIdPData(
			AuthenticationContext context,String idpName ) {
		AuthenticatedIdPData authenticatedIdPData = new AuthenticatedIdPData();
		// store authenticated user
		authenticatedIdPData.setUser(context.getSubject());
		


		SequenceConfig sequenceConfig = context.getSequenceConfig();
		int currentStep = context.getCurrentStep();
		StepConfig stepConfig = sequenceConfig.getStepMap().get(currentStep);
		
		// store authenticated idp
		stepConfig.setAuthenticatedIdP(idpName);
		authenticatedIdPData.setIdpName(idpName);
		
		
		return authenticatedIdPData;

	}

	/**
	 * Gets the user.
	 *
	 * @param context the context
	 * @return the user
	 */
	public static String getUser(OAuthTokenReqMessageContext context) {
		return context.getAuthorizedUser().getAuthenticatedSubjectIdentifier();
	}
	
	public static String getUserName(AuthenticationContext context) {
		return context.getSubject().getAuthenticatedSubjectIdentifier();
	}
	public static void setSubject2StepConfig(AuthenticationContext context) {

		SequenceConfig sequenceConfig = context.getSequenceConfig();
		int currentStep = context.getCurrentStep();
		StepConfig stepConfig = sequenceConfig.getStepMap().get(currentStep);
		
		
		// Set the authenticated user as an object. 5.1.0 onwards
		stepConfig.setAuthenticatedUser(context.getSubject());
		
	}

	public static void setSubject2StepConfig(StepConfig stepConfig,AuthenticationContext context) {

		// Set the authenticated user as an object. 5.1.0 onwards
		stepConfig.setAuthenticatedUser(context.getSubject());
		
	}
	
	
}
