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
    <script src="mcx-user-registration/js/tether.min.js" type="text/javascript"></script>
    <script src="mcx-user-registration/js/bootstrap.min.js" type="text/javascript"></script>
    <script src="mcx-user-registration/js/public/js/main.js" type="text/javascript"></script>
    <script src="mcx-user-registration/js/public/js/modal.js" type="text/javascript"></script>
    <script src="mcx-user-registration/js/parsley.min.js" type="text/javascript"></script>
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
        String action = request.getParameter("action");
        String id = request.getParameter("id");
        String endpoint = "/sessionupdater/tnspoints/endpoint/sms/response";
    %>

</head>

<body class="theme--light">
    <div class="site__root">
        <header class="site-header">
            <div class="site-header__inner site__wrap">
                <h1 class="visuallyhidden">Mobile Connect</h1>
                <a href="/"><img src="mcx-user-registration/mcresources/img/svg/mobile-connect.svg" alt="Mobile Connect&nbsp;Logo" width="150"
                                 class="site-header__logo"></a>
            </div>
        </header>


        <main class="site__main site__wrap section v-distribute v-grow">
                <div class="grid">
                     <div class="grid__item three-quarters">
                        <h2 id="header" class="page__heading">Please Wait!</h2>
                        <p id="content"></p>
                     </div>
                </div>
        </main>
    </div>
    <script type="text/javascript">
        $.ajax({
            url: "<%=endpoint%>",
            data: {"id": "<%=id%>", "action": "<%=action%>"},
            type: 'get',
            success: function(res) {
                if (res) {
                    let data = JSON.parse(res);
                    $("#header").html(data.status);
                    $("#content").html(data.text);
                }
            }
        });
    </script>
</body>

</html>
