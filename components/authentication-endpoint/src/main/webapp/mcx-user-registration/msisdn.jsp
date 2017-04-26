<head>
  
</head>
<input type="hidden" name="sessionDataKey" value='<%=request.getParameter("sessionDataKey")%>'/>

<body class="theme--dark">
   <div class="site__root" id="content-placeholder">
      
   </div>
</body>

<!-- The handlebar template -->
<script id="results-template" type="text/x-handlebars-template">

<main class="site__main site__wrap section v-distribute">
         <header class="page__header">
            <h1 class="page__heading">{{msdin-entry-heading}}</h1>
            <p>{{msdin-entry-intro}}</p>
         </header>
         <ul class="form-fields">
            <li>
               <label for="msisdn">{{msdin-entry-mobile-label}}</label>
               <input type="tel"  id="msisdn" onfocus="this.value = this.value;"  name="msisdn" autofocus required pattern="^\d{12}$" data-parsley-error-message="{{msdin-entry-phone-number-error}}">{{set_msisdn this}}</input>
            </li>
            <li>
               <button type="submit" class="btn btn--outline btn--large btn--full" onclick="submitLoginForm()" >
               		{{continue-button}}
               </button>
            </li>
         </ul>
      </main>
      
</script>

 <script type="text/javascript" src="mcx-user-registration/js/msisdn.js"></script>
