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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.wso2telco.core.spprovisionservice.sp.entity.AdminServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.ProvisionType;
import com.wso2telco.core.spprovisionservice.sp.entity.ServiceProviderDto;
import com.wso2telco.core.spprovisionservice.sp.entity.SpProvisionDto;
import com.wso2telco.sp.discovery.exception.DicoveryException;
import com.wso2telco.sp.entity.EksDiscovery;

public class RemoteExcKeySecretDiscovery extends RemoteDiscovery {

    private static Log log = LogFactory.getLog(RemoteExcKeySecretDiscovery.class);

    @Override
    public ServiceProviderDto servceProviderDiscovery(DiscoveryServiceConfig discoveryServiceConfig,
            DiscoveryServiceDto discoveryServiceDto,SpProvisionDto spProvisionDto) throws DicoveryException {
        log.info("EKS-> Service Provider Exchange Key Discovery Call");
        String encodedBasicAuthCode = buildBasicAuthCode(discoveryServiceDto.getClientId(),
                discoveryServiceDto.getClientSecret());
        String requestMethod = HTTP_POST;
        String data = MSISDN + EQA_OPERATOR + discoveryServiceDto.getMsisdn();
        Map<String, String> requestProperties = buildRequestProperties(encodedBasicAuthCode);

        EksDiscovery eksDiscovery = new Gson()
                .fromJson(getJsonWithDiscovery(buildEndPointUrl(discoveryServiceConfig, null), requestMethod, data,
                        requestProperties), EksDiscovery.class);
        return createServiceProviderDtoBy(eksDiscovery,null);
    }

    @Override
    public String buildEndPointUrl(DiscoveryServiceConfig discoveryServiceConfig,
            DiscoveryServiceDto discoveryServiceDto) {
        log.info("EKS-> Building end point url...");
        String endPointUrl = discoveryServiceConfig.getEksDiscoveryConfig().getServiceUrl() + QES_OPERATOR
                + REDIRECT_URL + EQA_OPERATOR + discoveryServiceConfig.getEksDiscoveryConfig().getRedirectUrl();
        return endPointUrl;
    }

    @Override
    public Map<String, String> buildRequestProperties(String encodedBasicAuthCode) {
        log.info("EKS-> Building request properties...");
        Map<String, String> requestProperties = new HashMap<String, String>();
        requestProperties.put(CONTENT_TYPE_HEADER_KEY, CONTENT_TYPE_HEADER_VAL_TYPE_EKS);
        requestProperties.put(AUTHORIZATION_HEADER, BASIC + SPACE + encodedBasicAuthCode);
        return requestProperties;
    }

    @Override
    public <K,T> ServiceProviderDto createServiceProviderDtoBy(K k,T t) {
        log.info("EKS-> Create service provider dto...");
        EksDiscovery eksDiscovery =  (EksDiscovery) k;
        ServiceProviderDto serviceProviderDto = new ServiceProviderDto();
        if (eksDiscovery != null && eksDiscovery.getResponse() != null) {
            AdminServiceDto adminServiceDto = new AdminServiceDto();
            adminServiceDto.setOauthConsumerKey(eksDiscovery.getResponse().getClient_id());
            adminServiceDto.setOauthConsumerSecret(eksDiscovery.getResponse().getClient_secret());
            serviceProviderDto.setAdminServiceDto(adminServiceDto);
        }
        serviceProviderDto.setExistance(ProvisionType.REMOTE);
        return serviceProviderDto;
    }

}
