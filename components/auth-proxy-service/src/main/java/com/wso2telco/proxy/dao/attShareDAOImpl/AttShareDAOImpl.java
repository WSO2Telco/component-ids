package com.wso2telco.proxy.dao.attShareDAOImpl;


import com.wso2telco.core.dbutils.DBUtilException;
import com.wso2telco.core.dbutils.DbUtils;
import com.wso2telco.proxy.dao.AttShareDAO;
import com.wso2telco.proxy.util.AuthProxyEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aushani on 9/20/17.
 */
public class AttShareDAOImpl implements AttShareDAO {

    private static Log log = LogFactory.getLog(AttShareDAOImpl.class);

    /**
     * Close the database connection.
     *
     * @param connection Connection instance used by the method call
     * @param statement  prepared Statement used by the method call
     * @param resultSet  result set which is used by the method call
     */
    public void close(Connection connection, PreparedStatement statement, ResultSet resultSet) {

        try {
            if (resultSet != null)
                resultSet.close();
            if (statement != null)
                statement.close();
            if (connection != null)
                connection.close();
        } catch (Exception e) {
            log.error("Error occurred while Closing the Connection");
        }
    }

    @Override
    public String getSPTypeConfigValue(String operatorName, String clientId, String trustedSatus) throws SQLException,DBUtilException {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT ");
        sqlBuilder.append("ts.status ");
        sqlBuilder.append("FROM ");
        sqlBuilder.append(AuthProxyEnum.TABLENAME.TRUSTED_STATUS + " ts ");
        sqlBuilder.append(" INNER JOIN ");
        sqlBuilder.append(AuthProxyEnum.TABLENAME.SP_CONFIGURATION + " sp_config ");
        sqlBuilder.append( "ON sp_config.config_value=ts.id_Trusted_type ");
        sqlBuilder.append(" where");
        sqlBuilder.append(" sp_config.client_id=? ");
        sqlBuilder.append(" AND sp_config.operator=? ");
        sqlBuilder.append(" AND sp_config.config_key=? ");

        try {
            connection = DbUtils.getConnectDbConnection();
            preparedStatement = connection.prepareStatement(sqlBuilder.toString());
            preparedStatement.setString(1, clientId);
            preparedStatement.setString(2, operatorName);
            preparedStatement.setString(3, trustedSatus);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return resultSet.getString("status");
            }
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }

        return null;
    }


    @Override
    public Map<String, String> getScopeParams() throws SQLException,DBUtilException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Map<String, String> scopeParams = new HashMap<>();
        try {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT ");
            sqlBuilder.append("scp.scope,scp.scope_type ");
            sqlBuilder.append("FROM ");
            sqlBuilder.append("scope_parameter scp ");

            connection = DbUtils.getConnectDbConnection();
                preparedStatement = connection.prepareStatement(sqlBuilder.toString());
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    String scopeType = resultSet.getString("scope_type");
                    if(AuthProxyEnum.SCOPETYPE.ATT_VERIFICATION.name().equalsIgnoreCase(scopeType) ||AuthProxyEnum.SCOPETYPE.ATT_SHARE.name().equalsIgnoreCase(scopeType)){
                        scopeParams.put(resultSet.getString("scope"),scopeType);
                    }
                }

        } catch (DBUtilException e) {
            log.debug("error occurred while retreiving the data from database");
            throw new DBUtilException(e.getMessage(), e);
        } catch (SQLException e){
            log.debug("error occurred while retreiving the data from database");
            throw new SQLException(e.getMessage(),e);
        } finally {
            close(connection, preparedStatement, resultSet);
        }
        return scopeParams;
    }
}
