package com.wso2telco.ssp.utils;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class PrepareResponse {

    /**
     * Prepare a success response from a string. Success responses are sent with 200 OK status code.
     * Sample : <code>{"data": {"message": "Wing"}}</code>
     * @param message the success message
     * @return Prepared success response
     */
    public static Response Success(String message){
        JSONObject response = new JSONObject();
        response.put("message", message);
        return PrepareResponse.Success(response);
    }

    /**
     * Prepare a success response from a json object. Success responses are sent with 200 OK status code.
     * Sample : <code>{"data": {"id": 1001, "name": "Wing"}}</code>
     * @param json the success response data payload
     * @return Prepared success response
     */
    public static Response Success(JSONObject json){
        JSONObject response = new JSONObject();
        response.put("data", json);
        return Response.ok(response.toString(), MediaType.APPLICATION_JSON).build();
    }

    /**
     * Prepare an error response. Error responses are sent with provided status code.
     * Sample : <code>{"error":{"code":"1","message":"Error message"}}</code>
     * @param message error message
     * @param errorCode custom error code
     * @param status http status
     * @return Prepared error response
     */
    public static Response Error(String message, String errorCode, Response.Status status){
        JSONObject response = new JSONObject();
        JSONObject errorObject = new JSONObject();
        errorObject.put("code", errorCode);
        errorObject.put("message", message);
        response.put("error", errorObject);
        return Response.status(status).entity(response.toString()).build();
    }

    /**
     * Redirects a response to given URI with query parameters
     * @param uri URI to redirect
     * @param nameValuePairsList query params
     * @return http redirect response
     * @throws URISyntaxException
     */
    public static Response Redirect(String uri, ArrayList<NameValuePair> nameValuePairsList) throws URISyntaxException{
        return Response.temporaryRedirect(new URIBuilder(uri).addParameters(nameValuePairsList).build()).build();
    }
}
