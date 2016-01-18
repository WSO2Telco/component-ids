package com.axiata.dialog.authenticationstephandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.InvalidCredentialsException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.DefaultStepHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;

public class MIFEStepHandler extends DefaultStepHandler {

	private static Log log = LogFactory.getLog(MIFEStepHandler.class);

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context) throws FrameworkException {

		StepConfig stepConfig = context.getSequenceConfig().getStepMap()
				.get(context.getCurrentStep());
		List<AuthenticatorConfig> authConfigList = stepConfig.getAuthenticatorList();
		String authenticatorNames = FrameworkUtils.getAuthenticatorIdPMappingString(authConfigList);
		String redirectURL = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
		String fidp = request.getParameter(FrameworkConstants.RequestParams.FEDERATED_IDP);

		Map<String, AuthenticatedIdPData> authenticatedIdPs = context
				.getPreviousAuthenticatedIdPs();
		Map<String, AuthenticatorConfig> authenticatedStepIdps = FrameworkUtils
				.getAuthenticatedStepIdPs(stepConfig, authenticatedIdPs);

		// check passive authentication
		if (context.isPassiveAuthenticate()) {

			if (authenticatedStepIdps.isEmpty()) {
				context.setRequestAuthenticated(false);
			} else {
				String authenticatedIdP = authenticatedStepIdps.entrySet().iterator().next()
						.getKey();
				AuthenticatedIdPData authenticatedIdPData = authenticatedIdPs.get(authenticatedIdP);
				populateStepConfigWithAuthenticationDetails(stepConfig, authenticatedIdPData);
			}

			stepConfig.setCompleted(true);
			return;
		}

		// if Request has fidp param and if this is the first step
		if (fidp != null && !fidp.isEmpty() && stepConfig.getOrder() == 1) {
			handleHomeRealmDiscovery(request, response, context);
			return;
		} else if (context.isReturning()) {
			// if this is a request from the multi-option page
			if (request.getParameter(FrameworkConstants.RequestParams.AUTHENTICATOR) != null
					&& !request.getParameter(FrameworkConstants.RequestParams.AUTHENTICATOR)
							.isEmpty()) {
				handleRequestFromLoginPage(request, response, context);
				return;
			} else {
				// if this is a response from external parties (e.g. federated
				// IdPs)
				handleResponse(request, response, context);
				return;
			}
		}
		// if dumbMode
		else if (ConfigurationFacade.getInstance().isDumbMode()) {

			if (log.isDebugEnabled()) {
				log.debug("Executing in Dumb mode");
			}

			try {
				response.sendRedirect(redirectURL
						+ ("?" + context.getContextIdIncludedQueryParams()) + "&authenticators="
						+ authenticatorNames + "&hrd=true");
			} catch (IOException e) {
				throw new FrameworkException(e.getMessage(), e);
			}
		} else {

			if (!context.isForceAuthenticate() && !authenticatedStepIdps.isEmpty()) {

				Map.Entry<String, AuthenticatorConfig> entry = authenticatedStepIdps.entrySet()
						.iterator().next();
				String idp = entry.getKey();
				AuthenticatorConfig authenticatorConfig = entry.getValue();

				if (context.isReAuthenticate()) {

					if (log.isDebugEnabled()) {
						log.debug("Re-authenticating with " + idp + " IdP");
					}

					context.setExternalIdP(ConfigurationFacade.getInstance().getIdPConfigByName(
							idp, context.getTenantDomain()));
					doAuthentication(request, response, context, authenticatorConfig);
					return;
				} else {

					if (log.isDebugEnabled()) {
						log.debug("Already authenticated. Skipping the step");
					}

					// skip the step if this is a normal request
					AuthenticatedIdPData authenticatedIdPData = authenticatedIdPs.get(idp);
					populateStepConfigWithAuthenticationDetails(stepConfig, authenticatedIdPData);
					stepConfig.setCompleted(true);
					return;
				}
			} else {
				// Find if step contains only a single authenticator with a
				// single
				// IdP. If yes, don't send to the multi-option page. Call
				// directly.
				boolean sendToPage = false;
				AuthenticatorConfig authenticatorConfig = null;

				// Are there multiple authenticators?
				if (authConfigList.size() > 1) {
					sendToPage = true;
				} else {
					// Are there multiple IdPs in the single authenticator?
					authenticatorConfig = authConfigList.get(0);
					if (authenticatorConfig.getIdpNames().size() > 1) {
						sendToPage = true;
					}
				}

				if (!sendToPage) {
					// call directly
					if (authenticatorConfig.getIdpNames().size() > 0) {

						if (log.isDebugEnabled()) {
							log.debug("Step contains only a single IdP. Going to call it directly");
						}

						// set the IdP to be called in the context
						context.setExternalIdP(ConfigurationFacade.getInstance()
								.getIdPConfigByName(authenticatorConfig.getIdpNames().get(0),
										context.getTenantDomain()));
					}

					doAuthentication(request, response, context, authenticatorConfig);
					return;
				} else {
					// In MIFE, multiple authenticators are always set in a
					// single step.
					// Therefore it should always start from the first
					// authenticator

					if (log.isDebugEnabled()) {
						log.debug("Sending to MIFE step authentication module");
					}

					doMIFEAuthentication(request, response, context);
					return;
				}
			}
		}
	}

	private void doMIFEAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context)
			throws FrameworkException {

		SequenceConfig sequenceConfig = context.getSequenceConfig();
		int currentStep = context.getCurrentStep();
		StepConfig stepConfig = sequenceConfig.getStepMap().get(currentStep);
		
		for(AuthenticatorConfig authenticatorConfig:stepConfig.getAuthenticatorList()){
			ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();

			try {
				context.setAuthenticatorProperties(FrameworkUtils.getAuthenticatorPropertyMapFromIdP(
						context.getExternalIdP(), authenticator.getName()));
				AuthenticatorFlowStatus status = authenticator.process(request, response, context);

				if (log.isDebugEnabled()) {
					log.debug(authenticator.getName() + " returned: " + status.toString());
				}

				if (status == AuthenticatorFlowStatus.INCOMPLETE) {
					if (log.isDebugEnabled()) {
						log.debug(authenticator.getName() + " is redirecting");
					}
					return;
				}

				AuthenticatedIdPData authenticatedIdPData = new AuthenticatedIdPData();

				// store authenticated user
				String authenticatedUser = context.getSubject();
				stepConfig.setAuthenticatedUser(authenticatedUser);
				authenticatedIdPData.setUsername(authenticatedUser);

				// store authenticated user's attributes
				Map<ClaimMapping, String> userAttributes = context.getSubjectAttributes();
				stepConfig.setAuthenticatedUserAttributes(userAttributes);
				authenticatedIdPData.setUserAttributes(userAttributes);

				authenticatorConfig.setAuthenticatorStateInfo(context.getStateInfo());
				stepConfig.setAuthenticatedAutenticator(authenticatorConfig);

				String idpName = FrameworkConstants.LOCAL_IDP_NAME;

				if (context.getExternalIdP() != null) {
					idpName = context.getExternalIdP().getIdPName();
				}

				// store authenticated idp
				stepConfig.setAuthenticatedIdP(idpName);
				authenticatedIdPData.setIdpName(idpName);
				authenticatedIdPData.setAuthenticator(authenticatorConfig);
				// add authenticated idp data to the session wise map
				context.getCurrentAuthenticatedIdPs().put(idpName, authenticatedIdPData);

			} catch (AuthenticationFailedException e) {
				if (e instanceof InvalidCredentialsException) {
					log.warn("A login attempt was failed due to invalid credentials");
				} else {
					log.error(e.getMessage(), e);
				}
				context.setRequestAuthenticated(false);
			} catch (LogoutFailedException e) {
				throw new FrameworkException(e.getMessage(), e);
			}
		}		

		stepConfig.setCompleted(true);
	}
}
