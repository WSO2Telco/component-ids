 
package com.gsma.authenticators.dialog.sms;

 
public class ReceiptRequest {
    
    private String notifyURL;
    private String callbackData;
    
    public String getNotifyURL() {
            return notifyURL;
    }

    public void setNotifyURL(String notifyURL) {
            this.notifyURL= notifyURL;
    }
    
     public String getcallbackData() {
            return callbackData;
    }

    public void setcallbackData(String callbackData) {
            this.callbackData = callbackData;
    }
    
}
