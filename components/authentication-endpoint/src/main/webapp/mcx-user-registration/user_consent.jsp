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
<%@page import="java.util.logging.Logger" %>
<html class="site no-js lang--en" lang="en">

<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <title>Mobile Connect</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no">

    <link rel="apple-touch-icon" href="apple-touch-icon.png">


    <script src="mcx-user-registration/js/landing.js" type="text/javascript"></script>
    <script src="mcx-user-registration/js/public/js/jquery.min.js" type="text/javascript"></script>
    <script src="mcx-user-registration/js/public/js/main.js" type="text/javascript"></script>
    <script src="mcx-user-registration/js/public/js/modal.js" type="text/javascript"></script>
    <script src="mcx-user-registration/mcresources/js/vendor/parsley.min.js" type="text/javascript"></script>
    <link rel="stylesheet" href="mcx-user-registration/mcresources/css/style.css">


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
        String operator = request.getParameter("operator") != null ? request.getParameter("operator") : "";
        log.info( "operator :"+operator );
        String token = request.getParameter("token") != null ? request.getParameter("token") : "";
        log.info( "token :"+token );
        String updateProfile = request.getParameter("updateProfile") != null ? request.getParameter("updateProfile") : "";
        log.info( "updateProfile :"+updateProfile );
        String authenticators = request.getParameter("authenticators");
        String imgPath = "mcx-user-registration/mcresources/img/svg/spark_logo.svg";
        String termsConditionsPath = "";
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
        <header class="page__header" id="ussdpin_header" style="display:none">
            <h1 class="page__heading">Now, let&rsquo;s make your account&nbsp;secure</h1>
            <p>Create a PIN for secure log-in and two questions we can ask you in case you ever forget
                your&nbsp;PIN.</p>
        </header>
        <header class="page__header" id="ussdpin_update_header" style="display:none">
            <h1 class="page__heading">Looks like you have to update your account, let’s make your
                account&nbsp;secure</h1>
            <p>Create a PIN for secure log-in and two questions we can ask you in case you ever forget
                your&nbsp;PIN.</p>
        </header>
        <div>
            <section class="slider__slide">
                <div class="slider__slide-inner v-distribute">
                    <header class="page__header">
                        <!--h1 class="page__heading">Private</h1-->
                        <h4>Please provide your consent to register in order to access the <br><b>SMS, Payment API</b></h4>
                        <div class="page__illustration v-grow v-align-content">
                            <div>
                                <img src="mcx-user-registration/mcresources/img/svg/personal.svg" alt="Secure" width="106" height="126">
                            </div>
                        </div>
                    </header>

                </div>
            </section>
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

            <div class="page__copy">
                <p id="msg">
                    Looks like you don't yet have an account. Want to set one up? It's quick and&nbsp;easy.
                </p>
            </div>
            <div id="term_ussd" class="page_term">
                <p style="font-size:13px; margin:0px; padding:0px;" align="center">By setting up an account, you are
                    agreeing to the <a href="/authenticationendpoint/terms_and_conditions"
                                       target="_blank">Terms and Conditions.</a></p>
                <p style="font-size:13px;margin-top:5px;" align="center">The <a href="/authenticationendpoint/privacy_promise" target="_blank">Mobile
                    Connect Privacy Promise</a> means that your mobile number won't be shared and no personal
                    information will be disclosed without your consent. See our full <a href="/authenticationendpoint/privacy_policy" target="_blank">privacy
                        policy&nbsp;here</a>.</p>
            </div>


            <div class="grid">
                <div class="grid__item one-half">
                    <a class="btn btn--fill btn--full btn--large" href="/commonauth/?sessionDataKey=<%=request.getParameter("sessionDataKey")%>&action=approve">
                        Approve
                    </a>
                </div>
                <div class="grid__item one-half">
                    <button type="button" id="validate-btn1" name="action" value="yes"
                            class="btn btn--full btn--large btn-danger">
                        Deny
                    </button>
                </div>
            </div>
            <div class="grid space--half-top">
                <div class="grid__item one-whole">
                    <a class="btn btn--full btn--large btn--outline " href="/commonauth/?sessionDataKey=<%=request.getParameter("sessionDataKey")%>&action=all">
                        Approve always
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

</script>
</body>

</html>
