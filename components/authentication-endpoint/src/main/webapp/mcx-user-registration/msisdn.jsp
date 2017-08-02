<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:message key="msdin-label-entry-phone-number-error" var="error" />
<%
    String validationRegex = request.getParameter("validationRegex");
    if(validationRegex==null || validationRegex.isEmpty()){
        validationRegex="^\\d{12}$";
    }
%>
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
            <h1 class="page__heading"><fmt:message key='msisdn-label-entry-heading'/></h1>
            <p><fmt:message key='msisdn-label-entry-intro'/></p>
         </header>
         <ul class="form-fields">
            <li>
               <label for="msisdn"><fmt:message key='msisdn-label-entry-mobile'/></label>
               <input type="tel"  id="msisdn" onfocus="this.value = this.value;"  name="msisdn" autofocus required pattern="<%=validationRegex%>" data-parsley-error-message='<%=pageContext.getAttribute("error") %>'>{{set_msisdn this}}</input>
            </li>
            <li>
               <button type="submit" class="btn btn--outline btn--large btn--full" onclick="submitLoginForm()" >
               		<fmt:message key='msisdn-button-continue'/>
               </button>
            </li>
         </ul>
      </main>
      
</script>

 <script type="text/javascript" src="mcx-user-registration/js/msisdn.js"></script>
