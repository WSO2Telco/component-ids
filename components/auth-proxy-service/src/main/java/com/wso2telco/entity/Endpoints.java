/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com)
 * <p>
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package com.wso2telco.entity;

import com.wso2telco.exception.AuthProxyServiceException;
import com.wso2telco.util.AuthProxyConstants;
import com.wso2telco.util.ConfigLoader;
import com.wso2telco.util.DBUtils;
import com.wso2telco.util.EncryptAES;
import com.wso2telco.util.MobileConnectConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map.Entry;

@Path("/")
public class Endpoints {
    private static Log log = LogFactory.getLog(Endpoints.class);
    DBUtils dbUtils = null;

    public Endpoints() {
        dbUtils = new DBUtils();
    }

    @GET
    @Path("/oauth2/authorize/operator/{operatorName}/")
    public void RedirectToAuthorizeEndpoint(@Context HttpServletRequest httpServletRequest, @Context
            HttpServletResponse httpServletResponse, @Context HttpHeaders httpHeaders, @Context UriInfo uriInfo,
                                            @PathParam("operatorName") String operator, String jsonBody)
            throws Exception {
        //Read query params from the header.
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

        String queryString = "";
        for (Entry<String, List<String>> entry : queryParams.entrySet()) {
            queryString = queryString + entry.getKey().toString() + "=" + entry.getValue().get(0) + "&";
        }

        String msisdn = null;
        String ipAddress = null;

        List<String> ipAHeaderAppProperty = null;
        String authorizeUrlProperty = null;

        //Read operator msisdn header from the operator database.
        String msisdnAppProperty = null;
        try {
            msisdnAppProperty = DBUtils.getOperatorProperty(operator, AuthProxyConstants.MSISDN_HEADER);
            List<String> msisdnRequestHeader = httpHeaders.getRequestHeader(msisdnAppProperty);
            //Load mobile-connect.xml file.
            MobileConnectConfig mobileConnectConfigs = ConfigLoader.getInstance().getMobileConnectConfig();
            if (mobileConnectConfigs != null) {
                //Read Ipheader and authorize url of the operator from mobile-connect.xml
                String ipHeaderAppProperty = mobileConnectConfigs.getAuthProxy().getIpHeader();
                authorizeUrlProperty = mobileConnectConfigs.getAuthProxy().getAuthorizeURL();
                ipAHeaderAppProperty = httpHeaders.getRequestHeader(ipHeaderAppProperty);
            } else {
                throw new AuthProxyServiceException("mobile-connect.xml could not be found");
            }

            if (msisdnRequestHeader != null) {
                msisdn = msisdnRequestHeader.get(0);
                // Encrypt MSISDN
                msisdn = EncryptAES.encrypt(msisdn);
                // URL encode
                msisdn = URLEncoder.encode(msisdn, AuthProxyConstants.UTF_ENCODER);
            }

            if (ipAHeaderAppProperty != null) {
                ipAddress = ipAHeaderAppProperty.get(0);
            }

            //Have to check whether authorize url exists or not in mobile-connect.xml
            if (authorizeUrlProperty != null) {
                String authorizeURL = authorizeUrlProperty + queryString + AuthProxyConstants.MSISDN_HEADER + "=" +
                        msisdn + "&" + AuthProxyConstants.OPERATOR + "=" + operator;

                // Reconstruct Authorize url with ip address.
                if (ipAddress != null) {
                    authorizeURL += "&" + AuthProxyConstants.IP_ADDRESS + "=" + ipAddress;
                }
                if (log.isDebugEnabled()) {
                    log.debug("authorizeURL : " + authorizeURL);
                }

                httpServletResponse.sendRedirect(authorizeURL);
            } else {
                throw new AuthProxyServiceException("AuthorizeURL could not be found in mobile-connect.xml");
            }
        } catch (Exception e) {
            log.error("RedirectToAuthorizeEndpoint failed!", e);
            throw e;
        }
    }
}