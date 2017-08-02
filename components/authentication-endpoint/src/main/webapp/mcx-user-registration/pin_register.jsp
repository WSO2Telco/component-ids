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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources">
<html class="site no-js lang--en" lang="en">

<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <title>Mobile Connect</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no">

    <link rel="apple-touch-icon" href="apple-touch-icon.png">


    <script src="js/landing.js" type="text/javascript"></script>
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
        String termsConditionsPath = "";
        if (operator != "") {
            imgPath = "images/branding/" + operator + "_logo.svg";
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
        <header class="page__header" id="ussdpin_header">
            <h1 class="page__heading"><fmt:message key='common-label-security-heading'/></h1>
            <p><fmt:message key='common-label-security-intro'/></p>
        </header>
        <header class="page__header" id="ussdpin_update_header" style="display:none">
            <h1 class="page__heading">Looks like you have to update your account, let’s make your
                account&nbsp;secure</h1>
            <p><fmt:message key='common-label-security-intro'/></p>
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

            <div id="questions" margin-bottom:5px;">
                <input type="hidden" value="" name="http://wso2.org/claims/challengeQuestion1">
                <input type="hidden" value="" name="http://wso2.org/claims/challengeQuestion2">


                <li class="field--select field--select-block">
                    <label for="field-account-security-question-1"><fmt:message key='common-label-security-question-1'/></label>
                    <select name="challengeQuestion1" id="q1" class="field__select-native" required="" autofocus
                            data-parsley-required-message="Please choose a security&nbsp;question.">
                        <option disabled selected value=""><fmt:message key='common-label-security-question-option-blank'/></option>

                        <option name="q" value="City where you were born ?"><fmt:message key='common-label-security-question-option-city'/></option>

                        <option name="q" value="Favorite vacation location ?"><fmt:message key='common-label-security-question-option-vacation'/></option>

                        <option name="q" value="Father's middle name ?"><fmt:message key='common-label-security-question-option-father'/></option>

                        <option name="q" value="Favorite food ?"><fmt:message key='common-label-security-question-option-food'/></option>
                    </select>
                </li>
                <li>
                    <label for="field-account-security-answer-1"><fmt:message key='common-label-security-answer-1'/></label>
                    <input type="text" name="challengeAns1" +="" json.fieldvalues.return[i].fieldname="" ""=""
                    id="field-account-security-answer-1" placeholder="Answer your&nbsp;question" required autofocus
                    data-parsley-error-message="Please answer your security&nbsp;question.">
                </li>
                <li class="field--select field--select-block">
                    <label for="field-account-security-question-2"><fmt:message key='common-label-security-question-2'/></label>
                    <select id="q2" name="challengeQuestion2" class="field__select-native" required=""
                            data-parsley-notequalto="#field-account-security-question-1"
                            data-parsley-required-message="Please choose a security&nbsp;question."
                            data-parsley-notequalto-message="Please choose a different security&nbsp;question.">
                        <option disabled selected value=""><fmt:message key='common-label-security-question-option-blank'/></option>
                        <option name="q" value="Favorite sport ?"><fmt:message key='common-label-security-question-option-sport'/></option>

                        <option name="q" value="Name of the hospital where you were born ?"><fmt:message key='common-label-security-question-option-hospital'/></option>

                        <option name="q" value="Name of your first pet ?"><fmt:message key='common-label-security-question-option-pet'/></option>

                        <option name="q" value="Model of your first car ?"><fmt:message key='common-label-security-question-option-car'/></option>
                    </select>
                </li>
                <li>
                    <label for="field-account-security-answer-2"><fmt:message key='common-label-security-answer-2'/></label>
                    <input type="text" name="challengeAns2" +="" json.fieldvalues.return[i].fieldname="" ""=""
                    id="field-account-security-answer-2" placeholder="Answer your&nbsp;question" required
                    data-parsley-error-message="Please answer your security&nbsp;question.">
                </li>

            </div>

            <div class="page__copy">

            </div>
            <div id="term_ussd" style="display:none" class="page_term">

                <fmt:message key="common-label-security-terms-policy" var="terms" />
                <%
                String terms=pageContext.getAttribute("terms").toString();
                String linked_terms = String.format(terms, "<a href='/authenticationendpoint/terms_and_conditions' target='_blank'>","</a>");
                %>
                <p style="font-size:13px; margin:0px; padding:0px;" align="center"><%=linked_terms%></p>

                <fmt:message key="common-label-account-setup-privacy-policy" var="privacy" />
                <fmt:message key="common-label-account-setup-privacy-policy-link-pph" var="policy" />
                <%
                String privacy=pageContext.getAttribute("privacy").toString();
                String mc_linked_privacy = String.format(privacy, "<a href='/authenticationendpoint/privacy_promise' target='_blank'>","</a>");             
                String policy=pageContext.getAttribute("policy").toString();
                String linked_statement=String.format(mc_linked_privacy.replace(policy,"%s"+policy+"%s"),"<a href='/authenticationendpoint/privacy_policy' target='_blank'>","</a>");
                %>
                <p style="font-size:13px;margin-top:5px;" align="center"><%=linked_statement%></p>
            </div>
            <div id="term_ussd_pin" style="display:none" class="page_term">
                <p style="font-size:11px; margin:0px; padding:0px;" align="center"><%=linked_terms%></p>
                <p style="font-size:11px;" align="center"><%=linked_statement%></p>
            </div>


            <div class="grid" style="margin-top:15px;">
                <div class="grid__item one-half">
                    <a onclick="cancelProcessToRegister()" class="btn btn--outline btn--full btn--large"> <fmt:message key='common-button-misc-no-thanks'/></a>
                </div>
                <div class="grid__item one-half">
                    <button type="button" id="validate-btn2" onclick="flow()"
                            name="action" value="yes" class="btn btn--full btn--fill btn--large btn--color"><fmt:message key='common-button-misc-yes'/></button>
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
</fmt:bundle>