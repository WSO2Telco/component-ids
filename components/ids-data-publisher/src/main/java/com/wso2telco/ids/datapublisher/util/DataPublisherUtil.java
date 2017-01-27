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

    public static void saveUSSD(UserStatus userStatus) {

        try {
            DBUtil.saveStatusData(userStatus);

        } catch (Exception e) {
            log.error("error occurred : " + e.getMessage());

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

}