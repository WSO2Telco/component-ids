/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 * 
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.gsma.authenticators;

import com.wso2telco.core.config.MIFEAuthentication;
import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.internal.CustomAuthenticatorServiceComponent;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import org.apache.commons.lang.StringUtils;
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
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.Property;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


// TODO: Auto-generated Javadoc
/**
 * The Class LOACompositeAuthenticator.
 */
public class LOACompositeAuthenticator implements ApplicationAuthenticator,
		LocalApplicationAuthenticator {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 119680530347040691L;
	
	/** The selected loa. */
	private String selectedLOA = null;
    
    /** The log. */
    private static Log log = LogFactory.getLog(LOACompositeAuthenticator.class);
	private String isAdminUserName = null;
	private String isAdminPassword = null;


    public LOACompositeAuthenticator() {
		//Use this credentials to login to IS.
		//TODO : get this username and password from a suitable configuration file.
		isAdminUserName = "admin";
		isAdminPassword = "admin";
	}

	/** The Configuration service */
	private static ConfigurationService configurationService = new ConfigurationServiceImpl();

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax.servlet.http.HttpServletRequest)
	 */
	public boolean canHandle(HttpServletRequest request) {
		LinkedHashSet<?> acrs = this.getACRValues(request);
		
		if(acrs == null || acrs.size() == 0){
			return false;
		}else{
			selectedLOA = (String) acrs.iterator().next();
			return selectedLOA != null;
		}			
	}

    /* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
	 */
	public AuthenticatorFlowStatus process(HttpServletRequest request,
                HttpServletResponse response, AuthenticationContext context)
			throws AuthenticationFailedException, LogoutFailedException {
		if (!canHandle(request)) {
			return AuthenticatorFlowStatus.INCOMPLETE;
		}
		boolean isLogin = false;
		boolean isAuthenticated;
		String mobileNetworkOperator = request.getParameter(Constants.OPERATOR);
		String serviceProvider = request.getParameter(Constants.CLIENT_ID);
		//Unregister Customer Token
		String msisdn = request.getParameter(Constants.MSISDN_AUTHENTICATOR_FRIENDLY_NAME);
        String msisdnHeader = request.getParameter(Constants.MSISDN_HEADER);

        String tokenId = request.getParameter(Constants.TOKEN_ID);
        boolean isLoginHintMandatory = Boolean.parseBoolean(request.getParameter(Constants.IS_LOGIN_HINT_MANDATORY));
        boolean isShowTnc = Boolean.parseBoolean(request.getParameter(Constants.IS_SHOW_TNC));
        ScopeParam.msisdnMismatchResultTypes headerMismatchResult = ScopeParam.msisdnMismatchResultTypes.valueOf(
                request.getParameter(Constants.HEADER_MISMATCH_RESULT));

        context.setProperty(Constants.IS_LOGIN_HINT_MANDATORY, isLoginHintMandatory);
        context.setProperty(Constants.IS_SHOW_TNC, isShowTnc);
        context.setProperty(Constants.HEADER_MISMATCH_RESULT, headerMismatchResult);

        String flowType = getFlowType(msisdnHeader, headerMismatchResult);

        //Change authentication flow just after registration
		if (tokenId != null && msisdn != null) {
			try {
				AuthenticationData authenticationData = DBUtils.getAuthenticateData(tokenId);
				int status = authenticationData.getStatus();
				int tenantId = -1234;
				UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService().getTenantUserRealm(
						tenantId);

				if (userRealm != null) {
					UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();

                   /* String userLocked = userStoreManager.getUserClaimValue(msisdn, "http://wso2
                   .org/claims/identity/accountLocked", "default");
                    if (userLocked != null && userLocked.equalsIgnoreCase("true")) {
                        log.info("Self Authenticator authentication failed ");
                        if (log.isDebugEnabled()) {
                            log.debug("User authentication failed due to locked account.");
                        }
                        throw new AuthenticationFailedException("Self Authentication Failed");
                    }*/

					isAuthenticated = userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(msisdn));
				} else {
					throw new AuthenticationFailedException(
							"Cannot find the user realm for the given tenant: " + tenantId);
				}
				if ((status == 1) & isAuthenticated) {
					isLogin = true;
				} else {
					isLogin = false;
				}
				DBUtils.deleteAuthenticateData(tokenId);
			} catch (Exception ex) {
				log.error("Self Authentication failed while trying to authenticate", ex);
			}

		} else {
			isLogin = false;
		}

		if (isLogin) {
			SequenceConfig sequenceConfig = context.getSequenceConfig();
			Map<Integer, StepConfig> stepMap = sequenceConfig.getStepMap();

			StepConfig sc = stepMap.get(1);
			sc.setSubjectAttributeStep(false);
			sc.setSubjectIdentifierStep(false);

			AuthenticatedUser user = new AuthenticatedUser();
			//context.setSubject(user);
			sc.setAuthenticatedUser(user);


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
			authenticatorConfig.setApplicationAuthenticator(
					FrameworkUtils.getAppAuthenticatorByName("SelfAuthenticator"));

			Map<String, String> parameterMap = new HashMap<String, String>();

			//parameterMap.put("isLastAuthenticat", "true");
			authenticatorConfig.setParameterMap(parameterMap);

			stepConfig.getAuthenticatorList().add(authenticatorConfig);
			stepMap.put(stepOrder, stepConfig);

			sequenceConfig.setStepMap(stepMap);
			context.setSequenceConfig(sequenceConfig);
			context.setProperty("msisdn", msisdn);
			AuthenticationContextHelper.setSubject(context, msisdn);
		} else {
			Map<String, MIFEAuthentication> authenticationMap = configurationService.getDataHolder().getAuthenticationLevelMap();
			MIFEAuthentication mifeAuthentication = authenticationMap.get(selectedLOA);

			SequenceConfig sequenceConfig = context.getSequenceConfig();
			Map<Integer, StepConfig> stepMap = sequenceConfig.getStepMap();

			StepConfig sc = stepMap.get(1);
			sc.setSubjectAttributeStep(false);
			sc.setSubjectIdentifierStep(false);

			int stepOrder = 2;

			// This is read from mobile-connect.xml. We can globally enable/disable MNO/SP based authenticator
			// selection.
			boolean isGlobalMNOBasedAuthenticatorSelectionEnabled = isMNObasedAunthenticatorSelectionEnabled();
			boolean isGlobalSPBasedAuthenticatorSelectionEnabled = isSPBasedAuthenticatorSelectionEnabled();

			Set<String> authenticatorsAllowedForMNO = new HashSet<>();
			Set<String> authenticatorsAllowedForSP = new HashSet<>();
			if (isGlobalMNOBasedAuthenticatorSelectionEnabled) {
				try {
					authenticatorsAllowedForMNO = DBUtils.getAllowedAuthenticatorSetForMNO(mobileNetworkOperator);
				} catch (AuthenticatorException e) {
					throw new AuthenticationFailedException(e.getMessage(), e);
				}
			}

			if (isGlobalSPBasedAuthenticatorSelectionEnabled) {
				try {
					authenticatorsAllowedForSP = DBUtils.getAllowedAuthenticatorSetForSP(serviceProvider);
				} catch (AuthenticatorException e) {
					throw new AuthenticationFailedException(e.getMessage(), e);
				}
			}

			//Authenticator selection is enabled for a given MNO/SP only when MNO/SP based authenticator selection is
			//globally enabled AND there are database entries of allowed authenticators for that specific MNO/SP.
			boolean isAuthenticatorSelectionEnabledForMNO = isGlobalMNOBasedAuthenticatorSelectionEnabled
					&& !authenticatorsAllowedForMNO.isEmpty();
			boolean isAuthenticatorSelectionEnabledForSP = isGlobalSPBasedAuthenticatorSelectionEnabled
					&& !authenticatorsAllowedForSP.isEmpty();


			while (true) {
				List<MIFEAuthentication.MIFEAbstractAuthenticator> authenticatorList =
						mifeAuthentication.getAuthenticatorList();
				String fallBack = mifeAuthentication.getLevelToFail();

				for (MIFEAuthentication.MIFEAbstractAuthenticator authenticator : authenticatorList) {
					if (isAuthenticatorAllowedForMNOAndSP(isAuthenticatorSelectionEnabledForMNO,
							isAuthenticatorSelectionEnabledForSP, authenticatorsAllowedForMNO,
							authenticatorsAllowedForSP, authenticator)) {
						String onFailAction = authenticator.getOnFailAction();
						String supportiveFlow = authenticator.getSupportFlow();
						if (supportiveFlow.equals("any") || supportiveFlow.equals(flowType)) {
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

							String authenticatorName = authenticator.getAuthenticator();
							ApplicationAuthenticator applicationAuthenticator = FrameworkUtils
									.getAppAuthenticatorByName(authenticatorName);
							AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
							authenticatorConfig.setName(authenticatorName);
							authenticatorConfig.setApplicationAuthenticator(applicationAuthenticator);

							Map<String, String> parameterMap = new HashMap<String, String>();
							parameterMap.put("currentLOA", selectedLOA);
							parameterMap.put("fallBack", (null != fallBack) ? fallBack : "");
							parameterMap.put("onFail", (null != onFailAction) ? onFailAction : "");
							//						parameterMap
							//								.put("isLastAuthenticator",
							//								     (authenticatorList.indexOf(authenticator) == authenticatorList.size() - 1) ?
							//										     "true"
							//										     : "false");
							authenticatorConfig.setParameterMap(parameterMap);

							stepConfig.getAuthenticatorList().add(authenticatorConfig);
							stepMap.put(stepOrder, stepConfig);

							stepOrder++;
						} else {
							//This change is just a revert back to what previously was. Need to check and change
							if (StringUtils.isEmpty(fallBack)) {
								selectedLOA = fallBack;
								mifeAuthentication = authenticationMap.get(selectedLOA);
							}
							break;
						}
					}
				}
				if (null == fallBack) {
					break;
				}
				//This change is just a revert back to what previously was. Need to check and change
				selectedLOA = fallBack;
				mifeAuthentication = authenticationMap.get(selectedLOA);
			}
			sequenceConfig.setStepMap(stepMap);
			context.setSequenceConfig(sequenceConfig);
		}
		return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework
	 * .ApplicationAuthenticator#getContextIdentifier(javax.servlet.http.HttpServletRequest)
	 */
	public String getContextIdentifier(HttpServletRequest request) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
	 */
	public String getName() {
		return Constants.LOACA_AUTHENTICATOR_NAME;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getFriendlyName()
	 */
	public String getFriendlyName() {
		return Constants.LOACA_AUTHENTICATOR_FRIENDLY_NAME;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getClaimDialectURI()
	 */
	public String getClaimDialectURI() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getConfigurationProperties()
	 */
	public List<Property> getConfigurationProperties() {
		return new ArrayList<Property>();
	}

	/**
	 * Gets the ACR values.
	 *
	 * @param request the request
	 * @return the ACR values
	 */
	private LinkedHashSet<?> getACRValues(HttpServletRequest request) {
		String sdk = request.getParameter(OAuthConstants.SESSION_DATA_KEY);
		CacheKey ck = new SessionDataCacheKey(sdk);
		SessionDataCacheKey sessionDataCacheKey=new SessionDataCacheKey(sdk);		
		SessionDataCacheEntry sdce = (SessionDataCacheEntry) SessionDataCache.getInstance()
				.getValueFromCache(sessionDataCacheKey);
		LinkedHashSet<?> acrValues = sdce.getoAuth2Parameters().getACRValues();
		return acrValues;
	}

    private String getFlowType(String msisdn, ScopeParam.msisdnMismatchResultTypes headerMismatchResult) {
        if (!ScopeParam.msisdnMismatchResultTypes.OFFNET_FALLBACK.equals(headerMismatchResult)) {
            if (!StringUtils.isEmpty(msisdn)) {
                return "onnet";
            }
        }
        return "offnet";
    }

//    private boolean isUserProfileUpdateRequired(HttpServletRequest request, String msisdnHeader, String selectedLOA) {
//        boolean userProfileUpdateRequired = false;
//        String requestURL = request.getRequestURL().toString();
//        String requestURI = request.getRequestURI();
//        String baseURL = requestURL.substring(0, requestURL.indexOf(requestURI));
//        LoginAdminServiceClient lAdmin = null;
//        try {
//            lAdmin = new LoginAdminServiceClient(baseURL);
//            String sessionCookie = lAdmin.authenticate(isAdminUserName, isAdminPassword);
//            ClaimManagementClient claimManager = new ClaimManagementClient(baseURL, sessionCookie);
//            if (msisdnHeader != null) {
//                String registeredLOA = claimManager.getRegisteredLOA(msisdnHeader);
//                if (Integer.parseInt(registeredLOA) < Integer.parseInt(selectedLOA)) {
//                    userProfileUpdateRequired = true;
//                }
//            }
//        } catch (AxisFault axisFault) {
//            axisFault.printStackTrace();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
//            e.printStackTrace();
//        } catch (LoginAuthenticationExceptionException e) {
//            e.printStackTrace();
//        }
//        return userProfileUpdateRequired;
//    }

	private boolean isAuthenticatorAllowedForMNOAndSP(
			boolean isAuthenticatorSelectionEnabledForMNO, boolean isAuthenticatorSelectionEnabledForSP,
			Set<String> allowedAuthenticatorMapForMNO, Set<String> allowedAuthenticatorMapForSP,
			MIFEAuthentication.MIFEAbstractAuthenticator authenticator) {
		//The effective allowed authenticator set for SP is a subset of the allowed authenticator set for MNO
		//(if MNO based selection is enabled).
		//Therefore if authentication selection is enabled for MNO, a given authenticator is allowed if its in
		//the allowed authenticator set for MNO regardless whether it is an allowed authenticator for SP.
		//Also, if authenticator selection is enabled for MNO and a given authenticator is not in the set of
		//the allowed authenticators for MNO then that authenticator is not an allowed authenticator regardless
		//whether it is an allowed authenticator for SP.
		if (isAuthenticatorSelectionEnabledForMNO) {
			return allowedAuthenticatorMapForMNO.contains(authenticator.getAuthenticator());
		}
		//We'll reach here if either global MNO based authenticator selection is not enabled or
		//authenticator based selection for this specific MNO is not enabled.
		//In that case we only have to consider SP based selection.
		if (isAuthenticatorSelectionEnabledForSP) {
			return allowedAuthenticatorMapForSP.contains(authenticator.getAuthenticator());
		}
		//We'll reach here if global SP based authenticator selection is not enabled or
		//authenticator based selection for this specific SP is not enabled AND same for MNO as well
		//Therefore we can allow this authenticator.
		return true;
	}

	private boolean isMNObasedAunthenticatorSelectionEnabled() {
		return "true".equals(configurationService.getDataHolder().getMobileConnectConfig()
				.getAuthenticatorSelectionConfig().getMobileNetworkOperatorBasedSelectionEnabled());
	}

	private boolean isSPBasedAuthenticatorSelectionEnabled() {
		return "true".equals(configurationService.getDataHolder().getMobileConnectConfig()
				.getAuthenticatorSelectionConfig().getServiceProviderBasedSelectionEnabled());
	}

}
