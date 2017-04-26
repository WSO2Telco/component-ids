package com.wso2telco.sp.discovery;

import java.util.Map;

import org.junit.*;

import com.wso2telco.core.spprovisionservice.sp.entity.CrValidateDiscoveryConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceConfig;
import com.wso2telco.core.spprovisionservice.sp.entity.DiscoveryServiceDto;
import com.wso2telco.core.spprovisionservice.sp.entity.ServiceProviderDto;
import com.wso2telco.sp.entity.Application;
import com.wso2telco.sp.entity.CrValidateRes;

import junit.framework.TestCase;

public class RemoteCredentialDiscoveryTest extends TestCase {

    protected static String ACCEPT = "Accept";
    protected static String CONTENT_TYPE_HEADER_KEY = "Content-Type";
    protected static String CONTENT_TYPE_HEADER_VAL_TYPE_EKS = "application/x-www-form-urlencoded";
    protected static String CONTENT_TYPE_HEADER_VAL_TYPE_CR = "application/json";
    protected static String AUTHORIZATION_HEADER = "Authorization";
    protected static String HTTP_POST = "POST";
    protected static String MSISDN = "msisdn";
    protected static String REDIRECT_URL = "Redirect_URL";
    protected static String CLIENT_ID = "client_id";
    protected static String CLIENT_SECRET = "client_secret";
    protected static String BASIC = "Basic";
    protected static String QES_OPERATOR = "?";
    protected static String EQA_OPERATOR = "=";
    protected static String AMP_OPERATOR = "&";
    protected static String SPACE = " ";
    protected static String COLON = ":";
    protected static String NEW_LINE = "\n";

    private static String APP_NAME = "APPNAME";
    private static String APP_DESCRIPTION = "APP-DESCRIPTION";

    protected static String BASIC_AUTH_CODE = "42552362-522633-5522333-522141525252";

    private RemoteCredentialDiscovery remoteDiscovery = null;
    private DiscoveryServiceConfig discoveryServiceConfig;
    private DiscoveryServiceDto discoveryServiceDto;
    private Application application;

    public void init() {
        remoteDiscovery = new RemoteCredentialDiscovery();
        discoveryServiceConfig = new DiscoveryServiceConfig();
        CrValidateDiscoveryConfig crValidateDiscoveryConfig = new CrValidateDiscoveryConfig();
        discoveryServiceConfig.setCrValidateDiscoveryConfig(crValidateDiscoveryConfig);
        discoveryServiceDto = new DiscoveryServiceDto();
    }

    @Test
    public void testBuildEndPointUrl() {
        String url = remoteDiscovery.buildEndPointUrl(discoveryServiceConfig, discoveryServiceDto);
        Assert.assertEquals(url, buildEndPointUrl(discoveryServiceConfig, discoveryServiceDto));
    }

    @Test
    public void testBuildRequestProperties() {
        Map<String, String> reqProperies = remoteDiscovery.buildRequestProperties(BASIC_AUTH_CODE);
        Assert.assertNotEquals(reqProperies, null);
        Assert.assertEquals(CONTENT_TYPE_HEADER_VAL_TYPE_CR, reqProperies.get(ACCEPT));
        Assert.assertEquals(BASIC + SPACE + BASIC_AUTH_CODE, reqProperies.get(AUTHORIZATION_HEADER));
    }

    @Test
    public void testCreateServiceProviderDto() {
        CrValidateRes crValidateRes = new CrValidateRes();
        application = new Application();

        application.setAppName(APP_NAME);
        application.setDescription(APP_DESCRIPTION);
        crValidateRes.setApplication(application);
        DiscoveryServiceDto discoveryServiceDto = new DiscoveryServiceDto();
        crValidateRes.setApplication(application);
        ServiceProviderDto serviceProviderDto = remoteDiscovery.createServiceProviderDtoBy(crValidateRes,
                discoveryServiceDto);
        Assert.assertEquals(APP_DESCRIPTION,serviceProviderDto.getDescription());
    }

    public RemoteCredentialDiscoveryTest(String name) {
        super(name);
        init();
    }

    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(RemoteCredentialDiscoveryTest.class);
    }

    public String buildEndPointUrl(DiscoveryServiceConfig discoveryServiceConfig,
            DiscoveryServiceDto discoveryServiceDto) {
        String endPointUrl = discoveryServiceConfig.getCrValidateDiscoveryConfig().getServiceUrl() + QES_OPERATOR
                + CLIENT_ID + EQA_OPERATOR + discoveryServiceDto.getClientId() + AMP_OPERATOR + CLIENT_SECRET
                + EQA_OPERATOR + discoveryServiceDto.getClientSecret();
        return endPointUrl;
    }

}
