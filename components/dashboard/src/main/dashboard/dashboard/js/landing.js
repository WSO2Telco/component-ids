
//function agreedToTerms() {
//
//    if(document.getElementById("tc_checked").checked == true){
//        document.getElementById("validate-btn").disabled = false;
//    }
//    else{
//        document.getElementById("validate-btn").disabled = true;
//    }
//
//}



/*function cancelProcessToLogin() {
document.getElementById('light').style.display = 'none';
document.getElementById('fade').style.display = 'none';

}*/

function cancelProcessToLogin() {

    var loginURL = decodeURIComponent(document.cookie.replace(new RegExp("(?:(?:^|.*;)\\s*" + "loginRequestURL" + "\\s*\\=\\s*([^;]*).*$)|^.*$"), "$1")) || null;
    var result = {};
    var sp="";
    var url="/dashboard/landing.jag";

    if(loginURL!=null){
        loginURL.split("&").forEach(function(part) {
            var item = part.split("=");
            result[item[0]] = decodeURIComponent(item[1]);
            if(decodeURIComponent(item[0])=="sp"){
                sp=decodeURIComponent(item[1]);
            }
        });

        var getCallbackURL = "/dashboard/callback.jag?applicationName="+sp;
        
        $.ajax({
            type: "GET",
            url: getCallbackURL,
            async:false,
        }).done(function (data) {
            json = $.parseJSON(data);
            url=json.return.callbackUrl+"?error=access_denied";

        });
        
    }
//redirect to callback url
window.location.href =url;

}

var msisdnval='';

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
    results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function validate() {

    var authenticator=getParameterByName('authenticator');
    var callbackUrl=getParameterByName('callback_url');
    var tokenid=getParameterByName('token');
    var acr=document.getElementById('acr').value;
    var operator=document.getElementById('operator').value;
    if(document.getElementById('smsClick')!= null){
        var smsClick=document.getElementById('smsClick').value;
    }
    

    
    if(acr=="USSDPinAuthenticator"){
        var selectQ1 = document.getElementsByName('challengeQuestion1')[0];
        var challengeQ1 = selectQ1.options[selectQ1.selectedIndex].value;
        var selectQ2 = document.getElementsByName('challengeQuestion2')[0];
        var challengeQ2 = selectQ2.options[selectQ2.selectedIndex].value;

        var challengeA1 = document.getElementsByName('challengeAns1')[0].value;
        var challengeA2 = document.getElementsByName('challengeAns2')[0].value;

        document.getElementsByName('http://wso2.org/claims/challengeQuestion1')[0].value = challengeQ1 + "!" + challengeA1;
        document.getElementsByName('http://wso2.org/claims/challengeQuestion2')[0].value = challengeQ2 + "!" + challengeA2;

    }

    var element = "<div class=\"modal fade\" id=\"messageModal\">\n" +
    "  <div class=\"modal-dialog\">\n" +
    "    <div class=\"modal-content\">\n" +
    "      <div class=\"modal-header\">\n" +
    "        <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\">&times;</button>\n" +
    "        <h3 class=\"modal-title\">Modal title</h4>\n" +
    "      </div>\n" +
    "      <div class=\"modal-body\">\n" +
    "        <p>One fine body&hellip;</p>\n" +
    "      </div>\n" +
    "      <div class=\"modal-footer\">\n" +
    "      </div>\n" +
    "    </div>\n" +
    "  </div>\n" +
    "</div>";
    $("#message").empty();
    $("#message").append(element);

    var msisdn = document.getElementsByName("http://wso2.org/claims/mobile")[0];
    msisdnval = msisdn.value;


    msisdnval = msisdnval.replace("+", "");

//if(msisdnval.startsWith('+')){
    //if user entered value with + (eg: +94XXXXXXXX), remove the '+'
  //  msisdnval = msisdnval.substring(1, msisdnval.length);
//}

var fld = document.getElementsByName("userName")[0];
var value = fld.value;

//fld.value = "MobileConnect" + Math.floor((Math.random() * 10000) + 1);;
//var value1 = fld.value;

fld.value = msisdnval;

var fldPwd = document.getElementsByName("pwd")[0];
var value = fldPwd.value;

fldPwd.value = "cY4L3dBf@";
var value2 = fldPwd.value;

var fldRe = document.getElementsByName("retypePwd")[0];
var value = fldRe.value;

fldRe.value = "cY4L3dBf@";
var value3 = fldRe.value;


var value4 = msisdnval;

// if (!(value4.charAt(0) == '9' && value4.charAt(1) == '4' && value4.charAt(2) == '7')) {
//     message({content: 'Invalid Mobile Number.Should be in 9477....... format', type: 'error', cbk: function () {
//     } });
//     return false;
// }

//var strBackend = "http://10.62.96.187:9764/mavenproject1-1.0-SNAPSHOT/webresources/endpoint/ussd/pin?username=" + fld.value + "&" + "msisdn=" + value4;


//$.ajax({
//	  type:"GET",
//	  url:strBackend

// })

var checkIfExistUser = "/dashboard/user_service.jag?username=" + msisdnval;
$.ajax({
    type: "GET",
    url: checkIfExistUser,
    async: false
}).done(function (data) {
    json = $.parseJSON(data);
    //$('#myModal').modal('show');
    //drawPage(json);
    if (json.return == 'true') {
        var msg = "User Name is already exist";
        message({
            content: msg, type: 'error', cbk: function () {
            }
        });
        return true;
    } else {

        if (validateEmpty("userName").length > 0) {
            var msg = "Mobile number is required to Register";
            message({
                content: msg, type: 'error', cbk: function () {
                }
            });
            return false;
        }

        if(isNaN(msisdnval)){
            var msg = "Please enter a valid mobile number to register";
            message({
                content: msg, type: 'error', cbk: function () {
                }
            });
            return false;
        }

        if (validateEmpty("pwd").length > 0) {
            var msg = "Password is required";
            ;
            message({
                content: msg, type: 'error', cbk: function () {
                }
            });
            return false;
        }

        if (validateEmpty("retypePwd").length > 0) {
            var msg = "Password verification is required";
            ;
            message({
                content: '', type: 'error', cbk: function () {
                }
            });
            return false;
        }

        var pwd = $("input[name='pwd']").val();
        var retypePwd = $("input[name='retypePwd']").val();

        if (pwd != retypePwd) {
            var msg = "Password does not match";
            message({
                content: msg, type: 'error', cbk: function () {
                }
            });
            return false;
        }

        var domain = $("select[name='domain']").val();
        var pwdRegex = $("input[name='regExp_" + domain + "']").val();

        var reg = new RegExp(pwdRegex);
        var valid = reg.test(pwd);
        if (pwd != '' && !valid) {
            message({
                content: 'Password does not match with password policy', type: 'error', cbk: function () {
                }
            });
            return false;
        }


        var unsafeCharPattern = /[<>`\"]/;
        var elements = document.getElementsByTagName("input");
        for (i = 0; i < elements.length; i++) {
            if ((elements[i].type === 'text' || elements[i].type === 'password') &&
                elements[i].value != null && elements[i].value.match(unsafeCharPattern) != null) {
                message({
                    content: 'Unsafe input found', type: 'error', cbk: function () {
                    }
                });
            return false;
        }
    }

        // if (!document.getElementsByName("tc")[0].checked) {
        //     message({
        //         content: 'Please accept terms & conditions to complete registration',
        //         type: 'error',
        //         cbk: function () {
        //         }
        //     });
        //     return false;
        // }

        for (i = 0; i < elements.length; i++) {
            if ((elements[i].type === 'text' || elements[i].type === 'password') &&
                (elements[i].value == null || elements[i].value == "" )) {
                message({
                    content: 'Input value should not be empty', type: 'error', cbk: function () {
                    }
                });
            return false;
        }
    }

    var mailRegex = $("input[name='mailRegEx']").val();
    var mailInputName = $("input[name='mailInput']").val();
    var mailValue = $("input[name='" + mailInputName + "']").val();
    var regMail = new RegExp(mailRegex);
    var validMail = regMail.test(mailValue);
    if (mailValue != '' && !validMail) {
        message({
            content: 'Email is not valid ', type: 'error', cbk: function () {
            }
        });
        return false;
    }
    var strBack;

    if(!tokenid){

  strBack = "/dashboard/backend_service.jag?msisdn=" +msisdnval;//+"&authenticator="+authenticator+"&operator="+operator;
}else{


  strBack = "/dashboard/backend_service.jag?msisdn=" +msisdnval+"&authenticator="+authenticator+"&tokenid="+ tokenid+"&operator="+operator;
}

        // var question1Answer = question1.value;
//  var question1Value = question1.id;


        //question1.value = question1Value + "!" +question1Answer;


        // var question2 = document.getElementsByName("http://wso2.org/claims/challengeQuestion2")[0];

        // var question2Answer = question2.value;
        // var question2Value = question2.id;

        // question2.value = question2Value + "!" + question2Answer;


    // var strBack = "/dashboard/backend_service.jag?msisdn=" + msisdnval;
    var $inputs = $('#selfReg :input');

    // not sure if you wanted this, but I thought I'd add it.
    // get an associative array of just the values.
    //alert(strBack);
    var values = {};
    $inputs.each(function() {
      values[this.name] = $(this).val();
    //alert("inblock : "+values[this.name]+" : "+this.name);
});

    $.ajax({
        type: "GET",
        url: strBack,
        data: values,
        dataType: "json",
        async: false
    }).done(function (data) {

        if (data && data.toString() == 'true') {

            var msg = "User Name is already exist";
            message({
                content: msg, type: 'error', cbk: function () {
                }
            });
            return true;

        }else {
            if(callbackUrl){
              window.location = callbackUrl+"&operator="+operator;
          }
          else{
           if(tokenid){
            window.location.href = "waiting.jsp?username=" + msisdnval+"&tokenid="+tokenid+"&operator="+operator+"&acr="+acr+"&smsClick="+smsClick;
        }else{
            window.location.href = "waiting.jsp?username="+ msisdnval+"&operator="+operator+"&acr="+acr+"&smsClick="+smsClick;

        }

    }
}
});


}});}
//
//function checkUserRegistered(){
//	//document.selfReg.submit();
//
//	var $inputs = $('#selfReg :input');
//
//    // not sure if you wanted this, but I thought I'd add it.
//    // get an associative array of just the values.
//    var values = {};
//    $inputs.each(function() {
//        values[this.name] = $(this).val();
//    });
//
//	$.ajax({
//		type: "POST",
//		url: "controllers/user-registration/add.jag?",
//		data: values,
//		time: 10000,
//		dataType: "json",
//		cache: false,
//		success: function(data){
//			if(data.status == "ok") {
//				window.location.href = "waiting.jsp?username="+ msisdnval;
//			}
//			else {
//				window.location.href = "index.jag?e=1&error=service_invok_error";
//			}
//		},
//		error: function(data){
//			window.location.href = "index.jag?e=1&error=service_invok_error";
//		}
//	});
//	//window.open("waiting.jsp?username="+ msisdnval);
//       //window.location.href = "waiting.jsp?username="+ msisdnval;
//}
function validateUser() {



    var status=false;

    var msisdn = document.getElementsByName("msisdn")[0];
    msisdnval = msisdn.value;


    msisdnval = msisdnval.replace("+", "");


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

    status=true;



}
});


    return status;
}

function getAcrValue(){

var acr=document.getElementById('acr').value;
var acrReturn="";
var url = "../UserRegistration-1.0-SNAPSHOT/webresources/endpoint/loa/authenticator?acr="+acr;
  //  alert("IN getAcrValue");
  $.ajax({
    type: "GET",
    url:url,
    async: false,
    success:function(result){
        if(result != null) {
           // alert("__________");
           // alert("__________"+result.authenticator.name);
           var responseStatus = result.status; 
           if( result.authenticator.name!= null) {
            //status = result.status;
            acrReturn=result.authenticator.name;
            //console.info(result);
            
        }
    }
}});
//acr_value="USSDPinAuthenticator";
// if(acr_value=="USSDAuthenticator"){
//     acrReturn=2;

// }
// if(acr_value=="SMSAuthenticator"){
//     acrReturn=2;
// }
// if(acr_value=="USSDPinAuthenticator"){
//     acrReturn=3;
// }
if(acrReturn == null | acrReturn == "") {
    acrReturn="USSDAuthenticator";
}
return acrReturn;

}