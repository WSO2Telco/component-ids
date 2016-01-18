/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gsma.authenticators.sms;

import java.util.List;

/**
 *
 * @author Dialog
 */
public class SendSMSEntity {
    
    private String message = "";
    private List<String> destinationAddresses;
    private String password = "";
    private String applicationId = "";
    
    
    public SendSMSEntity() {
    }
    
    public String getMessage() {
            return message;
    }

    public void setMessage(String message) {
            this.message = message;
    }
    
    public List<String> getAddress() {
            return destinationAddresses;
    }

    public void setAddress(List<String> destinationAddresses) {
            this.destinationAddresses = destinationAddresses;
    }
    
    
    public String getPassword() {
            return password;
    }

    public void setPassword(String password) {
            this.password = password;
    }
    
    
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    
    
}
