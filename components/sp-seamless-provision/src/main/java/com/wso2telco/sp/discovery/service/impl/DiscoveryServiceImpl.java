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

package com.wso2telco.sp.discovery.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.ServiceProviderDto;
import com.wso2telco.core.spprovisionservice.sp.entity.SpProvisionDto;
import com.wso2telco.sp.discovery.DiscoveryLocator;
import com.wso2telco.sp.discovery.LocalDiscovery;
import com.wso2telco.sp.discovery.RemoteCredentialDiscovery;
import com.wso2telco.sp.discovery.RemoteDiscovery;
import com.wso2telco.sp.discovery.RemoteExcKeySecretDiscovery;
import com.wso2telco.sp.discovery.exception.DicoveryException;
import com.wso2telco.sp.discovery.service.DiscoveryService;

public class DiscoveryServiceImpl implements DiscoveryService {

    private static Log log = LogFactory.getLog(DiscoveryServiceImpl.class);
    private DiscoveryLocator discoverSp = null;

    @Override
    public ServiceProviderDto servceProviderCredentialDiscovery(DiscoveryServiceConfig discoveryServiceConfig,
            DiscoveryServiceDto discoveryServiceDto,SpProvisionDto spProvisionDto) {
        ServiceProviderDto serviceProviderDto = null;
        try {
            log.info("Performing Credentials discovery for CR.");
            discoverSp = new LocalDiscovery();
            discoverSp.setNextDiscovery(new RemoteCredentialDiscovery());
            serviceProviderDto = discoverSp.servceProviderDiscovery(discoveryServiceConfig, discoveryServiceDto,spProvisionDto);
        } catch (DicoveryException e) {
            if (e.isSystemError()) {
                log.error("Error Occured While Trying To Fetch Discovery Call :: Message = " + e.getMessage());
            } else {
                log.info("Service Provider Does Not Exist." + e.getMessage());
            }
        }
        return serviceProviderDto;
    }

    @Override
    public ServiceProviderDto servceProviderEksDiscovery(DiscoveryServiceConfig discoveryServiceConfig,
            DiscoveryServiceDto discoveryServiceDto,SpProvisionDto spProvisionDto) {
        ServiceProviderDto serviceProviderDto = null;
        try {
            log.info("Performing Credentials discovery for EKS.");
            discoverSp = new LocalDiscovery();
            discoverSp.setNextDiscovery(new RemoteExcKeySecretDiscovery());
            serviceProviderDto = discoverSp.servceProviderDiscovery(discoveryServiceConfig, discoveryServiceDto,spProvisionDto);
        } catch (DicoveryException e) {
            if (e.isSystemError()) {
                log.error("Error Occured While Trying To Fetch Discovery Call " + e.getMessage());
            } else {
                log.info("Service Provider Does Not Exist." + e.getMessage());
            }
        }
        return serviceProviderDto;
    }

}
