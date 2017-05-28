/*******************************************************************************
 * Copyright (c) 2016, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to youunder the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


/*
* checkbox validation for Aprrove senario
*/
function TCBoxValidation(sessionkey,action) {

   if (jQuery("#tc_checkbox").is(":checked")) {

$('.checkbox-inline').css({'color':'#292b2c','border':'none','padding':'0px'});
  window.location = "/commonauth/?sessionDataKey="+sessionkey+"&action="+action;
 }
   else {
         $('.checkbox-inline').css({'color':'#E0000B' , 'border':'1px solid #E0000B' , 'padding':'5px'});
 }

}

/*
* checkbox validation for Aprroveall senario
*/
/*function TCBoxValidationall(sessionkey) {

   if (jQuery("#tc_checkbox").is(":checked")) {

$('.checkbox-inline').css({'color':'#292b2c','border':'none','padding':'0px'});
window.location = "/commonauth/?sessionDataKey="+sessionkey+"&action=all";
 }
   else {
         $('.checkbox-inline').css({'color':'#E0000B' , 'border':'1px solid #E0000B' , 'padding':'5px'});
 }

}*/



/*
 * Cancel registrations  and redirect to call back url
 */
function cancelProcessToRegister() {

    var sessionDataKey = getParameterByName('sessionDataKey');
    window.location = "/commonauth/?action=RegRejected&sessionDataKey=" + sessionDataKey;
}

/*
 * decode window locations params
 */
function getParameterByName(name) {
    var value = getParameterWithPlusByName(name).replace(/\+/g, " ");

    return decodeURIComponent(value);
}

function getParameterWithPlusByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);

    return results === null ? "" : results[1];
}

/*
 * for the sms registration get assigned values from waiting jsp 
 * or get values from register request
 */
var msisdnval = '';
var smsClick = false;
var sessionDataKey = '';
var acr = '';
var operator = '';
if (!smsClick) {
    acr = getParameterByName('acr_values');
    if (acr == null || acr == "") {
        acr = getParameterByName('acr')
    }
    sessionDataKey = getParameterByName('sessionDataKey');
    operator = getParameterByName('operator');
}

function randomPassword(length) {
    var chars = "abcdefghijklmnopqrstuvwxyz!@#$%&*ABCDEFGHIJKLMNOP1234567890";
    var pass = "";
    var generate = false;
    while (!generate) {
        pass = "";
        for (var x = 0; x < length; x++) {
            var i = Math.floor(Math.random() * chars.length);
            pass += chars.charAt(i);
        }
        var regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*(_|[^\w])).+$/;
        //print(pass);
        generate = regex.test(pass);
        //print(regex.test(pass));
    }

    return pass;
}

/*
 *  registering process starts and initiates the backend service process and
 *  forward to waiting jsp if the process is success
 *
 */
function registration() {

    var authenticator = getParameterByName('authenticator');
    var callbackUrl = getParameterByName('callback_url');
    var updateProfile = getParameterByName('updateProfile');
    var domain = "PRIMARY";
    var msisdn_header = getParameterByName('msisdn_header');
    var msisdn_header_enc_str = getParameterWithPlusByName('msisdn_header_enc_str');
    var msisdn_header_str = getParameterByName('msisdn_header_str');
    var isUserExists = getParameterByName('isUserExists');
    var acr_code;
    var selectQ1 = "";
    var challengeQ1 = "";
    var selectQ2 = "";
    var challengeQ2 = "";
    var challengeA1 = "";
    var challengeA2 = "";

    /*for the inline registration get acr code from acr value and msisdn (username) from token*/
    if (sessionDataKey) {
        //we can remove this code.
    } else {
        acr_code = "USSDAuthenticator";
    }

    if (document.getElementsByName('challengeQuestion1')) {

        selectQ1 = document.getElementsByName('challengeQuestion1')[0];
        challengeQ1 = selectQ1.options[selectQ1.selectedIndex].value;
        selectQ2 = document.getElementsByName('challengeQuestion2')[0];
        challengeQ2 = selectQ2.options[selectQ2.selectedIndex].value;

        challengeA1 = document.getElementsByName('challengeAns1')[0].value;
        challengeA2 = document.getElementsByName('challengeAns2')[0].value;

        document.getElementsByName('http://wso2.org/claims/challengeQuestion1')[0].value = challengeQ1 + "!" + challengeA1;
        document.getElementsByName('http://wso2.org/claims/challengeQuestion2')[0].value = challengeQ2 + "!" + challengeA2;
    }

    var data = {};
    data.challengeQuestion1 = challengeQ1;
    data.challengeQuestion2 = challengeQ2;
    data.challengeAnswer1 = challengeA1;
    data.challengeAnswer2 = challengeA2;
    data.sessionId = sessionDataKey;

    var json = JSON.stringify(data);

    $.ajax({
        type: "post",
        url: "/sessionupdater/tnspoints/endpoint/save/userChallenges",
        async: false,
        data: json,
        contentType: "application/json",
        success: function (result) {
            if (result.status == "S1000") {

                console.log("/commonauth/?sessionDataKey=" + sessionDataKey + "&msisdn=" + msisdn_header_str
                    + "&msisdn_header=" + msisdn_header_enc_str + "&operator=" + operator + "&isRegistration=true&domain=" + domain
                    + "&authenticator=" + authenticator + "&acr_code=" + acr_code + "&userName=" + msisdn_header_str);
                window.location = "/commonauth/?sessionDataKey=" + sessionDataKey + "&action=RegConsent";

            }
        }
    });
}

/*
 *  validate the msisdn for the self care registration (dashboard signup)
 *  check if user exists return false
 *
 */
function validateUser() {

    var status = false;

    var msisdn = document.getElementsByName("msisdn")[0];
    msisdnval = msisdn.value;

    var checkIfExistUser = "/dashboard/user_service.jag?username=" + msisdnval;
    $.ajax({
        type: "GET",
        url: checkIfExistUser,
        async: false
    }).done(function (data) {
        json = $.parseJSON(data);

        if (json.return == 'true') {

            alert("User Name is already exist");

            //window.location.href = "register.jsp?username="+ msisdnval;
        } else {

            status = true;


        }
    });


    return status;
}

/*
 *  send acr value and get the authenticator according to LOA
 *
 */


function selfAuthorize(sessionDataKey, msisdn, operator) {
    var commonAuthURL = "/commonauth/?sessionDataKey=" + sessionDataKey
        + "&msisdn_header=" + msisdn
        + "&operator=" + operator;

    window.location = commonAuthURL;
}

