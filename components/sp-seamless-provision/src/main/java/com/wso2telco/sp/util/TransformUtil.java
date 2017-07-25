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
package com.wso2telco.sp.util;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.model.MobileConnectConfig.CrValidateDiscoveryConfig;
import com.wso2telco.core.config.model.MobileConnectConfig.DiscoveryConfig;
import com.wso2telco.core.config.model.MobileConnectConfig.EksDiscoveryConfig;
import com.wso2telco.core.pcrservice.util.SectorUtil;
import com.wso2telco.core.spprovisionservice.sp.entity.AdminServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.EksDisConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.ProvisionType;
import com.wso2telco.core.spprovisionservice.sp.entity.ServiceProviderDto;
import com.wso2telco.core.spprovisionservice.sp.entity.SpProvisionConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.SpProvisionDto;

public class TransformUtil {

    public static DiscoveryServiceConfig transformDiscoveryConfig(DiscoveryConfig discoveryConfig,
            MobileConnectConfig mobileConnectConfig) {
        DiscoveryServiceConfig config = new DiscoveryServiceConfig();

        config.setEksDiscoveryConfig(transformEksDiscoveryConfig(discoveryConfig.getEksDiscoveryConfig()));
        config.setCrValidateDiscoveryConfig(
                transoformCrValidateDiscoveryConfig(discoveryConfig.getCrValidateDiscoveryConfig()));
        config.setPcrServiceEnabled(mobileConnectConfig.isPcrServiceEnabled());
        return config;
    }

    public static com.wso2telco.core.spprovisionservice.sp.entity.CrValidateDiscoveryConfig transoformCrValidateDiscoveryConfig(
            CrValidateDiscoveryConfig discoveryConf) {
        com.wso2telco.core.spprovisionservice.sp.entity.CrValidateDiscoveryConfig crValidateDiscoveryConfig = new com.wso2telco.core.spprovisionservice.sp.entity.CrValidateDiscoveryConfig();
        if (discoveryConf != null) {
            crValidateDiscoveryConfig.setServiceUrl(discoveryConf.getServiceUrl());
        }
        return crValidateDiscoveryConfig;
    }

    public static EksDisConfig transformEksDiscoveryConfig(EksDiscoveryConfig discoveryConf) {
        EksDisConfig eksDiscoveryConfig = new EksDisConfig();
        if (discoveryConf != null) {
            eksDiscoveryConfig.setRedirectUrl(discoveryConf.getRedirectUrl());
            eksDiscoveryConfig.setServiceUrl(discoveryConf.getServiceUrl());
            eksDiscoveryConfig.setMsisdn(discoveryConf.getMsisdn());
        }
        return eksDiscoveryConfig;
    }

    public static DiscoveryServiceDto transofrmDiscoveryDto(String clientId, String callbackUrl,MobileConnectConfig mobileConnectConfig) {
        DiscoveryServiceDto discoveryServiceDto = new DiscoveryServiceDto();
        discoveryServiceDto.setClientId(clientId);
        if(mobileConnectConfig != null){
            discoveryServiceDto.setAuth_clientId(mobileConnectConfig.getDiscoveryConfig().getAuth_clientId());
            discoveryServiceDto.setAuth_clientSecret(mobileConnectConfig.getDiscoveryConfig().getAuth_clientSecret());
        }
        String sectorId = null;
        if (callbackUrl != null && !callbackUrl.isEmpty()) {
            sectorId = SectorUtil.getSectorIdFromUrl(callbackUrl);
        }
        discoveryServiceDto.setSectorId(sectorId);
        return discoveryServiceDto;
    }

    public static SpProvisionDto getServiceProviderDto(ServiceProviderDto serviceProvider,
            MobileConnectConfig mConfig) {
        SpProvisionDto spProvisionDto = new SpProvisionDto();
        SpProvisionConfig spProvisionConfig = new SpProvisionConfig();
        MobileConnectConfig.Config config = mConfig.getSpProvisionConfig().getConfig();

        if (serviceProvider != null && serviceProvider.getAdminServiceDto() != null
                && serviceProvider.getAdminServiceDto().getOauthConsumerKey() != null
                && !serviceProvider.getAdminServiceDto().getOauthConsumerKey().isEmpty()) {

            String applicationName = serviceProvider.getApplicationName();
            String description = serviceProvider.getDescription();

            ServiceProviderDto serviceProviderDto = new ServiceProviderDto();
            serviceProviderDto.setApplicationName(applicationName);
            serviceProviderDto.setDescription(description);
            serviceProviderDto.setInboundAuthKey(serviceProvider.getAdminServiceDto().getOauthConsumerKey());
            if (serviceProvider.getAdminServiceDto().getOauthConsumerSecret() != null
                    && !serviceProvider.getAdminServiceDto().getOauthConsumerSecret().isEmpty()) {
                serviceProviderDto.setPropertyValue(serviceProvider.getAdminServiceDto().getOauthConsumerSecret());
            }

            serviceProviderDto.setAlwaysSendMappedLocalSubjectId(config.isAlwaysSendMappedLocalSubjectId());
            serviceProviderDto.setLocalClaimDialect(config.isLocalClaimDialect());
            serviceProviderDto.setInboundAuthType(config.getInboundAuthType());
            serviceProviderDto.setConfidential(config.isConfidential());
            serviceProviderDto.setDefaultValue(config.getDefaultValue());
            serviceProviderDto.setPropertyName(config.getPropertyName());
            serviceProviderDto.setPropertyRequired(config.isPropertyRequired());
            serviceProviderDto.setProvisioningEnabled(config.isProvisioningEnabled());
            serviceProviderDto.setProvisioningUserStore(config.getProvisioningUserStore());
            String idpRoles[] = { applicationName };
            serviceProviderDto.setIdpRoles(idpRoles);
            serviceProviderDto.setSaasApp(config.isSaasApp());
            serviceProviderDto
                    .setLocalAuthenticatorConfigsDisplayName(config.getLocalAuthenticatorConfigsDisplayName());
            serviceProviderDto.setLocalAuthenticatorConfigsEnabled(config.isLocalAuthenticatorConfigsEnabled());
            serviceProviderDto.setLocalAuthenticatorConfigsName(config.getLocalAuthenticatorConfigsName());
            serviceProviderDto.setLocalAuthenticatorConfigsValid(config.isLocalAuthenticatorConfigsValid());
            serviceProviderDto.setLocalAuthenticatorConfigsAuthenticationType(
                    config.getLocalAuthenticatorConfigsAuthenticationType());

            // Set values for spProvisionConfig

            serviceProviderDto.setAdminServiceDto(getAdminServiceDto(serviceProvider, config));
            serviceProviderDto.setExistance(ProvisionType.LOCAL);

            // Set Values for SpProvisionDTO
            spProvisionDto.setServiceProviderDto(serviceProviderDto);
            spProvisionDto.setDiscoveryServiceDto(null);
        }
        spProvisionDto.setProvisionType(ProvisionType.LOCAL);
        spProvisionDto.setSpProvisionConfig(spProvisionConfig);
        return spProvisionDto;

    }

    private static AdminServiceDto getAdminServiceDto(ServiceProviderDto serviceProvider,
            MobileConnectConfig.Config config) {

        String applicationName = serviceProvider.getApplicationName();
        String cutomerKey = serviceProvider.getAdminServiceDto().getOauthConsumerKey();
        String secretKey = serviceProvider.getAdminServiceDto().getOauthConsumerSecret();
        String callbackUrl = serviceProvider.getAdminServiceDto().getCallbackUrl();

        AdminServiceDto adminServiceDto = new AdminServiceDto();
        adminServiceDto.setApplicationName(applicationName);
        adminServiceDto.setCallbackUrl(callbackUrl);
        adminServiceDto.setOauthVersion(config.getoAuthVersion());
        adminServiceDto.setGrantTypes(config.getGrantTypes());
        adminServiceDto.setOauthConsumerKey(cutomerKey);
        adminServiceDto.setOauthConsumerSecret(secretKey);
        adminServiceDto.setPkceMandatory(config.isPkceMandatory());
        adminServiceDto.setPkceSupportPlain(config.isPkceSupportPlain());
        return adminServiceDto;

    }

}
