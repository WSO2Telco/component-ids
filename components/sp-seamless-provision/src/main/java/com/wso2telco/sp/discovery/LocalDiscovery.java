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

package com.wso2telco.sp.discovery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wso2telco.core.pcrservice.PCRGeneratable;
import com.wso2telco.core.pcrservice.Returnable;
import com.wso2telco.core.pcrservice.exception.PCRException;
import com.wso2telco.core.spprovisionservice.sp.entity.AdminServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.ProvisionType;
import com.wso2telco.core.spprovisionservice.sp.entity.ServiceProviderDto;
import com.wso2telco.core.spprovisionservice.sp.entity.SpProvisionDto;
import com.wso2telco.sp.discovery.exception.DicoveryException;
import com.wso2telco.sp.internal.SpProvisionPcrDataHolder;
import com.wso2telco.sp.provision.service.ProvisioningService;
import com.wso2telco.sp.provision.service.impl.ProvisioningServiceImpl;
import com.wso2telco.sp.util.ValidationUtil;

public class LocalDiscovery extends DiscoveryLocator {

    private static Log log = LogFactory.getLog(LocalDiscovery.class);
    private ProvisioningService provisioningService;
    private SpProvisionDto spProvisionDto = null;

    public LocalDiscovery() {
        provisioningService = new ProvisioningServiceImpl();
    }

    @Override
    public ServiceProviderDto servceProviderDiscovery(DiscoveryServiceConfig discoveryServiceConfig,
                                                      DiscoveryServiceDto discoveryServiceDto, SpProvisionDto
                                                                  spProvisionDto) throws DicoveryException {
        this.spProvisionDto = spProvisionDto;
        log.info("Performing Local Discovery on EKS");
        ServiceProviderDto serviceProviderDto = null;
        boolean isAppAvailable = false;

        ValidationUtil.validateInuts(discoveryServiceDto.getSectorId(), discoveryServiceDto.getClientId());
        populateMsisdnFrom(discoveryServiceConfig, discoveryServiceDto);
        isAppAvailable = checkLocally(discoveryServiceConfig.isDiscoverOnlyLocal(), discoveryServiceConfig
                        .isPcrServiceEnabled(),
                discoveryServiceDto
                        .getSectorId(),
                discoveryServiceDto.getClientId(), discoveryServiceDto.getClientSecret());

        if (!isAppAvailable) {
            log.info("SP NOT AVAILABLE locally... Fetching remotley ...");
            serviceProviderDto = checkRemotlyDiscovery(discoveryServiceConfig, discoveryServiceDto);
        } else {
            log.info("Successful -> SP Available locally...");
            serviceProviderDto = constructDefaultSp();
        }

        return serviceProviderDto;
    }

    private ServiceProviderDto constructDefaultSp() {
        ServiceProviderDto serviceProviderDto = null;
        serviceProviderDto = new ServiceProviderDto();
        serviceProviderDto.setExistance(ProvisionType.LOCAL);
        return serviceProviderDto;
    }

    private ServiceProviderDto checkRemotlyDiscovery(DiscoveryServiceConfig discoveryServiceConfig,
                                                     DiscoveryServiceDto discoveryServiceDto) throws DicoveryException {
        ServiceProviderDto serviceProviderDto = null;
        if (getNextDiscovery() != null) {
            serviceProviderDto = getNextDiscovery().servceProviderDiscovery(discoveryServiceConfig,
                    discoveryServiceDto, spProvisionDto);
        }

        return serviceProviderDto;
    }

    private boolean checkLocally(boolean isCacheOverride, boolean isPcrServiceEnabled, String sectorId, String clientId,
                                 String clientSecret) throws DicoveryException {
        boolean isAppAvailable = false;
        if (isPcrServiceEnabled) {
            isAppAvailable = checkSpAvailabilityInmemory(sectorId, clientId);
            if (isCacheOverride && !isAppAvailable) {
                isAppAvailable = dataStoreValidation(clientId, clientSecret);
            }
        } else {

            isAppAvailable = dataStoreValidation(clientId, clientSecret);
        }
        return isAppAvailable;
    }

    private boolean dataStoreValidation(String clientId, String clientSecret) {
        boolean isAppAvailable = false;
        AdminServiceDto adminServiceDto = provisioningService.getOauthServiceProviderData(clientId,
                this.spProvisionDto);

        if (clientSecret != null && !clientSecret.isEmpty() && adminServiceDto != null
                && adminServiceDto.getOauthConsumerSecret() != null
                && !adminServiceDto.getOauthConsumerSecret().isEmpty()
                && adminServiceDto.getOauthConsumerSecret().equals(clientSecret)) {
            isAppAvailable = true;
        }
        return isAppAvailable;
    }

    private void populateMsisdnFrom(DiscoveryServiceConfig discoveryServiceConfig,
                                    DiscoveryServiceDto discoveryServiceDto) {
        if (discoveryServiceConfig != null && discoveryServiceConfig.getEksDiscoveryConfig() != null
                && discoveryServiceConfig.getEksDiscoveryConfig().getMsisdn() != null
                && !discoveryServiceConfig.getEksDiscoveryConfig().getMsisdn().isEmpty()) {
            discoveryServiceDto.setMsisdn(discoveryServiceConfig.getEksDiscoveryConfig().getMsisdn());
        }
    }

    private boolean checkSpAvailabilityInmemory(String sector, String clientId) throws DicoveryException {
        boolean isAvailable = false;
        log.info("Performing Sp availability in redis.");
        Returnable returnable = null;
        try {
            PCRGeneratable pcrGeneratable = SpProvisionPcrDataHolder.getInstance().getPcrGeneratable();
            returnable = pcrGeneratable.isAppAvailableFor(sector, clientId);
        } catch (PCRException e) {
            throw new DicoveryException("Error Occured Whicle Local Discovery Operation" + e.getMessage(), true);
        }
        isAvailable = returnable.getAvailablity();
        return isAvailable;

    }

}
