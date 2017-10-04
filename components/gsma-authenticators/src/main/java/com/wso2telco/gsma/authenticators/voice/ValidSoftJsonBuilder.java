package com.wso2telco.gsma.authenticators.voice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by sheshan on 5/4/17.
 */

/*



 */
public class ValidSoftJsonBuilder {

    private String isUserenrollRequestjson;
    private String verifyUserJson;

    private static Log log = LogFactory.getLog(ValidSoftJsonBuilder.class);

    public String getIsUserEnrollRequestJson() {
        return isUserenrollRequestjson;
    }

    public void setIsUserEnrollRequestJsonJson(String logId , String msisdn , String mode) {
        String jsonTemplate = "{" +
                "\"serviceData\": {" +
                "\"loggingId\": \"" + logId+"\"" +
                "}," +
                "\"userData\": {" +
                "\"identifier\": \""+ msisdn +"\"" +
                "}," +
                "\"processingInformation\": {" +
                "\"biometric\": {" +
                "\"type\": \"text-dependent\"," +
                "\"mode\": \""+mode+"\"" +
                "}" +
                "}" +
                "}";
        log.info("Json ::::: " + jsonTemplate);
        this.isUserenrollRequestjson = jsonTemplate;
    }

    public String getVerifyUserJson() {
        return verifyUserJson;
    }

    public void setVerifyUserJson(String logId , String msisdn , String mode , String voice) {
        String jsonTemplate = "{" +
                "\"serviceData\": {" +
                "\"loggingId\": \""+logId+"\"" +
                "}," +
                "\"userData\": {" +
                "\"identifier\": \""+msisdn+"\"" +
                "}," +
                "\"processingInformation\": {" +
                "\"biometric\": {" +
                "\"type\": \"text-dependent\"," +
                "\"mode\": \""+mode+"\"" +
                "}," +
                "\"audioCharacteristics\": {" +
                "\"samplingRate\": \"8000\"," +
                "\"format\": \"alaw\"" +
                "}," +
                "\"metaInformation\": [" +
                "{" +
                "\"key\": \"usage-context\"," +
                "\"value\": {" +
                "\"value\": \"default\"," +
                "\"encrypted\": \"false\"" +
                "}" +
                "}" +
                "]" +
                "}," +
                "\"audioInput\": {" +
                "\"secondsThreshold\": \"20\"," +
                "\"audio\": {" +
                "\"base64\":" +
                "\""+voice+"\"" +
                "}" +
                "}" +
                "}";
        log.info("Json ::::: " + jsonTemplate);
        this.verifyUserJson = jsonTemplate;
    }
}
