<!DOCTYPE html>
<!-- login.jsp-->
<!--
~ Copyright (c) 2005-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@page import="java.util.Arrays" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.List" %>
<%@page import="java.net.HttpURLConnection" %>
<%@page import="java.net.URL" %>
<%@page import="java.io.BufferedReader" %>
<%@page import="java.io.InputStreamReader" %>
<%@page import="java.io.IOException" %>
<%@page import="java.net.URLEncoder" %>
<%@page import="org.apache.log4j.Logger" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.wso2.carbon.identity.oauth2.model.OAuth2Parameters" %>
<!-- container -->
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page import="org.wso2.carbon.identity.oauth2.model.OAuth2Parameters" %>
<%@ page import="org.wso2.carbon.identity.oauth.common.OAuthConstants" %>
<%@ page import="org.wso2.carbon.identity.oauth.cache.*" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.cache.*" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.model.*" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.util.*" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.context.*" %>

<%! static Logger logger = Logger.getLogger(login_jsp.class); %>

<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources">

	<html class="site no-js lang--en" lang="en">
	<input type="hidden" id="baseURL"
		   value='<%=request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath() %>'>
	<input type="hidden" id="baseURLWithPort"
		   value='<%=request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort() %>'>

	<head>
		<meta charset="utf-8">
		<meta http-equiv="x-ua-compatible" content="ie=edge">
		<title>Mobile Connect</title>
		<meta name="description" content="">
		<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no">

		<link rel="apple-touch-icon" href="apple-touch-icon.png">
		<link rel="stylesheet" href="css/style.css">

		<!-- load main script early asyncronously -->
		<script src="assets/js/jquery-1.7.1.min.js"></script>
		<script src="assets/js/handlebars-1.0.rc.1.js"></script>
		<script src="assets/js/parsley.min.js"></script>
		<script src="js/scripts.js"></script>
		<!--<script type="text/javascript" src="assets/js/main.js" async></script>-->


		<noscript>
			<!-- Fallback synchronous download, halt page rendering if load is slow  -->
			<link href="//fonts.googleapis.com/css?family=Roboto:400,300,700,400italic,300italic,700italic"
				  rel="stylesheet" type="text/css">
		</noscript>
		<!-- loads fonts asyncronously preventing font loading from block page render -->
		<script>
			// Config for loading the web fonts
			var WebFontConfig = {
				google: {
					families: ['Roboto:400,300,700,400italic,300italic,700italic']
				},
				active: function () {
					// Set a cookie that the font has been downloaded and should be cached
					var d = new Date();
					d.setTime(d.getTime() + (7 * 86400000)); // plus 7 Days
					document.cookie = "cachedroboto=true; expires=" + d.toGMTString() + "; path=/";
				}
			};
		</script>
		<script src="js/vendor/webfontloader.js"></script>
		<!-- Adds IE root class without breaking doctype -->
		<!--[if IE]>
		<script>document.documentElement.className = document.documentElement.className + " ie";</script>
		<![endif]-->
		<script type="text/javascript" src="assets/js/modernizr.js"></script>

		<%

			String operator = request.getParameter("operator") != null ? request.getParameter("operator") : "";
			if (!operator.isEmpty()) {
		%>
		<link href="/authenticationendpoint/css/branding/<%=operator%>-style.css" rel="stylesheet">
		<%}%>

	</head>

	<body>

	<%
		String sessionDataKey = request.getParameter("sessionDataKey");

		AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(sessionDataKey);
		Object cacheEntryObj = AuthenticationContextCache.getInstance().getValueFromCache(cacheKey);
		AuthenticationContext authnContext = null;
		String requestURL = request.getRequestURL().toString();

		String requestURI = request.getRequestURI();

		String baseURL = requestURL.substring(0, requestURL.indexOf(requestURI));

		if (cacheEntryObj != null) {
			authnContext = ((AuthenticationContextCacheEntry) cacheEntryObj).getContext();
		}
		String msisdn = null;
		try {
			msisdn = (String) authnContext.getProperty("msisdn");
		} catch (Exception e) {
			logger.info("msisdn is not available in AuthenticationContext");
		}

		AuthenticationRequest authRequest = authnContext.getAuthenticationRequest();
		Map<String, String[]> paramMap = authRequest.getRequestQueryParams();

		String queryString = request.getQueryString();
		request.getSession().invalidate();

		Map<String, String> idpAuthenticatorMapping = null;
		if (request.getAttribute("idpAuthenticatorMap") != null) {
			idpAuthenticatorMapping = (Map<String, String>) request.getAttribute("idpAuthenticatorMap");
		}

		String errorMessage = "Authentication Failed! Please Retry";
		String loginFailed = "false";

		if (request.getParameter(Constants.AUTH_FAILURE) != null &&
				"true".equals(request.getParameter(Constants.AUTH_FAILURE))) {
			loginFailed = "true";
			if (true) {

				if (request.getParameter(Constants.AUTH_FAILURE_MSG) != null) {
					errorMessage = request.getParameter(Constants.AUTH_FAILURE_MSG);

					if (errorMessage.equalsIgnoreCase("login.fail.message")) {
						if (request.getParameter("acr_values") != null) {
							String state = "";
							String nonce = "";
							try {
								state = paramMap.get("state")[0];

								nonce = paramMap.get("nonce")[0];

							} catch (Exception e) {

								logger.info("Exception " + e);

							}
							String clientId = paramMap.get("client_id")[0];

							String redirectUri = paramMap.get("redirect_uri")[0];

							String scope = paramMap.get("scope")[0];

							String arc = request.getParameter("acr_values");

							String responseType = paramMap.get("response_type")[0];

							String authenticator = request.getParameter("authenticators");

							String msisdn_header_str = request.getParameter("msisdn_header");
							Boolean msisdn_header = false;
							if (msisdn_header_str != null && !msisdn_header_str.equals("")) {
								msisdn_header = true;
							}
							if (msisdn == null) {
								Cookie[] cookies = request.getCookies();
								if (cookies != null) {
									for (Cookie cookie : cookies) {
										if ((cookie.getName()).compareTo("msisdn") == 0) {
											msisdn = cookie.getValue();

										}
									}
								}
							}
							String token = null;
							//String requestURL = request.getRequestURL().toString();

							//String requestURI = request.getRequestURI();

							//String baseURL = requestURL.substring(0, requestURL.indexOf(requestURI));

							URL url = new URL(baseURL + "/user-registration/webresources/endpoint/user/authenticate/add?scope=" + URLEncoder.encode(scope, "UTF-8") + "&redirecturi=" + redirectUri + "&clientid=" + clientId + "&acrvalue=" + arc + "&responsetype=" + responseType + "&operator=" + operator + "&msisdn=" + msisdn + "&nonce=" + nonce + "&state=" + state);
							logger.debug("Url " + url + "clientId" + clientId + "redirectUri: " + redirectUri + "scope" + scope + " arc : " + arc);
							HttpURLConnection conn = (HttpURLConnection) url.openConnection();
							conn.setRequestMethod("GET");
							conn.setRequestProperty("Accept", "application/json");
							if (conn.getResponseCode() != 200) {
								throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
							} else {
								BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
								token = br.readLine();
								conn.disconnect();
								String site = new String("mcx-user-registration/register.jsp?sessionDataKey=" + Encode.forUriComponent(request.getParameter("sessionDataKey")) + "&operator=" + operator + "&acr=" + arc + "&msisdn_header=" + msisdn_header + "&msisdn_header_enc_str=" + Encode.forUriComponent(msisdn_header_str)) + "&msisdn_header_str=" + msisdn;
								response.setStatus(response.SC_MOVED_TEMPORARILY);
								response.setHeader("Location", site);
							}
						} else {
							errorMessage = "You are not a registered user. Please register and try again";
						}
					}
				}
			} else {
				errorMessage = "Authentication failed.Please try again";
			}

		}

		boolean hasLocalLoginOptions = false;
		List<String> localAuthenticatorNames = new ArrayList<String>();

		if (idpAuthenticatorMapping.get(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME) != null) {
			String authList = idpAuthenticatorMapping.get(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME);
			if (authList != null) {
				localAuthenticatorNames = Arrays.asList(authList.split(","));
			}
		}

		for(String names : localAuthenticatorNames){
		}
	%>
	<input type="hidden" id="msisdn" value='<%=msisdn%>'>
	<header class="site-header">
		<div class="site-header__inner site__wrap">
			<h1 class="visuallyhidden">Mobile&nbsp;Connect</h1>
			<a href="mcx-user-registration/selfcare/index.html"><img src="images/svg/mobile-connect.svg"
																	 alt="Mobile Connect&nbsp;Logo" width="150"
																	 class="site-header__logo"></a>
			<% if (!operator.isEmpty()) {
				String imgPath = "mcx-user-registration/images/branding/" + operator + "_logo.svg";
			%>
			<p class="site-header__powered-by">powered&nbsp;by

			</p>
			<a class="brand">
				<img class="brandLogo" src='<%= imgPath %>' alt='<%= operator %>'>
			</a>
			<% } %>
			<!--form action="/lang" class="site-header__lang-menu field--select field--select-plain" novalidate>
              <label for="field-select-lang" class="visuallyhidden">Language:</label>
              <select id="field-select-lang" name="lang" class="field__select-native js-transparent">
                <option value="en" selected>English&nbsp;(UK)</option>
                <option value="de">Deutsche</option>
                <option value="th">urdu</option>
              </select>
              <input type="hidden" name="return-url" value="/registration/">
              <input type="submit" value="Go" class="btn btn--natural btn--light js-visuallyhidden">
            </form>-->
		</div>
	</header>
	<form action="../../commonauth" method="post" id="loginForm" class="form-horizontal" data-parsley-validate
		  novalidate>
		<%if (localAuthenticatorNames.contains("BasicAuthenticator")) { %>
		<div id="local_auth_div">
					<%} %>

					<% if ("true".equals(loginFailed)) { %>
			<div class="parsley-errors-list filled" style="text-align: center">
				<%=Encode.forHtmlContent(errorMessage)%>
			</div>
					<% } %>
			<div class="container">
				<%

					if (localAuthenticatorNames.size() > 0) {
						if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("OpenIDAuthenticator")) {
							hasLocalLoginOptions = true;
				%>

				<div class="row">
					<div class="span12">

						<%@ include file="openid.jsp" %>

					</div>
				</div>

				<%
				} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("BasicAuthenticator")) {
					hasLocalLoginOptions = true;
					if (TenantDataManager.isTenantListEnabled() && "true".equals(request.getParameter("isSaaSApp"))) {
				%>
				<div class="row">
					<div class="span12">

						<%@ include file="tenantauth.jsp" %>

					</div>
				</div>

				<script>
					//set the selected tenant domain in dropdown from the cookie value
					window.onload = selectTenantFromCookie;
				</script>

				<%
				} else {
				%>
				<div class="row">
					<div class="span12">
						<%@ include file="basicauth.jsp" %>

					</div>
				</div>
				<%
					}
				%>

				<%
				} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("PinAuthenticator")) {
					hasLocalLoginOptions = true;
				%>
				<div class="row">
					<div class="span12">

						<%@ include file="mcx-user-registration/pin.jsp" %>

					</div>
				</div>
				<%
				} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("MSISDNAuthenticator")) {
					hasLocalLoginOptions = true;
				%>

				<div class="row">
					<div class="span12">

						<%@ include file="mcx-user-registration/msisdn.jsp" %>

					</div>
				</div>
				<%
				} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("WhiteListMSISDNAuthenticator")) {
					hasLocalLoginOptions = true;
				%>

				<div class="row">
					<div class="span12">


					</div>
				</div>
				<%
				} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("MSSAuthenticator")) {
					hasLocalLoginOptions = true;
				%>

				<div class="row">
					<div class="span12">
						<%@ include file="mcx-user-registration/waiting/existing-user/waiting-existing.jsp" %>

					</div>
				</div>

				<%

				} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("MSSPinAuthenticator")) {
					hasLocalLoginOptions = true;
				%>

				<div class="row">
					<div class="span12">
						<%@ include file="mcx-user-registration/waiting/existing-user/waiting-existing.jsp" %>

					</div>
				</div>

				<%


				} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("GSMAMSISDNAuthenticator")) {
					hasLocalLoginOptions = true;
				%>

				<div class="row">
					<div class="span12">

						<%@ include file="mcx-user-registration/gsmamsisdn.jsp" %>

					</div>
				</div>

				<%
				} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("USSDAuthenticator")) {
					hasLocalLoginOptions = true;
				%>

				<div class="row">
					<div class="span12">

						<%@ include file="mcx-user-registration/waiting/existing-user/waiting-existing.jsp" %>

					</div>
				</div>
				<%
				} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("USSDPinAuthenticator")) {
					hasLocalLoginOptions = true;
				%>

				<div class="row">
					<div class="span12">

						<%@ include file="mcx-user-registration/waiting/existing-user/waiting-existing.jsp" %>

					</div>
				</div>

				<%
				} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("MePinAuthenticatorPIN")) {
					hasLocalLoginOptions = true;
				%>

				<div class="row">
					<div class="span12">

						<%@ include file="mcx-user-registration/waiting/existing-user/waiting-existing.jsp" %>

					</div>
				</div>


				<%
				} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("HeaderEnrichmentAuthenticator")) {
					hasLocalLoginOptions = true;
				%>

				<div class="row">
					<div class="span6">

						<%@ include file="mcx-user-registration/headerauth.jsp" %>

					</div>
				</div>


				<%
				} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("SMSAuthenticator")) {
					hasLocalLoginOptions = true;
				%>
				<div class="row">
					<div class="span12">

						<%@ include file="mcx-user-registration/waiting/existing-user/waiting-existing.jsp" %>

					</div>
				</div>

				<%
                } else if(localAuthenticatorNames.size()>0 && localAuthenticatorNames.contains("SmartPhoneAppAuthenticator")) {
                    hasLocalLoginOptions = true;
                %>
                <div class="row">
                    <div class="span12">

                        <%@ include file="saa_waiting.jsp" %>

                    </div>
                </div>
                <%
                        }
					}

					if ((hasLocalLoginOptions && localAuthenticatorNames.size() > 1) || (!hasLocalLoginOptions)
							|| (hasLocalLoginOptions && idpAuthenticatorMapping.size() > 1)) {
				%>
				<div class="row">
					<div class="span12">
						<% if (hasLocalLoginOptions) { %>
						<h2>Other login options:</h2>
						<%} else { %>
						<script type="text/javascript">
							document.getElementById('local_auth_div').style.display = 'block';
						</script>
						<%} %>
					</div>
				</div>

				<div class="row">

					<%
						for (Map.Entry<String, String> idpEntry : idpAuthenticatorMapping.entrySet()) {
							if (!idpEntry.getKey().equals(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME)) {
								String idpName = idpEntry.getKey();
								boolean isHubIdp = false;
								if (idpName.endsWith(".hub")) {
									isHubIdp = true;
									idpName = idpName.substring(0, idpName.length() - 4);
								}
					%>
					<div class="span6">
						<% if (isHubIdp) { %>
						<a href="#" class="main-link"><%=idpName%>
						</a>
						<div class="slidePopper" style="display:none">
							<input type="text" id="domainName" name="domainName"/>
							<input type="button" class="btn btn-primary go-btn"
								   onClick="javascript: myFunction('<%=idpName%>','<%=idpEntry.getValue()%>','domainName')"
								   value="Go"/>
						</div>
						<%} else { %>
						<a onclick="javascript: handleNoDomain('<%=idpName%>','<%=idpEntry.getValue()%>')"
						   class="main-link truncate" style="cursor:pointer" title="<%=idpName%>"><%=idpName%>
						</a>
						<%} %>
					</div>
					<%
					} else if (localAuthenticatorNames.size() > 0 && localAuthenticatorNames.contains("IWAAuthenticator")) {
					%>
					<div class="span6">
						<a onclick="javascript: handleNoDomain('<%=idpEntry.getKey()%>','IWAAuthenticator')"
						   class="main-link" style="cursor:pointer">IWA</a>
					</div>
					<%
							}

						}%>


				</div>
				<% } %>
			</div>
	</form>


	<div id="push"></div>
	<script>
		$(document).ready(function () {
			$('.main-link').click(function () {
				$('.main-link').next().hide();
				$(this).next().toggle('fast');
				var w = $(document).width();
				var h = $(document).height();
				$('.overlay').css("width", w + "px").css("height", h + "px").show();
			});
			$('.overlay').click(function () {
				$(this).hide();
				$('.main-link').next().hide();
			});

		});
		function myFunction(key, value, name) {
			var object = document.getElementById(name);
			var domain = object.value;


			if (domain != "") {
				document.location = "../../commonauth?idp=" + key + "&authenticator=" + value + "&sessionDataKey=<%=Encode.forUriComponent(request.getParameter("sessionDataKey"))%>&domain=" + domain;
			} else {
				document.location = "../../commonauth?idp=" + key + "&authenticator=" + value + "&sessionDataKey=<%=Encode.forUriComponent(request.getParameter("sessionDataKey"))%>";
			}
		}

		function handleNoDomain(key, value) {


			document.location = "../../commonauth?idp=" + key + "&authenticator=" + value + "&sessionDataKey=<%=Encode.forUriComponent(request.getParameter("sessionDataKey"))%>";

		}

		function saveLink() {
			var url = window.location.href;
			var date = new Date();
			var expiryMins = 5;
			date.setTime(date.getTime() + (expiryMins * 60 * 1000));
			var expires = "expires=" + date.toUTCString();
			document.cookie = "loginRequestURL=" + encodeURIComponent(url) + "; path=/; " + expires;
			return true;
		}

	</script>

	</body>
	</html>

</fmt:bundle>
