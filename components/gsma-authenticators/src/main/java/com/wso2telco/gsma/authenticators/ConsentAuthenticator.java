/*******************************************************************************
 * Copyright (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) 
 *
 * All Rights Reserved. WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.gsma.authenticators;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDataPublisher;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.common.model.User;

import com.wso2telco.gsma.authenticators.model.Consent;
import com.wso2telco.gsma.authenticators.model.UserConsent;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import com.wso2telco.gsma.authenticators.util.FrameworkServiceDataHolder;
import com.wso2telco.ids.datapublisher.model.UserStatus;
import com.wso2telco.ids.datapublisher.util.DataPublisherUtil;

// TODO: Auto-generated Javadoc

/**
 * The Class MSISDNAuthenticator.
 */
public class ConsentAuthenticator extends AbstractApplicationAuthenticator
		implements LocalApplicationAuthenticator, BaseApplicationAuthenticator {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 6817280268460894001L;

	/**
	 * The log.
	 */
	private static Log log = LogFactory.getLog(ConsentAuthenticator.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.carbon.identity.application.authentication.framework.
	 * ApplicationAuthenticator#canHandle(javax
	 * .servlet.http.HttpServletRequest)
	 */
	@Override
	public boolean canHandle(HttpServletRequest request) {
		if (log.isDebugEnabled()) {
			log.debug("Consent Authenticator canHandle invoked");
		}

		if ((request.getParameter(Constants.ACTION) != null && !request.getParameter(Constants.ACTION).isEmpty())
				|| (request.getParameter(Constants.MSISDN) != null
						&& !request.getParameter(Constants.MSISDN).isEmpty())) {
			log.info("msisdn forwarding ");
			return true;
		}

		return false;
	}

	private boolean canProcessResponse(AuthenticationContext context) {
		return ((context.getProperty(Constants.MSISDN) != null
				&& !context.getProperty(Constants.MSISDN).toString().isEmpty())
				&& (context.getProperty(Constants.REDIRECT_CONSENT) == null
						|| !(Boolean) context.getProperty(Constants.REDIRECT_CONSENT)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.carbon.identity.application.authentication.framework.
	 * AbstractApplicationAuthenticator#process
	 * (javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity
	 * .application.authentication.framework.context.AuthenticationContext)
	 */
	@Override
	public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {
		if (context.isLogoutRequest()) {
			return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
		} else {
			return processRequest(request, response, context);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.carbon.identity.application.authentication.framework
	 * .AbstractApplicationAuthenticator#initiateAuthenticationRequest(javax.
	 * servlet.http.HttpServletRequest, javax .servlet.http.HttpServletResponse,
	 * org.wso2.carbon.identity.application.authentication.framework.context
	 * .AuthenticationContext)
	 */
	@Override
	protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context) throws AuthenticationFailedException {

		log.info("Initiating authentication request");

		try {
			String msisdn = context.getProperty(Constants.MSISDN).toString();
			String clientID = context.getServiceProviderName();
			String operator = context.getProperty(Constants.OPERATOR).toString();
			String scope = context.getProperty(Constants.PARENT_SCOPE).toString();

			DataPublisherUtil.updateAndPublishUserStatus(
					(UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
					DataPublisherUtil.UserState.REDIRECT_TO_CONSENT_PAGE, "Redirecting to consent page");

			Consent Consent = DBUtil.getConsentDetails(scope, clientID, operator);
			if (Consent.getStatus() != null) {
				if (Consent.getStatus().equalsIgnoreCase("approve")) {
					response.sendRedirect("/authenticationendpoint/consent.do?sessionDataKey="
							+ context.getContextIdentifier() + "&skipConsent=true");
				} else if (Consent.getStatus().equalsIgnoreCase("approveall")) {
					UserConsent userConsent = DBUtil.getUserConsentDetails(msisdn, scope, clientID, operator);
					if (userConsent.getConsumerKey() == null && userConsent.getMsisdn() == null
							&& userConsent.getOperator() == null && userConsent.getScope() == null) {
						response.sendRedirect("/authenticationendpoint/consent.do?sessionDataKey="
								+ context.getContextIdentifier() + "&skipConsent=false");
					} else {
						response.sendRedirect(
								"/commonauth/?sessionDataKey=" + context.getContextIdentifier() + "&action=default");
					}
				} else {
					terminateAuthentication(context);
				}
			} else {
				response.sendRedirect(
						"/commonauth/?sessionDataKey=" + context.getContextIdentifier() + "&action=default");
			}

		} catch (IOException e) {
			log.error("Error occurred while redirecting request", e);
			DataPublisherUtil.updateAndPublishUserStatus(
					(UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
					DataPublisherUtil.UserState.MSISDN_AUTH_PROCESSING_FAIL, e.getMessage());
			throw new AuthenticationFailedException(e.getMessage(), e);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.carbon.identity.application.authentication.framework
	 * .AbstractApplicationAuthenticator#processAuthenticationResponse(javax.
	 * servlet.http.HttpServletRequest, javax .servlet.http.HttpServletResponse,
	 * org.wso2.carbon.identity.application.authentication.framework.context
	 * .AuthenticationContext)
	 */
	@Override
	protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context) throws AuthenticationFailedException {
		log.info("Processing authentication response");
		String scope = context.getProperty(Constants.PARENT_SCOPE).toString();
		String msisdn = context.getProperty(Constants.MSISDN).toString();
		String clientID = context.getServiceProviderName();
		String operator = context.getProperty(Constants.OPERATOR).toString();

		String userAction = request.getParameter(Constants.ACTION);
		if (userAction != null && !userAction.isEmpty()) {
			// Change behaviour depending on user action
			switch (userAction) {
			case Constants.APPROVEALL:
				log.debug("MSISDN before inserting :" + msisdn);
				log.debug("Service Provider Name before inserting:" + clientID);
				log.debug("operator before inserting:" + operator);
				log.debug("scope before inserting:" + scope);
				try {
					DBUtil.insertUserConsentDetails(msisdn, scope, clientID, operator);
				} catch (SQLException | NamingException e) {
					e.printStackTrace();
				}
				break;
			case Constants.APPROVE:
				// do nothing
				break;
			case Constants.DENY:
				// User rejected to registration consent
				terminateAuthentication(context);
				break;
			default:
				// do nothing
				break;
			}
		}
		AuthenticationContextHelper.setSubject(context, msisdn);
		context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
	}

	public AuthenticatorFlowStatus processRequest(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {
		if (context.isLogoutRequest()) {
			try {
				if (!canHandle(request)) {
					context.setCurrentAuthenticator(getName());
					initiateLogoutRequest(request, response, context);
					return AuthenticatorFlowStatus.INCOMPLETE;
				} else {
					processLogoutResponse(request, response, context);
					return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
				}
			} catch (UnsupportedOperationException var8) {
				if (log.isDebugEnabled()) {
					log.debug("Ignoring UnsupportedOperationException.", var8);
				}

				return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
			}
		} else if ((canHandle(request) || canProcessResponse(context))
				&& (request.getAttribute("commonAuthHandled") == null
						|| !(Boolean) request.getAttribute("commonAuthHandled"))) {
			try {
				processAuthenticationResponse(request, response, context);
				if (this instanceof LocalApplicationAuthenticator
						&& !context.getSequenceConfig().getApplicationConfig().isSaaSApp()) {
					String e = context.getSubject().getTenantDomain();
					String stepMap1 = context.getTenantDomain();
					if (!StringUtils.equals(e, stepMap1)) {
						context.setProperty("UserTenantDomainMismatch", Boolean.valueOf(true));
						throw new AuthenticationFailedException("Service Provider tenant domain must be equal to user"
								+ " tenant domain for non-SaaS applications");
					}
				}

				request.setAttribute("commonAuthHandled", Boolean.TRUE);
				publishAuthenticationStepAttempt(request, context, context.getSubject(), true);
				return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
			} catch (AuthenticationFailedException e) {
				Object property = context.getProperty(Constants.IS_TERMINATED);
				boolean isTerminated = false;
				if (property != null) {
					isTerminated = (boolean) property;
				}

				Map stepMap = context.getSequenceConfig().getStepMap();
				boolean stepHasMultiOption = false;
				publishAuthenticationStepAttempt(request, context, e.getUser(), false);
				if (stepMap != null && !stepMap.isEmpty()) {
					StepConfig stepConfig = (StepConfig) stepMap.get(Integer.valueOf(context.getCurrentStep()));
					if (stepConfig != null) {
						stepHasMultiOption = stepConfig.isMultiOption();
					}
				}

				if (isTerminated) {
					throw new AuthenticationFailedException("Authenticator is terminated");
				}
				if (retryAuthenticationEnabled() && !stepHasMultiOption) {
					context.setRetrying(true);
					context.setCurrentAuthenticator(getName());
					initiateAuthenticationRequest(request, response, context);
					return AuthenticatorFlowStatus.INCOMPLETE;
				} else {
					throw e;
				}
			}
		} else {
			initiateAuthenticationRequest(request, response, context);
			context.setCurrentAuthenticator(getName());
			return AuthenticatorFlowStatus.INCOMPLETE;
		}
	}

	private void publishAuthenticationStepAttempt(HttpServletRequest request, AuthenticationContext context, User user,
			boolean success) {
		AuthenticationDataPublisher authnDataPublisherProxy = FrameworkServiceDataHolder.getInstance()
				.getAuthnDataPublisherProxy();
		if (authnDataPublisherProxy != null && authnDataPublisherProxy.isEnabled(context)) {
			boolean isFederated = this instanceof FederatedApplicationAuthenticator;
			HashMap paramMap = new HashMap();
			paramMap.put("user", user);
			if (isFederated) {
				context.setProperty("hasFederatedStep", Boolean.valueOf(true));
				paramMap.put("isFederated", Boolean.valueOf(true));
			} else {
				context.setProperty("hasLocalStep", Boolean.valueOf(true));
				paramMap.put("isFederated", Boolean.valueOf(false));
			}

			Map unmodifiableParamMap = Collections.unmodifiableMap(paramMap);
			if (success) {
				authnDataPublisherProxy.publishAuthenticationStepSuccess(request, context, unmodifiableParamMap);
			} else {
				authnDataPublisherProxy.publishAuthenticationStepFailure(request, context, unmodifiableParamMap);
			}
		}
	}

	/**
	 * Terminates the authenticator due to user implicit action
	 *
	 * @param context
	 *            Authentication Context
	 * @throws AuthenticationFailedException
	 */
	private void terminateAuthentication(AuthenticationContext context) throws AuthenticationFailedException {
		log.info("User has terminated the authentication flow");

		context.setProperty(Constants.IS_TERMINATED, true);
		throw new AuthenticationFailedException("Authenticator is terminated");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.carbon.identity.application.authentication.framework
	 * .AbstractApplicationAuthenticator#retryAuthenticationEnabled()
	 */
	@Override
	protected boolean retryAuthenticationEnabled() {
		// Setting retry to true as we need the correct MSISDN to continue
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.carbon.identity.application.authentication.framework
	 * .ApplicationAuthenticator#getContextIdentifier(javax.servlet.http.
	 * HttpServletRequest)
	 */
	@Override
	public String getContextIdentifier(HttpServletRequest request) {
		return request.getParameter("sessionDataKey");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.carbon.identity.application.authentication.framework.
	 * ApplicationAuthenticator#getFriendlyName()
	 */
	@Override
	public String getFriendlyName() {
		return Constants.CONSENT_AUTHENTICATOR_FRIENDLY_NAME;
	}

	/**
	 * Gets the private key file.
	 *
	 * @return the private key file
	 */
	private String getPrivateKeyFile() {
		return Constants.PRIVATE_KEYFILE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.wso2.carbon.identity.application.authentication.framework.
	 * ApplicationAuthenticator#getName()
	 */
	@Override
	public String getName() {
		return Constants.CONSENT_AUTHENTICATOR_NAME;
	}

	@Override
	public String getAmrValue(int acr) {
		return null;
	}
}
