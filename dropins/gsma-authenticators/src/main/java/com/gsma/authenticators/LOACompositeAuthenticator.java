package com.gsma.authenticators;

import com.gsma.authenticators.config.LOA;
import com.gsma.authenticators.config.LOAConfig;
import com.gsma.authenticators.internal.CustomAuthenticatorServiceComponent;
import com.gsma.authenticators.config.LOA.MIFEAbstractAuthenticator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Routes to LOA based authenticators
 */
public class LOACompositeAuthenticator implements ApplicationAuthenticator,
		LocalApplicationAuthenticator {

	private static final long serialVersionUID = 119680530347040691L;
	private String selectedLOA = null;
    private static Log log = LogFactory.getLog(LOACompositeAuthenticator.class);

	public boolean canHandle(HttpServletRequest request) {
		LinkedHashSet<?> acrs = this.getACRValues(request);
		selectedLOA = (String) acrs.iterator().next();
		return acrs != null && acrs.size() > 0 && selectedLOA != null;
	}

	public AuthenticatorFlowStatus process(HttpServletRequest request,
			HttpServletResponse response, AuthenticationContext context)
			throws AuthenticationFailedException, LogoutFailedException {
		if (!canHandle(request)) {
			return AuthenticatorFlowStatus.INCOMPLETE;
		}
       boolean isLogin=false;
       boolean isAuthenticated;
        //Unregister Customer Token

       String msisdn=request.getParameter("msisdn");
       String tokenId=tokenId=request.getParameter("tokenid");
        //Change authentication flow just after registration
        if (tokenId != null && msisdn != null) {
            try {
                AuthenticationData authenticationData = DBUtils.getAuthenticateData(tokenId);
                int status = authenticationData.getStatus();
                int tenantId = IdentityUtil.getTenantIdOFUser(msisdn);
                UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService().getTenantUserRealm(tenantId);

                if (userRealm != null) {
                    UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();

                    String userLocked = userStoreManager.getUserClaimValue(msisdn, "http://wso2.org/claims/identity/accountLocked", "default");
                    if (userLocked != null && userLocked.equalsIgnoreCase("true")) {
                        log.info("Self Authenticator authentication failed ");
                        if (log.isDebugEnabled()) {
                            log.debug("User authentication failed due to locked account.");
                        }
                        throw new AuthenticationFailedException("Self Authentication Failed");
                    }

                    isAuthenticated = userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(msisdn));
                } else {
                    throw new AuthenticationFailedException("Cannot find the user realm for the given tenant: " + tenantId);
                }
                if ((status == 1) & isAuthenticated) {
                    isLogin = true;
                }else{
                    isLogin = false;
                }
                DBUtils.deleteAuthenticateData(tokenId);
            } catch (Exception ex) {
                log.error("Self Authentication failed while trying to authenticate", ex);
            }

        } else {
            isLogin = false;
        }

        if(isLogin) {
            SequenceConfig sequenceConfig = context.getSequenceConfig();
            Map<Integer, StepConfig> stepMap = sequenceConfig.getStepMap();

            StepConfig sc = stepMap.get(1);
            sc.setSubjectAttributeStep(false);
            sc.setSubjectIdentifierStep(false);
            sc.setAuthenticatedUser(msisdn);

            int stepOrder = 2;

            StepConfig stepConfig = new StepConfig();
            stepConfig.setOrder(stepOrder);
            stepConfig.setSubjectAttributeStep(true);
            stepConfig.setSubjectIdentifierStep(true);

            List<AuthenticatorConfig> authenticatorConfigs = stepConfig.getAuthenticatorList();
            if (authenticatorConfigs == null) {
                authenticatorConfigs = new ArrayList<AuthenticatorConfig>();
                stepConfig.setAuthenticatorList(authenticatorConfigs);
            }

            AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
            authenticatorConfig.setName("SelfAuthenticator");
            authenticatorConfig.setApplicationAuthenticator(FrameworkUtils.getAppAuthenticatorByName("SelfAuthenticator"));

            Map<String, String> parameterMap = new HashMap<String, String>();

            parameterMap.put("isLastAuthenticat", "true");
            authenticatorConfig.setParameterMap(parameterMap);

            stepConfig.getAuthenticatorList().add(authenticatorConfig);
            stepMap.put(stepOrder, stepConfig);

            sequenceConfig.setStepMap(stepMap);
            context.setSequenceConfig(sequenceConfig);
            context.setProperty("msisdn", msisdn);
            context.setSubject(msisdn);


        }else{
            LOAConfig config = DataHolder.getInstance().getLOAConfig();
            LOA loa = config.getLOA(selectedLOA);
            if (loa.getAuthenticators() == null) {
                config.init();
            }
            SequenceConfig sequenceConfig = context.getSequenceConfig();
            Map<Integer, StepConfig> stepMap = sequenceConfig.getStepMap();

            StepConfig sc = stepMap.get(1);
            sc.setSubjectAttributeStep(false);
            sc.setSubjectIdentifierStep(false);

            int stepOrder = 2;

            while (true) {
                List<MIFEAbstractAuthenticator> authenticators = loa.getAuthenticators();
                String fallBack = loa.getAuthentication().getFallbackLevel();

                for (MIFEAbstractAuthenticator authenticator : authenticators) {
                    StepConfig stepConfig = new StepConfig();
                    stepConfig.setOrder(stepOrder);
                    if (stepOrder == 2) {
                        stepConfig.setSubjectAttributeStep(true);
                        stepConfig.setSubjectIdentifierStep(true);
                    }

                    List<AuthenticatorConfig> authenticatorConfigs = stepConfig.getAuthenticatorList();
                    if (authenticatorConfigs == null) {
                        authenticatorConfigs = new ArrayList<AuthenticatorConfig>();
                        stepConfig.setAuthenticatorList(authenticatorConfigs);
                    }

                    AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
                    authenticatorConfig.setName(authenticator.getAuthenticator().getName());
                    authenticatorConfig.setApplicationAuthenticator(authenticator.getAuthenticator());

                    String onFail = authenticator.getOnFailAction();

                    Map<String, String> parameterMap = new HashMap<String, String>();
                    parameterMap.put("currentLOA", loa.getLevel());
                    parameterMap.put("fallBack", (null != fallBack) ? fallBack : "");
                    parameterMap.put("onFail", (null != onFail) ? onFail : "");
                    parameterMap
                            .put("isLastAuthenticator",
                                    (authenticators.indexOf(authenticator) == authenticators.size() - 1) ? "true"
                                            : "false");
                    authenticatorConfig.setParameterMap(parameterMap);

                    stepConfig.getAuthenticatorList().add(authenticatorConfig);
                    stepMap.put(stepOrder, stepConfig);
                    stepOrder++;
                }

                // increment LOA to fallBack level
                if (null == fallBack) {
                    break;
                }
                loa = config.getLOA(fallBack);
            }

            sequenceConfig.setStepMap(stepMap);
            context.setSequenceConfig(sequenceConfig);

        }
         return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
	}

	public String getContextIdentifier(HttpServletRequest request) {
		return null;
	}

	public String getName() {
		return Constants.LOACA_AUTHENTICATOR_NAME;
	}

	public String getFriendlyName() {
		return Constants.LOACA_AUTHENTICATOR_FRIENDLY_NAME;
	}

	public String getClaimDialectURI() {
		return null;
	}

	public List<Property> getConfigurationProperties() {
		return new ArrayList<Property>();
	}

	private LinkedHashSet<?> getACRValues(HttpServletRequest request) {
		String sdk = request.getParameter(OAuthConstants.SESSION_DATA_KEY);
		CacheKey ck = new SessionDataCacheKey(sdk);
		SessionDataCacheEntry sdce = (SessionDataCacheEntry) SessionDataCache.getInstance()
				.getValueFromCache(ck);
		LinkedHashSet<?> acrValues = sdce.getoAuth2Parameters().getACRValues();
		return acrValues;
	}

}