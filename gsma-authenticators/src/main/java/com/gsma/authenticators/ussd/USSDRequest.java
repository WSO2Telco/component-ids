package com.gsma.authenticators.ussd;

import com.gsma.authenticators.model.OutboundUSSDMessageRequest;


public class USSDRequest {
	
	private OutboundUSSDMessageRequest outboundUSSDMessageRequest = null;

	public OutboundUSSDMessageRequest getOutboundUSSDMessageRequest() {
		return outboundUSSDMessageRequest;
	}

	public void setOutboundUSSDMessageRequest(OutboundUSSDMessageRequest outboundUSSDMessageRequest) {
		this.outboundUSSDMessageRequest = outboundUSSDMessageRequest;
	}
}
