package com.wso2telco.ids.datapublisher.util;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.ids.datapublisher.IdsAgent;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataPublisherUtil {

    private static Log log = LogFactory.getLog(DataPublisherUtil.class);

    public static final String TRANSACTION_ID = "TransactionId";

    public static final String USER_STATUS_STREAM_NAME = "com.wso2telco.userstatus";
    public static final String USER_STATUS_STREAM_VERSION = "1.0.0";

    public static final String USER_STATUS_META_STREAM_NAME = "com.wso2telco.userstatus.meta";
    public static final String USER_STATUS_META_STREAM_VERSION = "1.0.0";

    public static final String AUTH_ENDPOINT_STREAM_NAME = "com.wso2telco.authorization.endpoint";
    public static final String AUTH_ENDPOINT_STREAM_VERSION = "1.0.0";

    public static final String TOKEN_ENDPOINT_STREAM_NAME = "com.wso2telco.token.endpoint";
    public static final String TOKEN_ENDPOINT_STREAM_VERSION = "1.0.0";

    public static final String NEW_USER_STREAM_NAME = "com.wso2telco.offline.user.registration";
    public static final String NEW_USER_STREAM_VERSION = "1.0.0";

    public static final String OAUTH2_CLIENT_TYPE = "oauth2";

    private static ApplicationManagementService applicationManagementService;

    public static void setApplicationManagementService(ApplicationManagementService applicationMgtService) {
        applicationManagementService = applicationMgtService;
    }

    public static UserStatus buildUserStatusFromRequest(HttpServletRequest request, AuthenticationContext context) {
        UserStatus.UserStatusBuilder userStatusBuilder = new UserStatus
                .UserStatusBuilder(getSessionID(request, context));
        String appId = null;
        try {
            appId = applicationManagementService != null ?
                    applicationManagementService
                            .getServiceProviderNameByClientId(request.getParameter("client_id"), OAUTH2_CLIENT_TYPE,
                                    CarbonContext.getThreadLocalCarbonContext().getTenantDomain()) :
                    null;
        } catch (IdentityApplicationManagementException e) {
            log.warn("An error occurred while obtaining Service Provider name");
        }

        return userStatusBuilder
                .appId(appId)
                .msisdn(request.getParameter("msisdn_header"))
                .operator(request.getParameter("operator"))
                .nonce(request.getParameter("nonce"))
                .state(request.getParameter("state"))
                .scope(request.getParameter("scope"))
                .telcoScope(request.getParameter("telco_scope"))
                .acrValue(request.getParameter("acr_values"))
                .ipHeader(request.getParameter("ipAddress"))
                .loginHint(request.getParameter("login_hint"))
                .userAgent(request.getHeader("User-Agent"))
                .transactionId(request.getParameter("transactionId"))
                .consumerKey(request.getParameter("client_id"))
                .xForwardIP(request.getHeader("X-Forwarded-For"))
                .build();
    }

 /*
    public static UserStatus buildUserStatusFromContext(HttpServletRequest request,
            AuthenticationContext context) {

        UserStatus.UserStatusBuilder userStatusBuilder = new UserStatus
                .UserStatusBuilder((String) context.getProperty(TRANSACTION_ID));
        Map<String, String> paramMap = new LinkedHashMap<String, String>();
        String params = context.getQueryParams();
        String[] pairs = params.split("&");

        for (String pair : pairs) {
            int idx = pair.indexOf("=");

            paramMap.put(pair.substring(0, idx), pair.substring(idx + 1));
        }

        return userStatusBuilder
                .msisdn((String) context.getProperty("msisdn"))
                .operator((String) context.getProperty("operator"))
                .nonce(paramMap.get("nonce") != null ? paramMap.get("nonce") : request.getParameter("nonce"))
                .state(paramMap.get("state") != null ? paramMap.get("state") : request.getParameter("state"))
                .scope(paramMap.get("scope") != null ? paramMap.get("scope") : request.getParameter("scope"))
                .acrValue(paramMap.get("acr_values") != null ? paramMap.get("acr_values")
                                  : request.getParameter("acr_values"))
                .telcoScope(paramMap.get("telco_scope") != null ? paramMap.get("telco_scope")
                                    : request.getParameter("telco_scope"))
                .build();
    }
 */

    public static String getSessionID(HttpServletRequest request, AuthenticationContext context) {
        if (context != null) {
            if (StringUtils.isNotEmpty(context.getContextIdentifier())) {
                return context.getContextIdentifier();
            }
        }
        return request.getParameter("sessionDataKey");
    }

    /**
     * construct and publish UserStatusMetaData event
     *
     * @param userStatus
     */
    public static void publishUserStatusMetaData(UserStatus userStatus) {
        List<Object> userstatusData = new ArrayList<Object>();

        if (userStatus.getSessionId() != null && !userStatus.getSessionId().isEmpty()) {

            userstatusData.add(userStatus.getSessionId());
        } else {
            userstatusData.add(null);
        }
        if (userStatus.getIpHeader() != null && !userStatus.getIpHeader().isEmpty()) {

            userstatusData.add(userStatus.getIpHeader());
        } else {
            userstatusData.add(null);
        }
        if (userStatus.getOperator() != null && !userStatus.getOperator().isEmpty()) {

            userstatusData.add(userStatus.getOperator());
        } else {
            userstatusData.add(null);
        }
        if (userStatus.getAppId() != null && !userStatus.getAppId().isEmpty()) {

            userstatusData.add(userStatus.getAppId());
        } else {
            userstatusData.add(null);
        }
        if (userStatus.getIsMsisdnHeader() == 1) {

            userstatusData.add(Boolean.TRUE);

        } else {
            userstatusData.add(Boolean.FALSE);
        }

        if (userStatus.getUserAgent() != null && !userStatus.getUserAgent().isEmpty()) {

            userstatusData.add(userStatus.getUserAgent());
        } else {
            userstatusData.add(null);
        }
        if (userStatus.getConsumerKey() != null && !userStatus.getConsumerKey().isEmpty()) {

            userstatusData.add(userStatus.getConsumerKey());
        } else {
            userstatusData.add(null);
        }
        if (userStatus.getState() != null && !userStatus.getState().isEmpty()) {

            userstatusData.add(userStatus.getState());
        } else {
            userstatusData.add(null);
        }
        if (userStatus.getNonce() != null && !userStatus.getNonce().isEmpty()) {

            userstatusData.add(userStatus.getNonce());
        } else {
            userstatusData.add(null);
        }
        if (userStatus.getScope() != null && !userStatus.getScope().isEmpty()) {

            userstatusData.add(userStatus.getScope());
        } else {
            userstatusData.add(null);
        }
        if (userStatus.getAcrValue() != null && !userStatus.getAcrValue().isEmpty()) {

            userstatusData.add(userStatus.getAcrValue());
        } else {
            userstatusData.add(null);
        }
        if (userStatus.getLoginHint() != null && !userStatus.getLoginHint().isEmpty()) {

            userstatusData.add(userStatus.getLoginHint());
        } else {
            userstatusData.add(null);
        }

        if (userStatus.getTelcoScope() != null && !userStatus.getTelcoScope().isEmpty()) {

            userstatusData.add(userStatus.getTelcoScope());
        } else {
            userstatusData.add(null);
        }
        if (userStatus.getStatus() != null && !userStatus.getStatus().isEmpty()) {

            userstatusData.add(userStatus.getStatus());
        } else {
            userstatusData.add(null);
        }

        userstatusData.add(userStatus.getTransactionId());

        userstatusData.add(System.currentTimeMillis());


        IdsAgent.getInstance().publish(USER_STATUS_META_STREAM_NAME, USER_STATUS_META_STREAM_VERSION,
                System.currentTimeMillis(), userstatusData.toArray());
    }

    /**
     * construct and publish UserStatusData event
     *
     * @param userStatus
     */
    private static void publishUserStatusData(UserStatus userStatus) {
        List<Object> userStatusMetaData = new ArrayList<Object>();
        String sessionId = userStatus.getSessionId();
        String status = userStatus.getStatus();
        Timestamp timestamp = userStatus.getTime();

        if (sessionId != null && !sessionId.isEmpty()) {
            userStatusMetaData.add(sessionId);
        } else {
            userStatusMetaData.add(null);
        }

        if (status != null && !status.isEmpty()) {
            userStatusMetaData.add(status);
        } else {
            userStatusMetaData.add(null);
        }

        //TODO: Following two if blocks are added just for testing with current DAS scripts.
        //Should be removed when scripts are refactored
        if (userStatus.getIpHeader() != null && !userStatus.getIpHeader().isEmpty()) {
            userStatusMetaData.add(userStatus.getIpHeader());
        } else {
            userStatusMetaData.add(null);
        }

        if (userStatus.getxForwardIP() != null && !userStatus.getxForwardIP().isEmpty()) {
            userStatusMetaData.add(userStatus.getxForwardIP());
        } else {
            userStatusMetaData.add(null);
        }

        //TODO: Once DAS scripts are refactored add the following if block
        /*if (timestamp != null) {
            userStatusMetaData.add(timestamp);
        }*/
        userStatusMetaData.add(userStatus.getTransactionId());

        if (userStatus.getMsisdn() != null && !userStatus.getMsisdn().isEmpty()) {
            userStatusMetaData.add(userStatus.getMsisdn());
        } else {
            userStatusMetaData.add(null);
        }

        userStatusMetaData.add(userStatus.getIsNewUser() == 1 ? Boolean.TRUE : Boolean.FALSE);

        userStatusMetaData.add(System.currentTimeMillis());

        IdsAgent.getInstance().publish(USER_STATUS_STREAM_NAME,
                USER_STATUS_STREAM_VERSION, System.currentTimeMillis(),
                userStatusMetaData.toArray());
    }

    /**
     * construct and publish TokenEndpointData event
     *
     * @param tokenMap
     */
    public static void publishTokenEndpointData(Map<String, String> tokenMap) {
        List<Object> tokenEndpointData = new ArrayList<Object>(17);

        if (tokenMap.get("AuthenticatedUser") != null && !tokenMap.get("AuthenticatedUser").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("AuthenticatedUser"));
        } else {
            tokenEndpointData.add(null);
        }
        if (tokenMap.get("State") != null && !tokenMap.get("State").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("State"));
        } else {
            tokenEndpointData.add(null);
        }
        if (tokenMap.get("Nonce") != null && !tokenMap.get("Nonce").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("Nonce"));
        } else {
            tokenEndpointData.add(null);
        }
        if (tokenMap.get("Amr") != null && !tokenMap.get("Amr").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("Amr"));
        } else {
            tokenEndpointData.add(null);
        }
        if (tokenMap.get("AuthenticationCode") != null && !tokenMap.get("AuthenticationCode").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("AuthenticationCode"));
        } else {
            tokenEndpointData.add(null);
        }
        if (tokenMap.get("AccessToken") != null && !tokenMap.get("AccessToken").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("AccessToken"));
        } else {
            tokenEndpointData.add(null);
        }
        if (tokenMap.get("ContentType") != null && !tokenMap.get("ContentType").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("ContentType"));
        } else {
            tokenEndpointData.add(null);
        }
        if (tokenMap.get("ClientId") != null && !tokenMap.get("ClientId").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("ClientId"));
        } else {
            tokenEndpointData.add(null);
        }
        if (tokenMap.get("RefreshToken") != null && !tokenMap.get("RefreshToken").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("RefreshToken"));
        } else {
            tokenEndpointData.add(null);
        }
        if (tokenMap.get("TokenClaims") != null && !tokenMap.get("TokenClaims").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("TokenClaims"));
        } else {
            tokenEndpointData.add(null);
        }
        if (tokenMap.get("ReturnedResult") != null && !tokenMap.get("ReturnedResult").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("ReturnedResult"));
        } else {
            tokenEndpointData.add(null);
        }
        if (tokenMap.get("StatusCode") != null && !tokenMap.get("StatusCode").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("StatusCode"));
        } else {
            tokenEndpointData.add(null);
        }
        if (tokenMap.get("sessionId") != null && !tokenMap.get("sessionId").isEmpty()) {
            tokenEndpointData.add(tokenMap.get("sessionId"));
        } else {
            tokenEndpointData.add(null);
        }
        tokenEndpointData.add(System.currentTimeMillis());

        IdsAgent.getInstance().publish(TOKEN_ENDPOINT_STREAM_NAME, TOKEN_ENDPOINT_STREAM_VERSION,
                System.currentTimeMillis(), tokenEndpointData.toArray());
    }

    public static Map<String, String> getAuthMapWithInitialData(HttpServletRequest request, AuthenticationContext
            context) {

        Map<String, String> authMap = new HashMap<String, String>();
        authMap.put("RequestType", "GET");
        authMap.put("AuthenticatorStartTime", String.valueOf(new java.util.Date().getTime()));
        authMap.put("Operator", context.getProperty("operator") == null ? null : (String) context.getProperty
                ("operator"));
        authMap.put("AcrValue", String.valueOf(context.getProperty("acr") == null ? null : context.getProperty("acr")));
        authMap.put("RequestUrl", request.getRequestURI());
        authMap.put("HTTPMethod", "GET");
        authMap.put("AppID", context.getSequenceConfig() == null ? null : context.getSequenceConfig()
                .getApplicationId());
        authMap.put("Scope", request.getParameter("scope"));
        authMap.put("State", request.getParameter("state"));
        authMap.put("Nonce", request.getParameter("nonce"));
        authMap.put("LoginHint", request.getParameter("login_hint"));
        return authMap;
    }

    /**
     * construct and publish TokenEndpointData event
     *
     * @param authMap
     */
    public static void publishAuthEndpointData(Map<String, String> authMap) {

        List<Object> authEndpointData = new ArrayList<Object>(32);

        if (authMap.get("RequestType") != null && !authMap.get("RequestType").isEmpty()) {
            authEndpointData.add(authMap.get("RequestType"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("AppID") != null && !authMap.get("AppID").isEmpty()) {
            authEndpointData.add(authMap.get("AppID"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("IsAuthenticated") != null && !authMap.get("IsAuthenticated").isEmpty()) {
            authEndpointData.add(Boolean.parseBoolean(authMap.get("IsAuthenticated")));
        } else {
            authEndpointData.add(false);
        }
        if (authMap.get("AuthenticatorMethods") != null && !authMap.get("AuthenticatorMethods").isEmpty()) {
            authEndpointData.add(authMap.get("AuthenticatorMethods"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("AuthenticatorEndTime") != null && !authMap.get("AuthenticatorEndTime").isEmpty()) {
            authEndpointData.add(Long.parseLong(authMap.get("AuthenticatorEndTime")));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("AuthenticatorStartTime") != null && !authMap.get("AuthenticatorStartTime").isEmpty()) {
            authEndpointData.add(Long.parseLong(authMap.get("AuthenticatorStartTime")));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("SessionId") != null && !authMap.get("SessionId").isEmpty()) {
            authEndpointData.add(authMap.get("SessionId"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("IsNewUser") != null && !authMap.get("IsNewUser").isEmpty()) {
            authEndpointData.add(Boolean.parseBoolean(authMap.get("IsNewUser")));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("Operator") != null && !authMap.get("Operator").isEmpty()) {
            authEndpointData.add(authMap.get("Operator"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("UserAgent") != null && !authMap.get("UserAgent").isEmpty()) {
            authEndpointData.add(authMap.get("UserAgent"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("Msisdn") != null && !authMap.get("Msisdn").isEmpty()) {
            authEndpointData.add(authMap.get("Msisdn"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("InternalCustomerReference") != null && !authMap.get("AuthenticatorStartTime").isEmpty()) {
            authEndpointData.add(authMap.get("InternalCustomerReference"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("ServerHost") != null && !authMap.get("ServerHost").isEmpty()) {
            authEndpointData.add(authMap.get("ServerHost"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("RequestUrl") != null && !authMap.get("RequestUrl").isEmpty()) {
            authEndpointData.add(authMap.get("RequestUrl"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("HTTPMethod") != null && !authMap.get("HTTPMethod").isEmpty()) {
            authEndpointData.add(authMap.get("HTTPMethod"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("URLParams") != null && !authMap.get("URLParams").isEmpty()) {
            authEndpointData.add(authMap.get("URLParams"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("RequestStatus") != null && !authMap.get("RequestStatus").isEmpty()) {
            authEndpointData.add(authMap.get("RequestStatus"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("ReturnedErorr") != null && !authMap.get("ReturnedErorr").isEmpty()) {
            authEndpointData.add(authMap.get("ReturnedErorr"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("TransactionId") != null && !authMap.get("TransactionId").isEmpty()) {
            authEndpointData.add(authMap.get("TransactionId"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("ConsentTimestamp") != null && !authMap.get("ConsentTimestamp").isEmpty()) {
            authEndpointData.add(Long.parseLong(authMap.get("ConsentTimestamp")));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("ConsentState") != null && !authMap.get("ConsentState").isEmpty()) {
            authEndpointData.add(authMap.get("ConsentState"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("ConsentType") != null && !authMap.get("ConsentType").isEmpty()) {
            authEndpointData.add(authMap.get("ConsentType"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("IsAuthCodeIssued") != null && !authMap.get("IsAuthCodeIssued").isEmpty()) {
            authEndpointData.add(Boolean.parseBoolean(authMap.get("IsAuthCodeIssued")));
        } else {
            authEndpointData.add(false);
        }
        if (authMap.get("State") != null && !authMap.get("State").isEmpty()) {
            authEndpointData.add(authMap.get("State"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("Nonce") != null && !authMap.get("Nonce").isEmpty()) {
            authEndpointData.add(authMap.get("Nonce"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("Scope") != null && !authMap.get("Scope").isEmpty()) {
            authEndpointData.add(authMap.get("Scope"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("AcrValue") != null && !authMap.get("AcrValue").isEmpty()) {
            authEndpointData.add(Integer.parseInt(authMap.get("AcrValue")));
        } else {
            authEndpointData.add(-1);
        }
        if (authMap.get("IsMsisdnHeader") != null && !authMap.get("IsMsisdnHeader").isEmpty()) {
            authEndpointData.add(Boolean.parseBoolean(authMap.get("IsMsisdnHeader")));
        } else {
            authEndpointData.add(false);
        }
        if (authMap.get("IpHeader") != null && !authMap.get("IpHeader").isEmpty()) {
            authEndpointData.add(authMap.get("IpHeader"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("LoginHint") != null && !authMap.get("LoginHint").isEmpty()) {
            authEndpointData.add(authMap.get("LoginHint"));
        } else {
            authEndpointData.add(null);
        }
        if (authMap.get("telco_scope") != null && !authMap.get("telco_scope").isEmpty()) {
            authEndpointData.add(authMap.get("telco_scope"));
        } else {
            authEndpointData.add(null);
        }
        authEndpointData.add(System.currentTimeMillis());

        IdsAgent.getInstance().publish(AUTH_ENDPOINT_STREAM_NAME,
                AUTH_ENDPOINT_STREAM_VERSION, System.currentTimeMillis(), authEndpointData.toArray());
    }

    public static void setContextData(AuthenticationContext context, HttpServletRequest request) {


        if (request.getParameter("telco_scope") != null) {
            context.setProperty("telco_scope", request.getParameter("telco_scope"));
        } else {
            context.setProperty("telco_scope", "openid");
        }

        if (request.getParameter("operator") != null) {
            context.setProperty("operator", request.getParameter("operator"));
        }
        if (request.getParameter("msisdn") != null) {
            context.setProperty("msisdn", request.getParameter("msisdn"));
        }
        String sessionId = getSessionID(request, context);
        context.setProperty(TRANSACTION_ID, sessionId);

    }


    public static void updateAndPublishUserStatus(UserStatus userStatus, UserState userState, String comment) {
        boolean dataPublishingEnabled = DataHolder.getInstance().getMobileConnectConfig().getDataPublisher()
                .isEnabled();
        if (dataPublishingEnabled) {
            if (userStatus != null) {
                userStatus.setStatus(userState.name());
                userStatus.setComment(comment);
                publishUserStatusData(userStatus);
            }
        }
    }

    public static void updateAndPublishUserStatus(UserStatus userStatus, UserState userState, String comment,
                                                  String msisdn) {
        boolean dataPublishingEnabled = DataHolder.getInstance().getMobileConnectConfig().getDataPublisher()
                .isEnabled();
        if (dataPublishingEnabled) {
            if (userStatus != null) {
                userStatus.setStatus(userState.name());
                userStatus.setComment(comment);
                userStatus.setMsisdn(msisdn);
                publishUserStatusData(userStatus);
            }
        }
    }

    public static void updateAndPublishUserStatus(UserStatus userStatus, UserState userState, String comment,
                                                  String msisdn, int isNewUser) {
        boolean dataPublishingEnabled = DataHolder.getInstance().getMobileConnectConfig().getDataPublisher()
                .isEnabled();
        if (dataPublishingEnabled) {
            if (userStatus != null) {
                userStatus.setStatus(userState.name());
                userStatus.setComment(comment);
                userStatus.setMsisdn(msisdn);
                userStatus.setIsNewUser(isNewUser);
                publishUserStatusData(userStatus);
            }
        }
    }

    public enum UserState {

        HE_AUTH_PROCESSING_FAIL, HE_AUTH_PROCESSING, HE_AUTH_SUCCESS,
        IP_HEADER_NOT_FOUND, IP_HEADER_NOT_IN_RANGE,
        MSISDN_AUTH_SUCCESS,
        MSISDN_AUTH_PROCESSING_FAIL, MSISDN_AUTH_PROCESSING,

        USSD_AUTH_PROCESSING, USSD_AUTH_PROCESSING_FAIL, USSD_AUTH_SUCCESS,

        SMS_AUTH_PROCESSING, SMS_AUTH_PROCESSING_FAIL, SMS_AUTH_SUCCESS,

        USSDPIN_AUTH_PROCESSING, USSDPIN_AUTH_PROCESSING_FAIL, USSDPIN_AUTH_SUCCESS,

        SEND_USSD_PUSH, SEND_USSD_PUSH_FAIL,

        SEND_USSD_PIN, SEND_USSD_PIN_FAIL,
        SEND_SMS, SEND_SMS_FAIL,

        RECEIVE_USSD_PUSH_APPROVED, RECEIVE_USSD_PUSH_FAIL, RECEIVE_USSD_PIN_APPROVED, RECEIVE_SMS_RESPONSE_SUCCESS,
        RECEIVE_USSD_PUSH_REJECTED, RECEIVE_USSD_PIN_REJECTED,
        RECEIVE_SMS_RESPONSE_FAIL,

        LOGIN_SUCCESS,
        INVALID_MNV_REQUEST,
        INVALID_REQUEST,

        REG_USER_TOKEN_FAIL, AUTH_INITIAL_STEP,

        USSDPIN_REDIRECT,
        CONFIGURATION_ERROR,
        LOGIN_HINT_INVALID,
        LOGIN_HINT_MISMATCH,
        MSISDN_INVALID,
        PROXY_REQUEST_FORWARDED_TO_IS,
        PROXY_PROCESSING,
        OTHER_ERROR,

        MSISDN_SET_TO_LOGIN_HINT,
        MSISDN_SET_TO_HEADER,
        MSISDN_SET_TO_USER_INPUT,
        MSISDN_CLEARED,
        REDIRECT_TO_CONSENT_PAGE,
        REG_CONSENT_AGREED

    }

    /**
     * Publish new user data
     *
     * @param userStatus
     */
    public static void publishNewUserData(UserStatus userStatus) {
        List<Object> userStatusMetaData = new ArrayList<Object>();

        userStatusMetaData.add(System.currentTimeMillis());
        userStatusMetaData.add(userStatus.getMsisdn());
        userStatusMetaData.add(userStatus.getOperator());
        userStatusMetaData.add(userStatus.getStatus());

        IdsAgent.getInstance().publish(NEW_USER_STREAM_NAME,
                NEW_USER_STREAM_VERSION, System.currentTimeMillis(), userStatusMetaData.toArray());
    }
}