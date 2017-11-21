<!doctype html>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="javax.servlet.jsp.jstl.core.Config" %>
<html class="site no-js lang--en" lang="en">

<head>
  <meta charset="utf-8">
  <meta http-equiv="x-ua-compatible" content="ie=edge">
  <title>Mobile Connect</title>
  <meta name="description" content="">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no">

  <link rel="apple-touch-icon" href="apple-touch-icon.png">
  <link rel="stylesheet" href="../css/style.css">


  <!-- load main script early asyncronously -->
  <script type="text/javascript" src="mcresources/js/main.js" async></script>
  <script src="js/public/js/jquery.min.js" type="text/javascript"></script>
  <script src="js/public/js/main.js" type="text/javascript"></script>
  <script src="js/public/js/modal.js" type="text/javascript"></script>
  <script src="js/landing.js" type="text/javascript"></script>



  <noscript>
    <!-- Fallback synchronous download, halt page rendering if load is slow  -->
    <link href="//fonts.googleapis.com/css?family=Roboto:400,300,700,400italic,300italic,700italic" rel="stylesheet" type="text/css">
  </noscript>
  <!-- loads fonts asyncronously preventing font loading from block page render -->
  <script>
    // Config for loading the web fonts
    var WebFontConfig = {
      google: {
        families: ['Roboto:400,300,700,400italic,300italic,700italic']
      },
      active: function() {
        // Set a cookie that the font has been downloaded and should be cached
        var d = new Date();
        d.setTime(d.getTime() + (7 * 86400000)); // plus 7 Days
        document.cookie = "cachedroboto=true; expires=" + d.toGMTString() + "; path=/";
      }
    };
    </script>
    <script src="mcresources/js/vendor/webfontloader.js"></script>
    <!-- Adds IE root class without breaking doctype -->
  <!--[if IE]>
        <script>document.documentElement.className = document.documentElement.className + " ie";</script>
        <![endif]-->
        <script type="text/javascript" src="mcresources/js/vendor/modernizr.js"></script>
        <%
        String acr = request.getParameter("http://wso2.org/claims/loa")!= null ?  request.getParameter("http://wso2.org/claims/loa") : "";
        String sessionDataKey = request.getParameter("sessionDataKey")!= null ?  request.getParameter("sessionDataKey") : "";
        String operator = request.getParameter("operator")!= null ?  request.getParameter("operator") : "";
        String msisdn = request.getParameter("msisdn")!= null ?  request.getParameter("msisdn") : "";
        String acr_code = request.getParameter("acr_code")!= null ?  request.getParameter("acr_code") : "";
        // Boolean smsClick = Boolean.valueOf(request.getParameter("smsClick"))!= null ?  Boolean.valueOf(request.getParameter("smsClick")) : false;
        String smsClick = request.getParameter("smsClick")!= null ?  request.getParameter("smsClick") : "false";
        if(operator != ""){
        %>
        <link href="css/branding/<%=operator%>-style.css" rel="stylesheet">
        <%}%>
        <script type="text/javascript">
        var values = {};
        values["msisdn"] = "<%=msisdn%>";
        values["sessionDataKey"] = "<%=sessionDataKey%>";
        values["acr"] = "<%=acr%>";
        values["smsClick"] = "<%=smsClick%>";
        values["operator"] = "<%=operator%>";
        </script>
        <script src="js/waiting.js"></script>
      </head>

      <body class="theme--light">
        <div class="site__root">
            <header class="site-header">
                <div class="site-header__inner site__wrap">
                    <h1 class="visuallyhidden">Mobile Connect</h1>
                    <div align="center">
                        <table class="site-header-brand-table" style="margin-bottom:0px;">
                            <tbody><tr>
                                <td width="30%">
                                    <a class="brand">
                                        <img src="../images/svg/mobile-connect.svg" alt="Mobile Connect&nbsp;Logo" width="150" class="site-header__logo">
                                    </a>
                                </td>
                                <td width="70%">
                                    <% if (!operator.isEmpty()) {
                                        String imgPath = "../images/branding/" + operator + "_logo.png";
                                    %>
                                    <img src='<%=imgPath%>' alt="Operator" class="brandLogo" style="float:right;">
                                    <% } %>
                                </td>
                            </tr>
                            </tbody></table>
                    </div>
                </div>
            </header>

    <main class="site__main site__wrap section v-distribute">
      <header class="page__header">
        <h1 class="page__heading">
            <fmt:message key='waiting-label-continue-on-ivr-device-heading'/>
        </h1>
        <!--div id="instruction_USSDAuthenticator">
         <p><strong>Reply with 1 to continue with your Registration.</strong></p>

       </div-->

    </header>

    <div class="page__illustration v-grow v-align-content">
      <div>

        <div class="timer-spinner-wrap">
          <div class="timer-spinner">
            <div class="pie spinner"></div>
            <div class="pie filler"></div>
            <div class="mask"></div>
          </div>
          <img src="mcresources/img/svg/phone-pin.svg" width="52" height="85">
        </div>
      </div>
    </div>
    <a onclick="cancelProcessToRegister(true)" class="btn btn--outline btn--full btn--large">
        <fmt:message key='common-button-misc-cancel'/>
    </a>
    <div>
        <p class="page-footer-message"><fmt:message key='waiting-label-continue-on-device-success-before-timeout-phase1'/><br><fmt:message key='waiting-label-continue-on-device-success-before-timeout-phase2'/></p>
    </div>
  </main>
</div>





</body>

</html>