<%@page import="org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants"%>
<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>Federation JSP</title>
</head>
<body>
	</br>
<%
    String code = request.getParameter("code");
String state = request.getParameter("state");
String erroDisctription = request.getParameter("error_description");
String error = request.getParameter("error");
String redirectURL = null;

    if (error == null)
        redirectURL = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, true, true) + "?sessionDataKey="
                + state + "&code=" + code;
    else
        redirectURL = (erroDisctription != null) ? IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH,
                true, true)
                + "?sessionDataKey="
                + state
                + "&error="
                + error
                + "&error_description="
                + erroDisctription : IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, true, true)
                + "?sessionDataKey=" + state + "&error=" + error;

    response.sendRedirect(redirectURL);
%>
</body>
</html>