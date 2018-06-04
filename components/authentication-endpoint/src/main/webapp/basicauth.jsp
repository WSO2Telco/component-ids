<%--
  ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ page import="org.owasp.encoder.Encode" %>

<form action="../commonauth" method="post" id="loginForm">
    <script>document.title = "Login";</script>
	 <div class="login-container">
        <div class="login-wrapper">
            <div class="logo-container_login">
                <img class="image_login" alt="store_logo" src="images/logo-white.png">
            </div>
            <div class="form-container">
                <div class="header-login">Sign in to your account</div>
                <form>
                    <div class="col-xs-12 col-sm-12 form-group">
						  <input id="username" name="username" type="text" class="form-control" tabindex="0" placeholder="Username" style="width:100%">
                    </div>
                    <div class="col-xs-12 col-sm-12 form-group">
                        <input id="password" name="password" type="password" class="form-control" placeholder="Password" autocomplete="off" style="width:100%">
                    </div>
					 <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute
            (request.getParameter("sessionDataKey"))%>'/>
                    <div class="col-xs-12 col-sm-12 form-group">
                        <button id="signin" name="signin" class="btn-saml" type="submit">
                            Sign in
                        </button>
                    </div>

                    <div class="col-sm-12 acc-actions">
                        <div class="stay-sign-in">
                            <label class="chk-rememberme-lbl" for="chkRemember">
                                <input type="checkbox" name="chkRemember"> Remember me on this computer</label>
                        </div>
                    </div>

					 <div class="col-sm-12 acc-actions">
        <%if(request.getParameter("relyingParty").equals("wso2.my.dashboard")) { %>
        <a id="registerLink" href="create-account.jsp?sessionDataKey=<%=Encode.forHtmlAttribute
            (request.getParameter("sessionDataKey"))%>" class="font-large">Create an
            account</a>
        <%} %>
    </div>
    <div class="clearfix"></div>
                </form>
            </div>
			<% if (Boolean.parseBoolean(loginFailed)) { %>
<div class="form-container errorDiv">
<div class="col-xs-12 col-sm-12 form-group" style="height:10px;text-align: center;">

<div class="alert alert-danger" id="error-msg"><%= Encode.forHtml(errorMessage) %></div>
</div><%}else if((Boolean.TRUE.toString()).equals(request.getParameter("authz_failure"))){%>
<div class="form-container errorDiv">
<div class="col-xs-12 col-sm-12 form-group" style="height:10px;text-align: center;">

<div class="alert alert-danger" id="error-msg">You are not authorized to login
</div>
</div><%}%>
        </div>
    </div>
</form>

