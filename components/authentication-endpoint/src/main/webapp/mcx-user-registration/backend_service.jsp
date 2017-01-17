<%@page import="java.util.logging.Level" %>
<%@page import="java.util.logging.Logger" %>
<%@page import="java.util.Arrays" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.List" %>
<%@page import="java.net.HttpURLConnection" %>
<%@page import="java.net.URL" %>
<%@page import="org.json.simple.parser.JSONParser" %>
<%@page import="org.json.JSONObject" %>
<%@page import="org.json.simple.parser.ParseException" %>
<%@page import="java.io.BufferedReader" %>
<%@page import="java.io.InputStreamReader" %>
<%@page import="java.io.InputStream" %>
<%@page import="java.util.Map" %>
<%@page import="java.util.Map.Entry" %>
<%@page import="org.apache.commons.io.IOUtils" %>
<%@page import="java.net.MalformedURLException" %>
<%@page import="java.io.IOException" %>
<%@page import="java.net.URLEncoder" %>
<%@page import="org.wso2.carbon.identity.application.authentication.endpoint.util.UserRegistrationAdminServiceClient" %>
<%@page import="org.wso2.carbon.identity.application.authentication.endpoint.util.TenantDataManager" %>
<%@page import="org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO" %>
<%@page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@page import="java.util.Properties" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%! Logger log = Logger.getLogger(this.getClass().getName()); %>
<%!

    public String getWebAppsUrl() {
        String serverWebappsUrl = null;
        try {
            InputStream input = getServletContext().getResourceAsStream("/WEB-INF/classes/ServerDetails.json");
            String myString = IOUtils.toString(input, "UTF-8");

            JSONObject obj = new JSONObject(myString);
            serverWebappsUrl = obj.getJSONObject("server").getString("webapps_url");

        } catch (IOException e) {
            log.log(Level.SEVERE, "Error while reading configuration file", e);

        }
        return "http://" + serverWebappsUrl;
    }
%>
<%
    InputStream propertyFileInputStream = getServletContext().getResourceAsStream("/WEB-INF/classes/Constants.properties");
    Properties properties = new Properties();
    properties.load(propertyFileInputStream);

    String webappsUrl = getWebAppsUrl();
    String msisdn = request.getParameter("msisdn");
    String tokenid = request.getParameter("token");
    String operator = request.getParameter("operator");
    String acr_code = request.getParameter("acr_code");
    String updateProfile = request.getParameter("updateProfile");
    String isUserExists = "";
    String smsClick = null;
    boolean status = false;
    if (request.getParameter("smsClick") != null) {
        smsClick = request.getParameter("smsClick");
        log.info("sms link clicked " + smsClick);
    }

    log.info("param :: acr_code value : " + acr_code + ",  updateProfile value : " + updateProfile + ", msisdn : " + msisdn + ", tokenid : " + tokenid + ", operator : " + operator);

    String strBackend = "";

    try {
        URL isUserExisturl = new URL(webappsUrl + "user-registration/webresources/endpoint/user/exists?username=" + msisdn);
        HttpURLConnection conn = (HttpURLConnection) isUserExisturl.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
            log.log(Level.SEVERE, "Failed : HTTP error code : " + conn.getResponseCode());

        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String temp;
            StringBuilder input = new StringBuilder();
            while ((temp = br.readLine()) != null) {
                input.append(temp);
            }
            isUserExists = input.toString();

            if (br != null) {
                br.close();
            }
            conn.disconnect();
        }
        if (isUserExists != "true" || updateProfile == "true") {

            URL saverequesturl = new URL(getWebAppsUrl() + "user-registration/webresources/endpoint/ussd/saverequest?msisdn=" + msisdn + "&requesttype=1");
            HttpURLConnection conn2 = (HttpURLConnection) saverequesturl.openConnection();
            conn2.setRequestMethod("GET");
            conn2.setRequestProperty("Accept", "application/json");
            if (conn2.getResponseCode() != 200) {
                log.log(Level.SEVERE, "Failed : HTTP error code : " + conn2.getResponseCode());

            } else {
                BufferedReader br2 = new BufferedReader(new InputStreamReader((conn2.getInputStream())));
                String save = br2.readLine();
                if (br2 != null) {
                    br2.close();
                }
                conn2.disconnect();
            }

            String domain = request.getParameter("domain");
            String userNameVal = request.getParameter("userName");
            String openId = (String) request.getSession().getAttribute("openIdURL");
            String password = request.getParameter("pwd");
            String claim = properties.getProperty("DEFAULT_CLAIM_URL");
            String isHERegistration = request.getParameter("isHERegistration");

            if (request.getSession().getAttribute("openid") != null) {
                claim = properties.getProperty("OPENID_REG_CLAIM_URL");
            }
            String paramValues = "";

            TenantDataManager.init();
            UserRegistrationAdminServiceClient registrationClient = new UserRegistrationAdminServiceClient();
            UserFieldDTO[] userFields = new UserFieldDTO[0];
            userFields = registrationClient.readUserFieldsForUserRegistration(Constants.UserRegistrationConstants.WSO2_DIALECT);

            for (UserFieldDTO userFieldDTO : userFields) {
                String paramName = userFieldDTO.getClaimUri();
                String value = request.getParameter(paramName);
                paramValues = paramValues + value + ",";
            }

            paramValues = paramValues.substring(0, paramValues.length() - 1);
            paramValues = "params=" + URLEncoder.encode(paramValues, "UTF-8").replaceAll("\\%21", "!");
            String tmp = paramValues + "&claim=" + claim;

            if (smsClick == "true") {
                strBackend = getWebAppsUrl() + "user-registration/webresources/endpoint/sms/oneapi?" + "username=" + msisdn + "&" + "msisdn=" + msisdn + "&openId=" + openId + "&password=" + password + "&domain=" + domain + "&" + tmp + "&operator=" + operator;

            } else if (isHERegistration != null && isHERegistration == "true" && acr_code == "USSDAuthenticator") {
                strBackend = getWebAppsUrl() + "user-registration/webresources/endpoint/user/registration?" + "username=" + msisdn + "&" + "msisdn=" + msisdn + "&openId=" + openId + "&password=" + password + "&domain=" + domain + "&" + tmp + "&updateProfile=" + updateProfile + "&operator=" + operator;

            } else {

                if (acr_code.equals("USSDPinAuthenticator")) {
                    strBackend = getWebAppsUrl() + "user-registration/webresources/endpoint/ussd/pin?" + "username=" + msisdn + "&" + "msisdn=" + msisdn + "&openId=" + openId + "&password=" + password + "&domain=" + domain + "&" + tmp + "&updateProfile=" + updateProfile + "&operator=" + operator;

                }
                if (acr_code.equals("USSDAuthenticator")) {
                    strBackend = getWebAppsUrl() + "user-registration/webresources/endpoint/ussd/push?" + "username=" + msisdn + "&" + "msisdn=" + msisdn + "&openId=" + openId + "&password=" + password + "&domain=" + domain + "&" + tmp + "&operator=" + operator;

                }
            }


            URL ussdPushurl = new URL(strBackend);
            HttpURLConnection conn3 = (HttpURLConnection) ussdPushurl.openConnection();
            conn3.setRequestMethod("GET");
            conn3.setRequestProperty("Accept", "application/json");
            if (conn3.getResponseCode() != 200) {
                log.log(Level.SEVERE, "Failed : HTTP error code : " + conn3.getResponseCode());

            } else {
                BufferedReader br3 = new BufferedReader(new InputStreamReader((conn3.getInputStream())));

                if (br3 != null) {
                    br3.close();
                }
                conn3.disconnect();
            }


            if (tokenid != null) {

                log.info("_________________calling updateMsisdn.....");
                URL updatemsisdnurl = new URL(getWebAppsUrl() + "user-registration/webresources/endpoint/user/authenticate/updatemsisdn?msisdn=" + msisdn + "&tokenid=" + tokenid);
                HttpURLConnection conn4 = (HttpURLConnection) updatemsisdnurl.openConnection();
                conn4.setRequestMethod("GET");
                conn4.setRequestProperty("Accept", "application/json");
                if (conn4.getResponseCode() != 200) {
                    log.log(Level.SEVERE, "Failed : HTTP error code : " + conn4.getResponseCode());
                } else {

                    BufferedReader br4 = new BufferedReader(new InputStreamReader((conn4.getInputStream())));

                    if (br4 != null) {
                        br4.close();
                    }
                    conn4.disconnect();
                }

            }
            status = false;

        } else {
            status = true;

        }
    } catch (MalformedURLException malformedUrlException) {
        log.log(Level.SEVERE, "Error while accessing url due to MalformedURLException");
    } catch (IOException ioexception) {
        log.log(Level.SEVERE, "Error while accessing url");
    } catch (Exception e) {
        out.println("Error occurred while accessing server" + e);
    }
%>

<%=status%>



      
