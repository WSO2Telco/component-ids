package com.axiata.dialog.util;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;

public class AuthenticationHealper {
	
	public static AuthenticatedIdPData createAuthenticatedIdPData(AuthenticationContext context){
		AuthenticatedIdPData authenticatedIdPData = new AuthenticatedIdPData();
		// store authenticated user
		authenticatedIdPData.setUser(context.getSubject());
		return authenticatedIdPData;
		
	}

}
