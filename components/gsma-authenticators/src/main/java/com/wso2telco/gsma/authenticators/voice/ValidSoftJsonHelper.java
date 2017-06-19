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
    private final String UNKNOWN_USER = "UNKNOWN_USER";
    private final String ACTIVE = "ACTIVE";

    private static Log log = LogFactory.getLog(ValidSoftJsonHelper.class);

    public String getIsUserActiveRequestJson() {
        return isUserActiveRequestJson;
    }

    public void setIsUserActiveRequestJson(String logId , String msisdn ) {

        // remove msisdn modifiation line this from when going to prod
               msisdn = "0094"+ msisdn.substring(3,12);
        //
        String jsonTemplate = "{" +
                "\"serviceData\": {" +
                "\"serviceId\": \"PasswordResetBiometric\"," +
                "\"loggingId\": \""+logId+"\"" +
                "}," +
                "\"userData\": {" +
                "\"identifier\": \""+msisdn+"\"" +
                "}" +
                "}";
        log.info("isUserActiveRequest Json ::::: " + jsonTemplate);
        this.isUserActiveRequestJson = jsonTemplate;
    }

    public void setUserRegistrationAndAuthenticationJson(String logId , String msisdn){

        // remove msisdn modifiation line this from when going to prod
        msisdn = "0094"+ msisdn.substring(3,12);
        //

        String jsonTemplate = "{"+
                "\"serviceData\": {" +
                "\"serviceId\": \"PasswordResetBiometric\"," +
                "\"loggingId\": \""+logId+"\"" +
                "}," +
                "\"userData\": {" +
                "\"identifier\": \"wso2Telco\"," +
                "\"contact\": [" +
                "{" +
                "\"type\": \"Mobile\"," +
                "\"label\": \"Work\"," +
                "\"address\": \""+msisdn+"\"" +
                "}" +
                "]" +
                "}" +
                "}";
        log.info("UserRegistrationAndAuthenticationJson Json ::::: " + jsonTemplate);
        this.userRegistrationAndAuthenticationJson = jsonTemplate;
    }

    public String getUserRegistrationAndAuthenticationJson() {
        return userRegistrationAndAuthenticationJson;
    }


    public boolean validateisUserEnrolledJsonRespone(JSONObject responseJsonObject){
        try {
            String outCome = responseJsonObject.getString("outcome");
            log.info("~~~ isUser ACTIVE  ::::: " + outCome);
            if(outCome.equals(UNKNOWN_USER)){
                return false;
            }if(outCome.equals(ACTIVE)){
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
