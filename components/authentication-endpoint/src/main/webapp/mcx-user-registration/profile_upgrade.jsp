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


    <script src="js/profile_upgrade.js" type="text/javascript"></script>
    <script src="js/public/js/jquery.min.js" type="text/javascript"></script>
    <script src="js/public/js/main.js" type="text/javascript"></script>
    <script src="js/public/js/modal.js" type="text/javascript"></script>
    <script src="mcresources/js/vendor/parsley.min.js" type="text/javascript"></script>
    <link rel="stylesheet" href="mcresources/css/style.css">


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
    <script src="mcresources/js/vendor/webfontloader.js"></script>
    <!-- Adds IE root class without breaking doctype -->
    <!--[if IE]>
    <script>document.documentElement.className = document.documentElement.className + " ie";</script>
    <![endif]-->

    <!-- load main script early asyncronously -->
    <script type="text/javascript" src="mcresources/js/main.min.js" async></script>
    <script type="text/javascript" src="mcresources/js/vendor/modernizr.js"></script>

    <%
        Logger log=Logger.getLogger(this.getClass().getName());
        String operator = request.getParameter("operator") != null ? request.getParameter("operator") : "";
        log.info( "operator :"+operator );
        String token = request.getParameter("token") != null ? request.getParameter("token") : "";
        log.info( "token :"+token );
        String updateProfile = request.getParameter("updateProfile") != null ? request.getParameter("updateProfile") : "";
        log.info( "updateProfile :"+updateProfile );
        String imgPath = "";
        if (operator != "") {
            imgPath = "images/branding/" + operator + "_logo.svg";
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
            <a href="/"><img src="mcresources/img/svg/mobile-connect.svg" alt="Mobile Connect&nbsp;Logo" width="150"
                             class="site-header__logo"></a>

            <p class="site-header__powered-by">powered by
                <img src='<%=imgPath%>' alt="Operator" class="brandLogo">
            </p>

            <!--form action="/lang" class="site-header__lang-menu field--select field--select-plain" novalidate>
              <label for="field-select-lang" class="visuallyhidden">Language:</label>
              <select id="field-select-lang" name="lang" class="field__select-native js-transparent">
                <option value="en" selected>English (UK)</option>
                <option value="de">Deutsche</option>
                <option value="th">ภาษาไทย</option>
                <option value="fr">fr_French</option>
              </select>
              <input type="hidden" name="return-url" value="/registration/02-prompt-to-create-account">
              <input type="submit" value="Go" class="btn btn--natural btn--light js-visuallyhidden">
            </form-->
        </div>
    </header>


    <main class="site__main site__wrap section v-distribute v-grow">
        <header class="page__header" style="display:block">
            <h1 class="page__heading">Looks like you have to update your account, let's make your
                account&nbsp;secure</h1>
            <p>Create a PIN for secure log-in and two questions we can ask you in case you ever forget
                your&nbsp;PIN.</p>
        </header>

        <form class="form-horizontal" id="selfReg" name="selfReg" data-parsley-validate>
            <input type="hidden" name="regExp_PRIMARY" value="^[\S]{5,30}$">

            <div class="control-group">
                <div class="controls" style="display:none;" type="hidden">
                    <select name="domain">

                        <option value="PRIMARY">PRIMARY</option>

                    </select>
                </div>
            </div>

            <div id="questions" style="margin-bottom:5px;">
                <input type="hidden" value="" name="http://wso2.org/claims/challengeQuestion1">
                <input type="hidden" value="" name="http://wso2.org/claims/challengeQuestion2">


                <li class="field--select field--select-block">
                    <label for="field-account-security-question-1">Choose a security&nbsp;question</label>
                    <select name="challengeQuestion1" id="q1" class="field__select-native" required="" autofocus
                            data-parsley-required-message="Please choose a security&nbsp;question.">
                        <option disabled selected value="">Choose a&nbsp;question</option>

                        <option name="q" value="City where you were born ?">City where you were born ?</option>

                        <option name="q" value="Favorite vacation location ?">Favorite vacation location ?</option>

                        <option name="q" value="Father's middle name ?">Father's middle name ?</option>

                        <option name="q" value="Favorite food ?">Favorite food ?</option>
                    </select>
                </li>
                <li>
                    <label for="field-account-security-answer-1">Type the answer to your question&nbsp;here</label>
                    <input type="text" name="challengeAns1" +="" json.fieldvalues.return[i].fieldname="" ""=""
                    id="field-account-security-answer-1" placeholder="Answer your&nbsp;question" required autofocus
                    data-parsley-error-message="Please answer your security&nbsp;question.">
                </li>
                <li class="field--select field--select-block">
                    <label for="field-account-security-question-2">Choose a second&nbsp;question</label>
                    <select id="q2" name="challengeQuestion2" class="field__select-native" required=""
                            data-parsley-notequalto="#field-account-security-question-1"
                            data-parsley-required-message="Please choose a security&nbsp;question."
                            data-parsley-notequalto-message="Please choose a different security&nbsp;question.">
                        <option disabled selected value="">Choose a&nbsp;question</option>
                        <option name="q" value="Favorite sport ?">Favorite sport ?</option>

                        <option name="q" value="Name of the hospital where you were born ?">Name of the hospital where
                            you were born ?
                        </option>

                        <option name="q" value="Name of your first pet ?">Name of your first pet ?</option>

                        <option name="q" value="Model of your first car ?">Model of your first car ?</option>
                    </select>
                </li>
                <li>
                    <label for="field-account-security-answer-2">Type the answer to your second
                        question&nbsp;here</label>
                    <input type="text" name="challengeAns2" +="" json.fieldvalues.return[i].fieldname="" ""=""
                    id="field-account-security-answer-2" placeholder="Answer your&nbsp;question" required
                    data-parsley-error-message="Please answer your security&nbsp;question.">
                </li>

            </div>

            <div class="grid">
                <div class="grid__item one-half">
                    <a onclick="cancelProcessToRegister()" class="btn btn--outline btn--full btn--large">
                        No thanks
                    </a>
                </div>
                <div class="grid__item one-half">
                    <button type="button" id="validate-btn1" onclick="flow()" name="action" value="yes"
                            class="btn btn--full btn--fill btn--large btn--color">
                        Yes
                    </button>
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


    var auth;
    var acr_code = getAcrValue();

    var term_ussd = document.getElementById("term_ussd");
    var term_ussd_pin = document.getElementById("term_ussd_pin");
    var slider = document.getElementById("slider");
    var header = document.getElementById("ussdpin_header");
    var header_update = document.getElementById("ussdpin_update_header");
    var questions = document.getElementById("questions");
    var msg = document.getElementById("msg");
    var btn1 = document.getElementById("validate-btn1");
    var btn2 = document.getElementById("validate-btn2");

    /*
     * USSD Pin Registration page 1
     * hide terms
     *
     */

    console.log('xxxxxxxxxxxxx uoioiuoiuoiuoi :' + acr_code)
    if (acr_code == "USSDPinAuthenticator") {
        term_ussd_pin.style.display = 'none';
        term_ussd.style.display = 'none';
        auth = "LoA3";
    }

    /*
     * USSD Registration page 1
     * display terms
     */
    if (acr_code == "USSDAuthenticator") {

        term_ussd_pin.style.display = 'none';
        term_ussd.style.display = 'block';
    }

    if ('<%=updateProfile%>' == "true") {
        term_ussd_pin.style.display = 'none';
        term_ussd.style.display = 'none';
    }

    /*
     * USSD Registration or USSD Pin Registration page 1
     * click on YES button
     */
    function flow() {

        $('#selfReg').parsley().validate();
        if (true === $('#selfReg').parsley().isValid()) {
            registration();
        }
    }
</script>
</body>

</html>
