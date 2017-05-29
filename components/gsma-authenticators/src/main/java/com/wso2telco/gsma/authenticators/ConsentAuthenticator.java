
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
import java.util.*;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.HashedMap;
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
			String clientID = context.getProperty(Constants.CLIENT_ID).toString();
			String operator = context.getProperty(Constants.OPERATOR).toString();
			int operatorID = (DBUtil.getOperatorDetails(operator)).getOperatorId();
			String apiScopes = context.getProperty(Constants.API_SCOPES).toString();
			String apiScopesMinBracket = apiScopes.substring( 1, apiScopes.length() - 1);
			String[] api_Scopes = apiScopesMinBracket.split( ", ");
			context.setProperty(Constants.OPERATOR_ID, operatorID);
			boolean registering = (boolean) context.getProperty(Constants.IS_REGISTERING);

			DataPublisherUtil.updateAndPublishUserStatus(
					(UserStatus) context.getParameter(Constants.USER_STATUS_DATA_PUBLISHING_PARAM),
					DataPublisherUtil.UserState.REDIRECT_TO_CONSENT_PAGE, "Redirecting to consent page");

			String consentStatus= "deny";
			List<String> approveScopes= new ArrayList<String>();
			List<String> approveAllScopes= new ArrayList<String>();
			Map<String,String> scopeDescription = new HashedMap();
			for (String apiScope:api_Scopes) {
				UserConsent userConsentDetails = DBUtil.getUserConsentDetails(msisdn, apiScope, clientID, operatorID);
				if ((userConsentDetails.getConsumerKey() == null && userConsentDetails.getMsisdn() == null) ||(userConsentDetails.getConsumerKey() != null && userConsentDetails.isIs_approved()==true)) {
					Consent consent = DBUtil.getConsentDetails(apiScope, clientID, operatorID);
					if (consent.getStatus() != null) {
						if(consent.getStatus().equalsIgnoreCase("approve") || consent.getStatus().equalsIgnoreCase("approveall")) {
							approveScopes.add(apiScope);
							scopeDescription.put(apiScope, consent.getDescription());
							if(consent.getStatus().equalsIgnoreCase("approve")){
								consentStatus="approve";
							}
							else if (!consentStatus.equals("approve")){
								consentStatus="approveall";
							}
						}
					}
				}
			}

			String scopesDisplay = Arrays.toString(approveScopes.toArray());
			context.setProperty(Constants.APPROVED_SCOPES, scopesDisplay);
			context.setProperty(Constants.SCOPE_DESCRIPTION, scopeDescription);
			String logoPath = DBUtil.getSPConfigValue(operator, clientID, Constants.SP_LOGO);
			context.setProperty(Constants.SP_LOGO, logoPath);
			if (consentStatus.equalsIgnoreCase("approve")) {
				response.sendRedirect("/authenticationendpoint/consent.do?sessionDataKey="
							+ context.getContextIdentifier() + "&skipConsent=true&scope="+scopesDisplay + "&registering="+registering);
			} else if (consentStatus.equalsIgnoreCase("approveall")) {
				for (String apiApproveScope:approveScopes) {
					UserConsent userConsent = DBUtil.getUserConsentDetails(msisdn, apiApproveScope, clientID, operatorID);
					if (userConsent.getConsumerKey() == null && userConsent.getMsisdn() == null && userConsent.getScope() == null) {
						approveAllScopes.add(apiApproveScope);
					}
				}
				if(approveAllScopes.size()==0){
					response.sendRedirect(
							"/commonauth/?sessionDataKey=" + context.getContextIdentifier() + "&action=default");
				}
				else{
					String scopesString = Arrays.toString(approveAllScopes.toArray());
					context.setProperty(Constants.APPROVED_ALL_SCOPES, scopesString);
					response.sendRedirect("/authenticationendpoint/consent.do?sessionDataKey="
							+ context.getContextIdentifier() + "&skipConsent=false&scope="+scopesString + "&registering="+registering);
				}
			} else {
				terminateAuthentication(context);
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
		String msisdn = context.getProperty(Constants.MSISDN).toString();
		String clientID = context.getProperty(Constants.CLIENT_ID).toString();
		String operator = context.getProperty(Constants.OPERATOR).toString();
		int operatorID = (int) context.getProperty(Constants.OPERATOR_ID);

		String userAction = request.getParameter(Constants.ACTION);
		if (userAction != null && !userAction.isEmpty()) {
			// Change behaviour depending on user action
			switch (userAction) {
			case Constants.APPROVEALL:
				String scopesString = context.getProperty(Constants.APPROVED_ALL_SCOPES).toString();
				String scopesStringMinBracket = scopesString.substring( 1, scopesString.length() - 1);
				String[] api_Scopes = scopesStringMinBracket.split( ", ");				
				log.debug("MSISDN before inserting :" + msisdn);
				log.debug("Service Provider Name before inserting:" + clientID);
				log.debug("operator before inserting:" + operator);
				log.debug("scope before inserting:" + api_Scopes);
				try {
					for(String apiScope : api_Scopes){
						DBUtil.insertUserConsentDetails(msisdn, apiScope, clientID, operatorID);
						DBUtil.insertConsentHistoryDetails(msisdn, apiScope, clientID, operatorID, "approveall");
					}
				} catch (SQLException | NamingException e) {
					e.printStackTrace();
				}
				break;
			case Constants.APPROVE:
				if(context.getProperty(Constants.APPROVED_SCOPES)!=null){
					String approvedScopesString = context.getProperty(Constants.APPROVED_SCOPES).toString();
					String approvedScopesStringMinBracket = approvedScopesString.substring( 1, approvedScopesString.length() - 1);
					String[] approved_scopes = approvedScopesStringMinBracket.split( ", ");	
					try {
						for(String apprScope : approved_scopes){
							DBUtil.insertConsentHistoryDetails(msisdn, apprScope, clientID, operatorID, "approve");
						}
					} catch (SQLException | NamingException e) {
						e.printStackTrace();
					}
				}
				
				else if(context.getProperty(Constants.APPROVED_ALL_SCOPES)!=null){
					String approvedScopesString = context.getProperty(Constants.APPROVED_ALL_SCOPES).toString();
					String approvedScopesStringMinBracket = approvedScopesString.substring( 1, approvedScopesString.length() - 1);
					String[] approved_scopes = approvedScopesStringMinBracket.split( ", ");	
					try {
						for(String apprScope : approved_scopes){
							DBUtil.insertConsentHistoryDetails(msisdn, apprScope, clientID, operatorID, "approve");
						}
					} catch (SQLException | NamingException e) {
						e.printStackTrace();
					}
				}			
				break;
			case Constants.DENY:
				if(context.getProperty(Constants.APPROVED_SCOPES)!=null){
					String approvedScopesString = context.getProperty(Constants.APPROVED_SCOPES).toString();
					String approvedScopesStringMinBracket = approvedScopesString.substring( 1, approvedScopesString.length() - 1);
					String[] approved_scopes = approvedScopesStringMinBracket.split( ", ");	
					try {
						for(String apprScope : approved_scopes){
							DBUtil.insertConsentHistoryDetails(msisdn, apprScope, clientID, operatorID, "deny");
						}
					} catch (SQLException | NamingException e) {
						e.printStackTrace();
					}
				}
				
				else if(context.getProperty(Constants.APPROVED_ALL_SCOPES)!=null){
					String approvedScopesString = context.getProperty(Constants.APPROVED_ALL_SCOPES).toString();
					String approvedScopesStringMinBracket = approvedScopesString.substring( 1, approvedScopesString.length() - 1);
					String[] approved_scopes = approvedScopesStringMinBracket.split( ", ");	
					try {
						for(String apprScope : approved_scopes){
							DBUtil.insertConsentHistoryDetails(msisdn, apprScope, clientID, operatorID, "deny");
						}
					} catch (SQLException | NamingException e) {
						e.printStackTrace();
					}
				}		
				terminateAuthentication(context);
				break;
			default:
				// do nothing
				break;
			}
		}
		AuthenticationContextHelper.setSubject(context, msisdn);
	    if((boolean) context.getProperty(Constants.IS_OFFNET_FLOW)){
	    	context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "false");
	    }
	    else{	    	
	    	context.setProperty(Constants.TERMINATE_BY_REMOVE_FOLLOWING_STEPS, "true");
	    }
	
		
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
