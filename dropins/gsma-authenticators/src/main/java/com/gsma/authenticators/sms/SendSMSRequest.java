/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gsma.authenticators.sms;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "outboundSMSMessageRequest")
public class SendSMSRequest { //implements ISMSresponse {
    private OutboundSMSMessageRequest outboundSMSMessageRequest;
    
    public SendSMSRequest() {
    }


    public OutboundSMSMessageRequest getOutboundSMSMessageRequest() {
            return outboundSMSMessageRequest;
    }


    public void setOutboundSMSMessageRequest(OutboundSMSMessageRequest receiptRequest) {
            this.outboundSMSMessageRequest = receiptRequest;
    }
    
}
