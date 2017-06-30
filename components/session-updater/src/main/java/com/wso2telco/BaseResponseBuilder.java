package com.wso2telco;

import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.Response;

public class BaseResponseBuilder {

    /**
     * Build the Faulty Response with relevant code/message
     *
     * @param responseCode
     * @param errCode
     * @param message
     * @return Response with specified parameters
     * @throws JSONException
     */
    protected Response buildErrorResponse(int responseCode, String errCode, String message) throws JSONException {
        JSONObject jsonErrMsg = new JSONObject();
        jsonErrMsg.put("errorCode", errCode);
        jsonErrMsg.put("message", message);
        return Response.status(responseCode).entity(jsonErrMsg.toString()).build();
    }
}
