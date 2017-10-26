package com.wso2telco.gsma.authenticators.voice;

import com.wso2telco.gsma.authenticators.AuthenticatorException;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.gsma.authenticators.DBUtils;
import com.wso2telco.gsma.authenticators.util.BasicFutureCallback;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by sheshan on 5/24/17.
 */
public class VoiceIVRFutureCallback extends BasicFutureCallback {

    private static Log log = LogFactory.getLog(VoiceIVRFutureCallback.class);
    private static final String IS_FLOW_COMPLETED = "isFlowCompleted";
    private final String UNKNOWN_USER = "UNKNOWN_USER";
    private final String ACTIVE = "ACTIVE";
    private final String ERROR = "ERROR";
    private final String AUTHENTICATED = "AUTHENTICATED";
    private AuthenticationContext authenticationContext;
    private String msisdn;

    private String SEND_IVR = "ivr_sent";
    private String SEND_IVR_FAIL = "ivr_falied";

    VoiceIVRFutureCallback() {
    }

    VoiceIVRFutureCallback(AuthenticationContext authenticationContext , String msisdn) {
        this.authenticationContext = authenticationContext;
        this.msisdn = msisdn;
    }

    public void completed(HttpResponse response) {
        if ((response.getStatusLine().getStatusCode() == 200)) {
            String json = null;
            try {
                json = EntityUtils.toString(response.getEntity());
                JSONObject jsonObj = new JSONObject(json);
                log.info(json);
                String outCome = jsonObj.getString("outcome");

                if(outCome.equals(AUTHENTICATED)){
                    DBUtils.insertAuthFlowStatus(this.msisdn, "Approved", this.authenticationContext.getContextIdentifier());

                }else if(outCome.equals(ACTIVE)){
                    DBUtils.insertAuthFlowStatus(this.msisdn, "Approved", this.authenticationContext.getContextIdentifier());
                }else{
                    DBUtils.insertAuthFlowStatus(this.msisdn, "FAILED_ATTEMPTS", this.authenticationContext.getContextIdentifier());
                    log.info("No response from Valid soft");
                }

            } catch (IOException e) {
                log.error("IOException occured ", e);
            } catch (JSONException e) {
                log.error("JSONException occured ", e);
            } catch (AuthenticatorException e) {
                log.error("AuthenticatorException occured ", e);
            } catch (SQLException e) {
                log.error("SQLException occured ", e);
            }
        } else {
            log.error("`Failed Request - " + postRequest.getURI().getSchemeSpecificPart());
        }
        closeClient();
    }

    public void failed(Exception exception) {
        super.failed(exception);
    }

}
