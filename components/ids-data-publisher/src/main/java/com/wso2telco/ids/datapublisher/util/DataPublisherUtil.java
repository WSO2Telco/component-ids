package com.wso2telco.ids.datapublisher.util;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.ids.datapublisher.IdsAgent;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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


    public static UserStatus buildUserStatusFromRequest(HttpServletRequest request, AuthenticationContext context) {
        UserStatus.UserStatusBuilder userStatusBuilder = new UserStatus
                .UserStatusBuilder(resolveSessionID(request, context));

        return userStatusBuilder
                .appId(context.getSequenceConfig() == null ? null : context.getSequenceConfig().getApplicationId())
                .msisdn(request.getParameter("msisdn_header"))
                .operator(request.getParameter("operator"))
                .nonce(request.getParameter("operator"))
                .state(request.getParameter("state"))
                .scope(request.getParameter("scope"))
                .telcoScope(request.getParameter("telco_scope"))
                .acrValue(request.getParameter("acr_values"))
                .ipHeader(request.getParameter("ipAddress"))
                .loginHint(request.getParameter("login_hint"))
                .userAgent(request.getHeader("User-Agent"))
                .build();
    }

    public static UserStatus getInitialUserStatusObject(HttpServletRequest request, AuthenticationContext context) {
        return buildUserStatusFromRequest(request, context);
    }

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

    public static String resolveSessionID(HttpServletRequest request,
                                          AuthenticationContext context) {

        if (request.getParameter("transactionId") != null && !request.getParameter("transactionId").isEmpty()) {
            return request.getParameter("transactionId");
        } else if (context.getProperty(TRANSACTION_ID) != null
                && !((String) context.getProperty(TRANSACTION_ID)).isEmpty()) {

            return (String) context.getProperty(TRANSACTION_ID);
        } else if (context.getSessionIdentifier() != null) {
            return context.getSessionIdentifier();
        } else {
            return request.getParameter("sessionDataKey");
        }
    }

    /**
     * construct and publish UserStatusMetaData event
     *
     * @param userStatus
     */
    public static void publishUserStatusMetaData(UserStatus userStatus) {
        List<Object> userstatusData = new ArrayList<Object>(17);

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
        if (userStatus.getMsisdn() != null && !userStatus.getMsisdn().isEmpty()) {

            userstatusData.add(userStatus.getMsisdn());
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
        if (userStatus.getIsNewUser() == 1) {

            userstatusData.add(Boolean.TRUE);
        } else {
            userstatusData.add(Boolean.FALSE);
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
        List<Object> userStatusMetaData = new ArrayList<Object>(4);
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

        if (timestamp != null) {
            userStatusMetaData.add(timestamp);
        }

        IdsAgent.getInstance().publish(USER_STATUS_STREAM_NAME,
                                       USER_STATUS_STREAM_VERSION, System.currentTimeMillis(),
                                       userStatusMetaData.toArray());
    }

    /**
     * construct and publish TokenEndpointData event
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

        IdsAgent.getInstance().publish(TOKEN_ENDPOINT_STREAM_NAME,
                TOKEN_ENDPOINT_STREAM_VERSION, System.currentTimeMillis(), tokenEndpointData.toArray());
    }

    public static void setContextData(AuthenticationContext context, HttpServletRequest request) {


        if(request.getParameter("telco_scope")!=null){
            context.setProperty("telco_scope",request.getParameter("telco_scope") );
        }
        else {
            context.setProperty("telco_scope","openid" );
        }

        if(request.getParameter("operator")!=null){
            context.setProperty("operator",request.getParameter("operator") );
        }
        if(request.getParameter("msisdn")!=null){
            context.setProperty("msisdn",request.getParameter("msisdn") );
        }
        String sessionId = resolveSessionID(request, context);
        context.setProperty(TRANSACTION_ID, sessionId);

    }


    public static void publishDataForHESkipFlow(HttpServletRequest request,
                                                AuthenticationContext context) {
        UserStatus uStatus = buildUserStatusFromRequest(request, context);
        uStatus.setIsNewUser(1);

        if (uStatus.getIsMsisdnHeader() == 1) {
            uStatus.setStatus(UserState.HE_AUTH_SUCCESS.name());
            //start publishing to meta
            publishUserStatusMetaData(uStatus);
            //end publishing
            //saveUSSD(uStatus);
            publishUserStatusData(uStatus);
        } else {
            uStatus.setStatus(UserState.HE_AUTH_PROCESSING_FAIL.name());

            //start publishing to meta
            publishUserStatusMetaData(uStatus);
            //end publishing

            //saveUSSD(uStatus);
            publishUserStatusData(uStatus);

            uStatus.setStatus(UserState.MSISDN_AUTH_PROCESSING_FAIL.name());
            //saveUSSD(uStatus);
            publishUserStatusData(uStatus);
        }
    }

    public static void publishInvalidRequest(HttpServletRequest request,
                                             AuthenticationContext context) {

        UserStatus uStatus = buildUserStatusFromRequest(request, context);
        uStatus.setStatus(UserState.INVALID_REQUEST.name());

        uStatus.setComment("Invalid Request");


        //start publishing to meta
        publishUserStatusMetaData(uStatus);
        //end publishing
        //saveUSSD(uStatus);
        publishUserStatusData(uStatus);
    }

    public static void updateAndPublishUserStatus(UserStatus userStatus, UserState userState, String comment) {
        boolean dataPublishingEnabled = DataHolder.getInstance().getMobileConnectConfig().getDataPublisher()
                .isEnabled();
        if (dataPublishingEnabled) {
            if (userState != null) {
                userStatus.setState(userState.name());
                userStatus.setComment(comment);
                publishUserStatusData(userStatus);
            }
        }
    }

    public enum UserState {

        HE_AUTH_PROCESSING_FAIL, HE_AUTH_PROCESSING, HE_AUTH_SUCCESS,
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
        OTHER;

    }
}