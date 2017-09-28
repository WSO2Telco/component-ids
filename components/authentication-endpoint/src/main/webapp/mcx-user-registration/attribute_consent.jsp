<!doctype html>
<!--
/*******************************************************************************
* Copyright (c) 2016, WSO2.Telco Inc. (http://www.wso2telco.com)
*
* All Rights Reserved. WSO2.Telco Inc. licences this file to youunder the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
-->
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@page import="java.util.logging.Logger" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.cache.*" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.context.*" %>
<html class="site no-js lang--en" lang="en">

<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <title>Mobile Connect</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no">

    <link rel="apple-touch-icon" href="apple-touch-icon.png">
    <script src="mcx-user-registration/js/jquery-3.2.1.min.js" type="text/javascript"></script>
    <script src="mcx-user-registration/js/landing.js" type="text/javascript"></script>
    <script src="https://npmcdn.com/tether@1.2.4/dist/js/tether.min.js"></script>
    <script src="mcx-user-registration/js/bootstrap.min.js" type="text/javascript"></script>
    <script src="mcx-user-registration/js/public/js/main.js" type="text/javascript"></script>
    <script src="mcx-user-registration/js/public/js/modal.js" type="text/javascript"></script>
    <script src="mcx-user-registration/mcresources/js/vendor/parsley.min.js" type="text/javascript"></script>
    <link rel="stylesheet" href="mcx-user-registration/mcresources/css/style.css">
    <link rel="stylesheet" href="mcx-user-registration/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">


    <noscript>
        <!-- Fallback synchronous download, halt page rendering if load is slow  -->
        <link href="//fonts.googleapis.com/css?family=Roboto:400,300,700,400italic,300italic,700italic" rel="stylesheet"
              type="text/css">
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
    <script src="mcx-user-registration/mcresources/js/vendor/webfontloader.js"></script>
    <!-- Adds IE root class without breaking doctype -->
    <!--[if IE]>
    <script>document.documentElement.className = document.documentElement.className + " ie";</script>
    <![endif]-->

    <!-- load main script early asyncronously -->
    <script type="text/javascript" src="mcx-user-registration/mcresources/js/main.js" async></script>
    <script type="text/javascript" src="mcx-user-registration/mcresources/js/vendor/modernizr.js"></script>

    <%
	    Logger log=Logger.getLogger(this.getClass().getName()); 
        String spLogo =null;
        String sessionDataKey = request.getParameter("sessionDataKey");
	    AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(sessionDataKey);
	    Object cacheEntryObj = AuthenticationContextCache.getInstance().getValueFromCache(cacheKey);
	    AuthenticationContext authnContext = null;
		if (cacheEntryObj != null) {
			authnContext = ((AuthenticationContextCacheEntry) cacheEntryObj).getContext();
		}

        String operator = authnContext.getProperty("operator").toString();
        log.info( "operator :"+operator );
        String spName = authnContext.getServiceProviderName();
        log.info( "ServiceProvider :"+spName );
        if(authnContext.getProperty("logo")!=null){
        	spLogo = authnContext.getProperty("logo").toString();
        }
        log.info( "logoPath :"+spLogo );
        String authenticators = request.getParameter("authenticators");
        String imgPath = "";
        String termsConditionsPath = "";
        String scopes = request.getParameter("scope");
        String[] attribute_Scopes;
        if(!scopes.equals("") || !scopes.isEmpty()){
            String scopesMinBracket = scopes.substring( 1, scopes.length() - 1);
            attribute_Scopes = scopesMinBracket.split( ", ");
            pageContext.setAttribute("attributeScopes", attribute_Scopes, pageContext.PAGE_SCOPE);
        } else{
            pageContext.setAttribute("attributeScopes",scopes, pageContext.PAGE_SCOPE);
        }
//        String[] attribute_Scopes = scopesMinBracket.split( ", ");
//        pageContext.setAttribute("attributeScopes", attribute_Scopes, pageContext.PAGE_SCOPE);
        if (operator != "") {
            imgPath = "mcx-user-registration/images/branding/" + operator + "_logo.svg";
            termsConditionsPath = "html/terms-conditions/" + operator + "-terms-conditions.html";
    %>
    <link href="css/branding/<%=operator%>-style.css" rel="stylesheet">
    <%
        }
    %>

</head>

<body class="theme--light">
<div class="site__root">
    <header class="site-header">
        <div class="site-header__inner site__wrap">
            <h1 class="visuallyhidden">Mobile Connect</h1>
            <a href="/"><img src="mcx-user-registration/mcresources/img/svg/mobile-connect.svg" alt="Mobile Connect&nbsp;Logo" width="150"
                             class="site-header__logo"></a>

            <p class="site-header__powered-by">powered by
                <img src='<%=imgPath%>' alt="Operator" class="brandLogo">
            </p>
        </div>
    </header>


    <main class="site__main site__wrap section v-distribute v-grow">
            <div class="grid">
                 <div class="grid__item one-quarter">
                     <img src='<%=spLogo%>' alt="App logo" width="100">
                 </div>
                 <div class="grid__item three-quarters">
                    <h2 class="page__heading"><%=spName%></h2>
                    <p>You are granting <b><%=spName%></b> the rights to access following attribute data:</p>
                 </div>
        </div>
   
        <header class="page__header" id="ussdpin_update_header" style="display:none">
            <h1 class="page__heading">Looks like you have to update your account, let’s make your
                account&nbsp;secure</h1>
            <p>Create a PIN for secure log-in and two questions we can ask you in case you ever forget
                your&nbsp;PIN.</p>
        </header>
        <br>
        
<div class="grid">
  <ul class="list-group" id="list-group-accordion">
      <c:forEach var="scope" items="${pageScope.attributeScopes}">
               <li class="list-group-item">
                   <p class="list-group-heading" data-toggle="collapse" data-target="#<c:out value="${scope}"/>" data-parent="#list-group-accordion">  <img src="mcx-user-registration/mcresources/img/privacy-list.jpg" alt="App logo"><c:out value="${scope}"/></p>

               </li>
      </c:forEach>
  </ul>
</div>

        <form class="form-horizontal" id="selfReg" name="selfReg" data-parsley-validate>
            <input type="hidden" name="regExp_PRIMARY" value="^[\S]{5,30}$">

            <div class="control-group">
                <div class="controls" style="display:none;" type="hidden">
                    <select name="domain">

                        <option value="PRIMARY">PRIMARY</option>

                    </select>
                </div>
            </div>
<br>

		<c:if test="${param.registering=='true'}">
            <div class="page__copy" class="page_term">
                <label class="checkbox-inline"><input id="tc_checkbox" type="checkbox" value="">&nbsp;Looks like you don't yet have an account. Want to set one up? It's quick and&nbsp;easy.By setting up an account, you are
                    agreeing to the <a href="/authenticationendpoint/terms_and_conditions"
                                       target="_blank">Terms and Conditions.</a></label>
            </div>
            <br>
        </c:if>
      

            <div class="grid">
                <div class="grid__item one-half">
                    <c:choose>
                       <c:when test="${param.registering=='true'}">
                           <a onclick="TCBoxValidation('<%=request.getParameter("sessionDataKey")%>','approve')" id="approve-btn" class="btn btn--full btn--white btn--fill btn--large btn--color">
                              Approve
                           </a>
                       </c:when> 
                       <c:otherwise>
                           <a id="approve-btn" class="btn btn--full btn--white btn--fill btn--large btn--color" href="/commonauth/?sessionDataKey=<%=request.getParameter("sessionDataKey")%>&action=RegConsent">
                              Approve
                           </a>
                       </c:otherwise>
                    </c:choose>
                </div>
                <div class="grid__item one-half">
                    <a id="deny-btn" class="btn btn-danger btn--full btn--large" href="/commonauth/?sessionDataKey=<%=request.getParameter("sessionDataKey")%>&action=RegRejected">
                        Deny
                    </a>
                 </div>
            </div>
        </form>
    </main>
</div>
<script type="text/javascript">

    $('#validate-btn1').click(function (event) {
        event.preventDefault();
        //do your action goes below
    });


    /*
    * Consent accepted
    */
    function accept() {
        var sessionDataKey = getParameterByName('sessionDataKey');
        window.location = "/commonauth/?sessionDataKey=" + sessionDataKey + "&action=RegConsent";
    }

    $(function() {
  $('.collapse').on('show.bs.collapse', function() {
    var toggle = $('[data-target="#' + this.id + '"]');
    if (toggle) {
      var parent = toggle.attr('data-parent');
      if (parent) {
        $(parent).find('.collapse.in').collapse('hide');
      }
    }
  });
})


</script>
</body>

</html>
