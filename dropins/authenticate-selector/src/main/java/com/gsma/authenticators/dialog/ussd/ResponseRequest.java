package com.gsma.authenticators.dialog.ussd;

/**
 * Created by paraparan on 5/14/15.
 */
public class ResponseRequest {

    private String notifyURL = "";
    private String callbackData = "";

    public String getNotifyURL() {
        return notifyURL;
    }

    public void setNotifyURL(String notifyURL) {
        this.notifyURL = notifyURL;
    }

    public String getCallbackData() {
        return callbackData;
    }

    public void setCallbackData(String callbackData) {
        this.callbackData = callbackData;
    }


}
