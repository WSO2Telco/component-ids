package com.wso2telco.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import com.wso2telco.core.dbutils.model.FederatedIdpMappingDTO;

public class FederatedTransactionDAO {

    private FederatedTransactionDAO() {

    }

    private static final Log log = LogFactory.getLog(FederatedTransactionDAO.class);

    public static AccessTokenDO getExisingTokenFromIdentityDB(OAuthTokenReqMessageContext tokReqMsgCtx,
            FederatedIdpMappingDTO existingToken) throws IdentityOAuth2Exception {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;
        String sql = null;
        AccessTokenDO existingAccessTokenDO = null;

        sql = "SELECT REFRESH_TOKEN, TIME_CREATED, REFRESH_TOKEN_TIME_CREATED, VALIDITY_PERIOD, "
                + "REFRESH_TOKEN_VALIDITY_PERIOD, TOKEN_STATE, USER_TYPE, TOKEN_ID, SUBJECT_IDENTIFIER FROM "
                + "IDN_OAUTH2_ACCESS_TOKEN WHERE CONSUMER_KEY_ID = (SELECT ID FROM IDN_OAUTH_CONSUMER_APPS "
                + "WHERE CONSUMER_KEY = ?) AND ACCESS_TOKEN=? ORDER BY TIME_CREATED DESC LIMIT 1";

        try {

            prepStmt = connection.prepareStatement(sql);
            prepStmt.setString(1, tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId());
            prepStmt.setString(2, existingToken.getAccessToken());
            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {

                long issuedTime = resultSet.getTimestamp(2, Calendar.getInstance(TimeZone.getTimeZone("UTC")))
                        .getTime();
                long refreshTokenIssuedTime = resultSet.getTimestamp(3,
                        Calendar.getInstance(TimeZone.getTimeZone("UTC"))).getTime();

                String scope = OAuth2Util.buildScopeString(tokReqMsgCtx.getScope());
                AuthenticatedUser user = new AuthenticatedUser();
                user.setAuthenticatedSubjectIdentifier(resultSet.getString(9));
                existingAccessTokenDO = new AccessTokenDO(tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientSecret(),
                        user, OAuth2Util.buildScopeArray(scope), new Timestamp(issuedTime), new Timestamp(
                                refreshTokenIssuedTime), resultSet.getLong(4), resultSet.getLong(5),
                        resultSet.getString(7));
                existingAccessTokenDO.setAccessToken(existingToken.getAccessToken());
                existingAccessTokenDO.setRefreshToken(resultSet.getString(1));
                existingAccessTokenDO.setTokenState(resultSet.getString(6));
                existingAccessTokenDO.setTokenId(resultSet.getString(8));

            }

        }

        catch (SQLException e) {
            String errorMsg = "Error occurred while trying to retrieve latest 'ACTIVE' access token for Client ID : "
                    + tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId() + " from identity Database";
            log.error(errorMsg);
            throw new IdentityOAuth2Exception(errorMsg, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, prepStmt);
        }

        return existingAccessTokenDO;

    }

}
