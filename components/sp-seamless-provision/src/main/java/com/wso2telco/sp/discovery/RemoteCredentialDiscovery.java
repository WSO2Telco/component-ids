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
import com.wso2telco.core.spprovisionservice.sp.entity.SpProvisionConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.SpProvisionDto;
import com.wso2telco.sp.discovery.exception.DicoveryException;
import com.wso2telco.sp.entity.CrValidateRes;

public class RemoteCredentialDiscovery extends RemoteDiscovery {

    private static Log log = LogFactory.getLog(RemoteCredentialDiscovery.class);

    @Override
    public ServiceProviderDto servceProviderDiscovery(DiscoveryServiceConfig discoveryServiceConfig,
            DiscoveryServiceDto discoveryServiceDto, SpProvisionDto spProvisionDto) throws DicoveryException {

        log.info("CR-> Service Provider Credentail Discovery Call");
        String encodedBasicAuthCode = buildBasicAuthCode(discoveryServiceDto.getAuth_clientId(),
                discoveryServiceDto.getAuth_clientSecret());
        String requestMethod = HTTP_GET;
        Map<String, String> requestProperties = buildRequestProperties(encodedBasicAuthCode);

        CrValidateRes crValidateRes = new Gson()
                .fromJson(getJsonWithDiscovery(buildEndPointUrl(discoveryServiceConfig, discoveryServiceDto),
                        requestMethod, null, requestProperties), CrValidateRes.class);
        return createServiceProviderDtoBy(crValidateRes, discoveryServiceDto);
    }

    @Override
    public Map<String, String> buildRequestProperties(String encodedBasicAuthCode) {
        log.info("CR-> Building request properties.");
        Map<String, String> requestProperties = new HashMap<String, String>();
        /*requestProperties.put(ACCEPT, CONTENT_TYPE_HEADER_VAL_TYPE_CR);*/
        requestProperties.put(AUTHORIZATION_HEADER, BASIC + SPACE + encodedBasicAuthCode);
        return requestProperties;
    }

    @Override
    public String buildEndPointUrl(DiscoveryServiceConfig discoveryServiceConfig,
            DiscoveryServiceDto discoveryServiceDto) {
        log.info("CR-> Build endpoint url");
        String endPointUrl = discoveryServiceConfig.getCrValidateDiscoveryConfig().getServiceUrl() + QES_OPERATOR
                + CLIENT_ID + EQA_OPERATOR + discoveryServiceDto.getClientId();
        if (discoveryServiceDto != null && discoveryServiceDto.getClientSecret() != null
                && !discoveryServiceDto.getClientSecret().isEmpty()) {
            endPointUrl = endPointUrl + AMP_OPERATOR + CLIENT_SECRET + EQA_OPERATOR
                    + discoveryServiceDto.getClientSecret();
        }
        return endPointUrl;
    }

    @Override
    public <K, T> ServiceProviderDto createServiceProviderDtoBy(K k, T t) {
        log.info("CR-> Create Service Provider DTO");
        CrValidateRes crValidateRes = (CrValidateRes) k;
        DiscoveryServiceDto discoveryServiceDto = (DiscoveryServiceDto) t;
        ServiceProviderDto serviceProviderDto = new ServiceProviderDto();
        if (crValidateRes != null && crValidateRes.getApplication() != null && discoveryServiceDto != null) {
            serviceProviderDto.setApplicationName(crValidateRes.getApplication().getAppName());
            AdminServiceDto adminServiceDto = new AdminServiceDto();
            adminServiceDto.setOauthConsumerKey(discoveryServiceDto.getClientId());
            adminServiceDto.setOauthConsumerSecret(discoveryServiceDto.getClientSecret());
            adminServiceDto.setCallbackUrl(crValidateRes.getApplication().getRedirectUri());
            serviceProviderDto.setDescription(crValidateRes.getApplication().getDescription());
            serviceProviderDto.setAdminServiceDto(adminServiceDto);
        }
        serviceProviderDto.setExistance(ProvisionType.REMOTE);
        return serviceProviderDto;
    }
}
