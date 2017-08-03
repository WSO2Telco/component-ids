<%@ page import="org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry" %>
<%@ page import="com.wso2telco.identity.application.authentication.endpoint.util.Constants" %>
<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html class="site no-js lang--en" lang="en">
<head>

    <script src="js/public/js/jquery.min.js" type="text/javascript"></script>
    <script src="js/public/js/main.js" type="text/javascript"></script>
    <script src="js/public/js/modal.js" type="text/javascript"></script>
    <script src="js/public/js/gadget.js" type="text/javascript"></script>
    <script src="mcresources/js/vendor/parsley.min.js" type="text/javascript"></script>
    <script src="mcresources/js/vendor/webfontloader.js"></script>

    <link rel="stylesheet" href="mcresources/css/style.css">
    <noscript>
        <!-- Fallback synchronous download, halt page rendering if load is slow  -->
        <link href="//fonts.googleapis.com/css?family=Roboto:400,300,700,400italic,300italic,700italic" rel="stylesheet"
              type="text/css">
    </noscript>


    <%--<% var token = request.getParameter("token")!= null ?  request.getParameter("token") : "";%>--%>

    <script>
        /* Config for loading the web fonts*/
        var WebFontConfig = {
            google: {
                families: ['Roboto:400,300,700,400italic,300italic,700italic']
            },
            active: function () {
                /*Set a cookie that the font has been downloaded and should be cached*/
                var d = new Date();
                d.setTime(d.getTime() + (7 * 86400000)); // plus 7 Days
                document.cookie = "cachedroboto=true; expires=" + d.toGMTString() + "; path=/";
            }
        };
    </script>

    <script type="text/javascript" src="mcresources/js/vendor/modernizr.js"></script>

    <script>
        $(function () {
            $('.max_view').click(function () {
                gadgets.Hub.publish('org.wso2.is.dashboard', {
                    msg: 'A message from User profile',
                    id: "pin_reset .expand-widget"
                });
            });
        });
    </script>

    <script type="text/javascript">
        var cookie = null;
        var json = null;
        var userName = null;

        /*variables for loading success window based on ussd status*/
        //        var pollingVar = setInterval(pollForStatus, pollingInterval);
        var pollingStatus = -1;

        var timeout = 60000;
        var pollingInterval = 2000;
        var timeRemaining = timeout;
        var hasResponse = false;
        var isTimeout = false;
        /* 1 = go forward*/


        $(function WindowLoad(event) {
            loadPrompt();
        });


        function qs(key) {

            var vars = [], hash;
            var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
            for (var i = 0; i < hashes.length; i++) {
                hash = hashes[i].split('=');
                vars.push(hash[0]);
                vars[hash[0]] = hash[1];
            }
            return vars[key];
        }

        function loadPrompt() {
            $("#gadgetBody").empty();
            $("#prompt").show();
            $("#gadgetBody").append($("#prompt"));
        }

        function loadQuestionsBlock() {
            $("#gadgetBody").empty();
            $("#questionsBlock").show();
            $("#gadgetBody").append($("#questionsBlock"));
        }


        function loadWaitingWindow() {

            $("#gadgetBody").empty();
            $("#waiting").show();
            $("#gadgetBody").append($("#waiting"));
        }

        function loadPinResetSuccessWindow() {

            $("#gadgetBody").empty();
            $("#pinResetSuccess").show();
            $("#gadgetBody").append($("#pinResetSuccess"));

        }

        function submitUpdate() {
            var sessionDataKey = qs("sessionDataKey");

            console.log('sessionDataKey: ' + sessionDataKey);

            var answer1 = document.getElementById("field-answer-security-question-1").value;
            var answer2 = document.getElementById('field-answer-security-question-2').value;
            var str = "/sessionupdater/tnspoints/endpoint/validate/answer1/" + answer1 + "/answer2/" + answer2 + "/sessionId/" + sessionDataKey;

            $.ajax({
                url: str,
                type: "GET"
            })
                    .done(function (data) {

                        var valildationResponse = data;


                        if (valildationResponse.status == "S1000") {
                            loadWaitingWindow();
                            pollForUssdStatus();
                        } else if (valildationResponse.status == "E1001") {
                            if (!valildationResponse.isAnswer1Valid) {
                                $('#lbl_ans1').show()
                            } else {
                                $('#lbl_ans1').hide()
                            }
                            if (!valildationResponse.isAnswer2Valid) {
                                $('#lbl_ans2').show()
                            } else {
                                $('#lbl_ans2').hide()
                            }
                        }
                    }).fail(function (data) {
                console.log('error');
            }).always(function () {
                console.log('Security questions submitted.');
            });

        }


        function clearlabel() {
            $("#field-answer-security-question-1").keypress(function () {
                document.getElementById('lbl_ans1').hidden = true;
            });
            $("#field-answer-security-question-2").keypress(function () {
                document.getElementById('lbl_ans2').hidden = true;
            });
            $("#field-msisdn").keypress(function () {
                document.getElementById('lbl_invalid_msisdn').hidden = true;
            });
        }

        function pollForUssdStatus() {

            console.log("Polling for ussd status")
            var sessionDataKey = qs("sessionDataKey");

            var url = "/sessionupdater/tnspoints/endpoint/ussd/status?sessionID=" + sessionDataKey;
            var STATUS_APPROVED = "Approved";
            var isApproved = false;

            setTimeout(function () {
                $.ajax({
                    url: url,
                    type: "GET",
                    async: false,
                    success: function (result) {
                        if (result != null) {
                            var responseStatus = result.status;

                            if (responseStatus != null && responseStatus.toUpperCase() === STATUS_APPROVED.toUpperCase()) {
                                status = result.status;
                                isApproved = true;
                                console.log("User status APPROVED.");
                                loadPinResetSuccessWindow();
                            }
                        }
                    },
                    complete: function (result) {
                        if(!isApproved){
                            pollForUssdStatus();
                        }
                    },
                    timeout: timeout
                })
            }, pollingInterval);
        }

        function handleTermination(status) {
            window.clearInterval(pollingVar);
            console.log("Polling interval cleared.");
            if (status == "TERMINATE") {
                console.log("Terminating the flow due to no response.");
                pinResetDone(status);
            } else if (status == "FORWARD") {
                console.log("Continuing the flow to success window");
            }
        }


        /**
         ** Decide whether user get authenticated automatically after successful
         ** pin reset or not.
         */
        function pinResetDone(status) {

            //If foward forward to authorize
            if (status == 'FORWARD') {
                console.log('Making user logged in');
                completeAuthorization();

            } else if (status == 'TERMINATE') {
                console.log("Pin reset done and redirecting...");
                cancelProcessToLogin();
            }
        }


        function validateAndSubmit() {
            if (validateEmpty("field-answer-security-question-1").length > 0) {
                var msg = "Challenge Question 1 is required";
                return false;
            }
            if (validateEmpty("field-answer-security-question-2").length > 0) {
                var msg = "Challenge Question 2 is required";
                return false;
            }

            submitUpdate();
        }


        function validateEmpty(fldname) {
            var fld = document.getElementById(fldname);
            console.log("Validating " + fldname);
            var error = "";
            var value = fld.value;
            if (value.length == 0) {
                error = fld.name + " ";
                console.error("ERROR in answer_1 " + error);
                return error;
            }
            value = value.replace(/^\s+/, "");
            if (value.length == 0) {
                error = fld.name + "(contains only spaces) ";
                console.error("ERROR in answer_2 " + error);
                return error;
            }
            return error;
        }

        /*
         * Invoke the endpoint for self authenticate.
         */
        function completeAuthorization() {

            var sessionDataKey = qs("sessionDataKey");
            console.log("Session data key: " + sessionDataKey);
            window.location = "/commonauth?sessionDataKey=" + sessionDataKey;
        }

        function cancelProcessToLogin() {

            var sp = qs("sp");
            var url = "/dashboard/landing.jag";
            var getCallbackURL = "/dashboard/callback.jag?applicationName=" + sp;

            $.ajax({
                type: "GET",
                url: getCallbackURL,
                contentType: "application/json; charset=utf-8",
                async: false,
                success: function (result) {
                    var data = $.parseJSON(result);
                    url = data.return.callbackUrl + "?error=access_denied";
                    console.log("url: " + url);
                }
            });

            /*redirect to callback url*/
            window.location.href = url;

        }


    </script>


    <link rel="apple-touch-icon-precomposed" sizes="144x144"
          href="../img/apple-touch-icon-144-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="114x114"
          href="../img/apple-touch-icon-114-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="72x72"
          href="../img/apple-touch-icon-72-precomposed.png">
    <link rel="apple-touch-icon-precomposed" href="../img/apple-touch-icon-57-precomposed.png">

    <%--<% var operator = request.getParameter("operator")!= null ?  request.getParameter("operator") : "";--%>
    <%--if(operator != ""){%>--%>
    <%--<link href="../css/branding/<%=operator%>-style.css" rel="stylesheet">--%>
    <%--<%}%>--%>

</head>



<body class="theme--light">

<%
    String sessionDataKey = request.getParameter(Constants.SESSION_ID);

    AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(sessionDataKey);
    Object cacheEntryObj = AuthenticationContextCache.getInstance().getValueFromCache(cacheKey);
    AuthenticationContext authenticationContext = null;

    authenticationContext = ((AuthenticationContextCacheEntry) cacheEntryObj).getContext();
    String msisdn = (String) authenticationContext.getProperty(Constants.MSISDN);
    String challengeQuestion1 = (String) authenticationContext.getProperty("challengeQuestion1");
    String challengeQuestion2 = (String) authenticationContext.getProperty(Constants.CHALLENGE_QUESTION_2);
%>
<div class="site__root">
    <header class="site-header">
        <div class="site-header__inner site__wrap">
            <h1 class="visuallyhidden">Mobile&nbsp;Connect</h1>
            <a href="/dashboard/selfcare/index.html"><img src="mcresources/img/svg/mobile-connect.svg"
                                                          alt="Mobile Connect&nbsp;Logo" width="150"
                                                          class="site-header__logo"></a>

            <div id="prompt" style="display: none">
                <header class="page__header"><h1 class="page__heading"><label class="inherit" for="field-reset-pin">Please
                    reset your&nbsp;PIN</label></h1></header>
                <p class="error-copy">Sorry, you've entered your PIN incorrectly three times.</p>
                <button id="defaultBtn" type="submit" class="btn btn--large btn--fill btn--full"
                        onclick="loadQuestionsBlock();">Reset&nbsp;PIN
                </button>
            </div>

            <div id="questionsBlock" style="display: none">
                <form onsubmit="return false" id="seq_q_form" class="registration__form" data-parsley-validate
                      novalidate>
                    <main class="site__main site__wrap section">
                        <div class="error-copy">
                        </div>
                        <header class="page__header">
                            <h1 class="page__heading">Answer your<br>security questions
                            </h1>
                            <p>Before resetting your PIN, please answer the questions&nbsp;below:</p>
                        </header>
                        <ul class="form-fields">
                            <li>
                                <label for="field-answer-security-question-1"><%=challengeQuestion1%>
                                </label>
                                <input type="text" name="ans_1" id="field-answer-security-question-1"
                                       placeholder="Answer your question" autofocus required
                                       data-parsley-required-message="Please answer your security question.">
                                <label hidden="true" id="lbl_ans1" style="color:#f74160">Invalid answer</label>
                            </li>
                            <li>
                                <label for="field-answer-security-question-2"><%=challengeQuestion2%>
                                </label>
                                <input type="text" name="ans_2" id="field-answer-security-question-2"
                                       placeholder="Answer your question" required
                                       data-parsley-required-message="Please answer your security question.">
                                <label hidden="true" id="lbl_ans2" style="color:#f74160">Invalid answer</label>
                            </li>
                            <li>
                                <button type="submit" class="btn btn--large btn--fill btn--full"
                                        onclick="validateAndSubmit();">
                                    Next
                                </button>
                            </li>
                            <li>
                                <a onclick="cancelProcessToLogin()" class="btn btn--outline btn--full btn--large">
                                    Cancel
                                </a>
                            </li>
                        </ul>
                    </main>
                </form>
            </div>

            <div id="waiting" style="display: none">
                <main class="site__main site__wrap section v-distribute">
                    <header class="page__header"><h1 class="page__heading">We've sent the prompt to enter 4 digit PIN to
                        your
                        mobile</h1>
                        <p>Sometimes when using Mobile Connect, you'll need to enter a PIN for extra security. Please
                            follow the
                            instructions on your mobile to create a PIN</p></header>
                    <div class="page__illustration v-grow v-align-content">
                        <div>
                            <div class="timer-spinner-wrap">
                                <div class="timer-spinner">
                                    <div class="pie spinner"></div>
                                    <div class="pie filler"></div>
                                    <div class="mask"></div>
                                </div>
                                <img src="mcresources/img/svg/phone-pin.svg" width="52" height="85"></div>
                        </div>
                    </div>
                    <div class="error-copy space--bottom hide" id="timeout-warning">Your mobile session is about to
                        timeout.
                        <br>Check your device
                    </div>
                    <a onclick="cancelProcessToLogin()" class="btn btn--outline btn--full btn--large">Cancel</a></main>
            </div>

            <div id="pinResetSuccess" style="display: none">
                <main class="site__main site__wrap section v-distribute">
                    <header class="page__header">
                        <h1 class="page__heading">
                            "Your PIN has been reset"
                        </h1>
                        <p>
                            You've successfully created a new PIN. You can continue to The Guardian.
                        </p>
                    </header>

                    <div class="page__illustration v-grow v-align-content">
                        <div>
                            <img src="mcresources/img/svg/successful-action.svg" alt="Reset successful" width="126"
                                 height="126">
                        </div>
                    </div>

                    <div class="space--top">
                        <button class="btn btn--full btn--fill btn--large btn--color"
                                onclick="completeAuthorization();">
                            Continue
                        </button>
                    </div>
                </main>
            </div>

        </div>
    </header>
    <main class="site__main site__wrap section" id="gadgetBody">
    </main>
</div>
</body>


<form id="gadgetForm" class="form-horizontal" style="margin:100px 20px 100px 10px;">
    <div id="message"></div>
    <div id="gadgetBodys">
    </div>
</form>

</html>
