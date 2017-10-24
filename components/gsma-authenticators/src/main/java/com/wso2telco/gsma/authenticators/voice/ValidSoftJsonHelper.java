package com.wso2telco.gsma.authenticators.voice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Created by sheshan on 5/4/17.
 */

public class ValidSoftJsonHelper {

    private String isUserActiveRequestJson;
    private String userRegistrationAndAuthenticationJson;
    private static final String UNKNOWN_USER = "UNKNOWN_USER";
    private static final String ACTIVE = "ACTIVE";
    private final String OUTCOME = "outcome";

    private static Log log = LogFactory.getLog(ValidSoftJsonHelper.class);

    public String getIsUserActiveRequestJson() {
        return isUserActiveRequestJson;
    }

    public void setIsUserActiveRequestJson(String logId , String msisdn ) {

        // remove msisdn modifiation line this from when going to prod
        // Todo : msisdn = "0094"+ msisdn.substring(3,12);
        //
        StringBuilder payloadBuilder = new StringBuilder();
        payloadBuilder.append("{");
        payloadBuilder.append("\"serviceData\": {");
        payloadBuilder.append("\"serviceId\": \"PasswordResetBiometric\",");
        payloadBuilder.append("\"loggingId\": \"");
        payloadBuilder.append(logId);
        payloadBuilder.append("\"");
        payloadBuilder.append("},");
        payloadBuilder.append("\"userData\": {");
        payloadBuilder.append("\"identifier\": \"");
        payloadBuilder.append(msisdn);
        payloadBuilder.append("\"");
        payloadBuilder.append("}");
        payloadBuilder.append("}");

        String jsonTemplate = payloadBuilder.toString();

        log.debug("isUserActiveRequest Json ::::: " + jsonTemplate);
        this.isUserActiveRequestJson = jsonTemplate;
    }

    public void setUserRegistrationAndAuthenticationJson(String logId , String msisdn){

        // remove msisdn modifiation line this from when going to prod
       // Todo : msisdn = "94"+ msisdn.substring(3,12);
        //

        StringBuilder payloadBuilder = new StringBuilder();
        payloadBuilder.append("{");
        payloadBuilder.append("\"serviceData\": {");
        payloadBuilder.append("\"serviceId\": \"PasswordResetBiometric\",");
        payloadBuilder.append("\"loggingId\": \"");
        payloadBuilder.append(logId);
        payloadBuilder.append("\"");
        payloadBuilder.append("},");
        payloadBuilder.append("\"userData\": {");
        payloadBuilder.append("\"identifier\": \"");
        payloadBuilder.append(msisdn);
        payloadBuilder.append("\",");
        payloadBuilder.append("\"contact\": [");
        payloadBuilder.append("{");
        payloadBuilder.append("\"type\": \"Mobile\",");
        payloadBuilder.append("\"label\": \"Work\",");
        payloadBuilder.append("\"address\": \"");
        payloadBuilder.append(msisdn);
        payloadBuilder.append("\"");
        payloadBuilder.append("}");
        payloadBuilder.append("]");
        payloadBuilder.append("}");
        payloadBuilder.append("}");

        String jsonTemplate = payloadBuilder.toString();

        log.debug("UserRegistrationAndAuthenticationJson Json ::::: " + jsonTemplate);
        this.userRegistrationAndAuthenticationJson = jsonTemplate;
    }

    public String getUserRegistrationAndAuthenticationJson() {
        return userRegistrationAndAuthenticationJson;
    }


    public boolean validateisUserEnrolledJsonRespone(JSONObject responseJsonObject) throws JSONException{
        boolean isUserActive = false;
        try {
            String outCome = responseJsonObject.getString(OUTCOME);
            log.debug("isUser ACTIVE  in ValidSoft: " + outCome);
            if(outCome.equals(ACTIVE)){
                isUserActive = true;
            }
        } catch (JSONException e) {
            log.error("JSONException occured ", e);
            throw e;
        }
        return isUserActive;
    }
}