package com.wso2telco.gsma.authenticators.util;

import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

public class SessionCacheUtil {
    public static void addToSessionCache(AuthenticationContext context) {
        AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(context.getContextIdentifier());
        AuthenticationContextCacheEntry entry = new AuthenticationContextCacheEntry(context);
        AuthenticationContextCache.getInstance().addToCache(cacheKey, entry);
    }
}
