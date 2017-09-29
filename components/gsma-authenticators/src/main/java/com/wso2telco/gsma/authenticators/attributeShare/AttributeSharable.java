package com.wso2telco.gsma.authenticators.attributeShare;


import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by aushani on 7/30/17.
 */
public interface AttributeSharable {

    /**
     *
     * @param context
     * @throws SQLException
     * @throws NamingException
     */
     Map<String,List<String>> getAttributeMap(AuthenticationContext context) throws SQLException, NamingException;

    /**
     *
     * @param context
     * @throws SQLException
     * @throws NamingException
     * @throws AuthenticationFailedException
     */
    public Map<String,String> getAttributeShareDetails(AuthenticationContext context) throws SQLException, NamingException,AuthenticationFailedException;



}
