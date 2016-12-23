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
 * Cancel registrations  and redirect to call back url
 */
function cancelProcessToRegister(token) {

    var url = "/dashboard/landing.jag";
    var tokenVal = token;

    if (tokenVal != null) {
        var callbackURL;
        var id = tokenVal;
        var username = getMSISDN(tokenVal);
        var backendurl = "../user-registration/webresources/endpoint/user/authenticate/get?tokenid=" + id;

        $.ajax({
            type: "GET",
            url: backendurl,
            async: false,
            dataType: 'json',
            success: function (result) {
                if (result != null) {
                    callbackURL = result.redirectUri;

                }
            }
        });

        url = callbackURL + "?error=access_denied";
        console.log("url   " + url);
    }
    /*redirect to callback url*/
    window.location = url;

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
    var pwd = randomPassword(10);
    //alert(pwd);
    var msisdn_header = getParameterByName('msisdn_header');
    var msisdn_header_enc_str = getParameterWithPlusByName('msisdn_header_enc_str');
    var msisdn_header_str = getParameterByName('msisdn_header_str');
    var isUserExists = getParameterByName('isUserExists');
    var acr_code;

    /*for the inline registration get acr code from acr value and msisdn (username) from token*/
    if (sessionDataKey) {
        //we can remove this code.
        acr_code = getAcrValue();
        msisdnval = getMSISDN(sessionDataKey);
    } else {
        acr_code = "USSDAuthenticator";
    }

    if (acr_code == "USSDPinAuthenticator") {
        var selectQ1 = document.getElementsByName('challengeQuestion1')[0];
        var challengeQ1 = selectQ1.options[selectQ1.selectedIndex].value;
        var selectQ2 = document.getElementsByName('challengeQuestion2')[0];
        var challengeQ2 = selectQ2.options[selectQ2.selectedIndex].value;

        var challengeA1 = document.getElementsByName('challengeAns1')[0].value;
        var challengeA2 = document.getElementsByName('challengeAns2')[0].value;

        document.getElementsByName('http://wso2.org/claims/challengeQuestion1')[0].value = challengeQ1 + "!" + challengeA1;
        document.getElementsByName('http://wso2.org/claims/challengeQuestion2')[0].value = challengeQ2 + "!" + challengeA2;

    }

    var strBack = "/commonauth";

    var values = {};
    values["msisdn"] = msisdn_header_str;
    values["sessionDataKey"] = sessionDataKey;
    values["acr_code"] = acr_code;
    values["authenticator"] = authenticator;
    values["domain"] = domain;
    values["pwd"] = pwd;
    values["http://wso2.org/claims/mobile"] = msisdn_header_str;
    values["http://wso2.org/claims/challengeQuestion1"] = challengeQ1 + "!" + challengeA1;
    values["http://wso2.org/claims/challengeQuestion2"] = challengeQ2 + "!" + challengeA2;
    values["smsClick"] = smsClick;
    values["updateProfile"] = updateProfile;
    values["operator"] = operator;
    values["http://wso2.org/claims/loa"] = acr;
    values["isHERegistration"] = msisdn_header;
    values["isUserExists"] = isUserExists;


    window.location = "/commonauth/?sessionDataKey=" + sessionDataKey + "&msisdn=" + msisdn_header_str
        + "&msisdn_header=" + msisdn_header_enc_str + "&operator=" + operator + "&isRegistration=true";

    // var commonAuthURL = "/commonauth/?sessionDataKey=" + sessionDataKey + "&msisdn=" + msisdn_header_str
    //     + "&msisdn_header=" + msisdn_header_enc_str + "&operator=" + operator + "&isRegistration=true"
    //     + "&acr_code=" + acr_code + "&authenticator=" + authenticator + "&domain=";

    // window.location = commonAuthURL;

    // $.ajax({
    //     type: "GET",
    //     url: strBack,
    //     data: values,
    //     dataType: "text",
    //     async: false
    // }).done(function (data) {
    //
    //     if (data && data.toString() == 'true') {
    //
    //         var msg = "User Name is already exist";
    //
    //         return true;
    //
    //     } else {
    //         if (callbackUrl) {
    //             window.location = callbackUrl + "&operator=" + operator;
    //         } else if (msisdn_header && msisdn_header == "true" && acr_code == "USSDAuthenticator") {
    //             console.log("HE Registration selfautherizing.....");
    //             selfAuthorize(sessionDataKey, msisdn_header_enc_str, operator);
    //         } else {
    //             //Delete previous code after refactoring
    //             //var f = document.createElement('form');
    //             //f.action='/authenticationendpoint/mcx-user-registration/waiting.jsp';
    //             //f.method='POST';
    //             //var i;
    //             //
    //             //for (var key in values) {
    //             //    console.log(key +" : "+values[key]);
    //             //    /*alert(key +" : "+values[key]);*/
    //             //    i=document.createElement('input');
    //             //    i.type='hidden';
    //             //    i.name=key;
    //             //    i.value=values[key];
    //             //    f.appendChild(i);
    //             //}
    //             //
    //             //document.body.appendChild(f);
    //             //f.submit();
    //             var commonAuthURL = "/commonauth/?sessionDataKey=" + sessionDataKey
    //                 + "&msisdn=" + msisdn_header_str
    //                 + "&msisdn_header=" + msisdn_header_enc_str
    //                 + "&operator=" + operator
    //                 + "&isRegistration=true";
    //
    //             window.location = commonAuthURL;
    //
    //         }
    //     }
    // });


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
function getAcrValue() {


    var acrReturn = "";
    var url = "/user-registration/webresources/endpoint/loa/authenticator?acr=" + acr;

    $.ajax({
        type: "GET",
        url: url,
        async: false,
        success: function (result) {
            if (result != null) {

                var responseStatus = result.status;
                if (result.authenticator.name != null) {

                    acrReturn = result.authenticator.name;


                }
            }
        }
    });

    if (acrReturn == null | acrReturn == "") {
        acrReturn = "USSDAuthenticator";
    }
    return acrReturn;

}

/*
 *  return msisdn from the token using authenticate request values
 *
 */
function getMSISDN(token) {

    var msisdn = '';
    var url = "/user-registration/webresources/endpoint/user/authenticate/get?tokenid=" + token;

    $.ajax({
        type: "GET",
        url: url,
        async: false,
        success: function (result) {
            if (result != null) {

                if (result.msisdn != null) {
                    msisdn = result.msisdn;

                }
            }
        }
    });

    return msisdn;

}

function selfAuthorize(sessionDataKey, msisdn, operator) {
    //getting userinfo from backend is useless here
    var callbackURL;
    var acr;
    var authendpoint;
    var token;
    var scope;
    var id = sessionDataKey;
    var state;
    var nonce;
    var username = msisdn;
    var url = "/user-registration/webresources/endpoint/user/authenticate/get?tokenid=" + id;

    //$.ajax({
    //	type: "GET",
    //	url:url,
    //	async: false,
    //	dataType: 'json',
    //	success:function(result){
    //		if(result != null) {
    //			scope = result.scope;
    //			callbackURL = result.redirectUri;
    //			state= result.state;
    //			nonce=result.nonce;
    //			clientkey = result.clientId;
    //			acr = result.acrValues;
    //			authendpoint = "/oauth2/authorize";
    //			token = result.tokenID;
    //		}
    //	}});

    //var url = authendpoint + "?scope="+encodeURIComponent(scope)+"&response_type=code&redirect_uri="
    //+ encodeURIComponent(callbackURL) + "&client_id=" + clientkey + "&acr_values="
    //+ acr+"&tokenid="+token+"&msisdn="+username+"&state="+state+"&nonce="+nonce + "&operator="+operator;
    var commonAuthURL = "/commonauth/?sessionDataKey=" + sessionDataKey
        + "&msisdn_header=" + msisdn
        + "&operator=" + operator;
    console.log("commonAuthURL   " + commonAuthURL);
    window.location = commonAuthURL;
}

