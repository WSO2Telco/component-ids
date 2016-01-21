/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gsma.authenticators.dialog.sms;

/**
 *
 * @author tharanga_07219
 */
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
