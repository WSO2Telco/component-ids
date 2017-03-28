var msisdn = values["msisdn"];
var tokenVal = values["sessionDataKey"];
console.log(msisdn + " : " + tokenVal);

var timeout = 60000;
var pollingInterval = 2000;
var timeRemaining = timeout;
var hasResponse = false;
var isTimeout = false;
var status = 'pending';
var sessionId;
console.log(" timeout : " + timeout + " pollingInterval : " + pollingInterval + " timeRemaining : " + timeRemaining + " hasResponse : " + hasResponse + " isTimeout : " + isTimeout + " status : " + status);

var pollingVar = setInterval(pollForStatus, pollingInterval);
console.log("waiting");

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
function handleTermination() {

    window.clearInterval(pollingVar);
    var STATUS_PENDING = "pending";
    if (!status == STATUS_PENDING) {
        //$('.page__header').show();
        $('#sms_fallback').show();
    }
    //$('.page__header').hide();
    $('#sms_fallback').hide();

    setTimeout(function () {
        callCommonAuth();
    }, 5000);


}

/*
 * Redirect after end of registration
 */


function callCommonAuth() {
    var commonAuthURL;

    if (hasResponse) {
        commonAuthURL = "/commonauth/?sessionDataKey=" + tokenVal + "&isTerminated=false";
    } else {
        commonAuthURL = "/commonauth/?sessionDataKey=" + tokenVal + "&isTerminated=true";
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
    console.log("Inside checkUSSDResponseStatus-------------------------------");
    var url = "/sessionupdater/tnspoints/endpoint/saa/status?sessionId=" + sessionId;

    var STATUS_APPROVED = "Approved";

    $.ajax({
        type: "GET",
        url: url,
        async: false,
        cache: false,
        success: function (result) {
            if (result != null) {
                var responseStatus = result.status;

                if (responseStatus != null && responseStatus.toUpperCase() == STATUS_APPROVED.toUpperCase()) {
                    status = result.status;
                    console.log("status : " + status);
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
