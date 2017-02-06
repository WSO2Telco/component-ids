package com.wso2telco.ids.datapublisher.util;

import com.wso2telco.ids.datapublisher.IdsAgent;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;


import javax.servlet.http.HttpServletRequest;
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


    public static void setValueFromRequest(HttpServletRequest request,
                                           AuthenticationContext context, UserStatus uStatus) {

        if (request.getParameter("msisdn_header") != null) {
            uStatus.setMsisdn(request.getParameter("msisdn_header"));
        }
        if (request.getParameter("operator") != null) {
            uStatus.setOperator(request.getParameter("operator"));
        }
        if (request.getParameter("nonce") != null) {
            uStatus.setNonce(request.getParameter("nonce"));
        }
        ;
        if (request.getParameter("state") != null) {
            uStatus.setState(request.getParameter("state"));
        }
        if (request.getParameter("scope") != null) {
            uStatus.setScope(request.getParameter("scope"));
        }
        if (request.getParameter("telco_scope") != null) {
            uStatus.setTelcoScope(request.getParameter("telco_scope"));
        }
        if (request.getParameter("acr_values") != null) {
            uStatus.setAcrValue(request.getParameter("acr_values"));
        }
        if (request.getParameter("ipAddress") != null) {
            uStatus.setIpHeader(request.getParameter("ipAddress"));
        }
        if (request.getParameter("login_hint") != null) {
            uStatus.setLoginHint(request.getParameter("login_hint"));
        }
        if (request.getHeader("User-Agent") != null) {
            uStatus.setUserAgent(request.getHeader("User-Agent"));
        }

        uStatus.setSessionId(resolveSessionID(request, context));

        String msisdnHeader = request.getParameter("msisdn_header");
        if (msisdnHeader != null && !msisdnHeader.isEmpty()) {
            uStatus.setIsMsisdnHeader(1);
        } else {
            uStatus.setIsMsisdnHeader(0);
        }

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


    public static void setValueFromContext(HttpServletRequest request,
                                           AuthenticationContext context, UserStatus uStatus) {


        Map<String, String> paramMap = new LinkedHashMap<String, String>();
        String params = context.getQueryParams();
        String[] pairs = params.split("&");

        for (String pair : pairs) {
            int idx = pair.indexOf("=");

            paramMap.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        uStatus.setMsisdn((String) context.getProperty("msisdn"));
        uStatus.setOperator((String) context.getProperty("operator"));
        uStatus.setSessionId((String) context.getProperty(TRANSACTION_ID));


        if (paramMap.get("nonce") != null) {
            uStatus.setNonce(paramMap.get("nonce"));
        } else {

            uStatus.setNonce(request.getParameter("nonce"));
        }

        if (paramMap.get("state") != null) {

            uStatus.setState(paramMap.get("state"));
        } else {

            uStatus.setState(request.getParameter("state"));
        }

        if (paramMap.get("scope") != null) {
            uStatus.setScope(paramMap.get("scope"));
        } else {
            uStatus.setScope(request.getParameter("scope"));
        }

        if (paramMap.get("acr_values") != null) {
            uStatus.setAcrValue(paramMap.get("acr_values"));
        } else {
            uStatus.setAcrValue(request.getParameter("acr_values"));
        }
        if (paramMap.get("telco_scope") != null) {
            uStatus.setTelcoScope(paramMap.get("telco_scope"));
        } else {
            uStatus.setTelcoScope(request.getParameter("telco_scope"));
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
    public static void publishUserStatusData(UserStatus userStatus) {
        List<Object> userStatusMetaData = new ArrayList<Object>(4);

        if (userStatus.getSessionId() != null && !userStatus.getSessionId().isEmpty()) {

            userStatusMetaData.add(userStatus.getSessionId());
        } else {
            userStatusMetaData.add(null);
        }

        if (userStatus.getStatus() != null && !userStatus.getStatus().isEmpty()) {
            userStatusMetaData.add(userStatus.getStatus());
        } else {
            userStatusMetaData.add(null);
        }
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

        userStatusMetaData.add(System.currentTimeMillis());

        IdsAgent.getInstance().publish(USER_STATUS_STREAM_NAME,
                                       USER_STATUS_STREAM_VERSION, System.currentTimeMillis(),
                                       userStatusMetaData.toArray());
    }


    private static void setMSISDNHeader(UserStatus uStatus, HttpServletRequest request) {
        log.info("request MSISDN header "+request.getParameter("msisdn_header"));

        if (request.getParameter("msisdn_header")!= null && !request.getParameter("msisdn_header").isEmpty()){
            uStatus.setIsMsisdnHeader(1);
        }
        else {
            uStatus.setIsMsisdnHeader(0);
        }
    }

    /**
     * construct and publish TokenEndpointData event
     * @param tokenMap
     */
    public static void publishTokenEndpointData(Map<String, String> tokenMap){
        List<Object> tokenEndpointData = new ArrayList<Object>(17);

        if (tokenMap.get("AuthenticatedUser") != null && !tokenMap.get("AuthenticatedUser").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("AuthenticatedUser"));
        }
        else {tokenEndpointData.add(null);
        }


        if (tokenMap.get("State") != null && !tokenMap.get("State").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("State"));
        }
        else {tokenEndpointData.add(null);
        }

        if (tokenMap.get("Nonce") != null && !tokenMap.get("Nonce").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("Nonce"));
        }
        else {tokenEndpointData.add(null);
        }

        if (tokenMap.get("Amr") != null && !tokenMap.get("Amr").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("Amr"));
        }
        else {tokenEndpointData.add(null);
        }

        if (tokenMap.get("AuthenticationCode") != null && !tokenMap.get("AuthenticationCode").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("AuthenticationCode"));
        }
        else {tokenEndpointData.add(null);
        }

        if (tokenMap.get("AccessToken") != null && !tokenMap.get("AccessToken").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("AccessToken"));
        }
        else {tokenEndpointData.add(null);
        }

        if (tokenMap.get("SourceIP") != null && !tokenMap.get("SourceIP").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("SourceIP"));
        }
        else {tokenEndpointData.add(null);
        }

        if (tokenMap.get("ContentType") != null && !tokenMap.get("ContentType").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("ContentType"));
        }
        else {tokenEndpointData.add(null);
        }
        if (tokenMap.get("ClientId") != null && !tokenMap.get("ClientId").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("ClientId"));
        }
        else {tokenEndpointData.add(null);
        }

        if (tokenMap.get("Payload") != null && !tokenMap.get("Payload").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("Payload"));
        }
        else {tokenEndpointData.add(null);
        }

        if (tokenMap.get("RefreshToken") != null && !tokenMap.get("RefreshToken").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("RefreshToken"));
        }
        else {tokenEndpointData.add(null);
        }

        if (tokenMap.get("TokenClaims") != null && !tokenMap.get("TokenClaims").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("TokenClaims"));
        }
        else {tokenEndpointData.add(null);
        }
        if (tokenMap.get("RequestStatus") != null && !tokenMap.get("RequestStatus").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("RequestStatus"));
        }
        else {tokenEndpointData.add(null);
        }

        if (tokenMap.get("ReturnedResult") != null && !tokenMap.get("ReturnedResult").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("ReturnedResult"));
        }
        else {tokenEndpointData.add(null);
        }


        if (tokenMap.get("StatusCode") != null && !tokenMap.get("StatusCode").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("StatusCode"));
        }
        else {tokenEndpointData.add(null);
        }


        if (tokenMap.get("sessionId") != null && !tokenMap.get("sessionId").isEmpty() ) {
            tokenEndpointData.add(tokenMap.get("sessionId"));
        }
        else {tokenEndpointData.add(null);
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
        UserStatus uStatus = new UserStatus();
        setValueFromRequest(request, context, uStatus);
        setMSISDNHeader(uStatus, request);
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

        UserStatus uStatus = new UserStatus();
        setValueFromRequest(request, context, uStatus);
        uStatus.setStatus(UserState.INVALID_REQUEST.name());

        uStatus.setComment("Invalid Request");


        //start publishing to meta
        publishUserStatusMetaData(uStatus);
        //end publishing
        //saveUSSD(uStatus);
        publishUserStatusData(uStatus);
    }

    public enum UserState {

        HE_AUTH_PROCESSING_FAIL, HE_AUTH_PROCESSING, HE_AUTH_SUCCESS,
        MSISDN_AUTH_SUCCESS,
        MSISDN_AUTH_PROCESSING_FAIL, MSISDN_AUTH_PROCESSING,

        USSD_AUTH_PROCESSING_FAIL, USSD_AUTH_SUCCESS,

        SMS_AUTH_PROCESSING_FAIL, SMS_AUTH_SUCCESS,

        SEND_USSD_PUSH, SEND_USSD_PUSH_FAIL,

        SEND_USSD_PIN, SEND_USSD_PIN_FAIL,
        SEND_SMS, SEND_SMS_FAIL,

        RECEIVE_USSD_PUSH_APPROVED, RECEIVE_USSD_PUSH_FAIL, RECEIVE_USSD_PIN_APPROVED, RECEIVE_SMS_RESPONSE_SUCCESS,
        RECEIVE_USSD_PUSH_REJECTED, RECEIVE_USSD_PIN_REJECTED,
        RECEIVE_SMS_RESPONSE_FAIL,

        LOGIN_SUCCESS,
        INVALID_MNV_REQUEST,
        INVALID_REQUEST,

        REG_USER_TOKEN_FAIL, AUTH_INITIAL_STEP;


    }
}