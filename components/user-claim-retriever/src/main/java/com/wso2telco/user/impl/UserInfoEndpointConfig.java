/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.user.impl;


import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth.user.UserInfoAccessTokenValidator;
import org.wso2.carbon.identity.oauth.user.UserInfoClaimRetriever;
import org.wso2.carbon.identity.oauth.user.UserInfoRequestValidator;
import org.wso2.carbon.identity.oauth.user.UserInfoResponseBuilder;

import com.wso2telco.util.EndpointUtil;


// TODO: Auto-generated Javadoc

/**
 * The Class UserInfoEndpointConfig.
 */
public class UserInfoEndpointConfig {

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(UserInfoEndpointConfig.class);

    /**
     * The config.
     */
    private static UserInfoEndpointConfig config = new UserInfoEndpointConfig();

    /**
     * The request validator.
     */
    private static UserInfoRequestValidator requestValidator = null;

    /**
     * The access token validator.
     */
    private static UserInfoAccessTokenValidator accessTokenValidator = null;

    /**
     * The response builder.
     */
    private static UserInfoResponseBuilder responseBuilder = null;

    /**
     * The claim retriever.
     */
    private static UserInfoClaimRetriever claimRetriever = null;

    /**
     * Instantiates a new user info endpoint config.
     */
    private UserInfoEndpointConfig() {
        log.debug("Initializing the UserInfoEndpointConfig");
        initUserInfoEndpointConfig();
    }

    /**
     * Inits the user info endpoint config.
     */
    private void initUserInfoEndpointConfig() {
    }

    /**
     * Gets the single instance of UserInfoEndpointConfig.
     *
     * @return single instance of UserInfoEndpointConfig
     */
    public static UserInfoEndpointConfig getInstance() {
        return config;
    }

    /**
     * Gets the user info request validator.
     *
     * @return the user info request validator
     * @throws OAuthSystemException the o auth system exception
     */
    public UserInfoRequestValidator getUserInfoRequestValidator() throws OAuthSystemException {
        if (requestValidator == null) {
            synchronized (UserInfoRequestValidator.class) {
                if (requestValidator == null) {
                    try {
                        String requestValidatorClassName = EndpointUtil.getUserInfoRequestValidator();
                        Class requestValidatorClass =
                                this.getClass().getClassLoader()
                                        .loadClass(requestValidatorClassName);
                        requestValidator = (UserInfoRequestValidator) requestValidatorClass.newInstance();
                    } catch (ClassNotFoundException e) {
                        log.error("Error while loading configuration", e);
                    } catch (InstantiationException e) {
                        log.error("Error while loading configuration", e);
                    } catch (IllegalAccessException e) {
                        log.error("Error while loading configuration", e);
                    }
                }
            }
        }
        return requestValidator;
    }

    /**
     * Gets the user info access token validator.
     *
     * @return the user info access token validator
     */
    public UserInfoAccessTokenValidator getUserInfoAccessTokenValidator() {
        if (accessTokenValidator == null) {
            synchronized (UserInfoAccessTokenValidator.class) {
                if (accessTokenValidator == null) {
                    try {
                        String accessTokenValidatorClassName = EndpointUtil.getAccessTokenValidator();
                        Class accessTokenValidatorClass =
                                this.getClass().getClassLoader()
                                        .loadClass(accessTokenValidatorClassName);
                        accessTokenValidator =
                                (UserInfoAccessTokenValidator) accessTokenValidatorClass.newInstance();
                    } catch (ClassNotFoundException e) {
                        log.error("Error while loading configuration", e);
                    } catch (InstantiationException e) {
                        log.error("Error while loading configuration", e);
                    } catch (IllegalAccessException e) {
                        log.error("Error while loading configuration", e);
                    }
                }
            }
        }
        return accessTokenValidator;
    }

    /**
     * Gets the user info response builder.
     *
     * @return the user info response builder
     */
    public UserInfoResponseBuilder getUserInfoResponseBuilder() {
        if (responseBuilder == null) {
            synchronized (UserInfoResponseBuilder.class) {
                if (responseBuilder == null) {
                    try {
                        String responseBilderClassName = EndpointUtil.getUserInfoResponseBuilder();
                        Class responseBuilderClass =
                                this.getClass().getClassLoader()
                                        .loadClass(responseBilderClassName);
                        responseBuilder = (UserInfoResponseBuilder) responseBuilderClass.newInstance();
                    } catch (ClassNotFoundException e) {
                        log.error("Error while loading configuration", e);
                    } catch (InstantiationException e) {
                        log.error("Error while loading configuration", e);
                    } catch (IllegalAccessException e) {
                        log.error("Error while loading configuration", e);
                    }
                }
            }
        }
        return responseBuilder;
    }

    /**
     * Gets the user info claim retriever.
     *
     * @return the user info claim retriever
     */
    public UserInfoClaimRetriever getUserInfoClaimRetriever() {
        if (claimRetriever == null) {
            synchronized (UserInfoClaimRetriever.class) {
                if (claimRetriever == null) {
                    try {
                        String claimRetrieverClassName = EndpointUtil.getUserInfoClaimRetriever();
                        Class claimRetrieverClass =
                                this.getClass().getClassLoader()
                                        .loadClass(claimRetrieverClassName);
                        claimRetriever = (UserInfoClaimRetriever) claimRetrieverClass.newInstance();
                    } catch (ClassNotFoundException e) {
                        log.error("Error while loading configuration", e);
                    } catch (InstantiationException e) {
                        log.error("Error while loading configuration", e);
                    } catch (IllegalAccessException e) {
                        log.error("Error while loading configuration", e);
                    }
                }
            }
        }
        return claimRetriever;
    }

}
