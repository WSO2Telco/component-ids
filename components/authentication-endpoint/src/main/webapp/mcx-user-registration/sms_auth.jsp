<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html class="site no-js lang--en" lang="en">

<head>
    <meta charset="utf-8">
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <title>Mobile Connect</title>
    <meta name="description" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no">

    <link rel="apple-touch-icon" href="apple-touch-icon.png">
    <link rel="stylesheet" href="css/style.css">

    <noscript>
        <!-- Fallback synchronous download, halt page rendering if load is slow  -->
        <link href="//fonts.googleapis.com/css?family=Roboto:400,300,700,400italic,300italic,700italic" rel="stylesheet"
              type="text/css">
    </noscript>
    <!-- loads fonts asyncronously preventing font loading from block page render -->
    <script
            src="https://code.jquery.com/jquery-2.2.4.min.js"
            integrity="sha256-BbhdlvQf/xTY9gja0Dq3HiwQF8LaCRTXxZKRutelT44="
            crossorigin="anonymous"></script>
    <script type="application/javascript">
        $(document).ready(function() {
            var sessionID = getUrlParameter("id");

            //saveRequestDetails();
            getUserChallanges();
            var finalResult;

            function getUserChallanges() {

                var url = "/sessionupdater/tnspoints/endpoint/sms/response?id=" + encodeURIComponent(sessionID);

                $.ajax({
                    type: "GET",
                    url: url,
                    success: function (result) {
                        finalResult = result.status;

                        if (finalResult != "APPROVED") {
                            $('#failedImage').show();
                            $('#failedText').show();
                        } else {
                            $('#successImage').show();
                            $('#successText').show();
                        }
                    }
                });
            }

            function getUrlParameter(sParam) {
                var sPageURL = decodeURIComponent(window.location.search.substring(1)),
                        sURLVariables = sPageURL.split('&'),
                        sParameterName,
                        i;

                for (i = 0; i < sURLVariables.length; i++) {
                    sParameterName = sURLVariables[i].split('=');

                    if (sParameterName[0] === sParam) {
                        return sParameterName[1] === undefined ? true : sParameterName[1];
                    }
                }
            }
        });
    </script>
    <!-- Adds IE root class without breaking doctype -->
    <!--[if IE]>
    <script>document.documentElement.className = document.documentElement.className + " ie";</script>
    <![endif]-->
</head>

<body class="theme--light">
<div class="site__root">
    <header class="site-header">
        <div class="site-header__inner site__wrap">
            <h1 class="visuallyhidden">Mobile&nbsp;Connect</h1>
            <a href="/"><img src="images/svg/mobile-connect.svg" alt="Mobile Connect&nbsp;Logo" width="150"
                             class="site-header__logo"></a>


        </div>
    </header>

    <main class="site__main site__wrap section v-distribute">
        <header class="page__header">
            <h1 class="page__heading">
                SMS Authenticator
            </h1>
            <p id="failedText" style="display:none;">
                Your session expired.
            </p>
            <p id="successText" style="display: none">
                You are successfully authenticated via mobile connect.
            </p>
        </header>

        <div class="page__illustration v-grow v-align-content">
            <div id="successImage" style="display: none">
                <img src="images/svg/successful-action.svg" alt="Reset successful" width="126" height="126">
            </div>
            <div id="failedImage" style="display: none">
                <img src="images/svg/failed.svg" alt="Reset successful" width="126" height="126">
            </div>
        </div>
    </main>
</div>
</body>
</html>














