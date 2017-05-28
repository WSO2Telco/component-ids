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

import com.wso2telco.Util;
import com.wso2telco.core.config.DataHolder;
import com.wso2telco.core.config.MIFEAuthentication;
import com.wso2telco.core.config.model.ScopeParam;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.model.PromptData;
import com.wso2telco.gsma.authenticators.util.AdminServiceUtil;
import com.wso2telco.gsma.authenticators.util.DecryptionAES;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
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
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;

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

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 119680530347040691L;

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(LOACompositeAuthenticator.class);

    public LOACompositeAuthenticator() {
        //Use this credentials to login to IS.
    }

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax
     * .servlet.http.HttpServletRequest)
     */
    public boolean canHandle(HttpServletRequest request) {
        String acr = request.getParameter(Constants.PARAM_ACR);
        if (acr == null) {
            return false;
        } else {
            return acr != null;
        }
    }

    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#process(javax
	 * .servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application
	 * .authentication.framework.context.AuthenticationContext)
	 */
    public AuthenticatorFlowStatus process(HttpServletRequest request,
                                           HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException {
        boolean dataPublisherEnabled = DataHolder.getInstance().getMobileConnectConfig().getDataPublisher().isEnabled();

        // set context current LOA
        context.setProperty(Constants.SELECTED_LOA, request.getParameter(Constants.PARAM_ACR));

        if (dataPublisherEnabled) {
            UserStatus userStatus = DataPublisherUtil.buildUserStatusFromRequest(request, context);
            context.addParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM, userStatus);
            userStatus.setStatus(DataPublisherUtil.UserState.AUTH_INITIAL_STEP.name());
            DataPublisherUtil.publishUserStatusMetaData(userStatus);
        }
        if (!canHandle(request)) {
            return AuthenticatorFlowStatus.INCOMPLETE;
        }

        String mobileNetworkOperator = request.getParameter(Constants.OPERATOR);
        String serviceProvider = request.getParameter(Constants.CLIENT_ID);
        String msisdnHeader = request.getParameter(Constants.MSISDN_HEADER);
        String loginHintMsisdn = request.getParameter(Constants.LOGIN_HINT_MSISDN);
        Integer requestedLoa = Integer.parseInt(request.getParameter(Constants.PARAM_ACR));
        String ipAddress = request.getParameter(Constants.IP_ADDRESS);
        String transactionId = request.getParameter(Constants.TRANSACTION_ID);
        String parentScope = request.getParameter(Constants.PARENT_SCOPE);
        String apiScope = request.getParameter(Constants.API_SCOPES);

        boolean isShowTnc = Boolean.parseBoolean(request.getParameter(Constants.IS_SHOW_TNC));
        ScopeParam.msisdnMismatchResultTypes headerMismatchResult = ScopeParam.msisdnMismatchResultTypes.valueOf(
                request.getParameter(Constants.HEADER_MISMATCH_RESULT));

        ScopeParam.heFailureResults heFailureResult = ScopeParam.heFailureResults.valueOf(
                request.getParameter(Constants.HE_FAILURE_RESULT));

        context.setProperty(Constants.IS_SHOW_TNC, isShowTnc);
        context.setProperty(Constants.HEADER_MISMATCH_RESULT, headerMismatchResult);
        context.setProperty(Constants.HE_FAILURE_RESULT, heFailureResult);
        context.setProperty(Constants.IS_SHOW_TNC, isShowTnc);
        context.setProperty(Constants.ACR, requestedLoa);
        context.setProperty(Constants.OPERATOR, mobileNetworkOperator);
        context.setProperty(Constants.LOGIN_HINT_MSISDN, loginHintMsisdn);
        context.setProperty(Constants.IP_ADDRESS, ipAddress);
        context.setProperty(Constants.TRANSACTION_ID, transactionId);
        context.setProperty(Constants.PARENT_SCOPE, parentScope);
        context.setProperty(Constants.API_SCOPES, apiScope);
        context.setProperty(Constants.CLIENT_ID, serviceProvider);

        // set prompt variable default to false
        Boolean isFrorceOffnetDueToPromptParameter = false;
        PromptData promptData = null;

        String[] scopes = request.getParameter(Constants.SCOPE).split(" ");

        // RULE 1: change the flow due to prompt parameter only on HE scenarios
        if(StringUtils.isNotEmpty(msisdnHeader)) {
            promptData = DBUtils.getPromptData(scopes[0],
                    request.getParameter(Constants.PROMPT), StringUtils.isNotEmpty(loginHintMsisdn));
            // RULE 2: put on offnet flow if prompt config is offnet, otherwise go in normal HE flow
            if (promptData.getBehaviour() != null && (promptData.getBehaviour() == PromptData.behaviorTypes.OFFNET
                    || promptData.getBehaviour() == PromptData.behaviorTypes.OFFNET_TRUST_LOGIN_HINT)) {
                isFrorceOffnetDueToPromptParameter = true;
            }
        }

        String flowType = getFlowType(msisdnHeader,
                loginHintMsisdn,
                headerMismatchResult,
                isFrorceOffnetDueToPromptParameter);

        //Can we find out the MSISDN here
        String msisdnToBeDecrypted = "";
        //Following variable is for data publishing purposes.
        DataPublisherUtil.UserState msisdnStatus = DataPublisherUtil.UserState.MSISDN_SET_TO_HEADER;
        if ("onnet".equals(flowType)) {
            msisdnToBeDecrypted = msisdnHeader;
            context.setProperty(Constants.IS_OFFNET_FLOW, false);
        } else {
            //RULE: Trust msisdn header or login hint depending on scope parameter configuration or prompt param
            if (isFrorceOffnetDueToPromptParameter){
                //set msisdn to login hint
                if (promptData.getBehaviour() == PromptData.behaviorTypes.OFFNET_TRUST_LOGIN_HINT) {
                        msisdnToBeDecrypted = loginHintMsisdn;
                        msisdnStatus = DataPublisherUtil.UserState.MSISDN_SET_TO_LOGIN_HINT;
                }else{
                    // don't set msisdn
                }
            } else if (StringUtils.isNotEmpty(msisdnHeader) && StringUtils.isNotEmpty(loginHintMsisdn)) {
                //from offnet fallback due to header mismatch

                //RULE: If offnet, either we can trust sent login hint is not empty
                if (headerMismatchResult != null && headerMismatchResult.equals(ScopeParam.msisdnMismatchResultTypes
                        .OFFNET_FALLBACK_TRUST_LOGINHINT) && StringUtils.isNotEmpty(loginHintMsisdn)) {
                    msisdnToBeDecrypted = loginHintMsisdn;
                    msisdnStatus = DataPublisherUtil.UserState.MSISDN_SET_TO_LOGIN_HINT;
                }

                //RULE: If msisdn header mismatch result is fallback trust header msisdn, and header msisdn is non
                // empty, select header msisdn
                if (headerMismatchResult != null && headerMismatchResult.equals(ScopeParam.msisdnMismatchResultTypes
                        .OFFNET_FALLBACK_TRUST_HEADER) && StringUtils.isNotEmpty(msisdnHeader)) {
                    msisdnToBeDecrypted = msisdnHeader;
                    msisdnStatus = DataPublisherUtil.UserState.MSISDN_SET_TO_HEADER;
                }
            } else {
                //not from offnet fallback due to header mismatch
                msisdnToBeDecrypted = loginHintMsisdn;
                msisdnStatus = DataPublisherUtil.UserState.MSISDN_SET_TO_LOGIN_HINT;
            }
            context.setProperty(Constants.IS_OFFNET_FLOW, true);
        }

        if (StringUtils.isNotEmpty(msisdnToBeDecrypted)) {
            try {
                //This should always be the MSISDN header
                String decryptedMsisdn = DecryptionAES.decrypt(msisdnToBeDecrypted);
                context.setProperty(Constants.MSISDN, decryptedMsisdn);
                boolean isUserExists = AdminServiceUtil.isUserExists(decryptedMsisdn);
                context.setProperty(Constants.IS_REGISTERING, !isUserExists);
                DataPublisherUtil.updateAndPublishUserStatus((UserStatus) context.getProperty(
                        Constants.USER_STATUS_DATA_PUBLISHING_PARAM), msisdnStatus,
                        "MSISDN value set in LOACompositeAuthenticator",
                        decryptedMsisdn, isUserExists ? 0 : 1);

                boolean isProfileUpgrade = Util.isProfileUpgrade(decryptedMsisdn, requestedLoa, isUserExists);
                context.setProperty(Constants.IS_PROFILE_UPGRADE, isProfileUpgrade);
            } catch (Exception e) {
                log.error(e);
                throw new AuthenticationFailedException("Decryption error", e);
            }
        }


        Map<String, MIFEAuthentication> authenticationMap = configurationService.getDataHolder()
                .getAuthenticationLevelMap();
        MIFEAuthentication mifeAuthentication = authenticationMap.get(context.getProperty(Constants.SELECTED_LOA));

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
                        parameterMap.put("currentLOA", (String) context.getProperty(Constants.SELECTED_LOA));
                        parameterMap.put("fallBack", (null != fallBack) ? fallBack : "");
                        parameterMap.put("onFail", (null != onFailAction) ? onFailAction : "");
                        parameterMap.put("isLastAuthenticator", (authenticatorList
                                .indexOf(authenticator) == authenticatorList.size() - 1) ? "true" : "false");
                        authenticatorConfig.setParameterMap(parameterMap);

                        stepConfig.getAuthenticatorList().add(authenticatorConfig);
                        stepMap.put(stepOrder, stepConfig);

                        stepOrder++;
                    } else {
                        continue;
                    }
                }
            }
            if (null == fallBack) {
                break;
            }
            context.setProperty(Constants.SELECTED_LOA, fallBack);
            mifeAuthentication = authenticationMap.get(fallBack);
        }
        sequenceConfig.setStepMap(stepMap);
        context.setSequenceConfig(sequenceConfig);
        if (dataPublisherEnabled) {
            context.setProperty(Constants.AUTH_ENDPOINT_DATA_PUBLISHING_PARAM,
                    DataPublisherUtil.getAuthMapWithInitialData(request, context));
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
     * @see org.wso2.carbon.identity.application.authentication.framework
     * .ApplicationAuthenticator#getConfigurationProperties()
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
        SessionDataCacheKey sessionDataCacheKey = new SessionDataCacheKey(sdk);
        SessionDataCacheEntry sdce = (SessionDataCacheEntry) SessionDataCache.getInstance()
                .getValueFromCache(sessionDataCacheKey);
        LinkedHashSet<String> acrValues = sdce.getoAuth2Parameters().getACRValues();
        return acrValues;
    }

    /**
     * Get flow type depending on the header msisdn, login hint msisdn and header mismatch result scope parameter
     *
     * @param headerMsisdn         MSISDN provided in header
     * @param loginHintMsisdn      MSISDN provided in login hint
     * @param headerMismatchResult Header mismatch result scope parameter
     * @param offnet               Force offnet
     * @return
     */
    private String getFlowType(String headerMsisdn, String loginHintMsisdn, ScopeParam.msisdnMismatchResultTypes
            headerMismatchResult, Boolean offnet) {

        //RULE : force offnet flow
        if(offnet){
            return "offnet";
        }

        //RULE 1: check if LOA is in any form of offnet fallback
        if (ScopeParam.msisdnMismatchResultTypes.OFFNET_FALLBACK.equals(headerMismatchResult) ||
                ScopeParam.msisdnMismatchResultTypes.OFFNET_FALLBACK_TRUST_HEADER.equals(headerMismatchResult) ||
                ScopeParam.msisdnMismatchResultTypes.OFFNET_FALLBACK_TRUST_LOGINHINT.equals(headerMismatchResult)) {
            //RULE 1.1: if header MSISDN is not empty and USSD_LOGIN HINT is empty OR header MSISDN and USSD_LOGIN HINT is
            // equal, [onnet]
            if (StringUtils.isNotEmpty(headerMsisdn) && (StringUtils.isEmpty(loginHintMsisdn) || headerMsisdn.equals
                    (loginHintMsisdn))) {
                return "onnet";
            } else {
                //RULE 1.3: if header MSISDN is empty or USSD_LOGIN HINT is not empty and not equal, [offnet]
                return "offnet";
            }
        }

        //RULE 2: if header MSISDN is not empty, [onnet]
        if (StringUtils.isNotEmpty(headerMsisdn)) {
            return "onnet";
        }

        //RULE 3: otherwise, [offnet]
        return "offnet";
    }

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
