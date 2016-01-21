package com.axiata.mife.user.impl;


import com.axiata.mife.config.ScopeConfigs;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.utils.JSONUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCache;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheEntry;
import org.wso2.carbon.identity.oauth.cache.AuthorizationGrantCacheKey;
import com.axiata.mife.config.ConfigLoader;
import com.axiata.mife.config.DataHolder;
import com.axiata.mife.config.Scope;
import org.wso2.carbon.identity.oauth.user.UserInfoClaimRetriever;
import org.wso2.carbon.identity.oauth.user.UserInfoEndpointException;
import org.wso2.carbon.identity.oauth.user.UserInfoResponseBuilder;
import com.axiata.mife.util.ClaimUtil;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;

import java.util.*;

public class ClaimInfoMultipleScopeResponseBuilder implements UserInfoResponseBuilder {
    private static Log log = LogFactory.getLog(ClaimInfoMultipleScopeResponseBuilder.class);
    List<String> openidScopes = Arrays.asList("profile", "email", "address", "phone", "openid");

    public String getResponseString(OAuth2TokenValidationResponseDTO tokenResponse)
            throws UserInfoEndpointException,
            OAuthSystemException {

        Map<ClaimMapping, String> userAttributes = getUserAttributesFromCache(tokenResponse);
        Map<String, Object> claims = null;
        //read claimValues per scope from scope-config.xml
        DataHolder.getInstance().setScopeConfigs(ConfigLoader.getInstance().getScopeConfigs());
        ScopeConfigs scopeConfigs = DataHolder.getInstance().getScopeConfigs();

        if (userAttributes != null) {
            UserInfoClaimRetriever retriever = UserInfoEndpointConfig.getInstance().getUserInfoClaimRetriever();
            claims = retriever.getClaimsMap(userAttributes);

            boolean hasAcr = claims.containsKey("acr");
            boolean hasAmr = claims.containsKey("amr");
            if (hasAcr && hasAmr && userAttributes.size() == 2) {
                //userattributes only contains only acr and amr values
                userAttributes = null;

            }
        }
        if (userAttributes == null || userAttributes.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("User attributes not found in cache. Trying to retrieve from user store.");
            }
            try {
                claims = ClaimUtil.getClaimsFromUserStore(tokenResponse);
            } catch (Exception e) {
                throw new UserInfoEndpointException("Error while retrieving claims from user store.");
            }
        }

        String contextPath = System.getProperty("request.context.path");
        String[] requestedScopes = tokenResponse.getScope();

        if (contextPath.equals("/oauth2")) {
            //oauth2/userinfo requests should serve scopes in openid connect onl
            requestedScopes = getValidScopes(requestedScopes);
        }
        Map<String, Object> requestedClaims = getRequestedClaims(requestedScopes, scopeConfigs, claims);

        try {
            return JSONUtils.buildJSON(requestedClaims);

        } catch (JSONException e) {
            throw new UserInfoEndpointException("Error while generating the response JSON");
        }
    }

    private Map<ClaimMapping, String> getUserAttributesFromCache(OAuth2TokenValidationResponseDTO tokenResponse) {
        AuthorizationGrantCacheKey cacheKey = new AuthorizationGrantCacheKey(tokenResponse
                .getAuthorizationContextToken().getTokenString());
        AuthorizationGrantCacheEntry cacheEntry = (AuthorizationGrantCacheEntry) AuthorizationGrantCache.getInstance().getValueFromCache(cacheKey);
        if (cacheEntry == null) {
            return new HashMap<ClaimMapping, String>();
        }
        return cacheEntry.getUserAttributes();
    }

    private Map<String, Object> getRequestedClaims(String[] scopes, ScopeConfigs scopeConfigs, Map<String, Object> totalClaims) {
        Map<String, Object> requestedClaims = new HashMap<String, Object>();
        String[] attributes;
        if (scopeConfigs != null) {
            for (String scopeName : scopes) {
                for (Scope scope : scopeConfigs.getScopes().getScopeList()) {
                    if (scopeName.equals(scope.getName())) {
                        attributes = new String[scope.getClaims().getClaimValues().size()];
                        requestedClaims = addClaims(totalClaims, requestedClaims, scope.getClaims().getClaimValues().toArray(attributes));
                        break;
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Could not load user-info claims.");
            }
        }
        return requestedClaims;
    }


    private Map<String, Object> addClaims(Map<String, Object> claims, Map<String, Object> requestedClaims, String[] attributeList) {
        int attributeIndex = 0;
        while (attributeIndex < attributeList.length) {
            if (claims.get(attributeList[attributeIndex]) != null) {
                requestedClaims.put(attributeList[attributeIndex], claims.get(attributeList[attributeIndex]));
            }
            attributeIndex++;
        }
        return requestedClaims;
    }

    private String[] getValidScopes(String[] requestedScopes) {
        List<String> validScopes = new ArrayList<String>();
        for (String scope : requestedScopes) {
            if (!openidScopes.contains(scope)) {
                continue;
            }
            validScopes.add(scope);
        }
        return validScopes.toArray(new String[validScopes.size()]);
    }
}
