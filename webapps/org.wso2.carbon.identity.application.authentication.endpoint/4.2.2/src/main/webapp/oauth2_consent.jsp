<!DOCTYPE html>
<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@page import="java.util.Hashtable"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap" %>
<%@page import="java.io.*" %>
<%@page import="org.apache.tools.ant.util.StringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.CharacterEncoder"%>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants"%>

<%@page import="org.wso2.carbon.identity.application.authentication.endpoint.oauth2.OAuth2Login"%>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.Constants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
	String app = OAuth2Login.getSafeText(request.getParameter("application"));
%>
<%@ page import = "org.wso2.carbon.identity.application.authentication.endpoint.Constants" %>

<%@ page import = "org.wso2.carbon.identity.application.authentication.endpoint.config.ConfigLoader" %>
<%@ page import = "org.wso2.carbon.identity.application.authentication.endpoint.config.ScopeConfigs" %>
<%@ page import = "org.wso2.carbon.identity.application.authentication.endpoint.config.DataHolder" %>
<%@ page import = "org.wso2.carbon.identity.application.authentication.endpoint.config.Scope" %>
<%@ page import = "org.wso2.carbon.identity.application.authentication.endpoint.config.Scopes" %>
<%@ page import = "org.wso2.carbon.identity.application.authentication.endpoint.config.Claims" %>
<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources">
<html lang="en">
<head>

    <meta charset="utf-8">
    <title>WSO2 Identity Server OAuth2.0 Consent</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
     <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->


        <link href="/authenticationendpoint/assets/css/bootstrap.min.css" rel="stylesheet">
        <link href="/authenticationendpoint/css/localstyles.css" rel="stylesheet">
        <!--[if lt IE 8]>
        <link href="css/localstyles-ie7.css" rel="stylesheet">
        <![endif]-->
	<link href="/authenticationendpoint/css/styles-axiata.css" rel="stylesheet">
    <!--[if lt IE 8]>
    <link href="css/localstyles-ie7.css" rel="stylesheet">
    <![endif]-->

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="assets/js/html5.js"></script>
    <![endif]-->
    <script src="assets/js/jquery-1.7.1.min.js"></script>
    <script src="js/scripts.js"></script>
</head>

<body>

<div>&nbsp;</div>
<div class="header-back">
    <div class="container">
        <div class="row">
            <div class="span12">
                <a class="logo">&nbsp</a>
            </div>
        </div>
    </div>
</div>
    <div>
	    <div class="container">
	        <div class="row">
	            <div class="span12 content-section">
  <span><fmt:message key='ussd.waiting.message'/></span>
  <h4><fmt:message key='msisdn'/> :
<%String mobno[]=request.getParameter("loggedInUser").split("@");
out.println(mobno[0]);

%></h4>

	            </div>
	        </div>
	   </div>
    </div>

    <div class="container" style="margin-top:10px;">
        <div class="row">
            <div class="span12 content-section">
                <h3 style="text-align:left;margin-bottom:10px;"></h3>
                <script type="text/javascript">
	                function approved() {
	                	 document.getElementById('consent').value="approve";
	                	 document.getElementById("profile").submit();
	                }
	                function approvedAlways() {
	                	 document.getElementById('consent').value="approveAlways";
	                	 document.getElementById("profile").submit();
	                }
	                function deny() {
	                	 document.getElementById('consent').value="deny";
	                	 document.getElementById("profile").submit();
	                }
	            </script>


<%

DataHolder.getInstance().setScopeConfigs(ConfigLoader.getInstance().getScopeConfigs());
ScopeConfigs scopeConfigs = DataHolder.getInstance().getScopeConfigs();


String[] scopes = request.getParameter("scope").split(" ");
Map<String, Object> requestedClaims = new HashMap<String, Object>();
String[] attributes;
List<String> list=new ArrayList<String>();
if(scopeConfigs !=null){
    for (String scopeName : scopes) {
        for(Scope scope: scopeConfigs.getScopes().getScopeList()){
            if(scopeName.equals(scope.getName())){
                attributes = new String[scope.getClaims().getClaimValues().size()];
                requestedClaims.put(scopeName, scope.getClaims().getClaimValues());
                list.addAll(scope.getClaims().getClaimValues());
                break;
            }
        }
    }
}
if(!list.isEmpty()){ %>
<strong><%=app%></strong> will receive the following info :
<%
out.println(list.toString().replace("[", "").replace("]", "") );
}

%>
<br/>
<br/>

	            <form id="profile" name="profile" method="post" action="../oauth2/authorize">

                     <input type="button" class="btn btn-primary btn-large" id="approve" name="approve"
                                           onclick="javascript: approved(); return false;"
                                           value="Approve"/>
                     <input type="button" class="btn btn-primary btn-large" id="approveAlways" name="approveAlways"
                                           onclick="javascript: approvedAlways(); return false;"
                                           value="Approve Always"/>
                     <input class="btn btn-primary-deny btn-large" type="reset"
								value="Deny" onclick="javascript: deny(); return false;" />

					<input type="hidden" name="<%=Constants.SESSION_DATA_KEY_CONSENT%>"
								value="<%=request.getParameter(Constants.SESSION_DATA_KEY_CONSENT)%>" />
					<input type="hidden" name="consent" id="consent"
								value="deny" />

	            </form>
            </div>
        </div>
    </div>




</body>
</html>
</fmt:bundle>