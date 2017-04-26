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

package com.wso2telco.sp.provision;

import com.wso2telco.core.spprovisionservice.sp.entity.AdminServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.ServiceProviderDto;
import com.wso2telco.core.spprovisionservice.sp.entity.SpProvisionConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.SpProvisionDto;
import com.wso2telco.core.spprovisionservice.sp.exception.SpProvisionServiceException;
import com.wso2telco.sp.builder.ServiceProviderBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalProvisioner extends Provisioner {

    private ServiceProviderBuilder serviceProviderBuilder = null;
    private static Log log = LogFactory.getLog(LocalProvisioner.class);

    @Override
    public void provisionServiceProvider(ServiceProviderDto serviceProviderDto, SpProvisionConfig spProvisionConfig)
            throws SpProvisionServiceException {

        serviceProviderBuilder = new ServiceProviderBuilder(spProvisionConfig);
        serviceProviderBuilder.buildServiceProvider(serviceProviderDto);
    }

    @Override
    public void updateOauthkeys(ServiceProviderDto serviceProviderDto, SpProvisionConfig spProvisionConfig)
            throws SpProvisionServiceException {
        serviceProviderBuilder = new ServiceProviderBuilder(spProvisionConfig);
        serviceProviderBuilder.reBuildOauthKey(serviceProviderDto);

    }

    public ServiceProviderDto getServiceApplicationDetails(String applicationName, SpProvisionConfig spProvisionConfig)
            throws SpProvisionServiceException {
        serviceProviderBuilder = new ServiceProviderBuilder(spProvisionConfig);
        return serviceProviderBuilder.getServiceProviderDetails(applicationName);
    }

    public AdminServiceDto getOauthServiceProviderData(String consumerKey, SpProvisionDto dto)
            throws SpProvisionServiceException {
        serviceProviderBuilder = new ServiceProviderBuilder(dto.getSpProvisionConfig());
        return serviceProviderBuilder.getOauthServiceProviderData(consumerKey);
    }

}
