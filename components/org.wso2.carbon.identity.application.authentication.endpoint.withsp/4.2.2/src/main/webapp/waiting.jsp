
<input type="hidden" name="sessionDataKey" id="sessionDataKey" value='<%=request.getParameter("sessionDataKey")%>'/>
<div class="site__root" id="content-placeholder">

    
</div>
  
  <!-- The handlebar template -->
<script id="results-template" type="text/x-handlebars-template">
	<main class="site__main site__wrap section v-distribute">
      <header class="page__header">
        <h1 class="page__heading">
				{{continue-on-device-heading}}
		</h1>
        <p>

	    	<% 
	    	String authenticators = request.getParameter("authenticators");
			Boolean smsotp =false;
	    	if(authenticators != null && authenticators.contains("SMSOTPAuthenticator")) {
				smsotp=true;
	    	%>
	    		{{continue-on-device-intro-otp-sms}}
	    	<%} else if(authenticators != null && authenticators.contains("SMSAuthenticator")) { %>
				{{continue-on-device-intro-sms}}
			<%} else if (authenticators != null && authenticators.contains("USSDAuthenticator")) { %>
	    		{{continue-on-device-intro-ussd}}
	    	<%} else if (authenticators != null && authenticators.contains("USSDPinAuthenticator")){%>
	    		{{continue-on-device-intro-ussd-pin}}

	    	<%} else {%>
	    		{{continue-on-device-intro-default}}
	    	<%} %>

        </p>
      </header>

      <div class="page__illustration v-grow v-align-content">
        <div>

          <div class="timer-spinner-wrap">
            <div class="timer-spinner">
              <div class="pie spinner"></div>
              <div class="pie filler"></div>
              <div class="mask"></div>
            </div>
            <img src="images/svg/phone-pin.svg" width="52" height="85">
          </div>
        </div>
      </div>
      <div class="error-copy space--bottom hide" id="timeout-warning">
        {{continue-on-device-timeout}}
      </div>
      <% if (authenticators != null && (authenticators.contains("USSDAuthenticator"))) { %> 
			<p class="page__copy flush">{{ussd-sent-resend-sms-prompt}}
			  <br>
			  <a onclick="sendSMS();" style="cursor:pointer"><u>
				{{ussd-sent-resend-sms-button}}
			  </u></a>
			</p>
			<br>
			<br>
		<%}
			if (smsotp) { %>
		<div>
			<input id="smsotp" type="number" name="smsotp"  placeholder="Enter OTP in SMS" />
			<a onclick="sendSMSOTP('<%=sessionDataKey%>');" class="btn btn--outline btn--full btn--large">
				{{misc-submit-button}}
			</a>
		</div>
		<%	} %>
      <a onclick="handleTermination();" class="btn btn--outline btn--full btn--large">
			{{misc-cancel-button}}
		</a>
    </main>
</script>
<script src="js/waiting.js"></script>

