$(document).ready(function(){

    //hide operator image from msisdn page
    $(".brandLogo").hide();

	// The template code
	var templateSource = $("#results-template").html();

	// compile the template
	var template = Handlebars.compile(templateSource);
	 
	// The div/container that we are going to display the results in
	var resultsPlaceholder = document.getElementById('content-placeholder');
	
	//
	
    var baseurl = $("#baseURL").val();
    $.getJSON(baseurl+'/mcx-user-registration/languages/en.json', function(data) {
    	resultsPlaceholder.innerHTML = template(data);
    });
	
	
	// Register a helper
	Handlebars.registerHelper('set_msisdn', function(element){
		var msisdn = getCookie("msisdn");
	
		if(msisdn != null && msisdn.length != 0) {
			element.value = msisdn;
			//document.getElementById('loginForm').submit();
		
		}
	});
});

function setCookie(cname, expirydays) {
	
	var cvalue = document.getElementById('msisdn').value;
	
    var date = new Date();
    date.setTime(date.getTime() + (expirydays * 24 * 60 * 60 * 1000));
    var expires = "expires=" + date.toGMTString();
    document.cookie = cname + "=" + cvalue + "; " + expires;
}

function getCookie(cname) {
    var name = cname + "=";
    var cookieArray = document.cookie.split(';');
    for(var i = 0; i < cookieArray.length; i++) {
        var cookie = cookieArray[i];
        while (cookie.charAt(0) == ' ') {
        	cookie = cookie.substring(1);
        } 
        if (cookie.indexOf(name) != -1) {
        	return cookie.substring(name.length,cookie.length);
        }
    }
    return "";
}

function saveRequestDetails(msisdn) {
	var url = "/authenticationendpoint/mcx-user-registration/request/saveLoginDetails.jsp?msisdn=" + msisdn + "&requesttype=2";
	$.ajax({
        type: "GET",
        url: url,
        async:false,
    })
    /*.done(function (data) {
        json = $.parseJSON(data);
     });*/
	
	
	/**
	log.info("url :" + url);
	var xhr = new XMLHttpRequest();
	xhr.open("GET", url,false);//async=false
	xhr.send();
	log.info("FFFFFFFFF : >" + xhr.responseText.toString());
	var result = parse(xhr.responseText.toString());
	return result.status;
	*/
}

function submitLoginForm() {
	if (true === $('#loginForm').parsley().isValid()) {
		var msisdnParam=document.getElementById('msisdn').value;
		setCookie("msisdn", 1);
		document.getElementById('loginForm').submit();
	}
}
