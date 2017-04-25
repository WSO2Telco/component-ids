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

package com.wso2telco.sp.provision.service.impl;

import com.wso2telco.core.spprovisionservice.sp.entity.AdminServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.ProvisionType;
import com.wso2telco.core.spprovisionservice.sp.entity.ServiceProviderDto;
import com.wso2telco.core.spprovisionservice.sp.entity.SpProvisionDto;
import com.wso2telco.core.spprovisionservice.sp.exception.SpProvisionServiceException;
import com.wso2telco.sp.provision.Provisioner;
import com.wso2telco.sp.provision.ServiceProviderProvisionFactory;
import com.wso2telco.sp.provision.service.ProvisioningService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProvisioningServiceImpl implements ProvisioningService {

    private static Log log = LogFactory.getLog(ProvisioningServiceImpl.class);
    private ServiceProviderProvisionFactory serviceProviderProvisionFactory = null;
    private ProvisionType provisionType = null;
    private Provisioner provisioner = null;

    @Override
    public void provisionServiceProvider(SpProvisionDto spProvisionDto) throws SpProvisionServiceException {
        serviceProviderProvisionFactory = new ServiceProviderProvisionFactory();
        provisionType = spProvisionDto.getProvisionType();
        provisioner = serviceProviderProvisionFactory.getProvisioner(provisionType);
        AdminServiceDto adminServiceDto = spProvisionDto.getServiceProviderDto().getAdminServiceDto();

        if ((provisioner != null) && (adminServiceDto.getOauthConsumerKey() != null)) {
            provisioner.provisionServiceProvider(spProvisionDto.getServiceProviderDto(),
                    spProvisionDto.getSpProvisionConfig());
        } else {
            log.error("Provisioner object doesn't contain mandatory details");
            return;
        }
    }

    @Override
    public void rebuildOauthKeys(SpProvisionDto spProvisionDto) throws SpProvisionServiceException {
        serviceProviderProvisionFactory = new ServiceProviderProvisionFactory();
        provisionType = spProvisionDto.getProvisionType();
        provisioner = serviceProviderProvisionFactory.getProvisioner(provisionType);
        AdminServiceDto adminServiceDto = spProvisionDto.getServiceProviderDto().getAdminServiceDto();

        if ((provisioner != null) && (adminServiceDto.getOauthConsumerKey() != null)) {
            provisioner.updateOauthkeys(spProvisionDto.getServiceProviderDto(), spProvisionDto.getSpProvisionConfig());
        } else {
            log.error("Provisioner object doesn't contain mandatory details");
            return;
        }
    }

    @Override
    public ServiceProviderDto getServiceProviderDetails(String applicationName, SpProvisionDto spProvisionDto) {
        serviceProviderProvisionFactory = new ServiceProviderProvisionFactory();
        provisioner = serviceProviderProvisionFactory.getProvisioner(provisionType);
        ServiceProviderDto serviceProviderDto = null;
        try {
            serviceProviderDto = provisioner.getServiceApplicationDetails(applicationName,
                    spProvisionDto.getSpProvisionConfig());
        } catch (SpProvisionServiceException e) {
            log.error("Error occurred while taking details of the Service Provider");
        } catch (NullPointerException e) {
            log.error(applicationName + " is not registered");
        }
        return serviceProviderDto;
    }

    @Override
    public AdminServiceDto getOauthServiceProviderData(String consumerKey, SpProvisionDto spProvisionDto) {
        serviceProviderProvisionFactory = new ServiceProviderProvisionFactory();
        provisionType = spProvisionDto.getProvisionType();
        provisioner = serviceProviderProvisionFactory.getProvisioner(provisionType);
        AdminServiceDto adminServiceDto = null;
        try {
            adminServiceDto = provisioner.getOauthServiceProviderData(consumerKey, spProvisionDto);
        } catch (SpProvisionServiceException e) {
            log.error("Error occurred while taking details of the Service Provider");
        } catch (NullPointerException e) {
            log.error("Application with consumer key " + consumerKey + " is not registered");
        }
        return adminServiceDto;
    }
}
