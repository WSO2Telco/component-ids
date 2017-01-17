<%@page import="java.util.logging.Logger" %>
<%@page import="java.net.URL"%>
<%@page import="java.net.HttpURLConnection"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.InputStream"%>
<%@page import="org.apache.commons.io.IOUtils"%>
<%@page import="org.json.JSONObject"%>
<%@page import="java.io.IOException"%>
<%!
public String getWebAppsUrl(){
String serverWebappsUrl = null;
try{
	InputStream input = getServletContext().getResourceAsStream("/WEB-INF/classes/ServerDetails.json");
	String myString = IOUtils.toString(input, "UTF-8");

	JSONObject obj = new JSONObject(myString);
	serverWebappsUrl = obj.getJSONObject("server").getString("webapps_url");

}catch(IOException e){
	System.out.println("**********************8User not authenticated");
	
}
	return "http://" + serverWebappsUrl;
}
%>
<% 
Logger log=Logger.getLogger(this.getClass().getName());
String msisdn = request.getParameter("msisdn") ;
String strBackend = getWebAppsUrl() +"user-registration/webresources/endpoint/ussd/saverequest?msisdn=" + msisdn + "&" + "requesttype=2";
URL url = new URL(strBackend);
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
conn.setRequestProperty("Accept", "application/json");
if (conn.getResponseCode() != 200) {
throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
}else{
BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
String isUserExists = br.readLine();
JSONObject saveRequestJsonObj = new JSONObject(isUserExists);
String statusSaved = saveRequestJsonObj.getString("status");
if(statusSaved != "1"){
	log.info("Couldn't save to 'pendingussd' table. Phones that can't do NI USSD will not support.");
}

if (br != null){br.close();}
conn.disconnect();
}
%>

