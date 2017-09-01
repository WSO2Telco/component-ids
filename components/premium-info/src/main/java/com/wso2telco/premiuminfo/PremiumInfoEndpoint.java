package com.wso2telco.premiuminfo;

import com.wso2telco.user.impl.UserInfoEndpointConfig;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.user.UserInfoAccessTokenValidator;
import org.wso2.carbon.identity.oauth.user.UserInfoEndpointException;
import org.wso2.carbon.identity.oauth.user.UserInfoRequestValidator;
import org.wso2.carbon.identity.oauth.user.UserInfoResponseBuilder;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationResponseDTO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;


@Path("/")
public class PremiumInfoEndpoint {

    private static Log log = LogFactory.getLog(PremiumInfoEndpoint.class);

    @GET
    @Path("/")
    @Produces("application/json")
    public Response getUserClaims(@Context HttpServletRequest request) throws OAuthSystemException {

        String response = null;
        try {
            // validate the request
            UserInfoRequestValidator requestValidator = UserInfoEndpointConfig.getInstance().getUserInfoRequestValidator();
            String accessToken = requestValidator.validateRequest(request);
            // validate the access token
            UserInfoAccessTokenValidator tokenValidator = UserInfoEndpointConfig.getInstance().getUserInfoAccessTokenValidator();
            OAuth2TokenValidationResponseDTO tokenResponse = tokenValidator.validateToken(accessToken);
            // build the claims
            //ToDO - Validate the grant type to be implicit or authorization_code before retrieving claims
            UserInfoResponseBuilder userInfoResponseBuilder = UserInfoEndpointConfig.getInstance().getUserInfoResponseBuilder();
            try {
              //  tokenResponse.setScope();
                response = userInfoResponseBuilder.getResponseString(tokenResponse);
            } catch (org.apache.oltu.oauth2.common.exception.OAuthSystemException e) {
                e.printStackTrace();
            }

        } catch (UserInfoEndpointException e) {
            return handleError(e);
        } catch (OAuthSystemException e) {
            log.error("UserInfoEndpoint Failed", e);
            throw new OAuthSystemException("UserInfoEndpoint Failed");
        }

        ResponseBuilder respBuilder =
                Response.status(HttpServletResponse.SC_OK)
                        .header(OAuthConstants.HTTP_RESP_HEADER_CACHE_CONTROL,
                                OAuthConstants.HTTP_RESP_HEADER_VAL_CACHE_CONTROL_NO_STORE)
                        .header(OAuthConstants.HTTP_RESP_HEADER_PRAGMA,
                                OAuthConstants.HTTP_RESP_HEADER_VAL_PRAGMA_NO_CACHE);

        return respBuilder.entity(response).build();
    }

    /**
     * Build the error message response properly
     *
     * @param e
     * @return
     * @throws OAuthSystemException
     */
    private Response handleError(UserInfoEndpointException e) throws OAuthSystemException {
        log.debug(e);
        OAuthResponse res = null;
        try {
            res =
                    OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(e.getErrorCode()).setErrorDescription(e.getErrorMessage())
                            .buildJSONMessage();
        } catch (OAuthSystemException e1) {
            OAuthResponse response =
                    OAuthASResponse.errorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                            .setError(OAuth2ErrorCodes.SERVER_ERROR)
                            .setErrorDescription(e1.getMessage()).buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        }
        return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
    }

}
