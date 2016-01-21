package com.gsma.authenticators.dialog.sms;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by paraparan on 5/13/15.
 */

@XmlRootElement(name = "outboundSMSMessageRequest")
public class SendSMSRequest {
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
