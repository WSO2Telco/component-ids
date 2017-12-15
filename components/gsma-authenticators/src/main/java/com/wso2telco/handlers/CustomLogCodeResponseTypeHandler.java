package com.wso2telco.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.authz.handlers.CodeResponseTypeHandler;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.Map;

/**
 * Extended CodeResponseTypeHandler to log generated auth code
 */
public class CustomLogCodeResponseTypeHandler extends CodeResponseTypeHandler {

    private static Log log = LogFactory.getLog(CustomLogCodeResponseTypeHandler.class);

    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    public OAuth2AuthorizeRespDTO issue(OAuthAuthzReqMessageContext oauthAuthzMsgCtx) throws IdentityOAuth2Exception {
        OAuth2AuthorizeRespDTO oAuth2AuthorizeRespDTO = super.issue(oauthAuthzMsgCtx);
        String code = oAuth2AuthorizeRespDTO.getAuthorizationCode();

        if(null != oauthAuthzMsgCtx.getAuthorizationReqDTO()
                && null != oauthAuthzMsgCtx.getAuthorizationReqDTO().getUser()) {
            Map<ClaimMapping, String> attributes = oauthAuthzMsgCtx.getAuthorizationReqDTO().getUser()
                    .getUserAttributes();

            if (null != attributes) {
                org.apache.log4j.MDC.put("REF_ID", getStateFromClaimMapping(attributes));
            }

            String msisdn = oauthAuthzMsgCtx.getAuthorizationReqDTO().getUser().getAuthenticatedSubjectIdentifier();
            org.apache.log4j.MDC.put("MSISDN", msisdn);
        }

        log.info("Auth Code Generated : " + code);
        return oAuth2AuthorizeRespDTO;
    }

    /**
     * Retrieves state value from authenticated user attribute map
     * @param attributes attribute map
     * @return state
     */
    private String getStateFromClaimMapping(Map<ClaimMapping, String > attributes) {
        for (Map.Entry<ClaimMapping, String> entry : attributes.entrySet()) {
            ClaimMapping mapping = entry.getKey();
            if (mapping.getLocalClaim() != null && mapping.getLocalClaim().getClaimUri().equals("state"))
                return entry.getValue();
        }

        return "";
    }
}
