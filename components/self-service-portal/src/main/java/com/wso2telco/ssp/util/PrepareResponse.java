/*******************************************************************************
 * Copyright (c) 2015-2017, WSO2.Telco Inc. (http://www.wso2telco.com)
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.ssp.util;

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
        if(nameValuePairsList == null){
            return Response.temporaryRedirect(new URIBuilder(uri).build()).build();
        }

        return Response.temporaryRedirect(new URIBuilder(uri).addParameters(nameValuePairsList).build()).build();
    }
}
