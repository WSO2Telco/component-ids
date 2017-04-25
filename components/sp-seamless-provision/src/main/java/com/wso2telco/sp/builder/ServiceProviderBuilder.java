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

package com.wso2telco.sp.builder;

import com.wso2telco.core.spprovisionservice.external.admin.service.OauthAdminService;
import com.wso2telco.core.spprovisionservice.external.admin.service.SpAppManagementService;
import com.wso2telco.core.spprovisionservice.external.admin.service.impl.OauthAdminServiceImpl;
import com.wso2telco.core.spprovisionservice.external.admin.service.impl.SpAppManagementServiceImpl;
import com.wso2telco.core.spprovisionservice.sp.entity.AdminServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.ServiceProviderDto;
import com.wso2telco.core.spprovisionservice.sp.entity.SpProvisionConfig;
import com.wso2telco.core.spprovisionservice.sp.exception.SpProvisionServiceException;
import com.wso2telco.sp.internal.SpProvisionAppManagementServiceDataHolder;
import com.wso2telco.sp.internal.SpProvisionOauthServiceDataHolder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;

public class ServiceProviderBuilder {

    private OauthAdminService adminService = null;
    private SpAppManagementService spAppManagementService = null;
    private static Log log = LogFactory.getLog(ServiceProviderBuilder.class);
    private SpProvisionConfig spProvisionConfig = null;

    public ServiceProviderBuilder(SpProvisionConfig spProvisionConfig) {
        this.spProvisionConfig = spProvisionConfig;
    }

    public void reBuildOauthKey(ServiceProviderDto serviceProviderDto) throws SpProvisionServiceException {
        if (serviceProviderDto != null) {
            reBuildOauthDataStructure(serviceProviderDto.getAdminServiceDto());
        } else {
            log.error("Service Provider details are empty");
        }
    }

    public void buildServiceProvider(ServiceProviderDto serviceProviderDto) throws SpProvisionServiceException {
        if (serviceProviderDto != null) {

            buildOauthDataStructure(serviceProviderDto.getAdminServiceDto());
            buildSpApplicationDataStructure(serviceProviderDto);
        } else {
            log.error("Service Provider details are empty");
        }
    }

    public void reBuildOauthDataStructure(AdminServiceDto adminServiceDto) throws SpProvisionServiceException {

        adminService = SpProvisionOauthServiceDataHolder.getInstance().getOauthAdminService();
        adminService.initialize(this.spProvisionConfig);

        if (adminServiceDto != null) {
            try {
                OAuthConsumerAppDTO authConsumerAppDTO = adminService.getOAuthApplicationData(adminServiceDto);
                if (authConsumerAppDTO != null) {
                    adminService.rebuildOAuthApplicationData(adminServiceDto, authConsumerAppDTO);
                }
            } catch (SpProvisionServiceException e) {
                throw new SpProvisionServiceException(e.getMessage());
            }
        } else {
            log.error("oAuth data object doesn't have data for the registration");
        }
    }

    public void buildOauthDataStructure(AdminServiceDto adminServiceDto) throws SpProvisionServiceException {

        adminService = SpProvisionOauthServiceDataHolder.getInstance().getOauthAdminService();
        adminService.initialize(this.spProvisionConfig);

        if (adminServiceDto != null) {
            try {
                OAuthConsumerAppDTO authConsumerAppDTO = adminService.getOAuthApplicationData(adminServiceDto);
                if (authConsumerAppDTO == null
                        && !adminService.isCredentailsEquals(adminServiceDto, authConsumerAppDTO)) {
                    adminService.registerOAuthApplicationData(adminServiceDto);
                }
            } catch (SpProvisionServiceException e) {
                throw new SpProvisionServiceException(e.getMessage());
            }
        } else {
            log.error("oAuth data object doesn't have data for the registration");
        }
    }

    public ServiceProvider buildSpApplicationDataStructure(ServiceProviderDto serviceProviderDto)
            throws SpProvisionServiceException {

        spAppManagementService = SpProvisionAppManagementServiceDataHolder.getInstance().getAppManagementService();
        spAppManagementService.initialize(this.spProvisionConfig);
        String applicationName = serviceProviderDto.getApplicationName();
        ServiceProvider serviceProvider = null;

        if (serviceProviderDto != null) {

            spAppManagementService.createSpApplication(serviceProviderDto);
            serviceProvider = spAppManagementService.getSpApplicationData(applicationName);

            if (serviceProvider != null) {
                spAppManagementService.updateSpApplication(serviceProviderDto);
            }
            serviceProvider = spAppManagementService.getSpApplicationData(applicationName);
        }

        return serviceProvider;
    }

    public void reBuildOauthDataStructure(String oldConsumerKey, AdminServiceDto adminServiceDto)
            throws SpProvisionServiceException {

        if (adminServiceDto != null) {
            adminService = SpProvisionOauthServiceDataHolder.getInstance().getOauthAdminService();
            adminService.initialize(this.spProvisionConfig);

            adminService.removeOAuthApplicationData(oldConsumerKey);
            adminService.registerOAuthApplicationData(adminServiceDto);
        }

    }

    public void revokeSpApplication(String oldConsumerKey, String applicationName) throws SpProvisionServiceException {

        adminService = SpProvisionOauthServiceDataHolder.getInstance().getOauthAdminService();
        adminService.initialize(this.spProvisionConfig);

        spAppManagementService = SpProvisionAppManagementServiceDataHolder.getInstance().getAppManagementService();
        spAppManagementService.initialize(this.spProvisionConfig);

        if (spAppManagementService.getSpApplicationData(applicationName) != null) {
            spAppManagementService.deleteSpApplication(applicationName);
        } else {
            log.error("Given service Provider is not available");
        }

        adminService.removeOAuthApplicationData(oldConsumerKey);

    }

    public ServiceProviderDto getServiceProviderDetails(String applicationName) throws SpProvisionServiceException {

        ServiceProviderDto serviceProviderDto;
        spAppManagementService = SpProvisionAppManagementServiceDataHolder.getInstance().getAppManagementService();
        spAppManagementService.initialize(this.spProvisionConfig);
        serviceProviderDto = spAppManagementService.getServiceProviderDetails(applicationName);
        return serviceProviderDto;
    }

    public AdminServiceDto getOauthServiceProviderData(String consumerKey) throws SpProvisionServiceException {

        AdminServiceDto adminServiceDto;
        adminService = SpProvisionOauthServiceDataHolder.getInstance().getOauthAdminService();
        adminService.initialize(this.spProvisionConfig);
        adminServiceDto = adminService.getOauthServiceProviderData(consumerKey);
        return adminServiceDto;
    }
}
