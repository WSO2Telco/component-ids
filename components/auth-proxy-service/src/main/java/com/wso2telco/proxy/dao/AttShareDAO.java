package com.wso2telco.proxy.dao;

import com.wso2telco.core.dbutils.DBUtilException;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by aushani on 9/20/17.
 */
public interface AttShareDAO {

  public String getSPTypeConfigValue(String operatorName, String clientId, String spType) throws SQLException,DBUtilException;

  public Map<String, String> getScopeParams() throws SQLException, DBUtilException;
}
