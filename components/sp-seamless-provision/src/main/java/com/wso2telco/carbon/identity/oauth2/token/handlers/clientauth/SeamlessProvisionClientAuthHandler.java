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
package com.wso2telco.carbon.identity.oauth2.token.handlers.clientauth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.clientauth.AbstractClientAuthHandler;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.model.MobileConnectConfig.DiscoveryConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.pcrservice.util.SectorUtil;
import com.wso2telco.core.spprovisionservice.sp.entity.AdminServiceConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.ProvisionType;
import com.wso2telco.core.spprovisionservice.sp.entity.ServiceProviderDto;
import com.wso2telco.core.spprovisionservice.sp.entity.SpProvisionDto;
import com.wso2telco.core.spprovisionservice.sp.exception.SpProvisionServiceException;
import com.wso2telco.sp.discovery.service.DiscoveryService;
import com.wso2telco.sp.discovery.service.impl.DiscoveryServiceImpl;
import com.wso2telco.sp.provision.service.ProvisioningService;
import com.wso2telco.sp.provision.service.impl.ProvisioningServiceImpl;
import com.wso2telco.sp.util.TransformUtil;

public class SeamlessProvisionClientAuthHandler extends AbstractClientAuthHandler {

    private static Log log = LogFactory.getLog(SeamlessProvisionClientAuthHandler.class);
    private static MobileConnectConfig mobileConnectConfigs = null;
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    public SeamlessProvisionClientAuthHandler() {

        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
    }

    @Override
    public boolean authenticateClient(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        seamlessProvisioning(tokReqMsgCtx);
        boolean isAuthenticated = super.authenticateClient(tokReqMsgCtx);

        if (!isAuthenticated) {
            OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();
            try {
                return OAuth2Util.authenticateClient(oAuth2AccessTokenReqDTO.getClientId(),
                        oAuth2AccessTokenReqDTO.getClientSecret());
            } catch (IdentityOAuthAdminException e) {
                throw new IdentityOAuth2Exception("Error while authenticating client", e);
            } catch (InvalidOAuthClientException e) {
                throw new IdentityOAuth2Exception("Invalid Client : " + oAuth2AccessTokenReqDTO.getClientId(), e);
            }
        } else {
            return true;
        }
    }

    private void seamlessProvisioning(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        log.info("Initiating seamless provisioning procees");
        if (mobileConnectConfigs.isSeamlessProvisioningEnabled()) {
            ServiceProviderDto serviceProviderDto = discoverServiceProvider(tokReqMsgCtx.getOauth2AccessTokenReqDTO());
            clearServiceProviderCredentials(serviceProviderDto);
            if (isServiceProviderExistRemotly(serviceProviderDto)) {
                log.info("Service Provider does not contain same credentials. Provisioning new credentials...");
                if (isInompleteServiceProviderExistLocally(
                        serviceProviderDto.getAdminServiceDto().getOauthConsumerKey())) {
                    provisionServiceProviderKeys(serviceProviderDto);
                } else {
                    log.info("Service Provider does not found LOCALLY... Auth token creation failed...");
                    throw new IdentityOAuth2Exception("Service Provider Not Found");
                }
            } else if (serviceProviderDto == null) {
                log.info("Service Provider does not found LOCALLY OR REMOTELY... Auth token creation failed...");
                throw new IdentityOAuth2Exception("Service Provider Not Found");
            }
        }
    }

    private void clearServiceProviderCredentials(ServiceProviderDto serviceProvider) {
        if (serviceProvider != null && serviceProvider.getAdminServiceDto() != null
                && serviceProvider.getAdminServiceDto().getOauthConsumerKey() != null
                && !serviceProvider.getAdminServiceDto().getOauthConsumerKey().isEmpty()
                && serviceProvider.getAdminServiceDto().getOauthConsumerSecret() != null
                && !serviceProvider.getAdminServiceDto().getOauthConsumerSecret().isEmpty()) {

            String cutomerKey = serviceProvider.getAdminServiceDto().getOauthConsumerKey();
            String secretKey = serviceProvider.getAdminServiceDto().getOauthConsumerSecret();
            serviceProvider.getAdminServiceDto().setOauthConsumerKey(cutomerKey);
            serviceProvider.getAdminServiceDto().setOauthConsumerSecret(secretKey);
        }
    }

    private boolean isInompleteServiceProviderExistLocally(String clinetId) {
        boolean isExist = false;
        ProvisioningServiceImpl provisioningService = new ProvisioningServiceImpl();
        MobileConnectConfig.Config config = mobileConnectConfigs.getSpProvisionConfig().getConfig();
        if (provisioningService.getOauthServiceProviderData(clinetId,
                getServiceProviderDto(null, mobileConnectConfigs)) != null) {
            isExist = true;
        }
        return isExist;
    }

    private boolean isServiceProviderExistRemotly(ServiceProviderDto serviceProviderDto) {
        boolean isExistRemotly = false;
        if (serviceProviderDto != null && serviceProviderDto.getExistance() != null
                && serviceProviderDto.getExistance().equals(ProvisionType.REMOTE)
                && serviceProviderDto.getAdminServiceDto() != null) {
            isExistRemotly = true;
        }
        return isExistRemotly;
    }

    private void provisionServiceProviderKeys(ServiceProviderDto serviceProvider) {

        SpProvisionDto spProvisionDto = null;
        ProvisioningService provisioningService = new ProvisioningServiceImpl();
        try {

            boolean isSeamlessProvisioningEnabled = mobileConnectConfigs.isSeamlessProvisioningEnabled();
            MobileConnectConfig.Config config = mobileConnectConfigs.getSpProvisionConfig().getConfig();

            if (isSeamlessProvisioningEnabled) {
                if (config != null) {
                    spProvisionDto = getServiceProviderDto(serviceProvider, mobileConnectConfigs);
                    provisioningService.rebuildOauthKeys(spProvisionDto);
                } else {
                    log.error("Config null");
                }
            }
        } catch (SpProvisionServiceException e) {
            log.error("Error occurred in provisioning a Service Provider " + e.getMessage());
        }
    }

    private SpProvisionDto getServiceProviderDto(ServiceProviderDto serviceProvider, MobileConnectConfig mConfig) {
        SpProvisionDto spProvisionDto = TransformUtil.getServiceProviderDto(serviceProvider, mConfig);
        spProvisionDto.getSpProvisionConfig().setAdminServiceConfig(getSpProvisionConfig(mConfig));
        return spProvisionDto;

    }

    private AdminServiceConfig getSpProvisionConfig(MobileConnectConfig config) {
        AdminServiceConfig adminServiceConfig = new AdminServiceConfig();
        adminServiceConfig.setAdminServiceUrl(config.getSpProvisionConfig().getAdminServiceUrl());
        adminServiceConfig
                .setApplicationManagementHostUrl(config.getSpProvisionConfig().getApplicationManagementHostUrl());
        adminServiceConfig.setStubAccessPassword(config.getSpProvisionConfig().getStubAccessPassword());
        adminServiceConfig.setStubAccessUserName(config.getSpProvisionConfig().getStubAccessUserName());
        return adminServiceConfig;
    }

    private ServiceProviderDto discoverServiceProvider(OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO)
            throws IdentityOAuth2Exception {
        ServiceProviderDto serviceProviderDto = null;
        DiscoveryService discoveryService = new DiscoveryServiceImpl();
        try {
            serviceProviderDto = discoveryService.servceProviderCredentialDiscovery(readDiscoveryConfigs(), getDiscoveryServiceDto(oAuth2AccessTokenReqDTO),
                    getServiceProviderDto(null, mobileConnectConfigs));
        } catch (Exception e) {
            log.error("" + e.getMessage());
        }
        return serviceProviderDto;
    }

    private DiscoveryServiceConfig readDiscoveryConfigs() {
        return TransformUtil.transformDiscoveryConfig(getDiscoveryConfig(), getMobileConnectConfig());
    }

    private DiscoveryServiceDto getDiscoveryServiceDto(OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO) {
        DiscoveryServiceDto discoveryServiceDto = new DiscoveryServiceDto();
        discoveryServiceDto.setClientId(oAuth2AccessTokenReqDTO.getClientId());
        discoveryServiceDto.setClientSecret(oAuth2AccessTokenReqDTO.getClientSecret());
        discoveryServiceDto.setAuth_clientId(mobileConnectConfigs.getDiscoveryConfig().getAuth_clientId());
        discoveryServiceDto.setAuth_clientSecret(mobileConnectConfigs.getDiscoveryConfig().getAuth_clientSecret());
        String sectorId = SectorUtil.getSectorIdFromUrl(oAuth2AccessTokenReqDTO.getCallbackURI());
        discoveryServiceDto.setSectorId(sectorId);
        return discoveryServiceDto;
    }

    private DiscoveryConfig getDiscoveryConfig() {
        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
        return mobileConnectConfigs.getDiscoveryConfig();
    }

    private MobileConnectConfig getMobileConnectConfig() {
        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
        return mobileConnectConfigs;
    }

}
