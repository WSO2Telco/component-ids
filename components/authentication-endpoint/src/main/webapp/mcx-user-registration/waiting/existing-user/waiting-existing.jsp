<%@ page import="com.wso2telco.identity.application.authentication.endpoint.util.ReadMobileConnectConfig" %>
<%@ page import="javax.xml.parsers.ParserConfigurationException" %>
<%@ page import="org.xml.sax.SAXException" %>
<%@ page import="javax.xml.xpath.XPathExpressionException" %>

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
					Boolean showSMSLink = false;
					if(authenticators != null && authenticators.contains("SMSAuthenticator")) {
				%>
				{{continue-on-device-intro-sms}}
				<%} else if (authenticators != null && authenticators.contains("USSDAuthenticator")) {
					showSMSLink = true; %>
				{{continue-on-device-intro-ussd}}
				<%} else if (authenticators != null && authenticators.contains("USSDPinAuthenticator")){
					showSMSLink = true; %>
				{{continue-on-device-intro-ussd-pin}}
				<%} else {%>
				{{continue-on-device-intro-default}}
				<%}%>

                <%
                    String acr = request.getParameter("acr_values");
                    if (acr.equals("3")) {
                        showSMSLink = false;
                    }
                %>

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
		<% if (showSMSLink) { %>
		<%  ReadMobileConnectConfig readMobileConnectConfig = new ReadMobileConnectConfig();
			String fallbackPrefix = readMobileConnectConfig.query("SMS").get("FallbackPrefix");
		%>
		<p class="page__copy flush">{{ussd-sent-resend-sms-prompt}}
			<br>
			<a onclick="sendSMS('<%=request.getParameter("sessionDataKey")%>');" style="cursor:pointer"><u>
				{{ussd-sent-resend-sms-button}}
			</u></a>
		</p>
		<br>
		<br>
		<%} %>
		<a onclick="handleTermination(true);" class="btn btn--outline btn--full btn--large">
			{{misc-cancel-button}}
		</a>
	</main>
</script>
<script src="mcx-user-registration/js/waiting/existing-user/waiting.js"></script>
