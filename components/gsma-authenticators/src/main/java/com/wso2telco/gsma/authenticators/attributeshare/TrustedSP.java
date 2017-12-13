/*******************************************************************************
 * Copyright  (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
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
package com.wso2telco.gsma.authenticators.attributeshare;

import com.wso2telco.gsma.authenticators.Constants;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.*;

/**
 * Use this class to implement TSP1 related functionality
 * which are not concluded yet.
 */
public class TrustedSP extends AbstractAttributeShare {

    @Override
    public Map<String, List<String>> getAttributeMap(AuthenticationContext context) throws SQLException, NamingException  {

        List<String> explicitScopes = new ArrayList();
        List<String> implicitScopes = new ArrayList();
        Map<String, List<String>> scopesList = new HashMap();
        List<String> longlivedScopes = new ArrayList();

        scopesList.put("explicitScopes", explicitScopes);
        scopesList.put("implicitScopes", implicitScopes);
        if (!longlivedScopes.isEmpty()) {
            context.setProperty("longlivedScopes", longlivedScopes.toString());
        }
        return scopesList;

    }

    @Override
    public Map<String, String> getAttributeShareDetails(AuthenticationContext context) throws SQLException, NamingException,AuthenticationFailedException {

        String displayScopes = "";
        String isDisplayScope = "false";
        String authenticationFlowStatus="true";
        Map<String, String> attributeShareDetails = new HashMap();
        boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);



        context.setProperty(Constants.IS_CONSENTED,Constants.YES);
        attributeShareDetails.put(Constants.IS_DISPLAYSCOPE, isDisplayScope);
        attributeShareDetails.put(Constants.DISPLAY_SCOPES, displayScopes);
        attributeShareDetails.put(Constants.IS_AUNTHENTICATION_CONTINUE,authenticationFlowStatus);


        if(isRegistering){
            createUserProfile(context);
        }

        return attributeShareDetails;
    }

}
