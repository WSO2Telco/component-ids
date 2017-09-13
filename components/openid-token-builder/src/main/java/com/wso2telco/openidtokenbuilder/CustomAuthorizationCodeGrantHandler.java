package com.wso2telco.openidtokenbuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AuthorizationCodeGrantHandler;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;

public class CustomAuthorizationCodeGrantHandler extends AuthorizationCodeGrantHandler {

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        boolean isvalidateGrant = super.validateGrant(tokReqMsgCtx);
        boolean dataPublisherEnabled = DataHolder.getInstance().getMobileConnectConfig().getDataPublisher().isEnabled();
        if (dataPublisherEnabled) {
            String clientId = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("Timestamp", String.valueOf(new Date().getTime()));
            tokenMap.put("ClientId", clientId);
            tokenMap.put("ContentType", "application/x-www-form-urlencoded");
            tokenMap.put("status", "AUTH_CODE_FAIL");
            tokenMap.put("StatusCode", "400");

            if (!isvalidateGrant) {
                DataPublisherUtil.publishTokenEndpointData(tokenMap);
            }
        }
        return isvalidateGrant;
    }
}
