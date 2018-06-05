/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.gsma.handlers;

import com.wso2telco.core.config.DataHolder;
import com.wso2telco.gsma.authenticators.Constants;
import com.wso2telco.historylog.DbTracelog;
import com.wso2telco.historylog.LogHistoryException;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl
        .DefaultStepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class MIFEStepBasedSequenceHandler.
 */
public class MIFEStepBasedSequenceHandler extends DefaultStepBasedSequenceHandler {

    /**
     * The log.
     */
    private static Log log = LogFactory.getLog(DefaultStepBasedSequenceHandler.class);
    private static final String HEADER_X_FORWARDED_FOR = "X-FORWARDED-FOR";
    private static final int MAX_NO_OF_STEPS = 30;


    /* (non-Javadoc)
     * @see org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl
     * .DefaultStepBasedSequenceHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http
     * .HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context
     * .AuthenticationContext)
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationContext context) throws FrameworkException {


        if (log.isDebugEnabled()) {
            log.debug("Executing the Step Based Authentication...");
        }

        while (!context.getSequenceConfig().isCompleted() && context.getCurrentStep() < MAX_NO_OF_STEPS) {
            int currentStep = context.getCurrentStep();

            // let's initialize the step count to 1 if this the beginning of
            // the sequence
            if (currentStep == 0) {
                currentStep++;
                context.setCurrentStep(currentStep);
            }

            StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(currentStep);
            if (null == stepConfig) {
                int curStep = context.getCurrentStep() + 1;
                context.setCurrentStep(curStep);
                continue;
            }

            // if the current step is completed
            if (stepConfig.isCompleted()) {
                stepConfig.setCompleted(false);
                stepConfig.setRetrying(false);


                // if the request didn't fail during the step execution
                if (context.isRequestAuthenticated()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Step " + stepConfig.getOrder()
                                + " is completed. Going to get the next one.");
                    }

                    currentStep = context.getCurrentStep() + 1;
                    context.setCurrentStep(currentStep);
                    stepConfig = context.getSequenceConfig().getStepMap().get(currentStep);

                } else {

                    if (log.isDebugEnabled()) {
                        log.debug("Authentication has failed in the Step "
                                + (context.getCurrentStep()));
                    }

                    // if the step contains multiple login options, we
                    // should give the user to retry
                    // authentication
                    if (stepConfig.isMultiOption() && !context.isPassiveAuthenticate()) {
                        stepConfig.setRetrying(true);
                        context.setRequestAuthenticated(true);
                    } else {
                        context.getSequenceConfig().setCompleted(true);
                    }
                }

                resetAuthenticationContext(context);
            }

            // if no further steps exists
            if (stepConfig == null) {

                if (log.isDebugEnabled()) {
                    log.debug("There are no more steps to execute");
                }

                // if no step failed at authentication we should do post
                // authentication work (e.g.
                // claim handling, provision etc)
                if (context.isRequestAuthenticated()) {

                    if (log.isDebugEnabled()) {
                        log.debug("Request is successfully authenticated");
                    }

                    context.getSequenceConfig().setCompleted(true);
                    handlePostAuthentication(request, response, context);
                }

                // we should get out of steps now.
                if (log.isDebugEnabled()) {
                    log.debug("Step processing is completed");
                }
                continue;
            }

            // if the sequence is not completed, we have work to do.
            if (log.isDebugEnabled()) {
                log.debug("Starting Step: " + String.valueOf(stepConfig.getOrder()));
            }

            FrameworkUtils.getStepHandler().handle(request, response, context);

            // if step is not completed, that means step wants to redirect
            // to outside
            if (!stepConfig.isCompleted()) {
                if (log.isDebugEnabled()) {
                    log.debug("Step is not complete yet. Redirecting to outside.");
                }
                return;
            }

            context.setReturning(false);
        }

/*
        String ipAddress = retrieveIPAddress(request);
        String authenticatedUser = "";
        String authenticators = "";
        if (context.isRequestAuthenticated()) {
            // authenticators
            Object amrValue = context.getProperty("amr");
            if (null != amrValue && amrValue instanceof ArrayList<?>) {
                @SuppressWarnings("unchecked")
                List<String> amr = (ArrayList<String>) amrValue;
                authenticators = amr.toString();
            }
            authenticatedUser = context.getSequenceConfig().getAuthenticatedUser().getUserName();
        } else {
            // authenticators
            Object amrValue = context.getProperty("failedamr");
            if (null != amrValue && amrValue instanceof ArrayList<?>) {
                @SuppressWarnings("unchecked")
                List<String> amr = (ArrayList<String>) amrValue;
                authenticators = amr.toString();
            }
            authenticatedUser = (String) context.getProperty("faileduser");
        }
        try {
            DbTracelog.LogHistory(context.getRequestType(), context.isRequestAuthenticated(),
                                  context.getSequenceConfig().getApplicationId(), authenticatedUser,
                                  authenticators, ipAddress);
        } catch (LogHistoryException ex) {
            log.error("Error occured while Login SP LogHistory", ex);
        }
        */
        writeLogHistory(request, context);

        boolean dataPublisherEnabled = DataHolder.getInstance().getMobileConnectConfig().getDataPublisher().isEnabled();
        if (dataPublisherEnabled && !("samlsso".equals(context.getRequestType()))) {
            publishAuthEndpointData(request, context);
            if (context.isRequestAuthenticated()) {
                DataPublisherUtil.updateAndPublishUserStatus(
                        (UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
                        DataPublisherUtil.UserState.LOGIN_SUCCESS,
                        "Authentication success");
            }
        }
        // Need to call this deliberately as sequenceConfig gets completed
        // within step handler

        if (context.isRequestAuthenticated()) {
            handlePostAuthentication(request, response, context);
        }
    }

    /**
     * Retrieve ip address.
     *
     * @param request the request
     * @return the string
     */
    private String retrieveIPAddress(HttpServletRequest request) {
        String header = request.getHeader(HEADER_X_FORWARDED_FOR);
        String ipAddress = header != null ? header : request.getRemoteAddr();
        return ipAddress;
    }

    private void writeLogHistory(HttpServletRequest request, AuthenticationContext context) {
        String authenticatedUser;
        String authenticators = "";
        Object amrValue;
        if (context.isRequestAuthenticated() && context.getSequenceConfig().getAuthenticatedUser() != null) {
            amrValue = context.getProperty("amr");
            authenticatedUser = context.getSequenceConfig().getAuthenticatedUser().getUserName();
        } else {
            amrValue = context.getProperty("failedamr");
            authenticatedUser = (String) context.getProperty("faileduser");
        }

        // authenticators
        if (null != amrValue && amrValue instanceof ArrayList<?>) {
            @SuppressWarnings("unchecked")
            List<String> amr = (ArrayList<String>) amrValue;
            authenticators = amr.toString();
        }

        try {
            String ipAddress = retrieveIPAddress(request);
            DbTracelog.LogHistory(context.getRequestType(), context.isRequestAuthenticated(),
                    context.getSequenceConfig().getApplicationId(), authenticatedUser,
                    authenticators, ipAddress);
        } catch (LogHistoryException ex) {
            log.error("Error occured while Login SP LogHistory", ex);
        }
    }

    private void publishAuthEndpointData(HttpServletRequest request, AuthenticationContext context) {

        Map<String, String> authMap = (Map<String, String>) context.getProperty(
                Constants.AUTH_ENDPOINT_DATA_PUBLISHING_PARAM);

        boolean isAuthenticated = context.isRequestAuthenticated();
        String consentStatus = "consent rejected";
        if (isAuthenticated) {
            consentStatus = "consent given";
        }

        boolean isNewuser = (boolean) context.getProperty(Constants.IS_REGISTERING);
        String authenticatedUser;
        String authenticators = "";
        Object amrValue;
        if (context.isRequestAuthenticated() && context.getSequenceConfig().getAuthenticatedUser() != null) {
            amrValue = context.getProperty("amr");
            authenticatedUser = context.getSequenceConfig().getAuthenticatedUser().getUserName();
        } else {
            amrValue = context.getProperty("failedamr");
            authenticatedUser = (String) context.getProperty("faileduser");
        }
        if (null != amrValue && amrValue instanceof ArrayList<?>) {
            @SuppressWarnings("unchecked")
            List<String> amr = (ArrayList<String>) amrValue;
            authenticators = amr.toString();
        }

        String telcoScope = "openid";
        if (request.getParameter("telco_scope") != null) {
            telcoScope = request.getParameter("telco_scope");
        } else if (context.getProperty("telco_scope") != null) {
            telcoScope = request.getParameter("telco_scope");
        }

        String systemTime = String.valueOf(new java.util.Date().getTime());

        authMap.put("ConsentTimestamp", systemTime);
        authMap.put("AuthenticatorEndTime", systemTime);
        authMap.put("IpHeader", retrieveIPAddress(request));
        authMap.put("AuthenticatorMethods", authenticators);
        authMap.put("IsAuthenticated", Boolean.toString(isAuthenticated));
        authMap.put("IsNewUser", Boolean.toString(isNewuser));
        authMap.put("Msisdn", authenticatedUser);
        authMap.put("Timestamp", systemTime);
        authMap.put("ConsentState", consentStatus);
        authMap.put("IsAuthCodeIssued", Boolean.toString(isAuthenticated));
        authMap.put("AppID", context.getSequenceConfig().getApplicationId());
        authMap.put("telco_scope", telcoScope);

        // Handling exception due to data unavailability in user registration scenario
        try {
            authMap.put("ServerHost", request.getHeader("X-FORWARDED-FOR"));
        } catch (NullPointerException e) {
            log.error("X-FORWARDED-FOR header does not exist", e);
        }

        if (authMap.get("AcrValue") == null) {
            authMap.put("AcrValue", request.getParameter("acr_values"));
        }
        authMap.put("SessionId", DataPublisherUtil.getSessionID(request, context));
        DataPublisherUtil.publishAuthEndpointData(authMap);
    }
}