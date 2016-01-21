package com.axiata.dialog.authenticationstephandler;

import com.axiata.dialog.claimhandler.ClaimReader;
import com.axiata.dialog.openidtokenbuilder.Messages;
import com.axiata.dialog.util.ApplicationManagementServiceClient;
import com.axiata.dialog.util.LoginAdminServiceClient;
import com.gsma.authenticators.LOACompositeAuthenticator;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
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
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

public class MIFEAuthenticationStepHandler extends DefaultStepHandler {

	private static Log log = LogFactory.getLog(MIFEAuthenticationStepHandler.class);
    private static String backEndUrl = Messages.getString("isBackEndUrl");
    private static String adminUsername = Messages.getString("adminUsername");
    private static String adminPassword = Messages.getString("adminPassword");
    private static String authenticatorClaimUri = Messages.getString("authenticatorClaimUri");

    //This is to identify the first iteration in 3rd step.
    private static boolean isFirstIteration = false;


	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context) throws FrameworkException {

        int currentStep = context.getCurrentStep();
        if(currentStep < 3){
            isFirstIteration = true;
        }
        //step ==3 is after LOA and MSISDN authenticators
        if(isFirstIteration && currentStep == 3){
            String msisdn = context.getProperty("msisdn").toString();
            String userPreferredAuthenticator = "";
            List<String> authenticators = null;
            try {
                LoginAdminServiceClient loginAdminServiceClient = new LoginAdminServiceClient(backEndUrl);
                String sessionCookie = loginAdminServiceClient.authenticate(adminUsername, adminPassword);
                ClaimReader claimReader = new ClaimReader(backEndUrl, sessionCookie);
                userPreferredAuthenticator = claimReader.getClaim(msisdn, authenticatorClaimUri);
                ApplicationManagementServiceClient applicationManagementServiceClient = new ApplicationManagementServiceClient(sessionCookie, backEndUrl);
                authenticators = applicationManagementServiceClient.getAuthenticatorList();
            } catch (AxisFault axisFault) {
                log.error(axisFault);
            } catch (RemoteException e) {
                log.error(e);
            } catch (LoginAuthenticationExceptionException e) {
                log.error(e);
            } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
                log.error(e);
            }
            if(!userPreferredAuthenticator.equals("null") &&
                    (authenticators != null && authenticators.contains(userPreferredAuthenticator))){
                removeAllFollowingSteps(context, currentStep);
                AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
                authenticatorConfig.setName(userPreferredAuthenticator);
                authenticatorConfig.setApplicationAuthenticator(FrameworkUtils.getAppAuthenticatorByName(
                        userPreferredAuthenticator));
                Map<String, String> parameterMap = new HashMap<String, String>();
                parameterMap.put("onFail", "break");
                parameterMap.put("isLastAuthenticator", "true");
                authenticatorConfig.setParameterMap(parameterMap);

                SequenceConfig sequenceConfig = context.getSequenceConfig();
                Map<Integer, StepConfig> stepMap = sequenceConfig.getStepMap();
                StepConfig stepConfig = new StepConfig();
                stepConfig.setOrder(currentStep);

                stepConfig.getAuthenticatorList().add(authenticatorConfig);
                stepMap.put(currentStep, stepConfig);
                sequenceConfig.setStepMap(stepMap);
                context.setSequenceConfig(sequenceConfig);
                isFirstIteration = false;
            }
        }

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
					// else send to the multi option page.
					if (log.isDebugEnabled()) {
						log.debug("Sending to the Multi Option page");
					}

					String retryParam = "";

					if (stepConfig.isRetrying()) {
						context.setCurrentAuthenticator(null);
						retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
					}

					try {
						response.sendRedirect(redirectURL
								+ ("?" + context.getContextIdIncludedQueryParams())
								+ "&authenticators=" + authenticatorNames + retryParam);
					} catch (IOException e) {
						throw new FrameworkException(e.getMessage(), e);
					}

					return;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context, AuthenticatorConfig authenticatorConfig)
			throws FrameworkException {

		SequenceConfig sequenceConfig = context.getSequenceConfig();
		int currentStep = context.getCurrentStep();
		StepConfig stepConfig = sequenceConfig.getStepMap().get(currentStep);

		// Set false for all steps
		/*
		 * stepConfig.setSubjectAttributeStep(false);
		 * stepConfig.setSubjectIdentifierStep(false);
		 */

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

			// This is only a routing authenticator
			if (authenticator instanceof LOACompositeAuthenticator) {
				removeFailedStep(context, currentStep);
			} else {
				try {
					if (Boolean.parseBoolean(authenticatorConfig.getParameterMap().get(
							"isLastAuthenticator"))) {
						removeAllFollowingSteps(context, currentStep);
					}

					// set authenticator name in the context
					// This gets used in ID token later
					Object amrValue = context.getProperty("amr");
					List<String> amr;
					if (null != amrValue ) {
						amr = new ArrayList<String>(Arrays.asList(StringUtils.split(amrValue.toString(), ',')));
						amr.add(authenticator.getName());
						context.setProperty("amr", StringUtils.join(amr, ','));
					} else {
						amr = new ArrayList<String>();
						amr.add(authenticator.getName());
						context.setProperty("amr", StringUtils.join(amr, ','));
					}
				} catch (NullPointerException e) {
					// Possible exception during dashboard login
					// Should continue even if NPE is thrown
				}

				setAuthenticationAttributes(context, stepConfig, authenticatorConfig);
			}

		} catch (AuthenticationFailedException e) {

			if (e instanceof InvalidCredentialsException) {
				log.warn("A login attempt was failed due to invalid credentials");
			} else {
				log.error(e.getMessage(), e);
			}

			// add failed authenticators
			Object amrValue = context.getProperty("failedamr");
			List<String> amr;
			if (null != amrValue ) {
				amr = new ArrayList<String>(Arrays.asList(StringUtils.split(amrValue.toString(), ',')));
				amr.add(authenticator.getName());
				context.setProperty("failedamr", StringUtils.join(amr, ','));
			} else {
				amr = new ArrayList<String>();
				amr.add(authenticator.getName());
				context.setProperty("failedamr", StringUtils.join(amr, ','));
			}

			// Remove failed step from step map
			removeFailedStep(context, currentStep);
			try {
				String onFailProperty = authenticatorConfig.getParameterMap().get("onFail");

				if (!"".equals(onFailProperty) && !onFailProperty.equals("continue")) {
					// Should fail the whole LOA and continue to next if defined
					String fallBacklevel = authenticatorConfig.getParameterMap().get("fallBack");
					if (onFailProperty.equals("fallback") && StringUtils.isNotBlank(fallBacklevel)) {
						removeFollowingStepsOfCurrentLOA(context, authenticatorConfig
								.getParameterMap().get("currentLOA"), currentStep);
					} else {
						context.setRequestAuthenticated(false);
						context.getSequenceConfig().setCompleted(true);
					}
				}
			} catch (NullPointerException e1) {
				// Possible exception during dashboard login
				removeAllFollowingSteps(context, currentStep);
				context.setRequestAuthenticated(false);
				context.getSequenceConfig().setCompleted(true);
			}
		} catch (LogoutFailedException e) {
			throw new FrameworkException(e.getMessage(), e);
		}

		stepConfig.setCompleted(true);
	}

	private void setAuthenticationAttributes(AuthenticationContext context, StepConfig stepConfig,
			AuthenticatorConfig authenticatorConfig) {
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

		// Set sequence config
		if (stepConfig.isSubjectAttributeStep()) {
			SequenceConfig sequenceConfig = context.getSequenceConfig();
			sequenceConfig.setAuthenticatedUser(context.getSubject());
			sequenceConfig.setUserAttributes(userAttributes);
			context.setSequenceConfig(sequenceConfig);
		}
	}

	private void removeFailedStep(AuthenticationContext context, int currentStep) {
		context.getSequenceConfig().getStepMap().remove(currentStep);
	}

	private void removeFollowingStepsOfCurrentLOA(AuthenticationContext context, String currentLOA,
			int currentStep) {
		Map<Integer, StepConfig> stepMap = new HashMap<Integer, StepConfig>(context
				.getSequenceConfig().getStepMap());

		Iterator<Integer> it = stepMap.keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			StepConfig config = stepMap.get(key);

			try {
				if (currentLOA.equals(config.getAuthenticatorList().get(0).getParameterMap()
						.get("currentLOA"))) {
					context.getSequenceConfig().getStepMap().remove(key);
				}
			} catch (NullPointerException e) {
				continue;
			}
		}

		// Increment the current step
		for (int i = currentStep; i < 30; i++) {
			if (context.getSequenceConfig().getStepMap().keySet().contains(i)) {
				context.setCurrentStep(i);
				break;
			}
		}
	}

	private void removeAllFollowingSteps(AuthenticationContext context, int currentStep) {
		Map<Integer, StepConfig> stepMap = new HashMap<Integer, StepConfig>(context
				.getSequenceConfig().getStepMap());

		Iterator<Integer> it = stepMap.keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();

			try {
				if (key > currentStep) {
					context.getSequenceConfig().getStepMap().remove(key);
				}
			} catch (NullPointerException e) {
				continue;
			}
		}
	}
}
