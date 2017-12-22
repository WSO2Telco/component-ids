var timeout = 120000;
var pollingInterval = 2000;
var timeRemaining = timeout;
var hasResponse = false;
var isTimeout = false;
var status;
var pollingVar;
var STATUS_PIN_FAIL ="FAILED_ATTEMPTS";
var STATUS_PENDING = "PENDING";
var STATUS_APPROVED = "APPROVED";
var pinResetUrl;
var smsRequested = false;
var xhr;

$(document).ready(function(){

	// The template code
	var templateSource = $("#results-template").html();

	// compile the template
	var template = Handlebars.compile(templateSource);

	// The div/container that we are going to display the results in
	var resultsPlaceholder = document.getElementById('content-placeholder');

	var operator= qs('operator');
	var token= qs('sessionDataKey');
	var sp= qs('sp');
	var baseurl = $("#baseURL").val();
	pinResetUrl = $("#baseURLWithPort").val() +"/dashboard/pin_reset/pinreset.jag?operator="+operator+"&token="+token+"&sp="+sp;

	$.getJSON(baseurl+'/mcx-user-registration/languages/en.json', function(data) {
		resultsPlaceholder.innerHTML = template(data);
	});

	pollingVar = setInterval(pollForStatus, pollingInterval);
});


/*
 * Check for USSD response status if timeout not is reached
 * or user approval status(USSD) is not specified (YES/NO).
 */
function pollForStatus() {

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
function handleTermination(cancelButton) {
	window.clearInterval(pollingVar);

	var sessionDataKey = qs('sessionDataKey');
	var commonAuthURL;
	var action;
	if(cancelButton){
		action = "userCanceled";
	} else if(status.toUpperCase() == STATUS_APPROVED){
        action = "";
    } else{
        action = "userRespondedOrTimeout";
    }

	commonAuthURL = "/commonauth/?sessionDataKey=" + sessionDataKey
		+ "&msisdn=" + msisdn
		+ "&action=" + action + "&canHandle=true";

	window.location = commonAuthURL;
}

/*
 * Invoke the endpoint to retrieve USSD status.
 */
function checkUSSDResponseStatus() {
	if(smsRequested)
		return;
	var sessionId = document.getElementById('sessionDataKey').value;
	var url = "../sessionupdater/tnspoints/endpoint/ussd/status?sessionID=" + sessionId;

	xhr = $.ajax({
		type: "GET",
		url:url,
		async: false,
		cache: false,
		success:function(result){
			if(result != null) {
				var responseStatus = result.status;

				if(responseStatus != null && responseStatus.toUpperCase() != STATUS_PENDING) {
					hasResponse = true;
					status = result.status;
				}else if (responseStatus != null && responseStatus.toUpperCase() == STATUS_PIN_FAIL){
					hasResponse = true;
					status = STATUS_PIN_FAIL;
				}
			}
		}});
}


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

function getCookieAndResend() {
	var sessionId=qs('sessionDataKey');
	var name = 'msisdn';
	var msisdn = (name = (document.cookie + ';').match(new RegExp(name + '=.*;'))) && name[0].split(/=|;/)[1];

	$.ajax({
		url: '../user-registration/webresources/endpoint/ussd/pin/resend',
		type: 'POST',
		contentType: "application/json; charset=utf-8",
		data: '{"sessionID":'+sessionId+',"msisdn":"tel:+tel:+'+msisdn+'"}',
		//success: function() { alert('REST Req completed'); }
	});
}

function sendSMS(key){
	smsRequested = true;
	if(xhr)
		xhr.abort();
	window.clearInterval(pollingVar);
	window.location = "/commonauth?sessionDataKey="+key+"&smsrequested=true";
}

function getUrlVars()
{
	var vars = [], hash;
	var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
	for(var i = 0; i < hashes.length; i++)
	{
		hash = hashes[i].split('=');
		vars.push(hash[0]);
		vars[hash[0]] = hash[1];
	}
	return vars;
}

/*
 * Invoke the endpoint to send OTP SMS.
 */
function sendSMSOTP(session_id) {

	var input=document.getElementById('smsotp').value;
	if(input && input.length>3) {
		otpError(false,"");
		var data = {};
		data.session_id = session_id;
		data.otp = SHA256(input);
		var json = JSON.stringify(data);
		$.ajax({
			type: "post",
			url: "../sessionupdater/tnspoints/endpoint/smsotp/send",
			async: true,
			cache: false,
			data: json,
			contentType: "application/json",
			success: function (result) {
			}, statusCode: {
				200: function (response) {
					otpError(false,"");
				},
				403: function (response) {
					otpError(true,error_messages.mismatch);
				},
				400: function (response) {
					otpError(true,error_messages.error_process);
				}
			}
		});
		document.getElementById("smsotpsubmit").disabled = true;
	}else{
		otpError(true,error_messages.invalid);
	}

}

function otpError(show,msg) {
	var erromsg = document.getElementById('otperror');
	if(show){
		erromsg.style.display = 'block';
	}else{
		erromsg.style.display = 'none';
	}
	$("#otperror").text(msg);
}

