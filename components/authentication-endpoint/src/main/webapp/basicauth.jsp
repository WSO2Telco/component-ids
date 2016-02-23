<%@ page import="com.wso2telco.identity.application.authentication.endpoint.util.CharacterEncoder"%>
<head>
  
</head>
<input type="hidden" name="sessionDataKey" value='<%=request.getParameter("sessionDataKey")%>'/>


<div class="site__root" id="content-placeholder">
  
</div>


<!-- The handlebar template -->
<script id="results-template" type="text/x-handlebars-template">

<main class="site__main site__wrap section v-distribute">
    <%
        loginFailed = CharacterEncoder.getSafeText(request.getParameter("loginFailed"));
        if (loginFailed != null) {
    %>
            <div >
                <fmt:message key='<%=CharacterEncoder.getSafeText(request.getParameter
                ("errorMessage"))%>'/>
            </div>
    <% } %>
<ul class="form-fields">
    <% if (CharacterEncoder.getSafeText(request.getParameter("username")) == null || "".equals
    (CharacterEncoder.getSafeText(request.getParameter("username")).trim())) { %>

        <!-- Username -->
        <li>
            <label class="control-label" for="username"><fmt:message key='username'/>:</label>

            <div class="controls">
                <input type="text" id='username' name="username" '/>
            </div>
        </li>

    <%} else { %>

        <input type="hidden" id='username' name='username' value='<%=CharacterEncoder.getSafeText
        (request.getParameter("username"))%>'/>

    <% } %>

    <!--Password-->
		<li>
       		<label  for="password"><fmt:message key='password'/>:</label>

            <input type="password" id='password' name="password" />
            <input type="hidden" name="sessionDataKey" value='<%=CharacterEncoder.getSafeText(request.getParameter("sessionDataKey"))%>'/>
    	</li>
    	<li>
            <label class="checkbox" style="margin-top:10px"><input type="checkbox" id="chkRemember" name="chkRemember"><fmt:message key='remember.me'/></label>
    	</li>
    

		<li>
		    <input type="submit" value='<fmt:message key='login'/>' class="btn btn-primary">
		    
		</li>
    </ul>
</main>
      
</script>

 <script type="text/javascript">
 	$(document).ready(function(){
		// The template code
		var templateSource = $("#results-template").html();

		// compile the template
		var template = Handlebars.compile(templateSource);
		 
		// The div/container that we are going to display the results in
		var resultsPlaceholder = document.getElementById('content-placeholder');
	
		//
		var baseurl = $("#baseURL").val();
		$.getJSON(baseurl+'/languages/en.json', function(data) {
			resultsPlaceholder.innerHTML = template(data);
		});
	
	});
 </script>
