package com.wso2telco;

import com.google.gson.Gson;
import com.wso2telco.core.config.ConfigLoader;
import com.wso2telco.entity.RegisterUserStatusInfo;
import com.wso2telco.entity.UserRegistrationResponse;
import com.wso2telco.user.UserService;
import com.wso2telco.util.Constants;
import com.wso2telco.util.Validation;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

public class ResponseBuilder extends BaseResponseBuilder {

    public Response registerUserResponseBuilder(JSONArray msisdnArr, String operator, UserRegistrationResponse response, List<RegisterUserStatusInfo> userRegistrationStatusList) throws UserStoreException, IdentityException, RemoteUserStoreManagerServiceUserStoreExceptionException, LoginAuthenticationExceptionException, IOException {

        Validation validation=new Validation();
        UserService userService=new UserService();
        Gson userStatusInfosJson = new Gson();
        int maxMsisdnLimit=ConfigLoader.getInstance().getMobileConnectConfig().getUserRegistrationAPI().getMaxMSISDNLimit();

        if (!validation.validateOperator(operator)) {
            return buildErrorResponse(HttpStatus.SC_BAD_REQUEST, Constants.ERR_INVALID_OPERATOR, "Invalid operator");
        }
        if (msisdnArr == null ){
            return buildErrorResponse(HttpStatus.SC_BAD_REQUEST, Constants.ERR_INVALID_MESSAGE_FORMAT, "Invalid message format");
        }
        if (msisdnArr.length() == 0 ) {
            return buildErrorResponse(HttpStatus.SC_BAD_REQUEST, Constants.ERR_MSISDN_LIST_EMPTY, "msisdn list cannot be empty");
        }
        if (msisdnArr.length() > maxMsisdnLimit) {
            return buildErrorResponse(HttpStatus.SC_BAD_REQUEST, Constants.ERR_MSISDN_EXCEED_LIMIT, "Provided list of numbers exceeds allowed limit");
        }

        userService.msisdnStatusUpdate(msisdnArr,operator,userRegistrationStatusList);
        return Response.status(HttpStatus.SC_CREATED).entity(userStatusInfosJson.toJson(response)).build();
    }

}
