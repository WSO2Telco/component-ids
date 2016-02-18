var timeout = 60000;
var pollingInterval = 2000;
var timeRemaining = timeout;
var hasResponse = false;
var isTimeout = false;
var status='pending';
var sessionId;
var acr=document.getElementById('acr').value;
var token=document.getElementById('token').value;
var msisdn=document.getElementById('Mobile').value;
var operator=document.getElementById('operator').value;
console.log(" timeout : " +timeout+" pollingInterval : " +pollingInterval+" timeRemaining : " +timeRemaining+" hasResponse : " +hasResponse+" isTimeout : " +isTimeout+" status : " +status);



var pollingVar = setInterval(pollForStatus, pollingInterval);
console.log("waiting");

/* 
 * Check for USSD response status if timeout not is reached 
 * or user approval status(USSD) is not specified (YES/NO).
 */
 function pollForStatus() {
 	console.log(" timeout : " +timeout+" pollingInterval : " +pollingInterval+" timeRemaining : " +timeRemaining+" hasResponse : " +hasResponse+" isTimeout : " +isTimeout+" status : " +status);

	
	// If timeout has not reached.
	if(timeRemaining > 0) {
		// If user has not specified a response(YES/NO).
		
		if(!hasResponse) {
			checkUSSDResponseStatus();
			timeRemaining = timeRemaining - pollingInterval;
			
		} else {
			handleTermination();
			
		}
		
	} else {
		isTimeout = true;
		handleTermination();
	}
}

/*
 * Handle polling termination and form submit.
 */
 function handleTermination() {
	//window.clearInterval(pollingVar);
	//window.open("./landing.html",'_self',"User Registration");
	
	window.clearInterval(pollingVar);
	var STATUS_PENDING = "pending";
	if(!status==STATUS_PENDING){
		$('#waiting_screen_success').show();
	}
	$('#waiting_screen').hide();
	//setTimeout(redirectBack(), (5000);
		
		
			setTimeout(function(){
				redirectBack();
			}, 5000);
		

		
	}

/*
 * Redirect after end of registration
 */
 function redirectBack() {
    // Get the value of the 'loginRequestURL' cookie
    var loginURL = decodeURIComponent(document.cookie.replace(new RegExp("(?:(?:^|.*;)\\s*" + "loginRequestURL" + "\\s*\\=\\s*([^;]*).*$)|^.*$"), "$1")) || null;
    var tokenid=qs("tokenid");

 

	if(tokenid){
		if(isTimeout){
			var callbackURL ;
			var id=qs("tokenid");
			var url = "../user-registration/webresources/endpoint/user/authenticate/get?tokenid="+ id;
			
			$.ajax({
		 		type: "GET",
		 		url:url,
		 		async: false,
		 		dataType: 'json',
		 		success:function(result){
		 			if(result != null) {
		 				callbackURL = result.redirectUri;  
		 			}
		 	 }});
		 	 
		 	 window.location.href = callbackURL + "?error=access_denied&error_description=Authentication+required";
		}else {
			selfAuthorize();
		}
	}else{

		if(isTimeout){
			window.location.href = "./landing.jag";
		}else{
			window.location.href = "./account-setup-success.jag";
		}
		
	} 
}


/*
 * Invoke the endpoint for self authenticate.
 */
 function selfAuthorize(){
 	var callbackURL;
 	var acr;
 	var authendpoint;
 	var token;
 	var scope;
 	var id=qs("tokenid");
	var state;
      	var nonce;
 	var username=qs("username");
 	var url = "../user-registration/webresources/endpoint/user/authenticate/get?tokenid="+ id;

 	$.ajax({
 		type: "GET",
 		url:url,
 		async: false,
 		dataType: 'json',
 		success:function(result){
 			if(result != null) {
 				scope = result.scope; 
 				callbackURL = result.redirectUri; 
				state= result.state;
		                nonce=result.nonce;
 				clientkey = result.clientId; 
 				acr = result.acrValues; 
 				authendpoint = "../oauth2/authorize"; 
 				token = result.tokenID; 
 			}
 		}});

 	var url = authendpoint + "?scope="+encodeURIComponent(scope)+"&response_type=code&redirect_uri="
 	+ encodeURIComponent(callbackURL) + "&client_id=" + clientkey + "&acr_values=" 
 	+ acr+"&tokenid="+token+"&msisdn="+username+"&state="+state+"&nonce="+nonce;
 	console.log("url   " + url);
 	window.location = url;
 }

/*
 * Invoke the endpoint to retrieve USSD status.
 */


 function qs(key) {
 	
 	var vars = [], hash;
 	var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
 	for(var i = 0; i < hashes.length; i++)
 	{
 		hash = hashes[i].split('=');
 		vars.push(hash[0]);
 		vars[hash[0]] = hash[1];
 	}
 	return vars[key];
 }

 function deleteUser(sessionId){
 	var deleteUserUrl = "/dashboard/delete_user.jag?username=" + sessionId;
 	$.ajax({
 		type: "GET",
 		url: deleteUserUrl,
 		async:false,
 	})
 	.done(function (data) {
 	})
 	.fail(function () {
 		console.log('error');
 	})
 	.always(function () {
 		console.log('completed');
 	});
 }

 function checkUSSDResponseStatus() {
 	
	//var sessionId = document.getElementById('username').value;
	sessionId=qs('username');
	
	///var url = "../MediationTest/tnspoints/endpoint/ussd/status?sessionID=" + sessionId;

	var url = "../user-registration/webresources/endpoint/ussd/status?username=" + sessionId;
	var STATUS_APPROVED = "Approved";
	
	$.ajax({
		type: "GET",
		url:url,
		async: false,
		success:function(result){
			if(result != null) {
				var responseStatus = result.status; 
				
				if(responseStatus != null && responseStatus == STATUS_APPROVED) {
					status = result.status;
					hasResponse = true;
				}
			}
		}});

}


function resendUSSD(){
	var msisdn=qs('username');
	/*
	$.ajax({
    url: 'backend_service.jag',
    type: 'GET',
    error: function(){
        alert('NOT EXISTS');
    },
    success: function(){
        alert('EXISTS');
    }
	});
*/
var strBack = "backend_service.jag?msisdn=" + msisdn;
$.ajax({
	type: "GET",
	url: strBack
})
}





function handleTerminationSms() {
	window.clearInterval(pollingVar);
	var STATUS_PENDING = "pending";
	console.log(status +" = " +STATUS_PENDING);
	if(status==STATUS_PENDING){
		console.log('changed the flow');
		//alert(" timeout : " +timeout+" pollingInterval : " +pollingInterval+" timeRemaining : " +timeRemaining+" hasResponse : " +hasResponse+" isTimeout : " +isTimeout+" status : " +status);

		window.location.href = "smsClickHandler.jag?username=" + msisdn+"&token="+token+"&operator="+operator+"&acr="+acr;
		
	}else{

		console.log('registered already');
		pollForStatus();
	}





}
