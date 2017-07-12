package com.wso2telco;

import com.google.gson.Gson;
import com.wso2telco.core.config.ConfigLoader;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.entity.RegisterUserResponseBuilderRequest;
import com.wso2telco.entity.RegisterUserStatusInfo;
import com.wso2telco.entity.UserRegistrationResponse;
import com.wso2telco.user.UserService;
import com.wso2telco.util.Constants;
import com.wso2telco.util.ErrorCode;
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

    /**
     * The Configuration service
     */
    private static ConfigurationService configurationService = new ConfigurationServiceImpl();
    /**
     * The Validation service
     */
    private Validation validation = new Validation();
    /**
     * The UserService service
     */
    private UserService userService = new UserService();
    /**
     * The Gson object
     */
    private Gson userStatusInfosJson = new Gson();

    public Response registerUserResponseBuilder(RegisterUserResponseBuilderRequest registerUserResponseBuilderRequest) throws UserStoreException, IdentityException, RemoteUserStoreManagerServiceUserStoreExceptionException, LoginAuthenticationExceptionException, IOException {

        int maxMsisdnLimit = configurationService.getDataHolder().getMobileConnectConfig().getUserRegistrationAPI().getMaxMSISDNLimit();

        if (!validation.validateOperator(registerUserResponseBuilderRequest.getOperator())) {
            return buildErrorResponse(HttpStatus.SC_BAD_REQUEST, ErrorCode.ERR_INVALID_OPERATOR.getCode(), ErrorCode.ERR_INVALID_OPERATOR.getDescription());
        } else if (registerUserResponseBuilderRequest.getMsisdnArr() == null) {
            return buildErrorResponse(HttpStatus.SC_BAD_REQUEST, ErrorCode.ERR_INVALID_MESSAGE_FORMAT.getCode(), ErrorCode.ERR_INVALID_MESSAGE_FORMAT.getDescription());
        } else if (registerUserResponseBuilderRequest.getMsisdnArr().length() == 0) {
            return buildErrorResponse(HttpStatus.SC_BAD_REQUEST, ErrorCode.ERR_MSISDN_LIST_EMPTY.getCode(), ErrorCode.ERR_MSISDN_LIST_EMPTY.getDescription());
        } else if (registerUserResponseBuilderRequest.getMsisdnArr().length() > maxMsisdnLimit) {
            return buildErrorResponse(HttpStatus.SC_BAD_REQUEST, ErrorCode.ERR_MSISDN_EXCEED_LIMIT.getCode(), ErrorCode.ERR_MSISDN_EXCEED_LIMIT.getDescription());
        }

        userService.msisdnStatusUpdate(registerUserResponseBuilderRequest.getMsisdnArr(), registerUserResponseBuilderRequest.getOperator(), registerUserResponseBuilderRequest.getUserRegistrationStatusList());
        return Response.status(HttpStatus.SC_CREATED).entity(userStatusInfosJson.toJson(registerUserResponseBuilderRequest.getResponse())).build();
    }


    public Response unRegisterUserResponseBuilder(RegisterUserResponseBuilderRequest registerUserResponseBuilderRequest) throws UserStoreException, IdentityException, RemoteUserStoreManagerServiceUserStoreExceptionException, LoginAuthenticationExceptionException, IOException {

        int maxMsisdnLimit = configurationService.getDataHolder().getMobileConnectConfig().getUserUnRegistrationAPI().getMaxMSISDNLimit();

        if (!validation.validateOperator(registerUserResponseBuilderRequest.getOperator())) {
            return buildErrorResponse(HttpStatus.SC_BAD_REQUEST, ErrorCode.ERR_INVALID_OPERATOR.getCode(), ErrorCode.ERR_INVALID_OPERATOR.getDescription());
        } else if (registerUserResponseBuilderRequest.getMsisdnArr() == null) {
            return buildErrorResponse(HttpStatus.SC_BAD_REQUEST, ErrorCode.ERR_INVALID_MESSAGE_FORMAT.getCode(), ErrorCode.ERR_INVALID_MESSAGE_FORMAT.getDescription());
        } else if (registerUserResponseBuilderRequest.getMsisdnArr().length() == 0) {
            return buildErrorResponse(HttpStatus.SC_BAD_REQUEST, ErrorCode.ERR_MSISDN_LIST_EMPTY.getCode(), ErrorCode.ERR_MSISDN_LIST_EMPTY.getDescription());
        } else if (registerUserResponseBuilderRequest.getMsisdnArr().length() > maxMsisdnLimit) {
            return buildErrorResponse(HttpStatus.SC_BAD_REQUEST, ErrorCode.ERR_MSISDN_EXCEED_LIMIT.getCode(), ErrorCode.ERR_MSISDN_EXCEED_LIMIT.getDescription());
        }

        userService.unRegisterStatusUpdate(registerUserResponseBuilderRequest.getMsisdnArr(), registerUserResponseBuilderRequest.getOperator(), registerUserResponseBuilderRequest.getUserRegistrationStatusList());
        return Response.status(HttpStatus.SC_CREATED).entity(userStatusInfosJson.toJson(registerUserResponseBuilderRequest.getResponse())).build();
    }

}
