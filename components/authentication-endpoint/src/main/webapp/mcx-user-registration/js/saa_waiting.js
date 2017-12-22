var msisdn = values["msisdn"];
var tokenVal = values["sessionDataKey"];
console.log(msisdn + " : " + tokenVal);

var timeout = 60000;
var pollingInterval = 2000;
var timeRemaining = timeout;
var hasResponse = false;
var isTimeout = false;
var status = 'pending';
var STATUS_APPROVED = "APPROVED";
var STATUS_REJECTED = "REJECTED";
var STATUS_PENDING = "PENDING";
var sessionId;
var action;
console.log(" timeout : " + timeout + " pollingInterval : " + pollingInterval + " timeRemaining : " + timeRemaining + " hasResponse : " + hasResponse + " isTimeout : " + isTimeout + " status : " + status);

var pollingVar = setInterval(pollForStatus, pollingInterval);

/* 
 * Check for USSD response status if timeout not is reached 
 * or user approval status(USSD) is not specified (YES/NO).
 */
function pollForStatus() {
    console.log(" timeout : " + timeout + " pollingInterval : " + pollingInterval + " timeRemaining : " + timeRemaining + " hasResponse : " + hasResponse + " isTimeout : " + isTimeout + " status : " + status);

    // If timeout has not reached.
    if (timeRemaining > 0) {
        // If user has not specified a response(YES/NO).

        if (!hasResponse) {
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

    var commonAuthURL;
    window.clearInterval(pollingVar);

    	if(cancelButton){
    		action = "userCanceled";
    	} else if(status.toUpperCase() == STATUS_APPROVED){
            action = "approved";
        } else if(status.toUpperCase() == STATUS_REJECTED){
            action = "userCanceled";
        }else{
            action = "userRespondedOrTimeout";
        }

        callCommonAuth();
}

/*
 * Redirect after end of registration
 */
function callCommonAuth() {
    var commonAuthURL;

    if (hasResponse) {
	    commonAuthURL = "/commonauth/?sessionDataKey=" + sessionDataKey
		    + "&msisdn=" + msisdn
		    + "&action=" + action + "&canHandle=true"+"&isTerminated=false";
    } else {
	    commonAuthURL = "/commonauth/?sessionDataKey=" + sessionDataKey
		    + "&msisdn=" + msisdn
		    + "&action=" + action + "&canHandle=true"+"&isTerminated=true";
    }
    window.location = commonAuthURL;
}

function deleteUser(sessionId) {
    var deleteUserUrl = "/dashboard/delete_user.jag?username=" + sessionId;
    $.ajax({
        type: "GET",
        url: deleteUserUrl,
        async: false,
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

/*
 * Invoke the endpoint to retrieve USSD status.
 */
function checkUSSDResponseStatus() {

    sessionId = tokenVal;
    console.log("Inside check saa status -------------------------------");
    var url = "/sessionupdater/tnspoints/endpoint/saa/status?sessionId=" + sessionId;

    var STATUS_APPROVED = "APPROVED";
    var STATUS_REJECTED = "REJECTED";

    $.ajax({
        type: "GET",
        url: url,
        async: false,
        cache: false,
        success: function (result) {
            if (result != null) {
                var responseStatus = result.status;

                if (responseStatus != null && (responseStatus.toUpperCase() == STATUS_APPROVED ||responseStatus.toUpperCase() == STATUS_REJECTED )) {
                    status = result.status;
                    hasResponse = true;
                }
            }
        }
    });

}


function resendUSSD() {

    var strBack = "backend_service.jag?msisdn=" + msisdn;
    $.ajax({
        type: "GET",
        url: strBack
    })
}

/*
 * when sms registration starts clear the polling values.
 * if the status is still pending, start the sms registration.
 */
function handleTerminationSms() {
    window.clearInterval(pollingVar);
    var STATUS_PENDING = "pending";
    console.log(status + " = " + STATUS_PENDING);
    if (status == STATUS_PENDING) {
        console.log('changed the flow');
        console.log(" timeout : " + timeout + " pollingInterval : " + pollingInterval + " timeRemaining : " + timeRemaining + " hasResponse : " + hasResponse + " isTimeout : " + isTimeout + " status : " + status);
        smsClick = true;
        token = values["token"];
        acr = values["acr"];
        operator = values["operator"];
        registration();

    } else {
        /*when sms link clicked if already registered using ussd*/
        console.log('registered already');
        pollForStatus();
    }

}
