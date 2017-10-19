package com.wso2telco;

import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.util.BasicFutureCallback;
import com.wso2telco.gsma.manager.client.ClaimManagementClient;
import com.wso2telco.gsma.manager.client.LoginAdminServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class Util {
    private static Log log = LogFactory.getLog(Util.class);

    private static ConfigurationService configurationService = new ConfigurationServiceImpl();

    /**
     * Asynchronously call the REST endpoint
     *
     * @param postRequest    Request
     * @param futureCallback Call back function
     * @throws java.io.IOException io exceptionDBUtils
     */
    public static void sendAsyncRequest(final HttpPost postRequest, BasicFutureCallback futureCallback)
            throws IOException {

        int socketTimeout = 60000;
        int connectTimeout = 60000;
        int connectionRequestTimeout = 30000;

        try {
            MobileConnectConfig.TimeoutConfig timeoutConfig = configurationService.getDataHolder()
                    .getMobileConnectConfig().getUssdConfig().getTimeoutConfig();
            socketTimeout = timeoutConfig.getSocketTimeout() * 1000;
            connectTimeout = timeoutConfig.getConnectionTimeout() * 1000;
            connectionRequestTimeout = timeoutConfig.getConnectionRequestTimeout() * 1000;

        } catch (Exception e) {

            log.debug("Error in reading TimeoutConfig:using default timeouts:"
                    + e);
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout).build();

        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setDefaultRequestConfig(requestConfig).build();
        futureCallback.setClient(client);
        client.start();
        client.execute(postRequest, futureCallback);
    }

    /**
     * Converts query params string to a map
     *
     * @param params query params as a string
     * @return Map of query params
     */
    public static Map<String, String> createQueryParamMap(String params) {
        String[] queryParams = params.split("&");
        Map<String, String> paramMap = new HashMap<>();

        for (String queryParam : queryParams) {
            String[] param = queryParam.split("=");
            String key = param[0];
            String value = null;
            if (param.length > 1) {
                value = param[1];
            }
            paramMap.put(key, value);
        }
        return paramMap;
    }

    public static boolean isProfileUpgrade(String msisdn, int requestedLoa, boolean isUserExits) throws
            RemoteException, LoginAuthenticationExceptionException,
            RemoteUserStoreManagerServiceUserStoreExceptionException, AuthenticationFailedException,
            UserStoreException {

        if (isUserExits) {
            String adminURL = configurationService.getDataHolder().getMobileConnectConfig().getAdminUrl();
            LoginAdminServiceClient lAdmin = new LoginAdminServiceClient(adminURL);
            String sessionCookie = lAdmin.authenticate(configurationService.getDataHolder().getMobileConnectConfig()
                            .getAdminUsername(),
                    configurationService.getDataHolder().getMobileConnectConfig().getAdminPassword());
            ClaimManagementClient claimManager = new ClaimManagementClient(adminURL, sessionCookie);
            int registeredLoa = Integer.parseInt(claimManager.getRegisteredLOA(msisdn));

            return requestedLoa > registeredLoa;
        } else {
            return false;
        }

    }


}