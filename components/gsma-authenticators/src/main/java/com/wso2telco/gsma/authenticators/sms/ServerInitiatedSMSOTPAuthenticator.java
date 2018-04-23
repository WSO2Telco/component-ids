package com.wso2telco.gsma.authenticators.sms;

import com.wso2telco.Util;
import com.wso2telco.core.config.model.MobileConnectConfig;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.authenticators.BaseApplicationAuthenticator;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.Utility;
import com.wso2telco.gsma.authenticators.cryptosystem.AESencrp;
import com.wso2telco.gsma.authenticators.model.SMSMessage;
import com.wso2telco.gsma.authenticators.util.Application;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.authenticators.util.BasicFutureCallback;
import com.wso2telco.gsma.authenticators.util.OutboundMessage;
import com.wso2telco.gsma.shorten.SelectShortUrl;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hashids.Hashids;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ServerInitiatedSMSOTPAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator, BaseApplicationAuthenticator {

    private static Log log = LogFactory.getLog(ServerInitiatedSMSOTPAuthenticator.class);

    protected static ConfigurationService configurationService = new ConfigurationServiceImpl();

    private static final String AUTH_FAILED = "Authentication failed";

    private static MobileConnectConfig mobileConnectConfigs = null;

    static {
        mobileConnectConfigs = configurationService.getDataHolder().getMobileConnectConfig();
    }

    protected enum UserResponse {

        /**
         * The pending.
         */
        PENDING,

        /**
         * The approved.
         */
        APPROVED,

        /**
         * The rejected.
         */
        REJECTED,

        /**
         * The Expired.
         */
        EXPIRED
    }


    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {
        if (log.isDebugEnabled()) {
            log.debug(this.getClass().getName() + " canHandle invoked");
        }

        return true;
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context) throws AuthenticationFailedException {
        log.info("Initiating authentication request");
    }

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {
        DataPublisherUtil
                .updateAndPublishUserStatus((UserStatus) context.getParameter(Constants
                                .USER_STATUS_DATA_PUBLISHING_PARAM),
                        DataPublisherUtil.UserState.SMS_AUTH_PROCESSING, "ServerInitiatedSMSOTPAuthenticator processing started");

        if (context.isLogoutRequest()) {
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        } else {
            return processRequest(request, response, context);
        }
    }

    public AuthenticatorFlowStatus processRequest(HttpServletRequest request, HttpServletResponse response,
                                                  AuthenticationContext context) throws
            AuthenticationFailedException, LogoutFailedException {
        log.info("Processing authentication response");

        processAuthenticationResponse(request, response, context);



        return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context) throws AuthenticationFailedException {
        UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);
        String sessionDataKey = request.getParameter("sessionDataKey");
        String msisdn = (String) context.getProperty("msisdn");
        String status = null;

        if (log.isDebugEnabled()) {
            log.debug("SessionDataKey : " + sessionDataKey);
        }

        try {
            sendSMS(response, context);
            status = UserResponse.APPROVED.name();
            log.info("OTP authentication approved");
        } catch (LogoutFailedException e) {
            status = UserResponse.REJECTED.name();
            log.error(AUTH_FAILED, e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        } finally {
            try {
                if (sessionDataKey != null && !sessionDataKey.isEmpty() && status != null && !status.isEmpty()) {
                    DBUtils.updateOTPForSMS(sessionDataKey, status);
                    log.info("OTP response status : " + status + " for session : " + sessionDataKey);
                }
            } catch (Exception e) {
                log.error("Error while updating sms otp status", e);
            }
        }

        AuthenticationContextHelper.setSubject(context, msisdn);

        log.info("Authentication success");

        DataPublisherUtil.updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.SMS_AUTH_SUCCESS,
                "SMS Authentication success");
    }

    private void sendSMS(HttpServletResponse response,
                         AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {
        UserStatus userStatus = (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM);
        SMSMessage smsMessage = getRedirectInitAuthentication(response, context, userStatus);

        if (smsMessage != null && smsMessage.getRedirectURL() != null && !smsMessage.getRedirectURL().isEmpty()) {
            try {
                MobileConnectConfig connectConfig = configurationService.getDataHolder().getMobileConnectConfig();
                MobileConnectConfig.SMSConfig smsConfig = connectConfig.getSmsConfig();
                int otpLength = smsConfig.getOTPLength();
                String otp = Utility.genarateOTP(otpLength);
                String hashedotp = Utility.generateSHA256Hash(otp);
                String sessionDataKey = context.getContextIdentifier();
                DBUtils.insertOTPForSMS(sessionDataKey, hashedotp, SMSAuthenticator.UserResponse.PENDING.name());

                HashMap<String, String> variableMap = new HashMap<String, String>();
                variableMap.put("smsotp", otp);
                String otpmessageText = OutboundMessage.prepare(smsMessage.getClient_id(),
                        OutboundMessage.MessageType.SMS_OTP, variableMap, smsMessage.getOperator());
                smsMessage.setMessageText(otpmessageText + smsMessage.getMessageText());
                if (log.isDebugEnabled()) {
                    log.debug("OTP Message: " + smsMessage.getMessageText());
                }
                log.info("OTP Message: " + smsMessage.getMessageText());
                BasicFutureCallback futureCallback = userStatus != null ? new SMSFutureCallback(
                        userStatus.cloneUserStatus(), "SMSOTP") : new SMSFutureCallback();
                smsMessage.setFutureCallback(futureCallback);
                String smsResponse = new SendSMS().sendSMS(smsMessage.getMsisdn(), smsMessage.getMessageText(),smsMessage.getOperator(), smsMessage.getFutureCallback());
            } catch (Exception ex) {
                DataPublisherUtil.updateAndPublishUserStatus(userStatus,
                        DataPublisherUtil.UserState.SMS_AUTH_PROCESSING_FAIL, ex.getMessage());
                throw new AuthenticationFailedException(ex.getMessage(), ex);
            }
        } else {
            log.error("SMS OTP Authentication failed while trying to authenticate");
            throw new AuthenticationFailedException("SMS OTP Authentication failed while trying to authenticate");
        }
    }

    protected SMSMessage getRedirectInitAuthentication(HttpServletResponse response, AuthenticationContext context,
                                                       UserStatus userStatus) throws AuthenticationFailedException {
        SMSMessage smsMessage = null;
        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                        context.getCallerSessionKey(),
                        context.getContextIdentifier());

        if (log.isDebugEnabled()) {
            log.debug("Query parameters : " + queryParams);
        }

        try {
            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            } else {
                // Insert entry to DB only if this is not a retry
                DBUtils.insertUserResponse(context.getContextIdentifier(), ServerInitiatedSMSAuthenticator.UserResponse.PENDING.name());
            }

            //MSISDN will be saved in the context in the MSISDNAuthenticator
            String msisdn = (String) context.getProperty(Constants.MSISDN);
            Application application = new Application();

            MobileConnectConfig connectConfig = configurationService.getDataHolder().getMobileConnectConfig();
            MobileConnectConfig.SMSConfig smsConfig = connectConfig.getSmsConfig();

            String encryptedContextIdentifier = AESencrp.encrypt(context.getContextIdentifier());
            //String messageURL = connectConfig.getSmsConfig().getAuthUrl() + Constants.AUTH_URL_ID_PREFIX;
            String messageURL =  mobileConnectConfigs.getBackChannelConfig().getSmsCallbackUrl() + Constants.AUTH_URL_ID_PREFIX; //todo: add to config


            Map<String, String> paramMap = Util.createQueryParamMap(queryParams);
            String client_id = paramMap.get(Constants.CLIENT_ID);
            String operator = (String) context.getProperty(Constants.OPERATOR);

            if (smsConfig.isShortUrl()) {
                // If a URL shortening service is enabled, then we need to encrypt the context identifier, create the
                // message URL and shorten it.
                log.info("URL shortening service is enabled");
                SelectShortUrl selectShortUrl = new SelectShortUrl();
                messageURL = selectShortUrl.getShortUrl(smsConfig.getShortUrlClass(),
                        messageURL + response.encodeURL(encryptedContextIdentifier), smsConfig.getAccessToken(),
                        smsConfig.getShortUrlService());
            } else {
                // If a URL shortening service is not enabled, we need to created a hash key for the encrypted
                // context identifier and insert a database entry mapping ths hash key to the context identifier.
                // This is done to shorten the message URL as much as possible.
                log.info("Generating hash key for the SMS");
                String hashForContextId = getHashForContextId(encryptedContextIdentifier);
                messageURL += hashForContextId;
                DBUtils.insertHashKeyContextIdentifierMapping(hashForContextId, context.getContextIdentifier());
            }

            // prepare the USSD message from template
            HashMap<String, String> variableMap = new HashMap<String, String>();
            variableMap.put("application", application
                    .changeApplicationName(context.getSequenceConfig().getApplicationConfig().getApplicationName()));
            variableMap.put("link", messageURL);
            boolean isRegistering = (boolean) context.getProperty(Constants.IS_REGISTERING);
            OutboundMessage.MessageType messageType = OutboundMessage.MessageType.SMS_LOGIN;

            if (isRegistering) {
                messageType = OutboundMessage.MessageType.SMS_REGISTRATION;
            }
            String messageText = OutboundMessage
                    .prepare(client_id, messageType, variableMap, operator);

            if (log.isDebugEnabled()) {
                log.debug("Message URL: " + messageURL);
                log.debug("Message: " + messageText);
                log.debug("Operator: " + operator);
            }

            DBUtils.insertAuthFlowStatus(msisdn, Constants.STATUS_PENDING, context.getContextIdentifier());

            smsMessage = new SMSMessage();
            smsMessage.setMsisdn(msisdn);
            smsMessage.setMessageText(messageText);
            smsMessage.setOperator(operator);
            smsMessage.setClient_id(client_id);
        } catch (Exception e) {
            DataPublisherUtil
                    .updateAndPublishUserStatus(userStatus, DataPublisherUtil.UserState.SMS_AUTH_PROCESSING_FAIL,
                            e.getMessage());
            log.error(AUTH_FAILED, e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }
        return smsMessage;
    }

    protected String getHashForContextId(String contextIdentifier) {
        int hashLength = 7;

        Hashids hashids = new Hashids(contextIdentifier, hashLength);

        return hashids.encode(new Date().getTime());
    }

    @Override
    public String getName() {
        return Constants.SERVER_INITIATED_SMSOTP_AUTHENTICATOR_NAME;
    }

    @Override
    public String getFriendlyName() {
        return Constants.SERVER_INITIATED_SMSOTP_AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public String getAmrValue(int acr) {
        return "SMS_URL_OK";
    }

    @Override
    public String getContextIdentifier(HttpServletRequest request) {
        return request.getParameter("sessionDataKey");
    }
}