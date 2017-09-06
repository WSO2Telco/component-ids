package com.wso2telco.gsma.authenticators.federated;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import com.wso2telco.Util;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.core.dbutils.DbService;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;

public class FederatedAuthenticator extends AbstractApplicationAuthenticator implements LocalApplicationAuthenticator {

    /**
	 * 
	 */
    private static final long serialVersionUID = 3704533210386143972L;
    private static Log log = LogFactory.getLog(FederatedAuthenticator.class);
    private static final String IS_FLOW_COMPLETED = "isFlowCompleted";
    private static MobileConnectConfig mobileConnectConfig = null;
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    private static MobileConnectConfig.Provider[] providers;
    private static final String LOGIN_HINT = "login_hint";
    private static final String NONCE = "nonce";
    private static DbService dbConnection = new DbService();
    private static final String SESSION_DATA_KEY = "sessionDataKey";
    private static final String IDP_AOUTH_CODE = "code";
    private static final String IDP_ERROR_CODE = "error_description";
    private static final String IDP_ERROR = "error";

    static {
        mobileConnectConfig = configurationService.getDataHolder().getMobileConnectConfig();
        providers = mobileConnectConfig.getFederatedIdentityProviders().getProvider();

    }

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {

        log.info("FederatedAuthenticator process Triggered");
        if ((canHandle(request) && !triggerInitiateAuthRequest(context) && (request
                .getAttribute(FrameworkConstants.REQ_ATTR_HANDLED) == null || !(Boolean) request
                .getAttribute(FrameworkConstants.REQ_ATTR_HANDLED)))) {
            try {
                processAuthenticationResponse(request, response, context);
                if (this instanceof LocalApplicationAuthenticator
                        && !context.getSequenceConfig().getApplicationConfig().isSaaSApp()) {
                    String e = context.getSubject().getTenantDomain();
                    String stepMap1 = context.getTenantDomain();
                    if (!StringUtils.equals(e, stepMap1)) {
                        context.setProperty("UserTenantDomainMismatch", Boolean.valueOf(true));
                        throw new AuthenticationFailedException("Service Provider tenant domain must be equal to user"
                                + " tenant domain for non-SaaS applications");
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

        return (context.getProperty(Constants.FEDERATED_AUTHENTICATOR_TRIGGERED) == null || !(Boolean) context
                .getProperty(Constants.FEDERATED_AUTHENTICATOR_TRIGGERED));

    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws AuthenticationFailedException {
        super.initiateAuthenticationRequest(request, response, context);
        String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(),
                context.getCallerSessionKey(), context.getContextIdentifier());
        Map<String, String> paramMap = Util.createQueryParamMap(queryParams);
        String sessionKey = context.getContextIdentifier();
        String operator = (String) context.getProperty(Constants.OPERATOR);
        String acrValue = paramMap.get(Constants.PARAM_ACR);
        String federatedAouthEndpoint = providers[0].getAuthzEndpoint();
        String federatedCallBackUrl = null;
        try {
            federatedCallBackUrl = URLEncoder.encode(mobileConnectConfig.getFederatedCallbackUrl(),
                    String.valueOf(StandardCharsets.UTF_8));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        String clientId = paramMap.get(Constants.CLIENT_ID);
        String nonce = paramMap.get(NONCE);
        String federatedMobileConnectCallUrl = null;

        log.debug("sessionKey :" + sessionKey + " ~ operator : " + operator + " ~ acrValue : " + acrValue
                + " ~ federatedAouthEndpoint : " + federatedAouthEndpoint + " ~ federatedCallBackUrl : "
                + federatedCallBackUrl + " ~ client_id : " + clientId + " ~ nonce : " + nonce);
        if (paramMap.containsKey(LOGIN_HINT)) {
            String loginHint = paramMap.get(LOGIN_HINT);
            federatedMobileConnectCallUrl = federatedAouthEndpoint + "?response_type=code&state=" + sessionKey
                    + "&nonce=" + nonce + "&max_age=3600&scope=openid&response_type=code&redirect_uri="
                    + federatedCallBackUrl + "&client_id=" + clientId + "&acr_values=" + acrValue + "&login_hint="
                    + loginHint;
        } else {
            federatedMobileConnectCallUrl = federatedAouthEndpoint + "?response_type=code&state=" + sessionKey
                    + "&nonce=" + nonce + "&max_age=3600&scope=openid&response_type=code&redirect_uri="
                    + federatedCallBackUrl + "&client_id=" + clientId + "&acr_values=" + acrValue;
        }

        try {
            log.debug(" ~ federatedMobileConnectCallUrl : " + federatedMobileConnectCallUrl);
            // context.setProperty(Constants.MSISDN , generateRandomNumber(BRAZIL_COUNTRY_CODE));
            log.debug(" ~ MSISDN : " + context.getProperty(Constants.MSISDN));
            context.setProperty(IS_FLOW_COMPLETED, false);
            response.sendRedirect(federatedMobileConnectCallUrl);

        } catch (IOException e) {
            log.error("Error Calling IDP Aouth Url : " + e.getMessage());
        }
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, AuthenticationContext authenticationContext)
            throws AuthenticationFailedException {
        log.info("FederatedAuthenticator process Authentication Response Triggered");
        String federatedOuthCode = httpServletRequest.getParameter(IDP_AOUTH_CODE);
        if(federatedOuthCode!=null) {
            log.debug("Federated IDP returned AouthCode ~ " + federatedOuthCode);
            authenticationContext.setProperty(IS_FLOW_COMPLETED, true);
            authenticationContext.setProperty(Constants.MSISDN, federatedOuthCode);
            log.debug("~ MSISDN : " + authenticationContext.getProperty(Constants.MSISDN));
            String operator = (String) authenticationContext.getProperty(Constants.OPERATOR);

            try {
                dbConnection.insertFederatedAuthCodeMappings(operator, federatedOuthCode);
            } catch (Exception e) {
                log.error("Error Persisting Federdeated Aouth Code : " + e.getMessage());
            }
            AuthenticationContextHelper.setSubject(authenticationContext,
                    authenticationContext.getProperty(Constants.MSISDN).toString());
            log.info("FederatedAuthenticator Authentication success");
        }else{
            String erroCode = httpServletRequest.getParameter(IDP_ERROR_CODE);
            String erro = httpServletRequest.getParameter(IDP_ERROR);
            String sessionKey = authenticationContext.getContextIdentifier();
            String redirectUrl = (String) authenticationContext.getProperty(Constants.REDIRECT_URI);
            log.info("FederatedAuthenticator Authentication Failed");
            //Data Publishing Code Comes Here
            throw new AuthenticationFailedException("Authentication Failed");


        }

    }

    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {
        log.info("FederatedAuthenticator Authenticator canHandle invoked");

        return true;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getParameter(SESSION_DATA_KEY);
    }

    @Override
    public String getName() {
        return Constants.FEDERATED_AUTHENTICATOR_NAME;
    }

    @Override
    public String getFriendlyName() {
        return Constants.FEDERATED_AUTHENTICATOR_FRIENDLY_NAME;
    }

}
