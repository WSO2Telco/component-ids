package com.gsma.authenticators;

import com.gsma.authenticators.model.MSSRequest;
import org.apache.http.HttpResponse;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by nilan on 11/17/14.
 */
public class MSSAuthenticator extends AbstractApplicationAuthenticator
        implements LocalApplicationAuthenticator {

    private static final long serialVersionUID = 6817280268460894001L;
    private static Log log = LogFactory.getLog(MSSAuthenticator.class);

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request,
                                           HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException {

        if (context.isLogoutRequest()) {
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        } else {
            return super.process(request, response, context);
        }
    }

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response, AuthenticationContext context)
            throws AuthenticationFailedException {

        String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();

        String queryParams = FrameworkUtils
                .getQueryStringWithFrameworkContextId(context.getQueryParams(),
                        context.getCallerSessionKey(),
                        context.getContextIdentifier());

        try {

            String retryParam = "";

            if (context.isRetrying()) {
                retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
            }else {
                // Insert entry to DB only if this is not a retry
                DBUtils.insertUserResponse(context.getContextIdentifier(), String.valueOf(MSSAuthenticator.UserResponse.PENDING));
            }

            //MSISDN will be saved in the context in the MSISDNAuthenticator
            String msisdn = (String) context.getProperty("msisdn");
            MSSRequest mssRequest=new MSSRequest();
            mssRequest.setMsisdnNo("+"+msisdn);
            mssRequest.setSendString(DataHolder.getInstance().getMobileConnectConfig().getMSS().getMssText());

            String contextIdentifier=context.getContextIdentifier();
            MSSRestClient mssRestClient =new MSSRestClient(contextIdentifier,mssRequest);
            mssRestClient.start();

            response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams)) + "&authenticators="
                    + getName() + ":" + "LOCAL" + retryParam);

        } catch (IOException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }catch (AuthenticatorException e) {
            throw new AuthenticationFailedException(e.getMessage(), e);
        }


    }


    @Override
    protected void processAuthenticationResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationContext context) throws AuthenticationFailedException {

        // String msisdn = httpServletRequest.getParameter("msisdn");
        log.info("MSS PIN Authenticator authentication Start ");
        String sessionDataKey = httpServletRequest.getParameter("sessionDataKey");
        String msisdn = (String) context.getProperty("msisdn");
        boolean isAuthenticated = false;

        try {
            String responseStatus = DBUtils.getUserResponse(sessionDataKey);

            if (responseStatus.equalsIgnoreCase(UserResponse.APPROVED.toString())) {
                isAuthenticated = true;
            }

        } catch (AuthenticatorException e) {
            log.error("SMS Authentication failed while trying to authenticate", e);
            throw new AuthenticationFailedException(e.getMessage(), e);
        }

        if (!isAuthenticated) {
            log.info("MSS PIN Authenticator authentication failed ");
            context.setProperty("faileduser", msisdn);
            if (log.isDebugEnabled()) {
                log.debug("User authentication failed due to not existing user MSISDN.");
            }

            throw new AuthenticationFailedException("Authentication Failed");
        }
        log.info("MSS PIN Authenticator authentication success for MSISDN - " + msisdn);

        context.setProperty("msisdn", msisdn);
        context.setSubject(msisdn);
        String rememberMe = httpServletRequest.getParameter("chkRemember");

        if (rememberMe != null && "on".equals(rememberMe)) {
            context.setRememberMe(true);
        }


    }

    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {
        if (log.isDebugEnabled()) {
            log.debug("MSS PIN Authenticator canHandle invoked");
        }

        return true;

    }

    @Override
    public String getContextIdentifier(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getParameter("sessionDataKey");
    }

    @Override
    public String getName() {
        return Constants.MSS_AUTHENTICATOR_NAME;
    }

    @Override
    public String getFriendlyName() {
        return Constants.MSS_AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    protected boolean retryAuthenticationEnabled() {
        // Setting retry to true as we need the correct continue
        return false;
    }


    private String readResponseEntity(HttpResponse httpResponse) throws IOException {

        String resp = "";
        if(httpResponse.getEntity() != null){
            BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            String line;
            while ((line = rd.readLine()) != null) {
                resp += line;
            }

        }


        return resp;
    }

    private enum UserResponse {
        PENDING,
        APPROVED,
        REJECTED
    }


}
