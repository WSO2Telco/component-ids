<!doctype html>
<html class="site no-js lang--en" lang="en">

<head>
  <meta charset="utf-8">
  <meta http-equiv="x-ua-compatible" content="ie=edge">
  <title>Mobile Connect</title>
  <meta name="description" content="">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no">

  <link rel="apple-touch-icon" href="apple-touch-icon.png">
  <link rel="stylesheet" href="mcresources/css/style.css">

  <!-- load main script early asyncronously -->
  <script type="text/javascript" src="mcresources/js/main.js" async></script>
  <script src="/portal/gadgets/user_profile/js/jquery.min.js" type="text/javascript"></script>
  <script src="/portal/gadgets/user_profile/js/main.js" type="text/javascript"></script>
  <script src="/portal/gadgets/user_profile/js/modal.js" type="text/javascript"></script>
  <script src="/dashboard/js/landing.js" type="text/javascript"></script>



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
        String acr = request.getParameter("acr")!= null ?  request.getParameter("acr") : "";
        String token = request.getParameter("tokenid")!= null ?  request.getParameter("tokenid") : "";
        String operator = request.getParameter("operator")!= null ?  request.getParameter("operator") : "";
        String msisdn = request.getParameter("username")!= null ?  request.getParameter("username") : "";
        // Boolean smsClick = Boolean.valueOf(request.getParameter("smsClick"))!= null ?  Boolean.valueOf(request.getParameter("smsClick")) : false;
        String smsClick = request.getParameter("smsClick")!= null ?  request.getParameter("smsClick") : "false";


        if(operator != ""){
        %>
        <link href="css/branding/<%=operator%>-style.css" rel="stylesheet">
        <%}%>




      </head>

      <body class="theme--light">
        <div class="site__root">
          <header class="site-header">
            <div class="site-header__inner site__wrap">
              <h1 class="visuallyhidden">Mobile&nbsp;Connect</h1>
              <a href="/"><img src="mcresources/img/svg/mobile-connect.svg" alt="Mobile Connect&nbsp;Logo" width="150" class="site-header__logo"></a>
              
              <% if(operator != ""){ 
              String imgPath = "img/branding/" + operator + "_logo.svg";
              %>
              <p class="site-header__powered-by">powered&nbsp;by      
              </p>
              <a >
                <img class="brandLogo" src='<%= imgPath %>' alt='<%= operator %>' >
              </a>
              <% } %>


              <form class="form-horizontal" id="selfReg" name="selfReg">

                <input type="hidden" name="regExp_PRIMARY" value="^[\S]{5,30}$">

                <div class="control-group">
                  <div class="controls" style="display:none;" type="hidden">
                    <select name="domain">

                      <option value="PRIMARY">PRIMARY</option>

                    </select>
                  </div>
                </div>


                <input type="hidden" value="" id="user_name" name="userName">
                <input type="hidden" value="" id="password" name="pwd">
                <input type="hidden" value="" id="retype_pwd" name="retypePwd">
                <input type="hidden" name="sessionDataKey" id="sessionDataKey" value='<%=request.getParameter("sessionDataKey")%>' />
                <input type="hidden" name="operator" id="operator" value='<%=operator%>'/>
                <input type="hidden" name="acr" id="acr" value='<%=acr%>'/>
                <input type="hidden" name="smsClick" id="smsClick" value='<%=smsClick%>'/>
                <input type="hidden" name="token" id="token" value='<%=token%>'/>
                <input type="hidden" value="<%=msisdn%>" id="Mobile" name="http://wso2.org/claims/mobile">
              </form>



       <!--  <form action="/lang" class="site-header__lang-menu field--select field--select-plain" novalidate>
          <label for="field-select-lang" class="visuallyhidden">Language:</label>
          <select id="field-select-lang" name="lang" class="field__select-native js-transparent">
            <option value="en" selected>English&nbsp;(UK)</option>
            <option value="de">Deutsche</option>
            <option value="th">ภาษาไทย</option>
          </select>

          

          <input type="hidden" name="return-url" value="/registration/on-device">
          <input type="submit" value="Go" class="btn btn--natural btn--light js-visuallyhidden">
        </form> -->
      </div>
    </header>

    <main class="site__main site__wrap section v-distribute">
      <header class="page__header">
        <h1 class="page__heading">
          We've sent a message to your&nbsp;mobile
        </h1>
        <div id ="LoA3" style="display:none">
          <p>Sometimes when using Mobile Connect, you'll need to enter a PIN for extra security. Please follow the instructions on your mobile to create a&nbsp;PIN.</p>
        </div>
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
      <div class="error-copy space--bottom hide" id="timeout-warning">
        Your mobile session is about to&nbsp;timeout.
        <br>Check your&nbsp;device.
      </div>
      <div align="center" id ="LoA2" style="display:block">
        <p>No message arrived? <br><u><a onclick="sendSms()" style="cursor: pointer;">Click to get a text message instead.</a><u></p>
      </div>
      <a onclick="cancelProcessToLogin()" class="btn btn--outline btn--full btn--large">
        Cancel
      </a>
    </main>
  </div>

  
  <script src="js/waiting.js"></script>

  <script type="text/javascript">

  var e1 = document.getElementById("LoA2");
  var e2 = document.getElementById("LoA3");
  if("<%=acr%>"=="USSDPinAuthenticator" ){
    e1.style.display = 'none';
    e2.style.display = 'block';
  }
  if("<%=acr%>"=="USSDAuthenticator" ){
    e1.style.display = 'block';
    e2.style.display = 'none';
  }
  
  if("<%=smsClick%>"== "true"){
    e1.style.display = 'none';
    e2.style.display = 'none';
  }

  function sendSms(){

    var smsClick = document.getElementById("smsClick");
    smsClick.value="true";
    e1.style.display = 'none';
    console.log(isTimeout);

    isTimeout = true;
    //handleTermination();
    handleTerminationSms();

  }
  
  </script>
</body>

</html>
