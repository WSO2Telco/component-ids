package com.wso2telco.ssp.api;

import com.wso2telco.ssp.exception.ApiException;
import com.wso2telco.ssp.util.PrepareResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UnexpectedExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception ex) {

        if(ex instanceof ApiException){
            return PrepareResponse.Error(ex.getMessage(), ((ApiException) ex).getErrorCode(),
                    ((ApiException) ex).getStatus());
        }

        return PrepareResponse.Error(ex.getMessage(), "general_error", Response.Status.INTERNAL_SERVER_ERROR);
    }
}