package com.wso2telco.gsma.authenticators.federated;

import com.wso2telco.Util;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.dbutils.DbService;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.authenticators.util.UserProfileManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.user.registration.stub.UserRegistrationAdminServiceIdentityException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FederatedAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private static Log log = LogFactory.getLog(FederatedAuthenticator.class);
    private static final String IS_FLOW_COMPLETED = "isFlowCompleted";
    private static MobileConnectConfig mobileConnectConfig = null;
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    private static MobileConnectConfig.FederatedIdentityProviders federatedIdps = mobileConnectConfig
            .getFederatedIdentityProviders();
    private static HashMap<String, MobileConnectConfig.Provider> federatedIdpMap = new HashMap<>();
    private static final String LOGIN_HINT = "login_hint";
    private static final String NONCE = "nonce";
    private static DbService dbConnection = new DbService();


    static {
        mobileConnectConfig = configurationService.getDataHolder().getMobileConnectConfig();

    }

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {

        log.info("Yepeeeeeeeeeeeeeeee");
        if ((canHandle(request) && !triggerInitiateAuthRequest(context) && (request.getAttribute(FrameworkConstants
                .REQ_ATTR_HANDLED) == null || !(Boolean) request.getAttribute(FrameworkConstants.REQ_ATTR_HANDLED)))) {
            try {
                processAuthenticationResponse(request, response, context);
                if (this instanceof LocalApplicationAuthenticator && !context.getSequenceConfig()
                        .getApplicationConfig().isSaaSApp()) {
                    String e = context.getSubject().getTenantDomain();
                    String stepMap1 = context.getTenantDomain();
                    if (!StringUtils.equals(e, stepMap1)) {
                        context.setProperty("UserTenantDomainMismatch", Boolean.valueOf(true));
                        throw new AuthenticationFailedException("Service Provider tenant domain must be equal to user" +
                                " tenant domain for non-SaaS applications");
                    }
                }

                request.setAttribute(FrameworkConstants.REQ_ATTR_HANDLED, Boolean.TRUE);
                return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
            } catch (AuthenticationFailedException e) {
                Object property = context.getProperty(Constants.IS_TERMINATED);
                boolean isTerminated = false;
                if (property != null) {
                    isTerminated = (boolean) property;
                }

                Map stepMap = context.getSequenceConfig().getStepMap();
                boolean stepHasMultiOption = false;
                if (stepMap != null && !stepMap.isEmpty()) {
                    StepConfig stepConfig = (StepConfig) stepMap.get(Integer.valueOf(context.getCurrentStep()));
                    if (stepConfig != null) {
                        stepHasMultiOption = stepConfig.isMultiOption();
                    }
                }

                if (isTerminated) {
                    throw new AuthenticationFailedException("Authenticator is terminated");
                }
                if (retryAuthenticationEnabled() && !stepHasMultiOption) {
                    context.setRetrying(true);
                    context.setCurrentAuthenticator(getName());
                    initiateAuthenticationRequest(request, response, context);
                    return AuthenticatorFlowStatus.INCOMPLETE;
                } else {
                    throw e;
                }
            }
        } else {
            initiateAuthenticationRequest(request, response, context);
            context.setProperty(Constants.FEDERATED_AUTHENTICATOR_TRIGGERED, Boolean.TRUE);
            context.setCurrentAuthenticator(getName());
            return AuthenticatorFlowStatus.INCOMPLETE;
        }

    }

    private boolean triggerInitiateAuthRequest(AuthenticationContext context) {

        return (context.getProperty(Constants.FEDERATED_AUTHENTICATOR_TRIGGERED) == null || !(Boolean) context.getProperty
                (Constants.FEDERATED_AUTHENTICATOR_TRIGGERED));

    }



    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context) throws AuthenticationFailedException {
        super.initiateAuthenticationRequest(request, response, context);
        String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(),
                context.getCallerSessionKey(), context.getContextIdentifier());
        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();
        Map<String, String> paramMap = Util.createQueryParamMap(queryParams);
        String sessionKey = context.getContextIdentifier();
        String operator = (String) context.getProperty(Constants.OPERATOR);
        String acrValue = paramMap.get(Constants.ACR);
        String federatedAouthEndpoint = federatedIdpMap.get(operator).getAuthzEndpoint();
        String federatedCallBackUrl = mobileConnectConfig.getFederatedCallbackUrl();
        String client_id = paramMap.get(Constants.CLIENT_ID);
        String nonce = paramMap.get(NONCE);
        String federatedMobileConnectCallUrl=null;

        log.info("~~~"+sessionKey +"~~~"+operator+"~~~"+acrValue+"~~~"+federatedAouthEndpoint+"~~~"+federatedCallBackUrl+"~~~"+client_id+"~~~"+nonce);
        if(paramMap.containsKey(LOGIN_HINT)){
            String login_hint = paramMap.get(LOGIN_HINT);
            federatedMobileConnectCallUrl = federatedAouthEndpoint +"?response_type=code&state="+sessionKey+"&nonce="+nonce+"&max_age=3600&scope=openid&response_type=code&redirect_uri="+federatedCallBackUrl+"&client_id="+client_id+"&acr_values="+acrValue+"&login_hint="+login_hint;
        }else{
            federatedMobileConnectCallUrl = federatedAouthEndpoint +"?response_type=code&state="+sessionKey+"&nonce="+nonce+"&max_age=3600&scope=openid&response_type=code&redirect_uri="+federatedCallBackUrl+"&client_id="+client_id+"&acr_values="+acrValue;
        }

        try {
            log.info("~~~~~~~~~~~~~~~~"+federatedMobileConnectCallUrl);
            context.setProperty(Constants.MSISDN , generateRandomNumber("55"));
            log.info("~~~~~~~~~~~~~~~~"+context.getProperty(Constants.MSISDN));
            context.setProperty(IS_FLOW_COMPLETED, false);
            response.sendRedirect(federatedMobileConnectCallUrl);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void processAuthenticationResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationContext authenticationContext) throws AuthenticationFailedException {
        log.info("Tagaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(authenticationContext.getQueryParams(),
                authenticationContext.getCallerSessionKey(), authenticationContext.getContextIdentifier());
        ApplicationConfig applicationConfig = authenticationContext.getSequenceConfig().getApplicationConfig();
        Map<String, String> paramMap = Util.createQueryParamMap(queryParams);
        String applicationName = applicationConfig.getApplicationName();
        String sessionKey = authenticationContext.getCallerSessionKey();
        String federatedOuthCode = httpServletRequest.getParameter("code");
        log.info("~~~~~~~~~~~"+federatedOuthCode);
        authenticationContext.setProperty(IS_FLOW_COMPLETED, true);
        log.info("~~~~~~~~~~~~~~~~"+authenticationContext.getProperty(Constants.MSISDN));
        String operator = (String) authenticationContext.getProperty(Constants.OPERATOR);
        try {
            new UserProfileManager().createUserProfileLoa2(authenticationContext.getProperty(Constants.MSISDN).toString(), operator, Constants.SCOPE_MNV);
        } catch (UserRegistrationAdminServiceIdentityException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //AuthenticatedUser.createFederateAuthenticatedUserFromSubjectIdentifier(authenticationContext.getProperty(Constants.MSISDN).toString());
        try {
            dbConnection.insertFederatedAuthCodeMappings(operator, null, federatedOuthCode);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Error Persisting Federdeated Aouth Code");
        }
        AuthenticationContextHelper.setSubject(authenticationContext, authenticationContext.getProperty(Constants.MSISDN).toString());
        log.info("Authentication success");

    }

    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {
        log.info("FederatedAuthenticator Authenticator canHandle invoked");

        return true;
    }




    @Override
    public String getContextIdentifier(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getParameter("sessionDataKey");
    }

    @Override
    public String getName() {
        return Constants.FEDERATED_AUTHENTICATOR_NAME.toString();
    }

    @Override
    public String getFriendlyName() {
        return Constants.FEDERATED_AUTHENTICATOR_FRIENDLY_NAME.toString();
    }


    private String generateRandomNumber(String countryCode){
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        long mills = calendar.getTimeInMillis();
        long modulesvalue = mills%10000000000L;
        String msisdn = "55"+String.valueOf(modulesvalue);
        return msisdn;
    }

}
