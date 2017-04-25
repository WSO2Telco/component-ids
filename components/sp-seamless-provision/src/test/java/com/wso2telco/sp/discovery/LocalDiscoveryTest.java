package com.wso2telco.sp.discovery;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.wso2telco.core.spprovisionservice.sp.entity.CrValidateDiscoveryConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.EksDisConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.ServiceProviderDto;


import junit.framework.TestCase;

public class LocalDiscoveryTest {

    private DiscoveryServiceDto discoveryServiceDto = null;
    private DiscoveryServiceConfig discoveryServiceConfig = null;

    private ServiceProviderDto serviceProviderDto = null;
    private CrValidateDiscoveryConfig crValidateDiscoveryConfig = null;
    private EksDisConfig eksDiscoveryConfig = null;

    private static final String URL = "https://localhost:9443/playground2/oauth2.jsp";
    private static final String SERVICE_URL_CR = "https://india.discover.mobileconnect.io/rv/v1/exchange/organizations/gsma/validate";
    private static final String SERVICE_URL_EKS = "https://india.discover.mobileconnect.io/gsma/v2/discovery/";
    private static final String CLIENT_ID = "56233566988556222365223";
    private static final String CLIENT_SECRET = "90992999292002992020";
    private static final String SECTOR = "localhost";
    private static final String MSISDN = "0094719531809";
    private static final String EMPTY = "";

    @Before
    public void before() {
        discoveryServiceDto = new DiscoveryServiceDto();
        discoveryServiceDto.setClientId(CLIENT_ID);
        discoveryServiceDto.setClientSecret(CLIENT_SECRET);
        discoveryServiceDto.setMsisdn(MSISDN);
        discoveryServiceDto.setSectorId(SECTOR);

        crValidateDiscoveryConfig = new CrValidateDiscoveryConfig();
        crValidateDiscoveryConfig.setServiceUrl(SERVICE_URL_CR);

        eksDiscoveryConfig = new EksDisConfig();
        eksDiscoveryConfig.setRedirectUrl(URL);
        eksDiscoveryConfig.setServiceUrl(SERVICE_URL_EKS);

        discoveryServiceConfig = new DiscoveryServiceConfig();
        discoveryServiceConfig.setCrValidateDiscoveryConfig(crValidateDiscoveryConfig);
        discoveryServiceConfig.setEksDiscoveryConfig(eksDiscoveryConfig);
        discoveryServiceConfig.setPcrServiceEnabled(false);
        discoveryServiceConfig.setDiscoverOnlyLocal(false);
        discoveryServiceConfig.setCrValidateDiscoveryConfig(crValidateDiscoveryConfig);
        discoveryServiceConfig.setEksDiscoveryConfig(eksDiscoveryConfig);

    }
/*
    @Test
    public void testServceProviderDiscovery() {

        LocalDiscovery localDiscovery = new LocalDiscovery();
        localDiscovery.setNextDiscovery(new RemoteCredentialDiscovery());

        try {
           // serviceProviderDto = localDiscovery.servceProviderDiscovery(discoveryServiceConfig, discoveryServiceDto);
        } catch (DicoveryException e) {
            serviceProviderDto = null;
        }

        Assert.assertEquals(serviceProviderDto, null);

    }
*/

}
