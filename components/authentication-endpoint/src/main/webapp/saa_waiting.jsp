<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.ReadMobileConnectConfig" %>
<input type="hidden" name="sessionDataKey" id="sessionDataKey" value='<%=request.getParameter("sessionDataKey")%>'/>
<body class="theme--dark">
<div class="site__root" id="content-placeholder">

</div>
</body>

<!-- The handlebar template -->
<script id="results-template" type="text/x-handlebars-template">
    <main class="site__main site__wrap section v-distribute">
        <header class="page__header">

            <%
                String authenticators = request.getParameter("authenticators");
                String acr = request.getParameter("acr_values");
                String message = ReadMobileConnectConfig.readSaaConfig("PushMessageLOA_" + acr);
            %>

            <h1 class="page__heading" id="pageHeading">
                <%=message%>
            </h1>
            <%--<p>--%>

            <%--<% --%>
            <%--Boolean showSMSLink = false;--%>
            <%--String scope = request.getParameter("scope");--%>
            <%--String sp = request.getParameter("sp");--%>
            <%--String telco_scope = request.getParameter("telco_scope");--%>
            <%----%>
            <%--if(authenticators != null && authenticators.contains("SMSAuthenticator")) {--%>
            <%--if (scope != null && scope.contains("mnv")){%>--%>
            <%--{{continue-on-device-intro-sms-mnv}}--%>
            <%--<%}else{%>--%>
            <%--{{continue-on-device-intro-sms}}--%>
            <%--<%}--%>
            <%--} else if (authenticators != null && authenticators.contains("USSDAuthenticator")) { --%>
            <%--showSMSLink = true; --%>
            <%--if (scope != null && scope.contains("mnv")){%>--%>
            <%--{{continue-on-device-intro-default}}--%>
            <%--<%}else{%>--%>
            <%--{{continue-on-device-intro-default}}--%>
            <%--<%}   		--%>
            <%--} else if (authenticators != null && authenticators.contains("USSDPinAuthenticator")){--%>
            <%--if (scope != null && telco_scope.contains("mnv")){--%>
            <%--showSMSLink = false;--%>
            <%--}%>--%>
            <%--{{continue-on-device-intro-default}}		--%>
            <%--<%} else {%>--%>
            <%--{{continue-on-device-intro-default}}--%>
            <%--<%}%>--%>


            <%--</p>--%>
        </header>

        <div class="page__illustration v-grow v-align-content">
            <div>
                <div style="overflow-x:auto;" align="center">
                    <%--<table cellpadding>--%>
                    <%--<tr id="flashmsg_container" style="display:block;">--%>
                    <%--<td>--%>
                    <%--<% if (showSMSLink) { %>--%>
                    <%--<img src="images/login-user-guide.gif" style="margin-left:2rem;" alt="user guide"--%>
                    <%--width="150" height="185">--%>
                    <%--<%--%>
                    <%--}--%>
                    <%--if (authenticators.contains("USSDPinAuthenticator")) {--%>
                    <%--%>--%>
                    <%--<img src="images/pinuserguide.gif" style="margin-left:2rem;" alt="user guide"--%>
                    <%--width="150" height="185">--%>
                    <%--<%} %>--%>
                    <%--</td>--%>

                    <%--</tr>--%>
                    <%--<% if (showSMSLink) { %>--%>
                    <%--<%} %>--%>
                    <%--</table>--%>
                    <div class="error-copy space--bottom hide" id="timeout-warning">
                        {{continue-on-device-timeout}}
                    </div>
                </div>
            </div>
        </div>

        <div class="error-copy space--bottom hide" id="timeout-warning">
            {{continue-on-device-timeout}}
        </div>
    </main>
</script>
<script src="js/waiting.js?20161104"></script>
<script type="text/javascript">
    window.onload = function () {
        setTimeout(function () {
            document.getElementById('smslink_container').style.visibility = "visible";
            document.getElementById('flashmsg_container').style.display = 'none';
        }, 10000);
    }
</script>