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
package com.wso2telco.authenticationstephandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;

import com.wso2telco.gsma.authenticators.LOACompositeAuthenticator;
import com.wso2telco.util.AuthenticationHealper;
import com.wso2telco.util.Params;

// TODO: Auto-generated Javadoc
/**
 * The Class MIFEAuthenticationStepHandler.
 */
public class MIFEAuthenticationStepHandler extends DefaultStepHandler {

	/** The log. */
	private static Log log = LogFactory.getLog(MIFEAuthenticationStepHandler.class);

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.DefaultStepHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
	 */
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

		
		//acr_values validation
		
		AuthenticationRequest authRequest = context.getAuthenticationRequest();
	    Map< String, String[]> paramMap = authRequest.getRequestQueryParams();
	    
		if (!paramMap.containsKey(Params.ACR_VALUES.toString())  ) {
			
			String redirectUri = paramMap.get(Params.REDIRECT_URI.toString())[0];
            String invalidRedirectUrl = redirectUri + "?error=invalid_request&error_description=acr_values_required";
            log.info("acr_values not found");

            try {
                response.sendRedirect(invalidRedirectUrl); 
            } catch (IOException ex) {
                Logger.getLogger(MIFEAuthenticationStepHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

		
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

					try {
						context.setExternalIdP(ConfigurationFacade.getInstance().getIdPConfigByName(
								idp, context.getTenantDomain()));
					} catch (IdentityProviderManagementException e) {
						e.printStackTrace();
						log.error(e);
						throw new FrameworkException(e.toString());
					}
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
						try {
							context.setExternalIdP(ConfigurationFacade.getInstance()
									.getIdPConfigByName(authenticatorConfig.getIdpNames().get(0),
											context.getTenantDomain()));
						} catch (IdentityProviderManagementException e) {
							e.printStackTrace();
							log.error(e);
							throw new FrameworkException(e.toString());
						}
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

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.DefaultStepHandler#doAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext, org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context, AuthenticatorConfig authenticatorConfig)
			throws FrameworkException {

		SequenceConfig sequenceConfig = context.getSequenceConfig();
		int currentStep = context.getCurrentStep();
		StepConfig stepConfig = sequenceConfig.getStepMap().get(currentStep);

		AuthenticationRequest authRequest = context.getAuthenticationRequest();
	    Map< String, String[]> paramMap = authRequest.getRequestQueryParams();
	        
	       
	    String scope = "";
	    String redirectUri = null;
	    String state = null;
	    String responseType = null;   
	    String client_id = paramMap.get(Params.CLIENT_ID.toString())[0];
	
		ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();

		try {
			context.setAuthenticatorProperties(FrameworkUtils.getAuthenticatorPropertyMapFromIdP(
					context.getExternalIdP(), authenticator.getName()));
			AuthenticatorFlowStatus status = authenticator.process(request, response, context);
			request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, status);
			
			//state validation
	        if (!paramMap.containsKey(Params.STATE.toString())  ) {
	            redirectUri = paramMap.get(Params.REDIRECT_URI.toString())[0];

	            String invalidRedirectUrl = redirectUri + "?error=invalid_request&error_description=state_required" ;
	            log.debug("state not found. client_id : "+client_id);

	            try {
	                response.sendRedirect(invalidRedirectUrl);	   
	            } catch (IOException ex) {
	                Logger.getLogger(MIFEAuthenticationStepHandler.class.getName()).log(Level.SEVERE, null, ex);
	            }
	        }
	        else {

	            state = paramMap.get(Params.STATE.toString())[0];
	            redirectUri = paramMap.get(Params.REDIRECT_URI.toString())[0];

	        }
			
			//scope validation			
			 if (!paramMap.containsKey(Params.SCOPE.toString())) {
				
				redirectUri = paramMap.get(Params.REDIRECT_URI.toString())[0];
				String invalidRedirectUrl = redirectUri + "?error=invalid_request&error_description=no+scope&state=" + state ;
	            log.debug("scope not found. client_id : "+client_id);

	            try {
	            	response.sendRedirect(invalidRedirectUrl);
	            } catch (IOException ex) {
	                Logger.getLogger(MIFEAuthenticationStepHandler.class.getName()).log(Level.SEVERE, null, ex);
	            }
			 }else{
				 
				 scope = paramMap.get(Params.SCOPE.toString())[0];
				 log.debug("Scope:" + scope);
			 }
			
	        //responseType validation
	        if (!paramMap.containsKey(Params.RESPONSE_TYPE.toString())  ) {
	        	
	        	
	            String invalidRedirectUrl = redirectUri + "?error=invalid_request&error_description=no+response_type" ;
	            log.debug("response_type not found. client_id : "+client_id);

	            try {
	                response.sendRedirect(invalidRedirectUrl);
	            } catch (IOException ex) {
	                Logger.getLogger(MIFEAuthenticationStepHandler.class.getName()).log(Level.SEVERE, null, ex);
	            }
	        }
	        else {

	        	responseType = paramMap.get(Params.RESPONSE_TYPE.toString())[0];
	        	log.debug("response_type : " + responseType);

	        	if(!responseType.equals(Params.CODE.toString())){
	        		String invalidRedirectUrl = redirectUri + "?error=invalid_request&error_description=response_type_should_be_code" ;
	 	            log.debug("invalid redirect URI = " + invalidRedirectUrl);
	 	            
	 	            
	 	           try {
	 	        	  response.sendRedirect(invalidRedirectUrl);					
	 	           } catch (IOException ex) {
		                Logger.getLogger(MIFEAuthenticationStepHandler.class.getName()).log(Level.SEVERE, null, ex);
	 	           }
	 	           
	        	}
	        }
		
			//nonce validation
			if (!paramMap.containsKey(Params.NONCE.toString())  ) {

	            String invalidRedirectUrl = redirectUri + "?error=invalid_request&error_description=nonce_required";
	            log.debug("nonce not found. client_id : "+client_id);

	            try {
	                response.sendRedirect(invalidRedirectUrl);	
	            } catch (IOException ex) {
	                Logger.getLogger(MIFEAuthenticationStepHandler.class.getName()).log(Level.SEVERE, null, ex);
	            }
	        }
			
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
					Object amrValue = context.getProperty(Params.AMR.toString());
					List<String> amr;
					if (null != amrValue && amrValue instanceof ArrayList<?>) {
						amr = (ArrayList<String>) amrValue;
						amr.add(authenticator.getName());
						context.setProperty(Params.AMR.toString(), amr);
					} else {
						amr = new ArrayList<String>();
						amr.add(authenticator.getName());
						context.setProperty(Params.AMR.toString(), amr);
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
			if (null != amrValue && amrValue instanceof ArrayList<?>) {
				amr = (ArrayList<String>) amrValue;
				amr.add(authenticator.getName());
				context.setProperty("failedamr", amr);
			} else {
				amr = new ArrayList<String>();
				amr.add(authenticator.getName());
				context.setProperty("failedamr", amr);
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

	/**
	 * Sets the authentication attributes.
	 *
	 * @param context the context
	 * @param stepConfig the step config
	 * @param authenticatorConfig the authenticator config
	 */
	private void setAuthenticationAttributes(AuthenticationContext context, StepConfig stepConfig,
			AuthenticatorConfig authenticatorConfig) {
		AuthenticatedIdPData authenticatedIdPData = AuthenticationHealper.createAuthenticatedIdPData(context);

		


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
			context.setSequenceConfig(sequenceConfig);
		}
		log.info("===============================================================================setting authenticated user=============");
		// Set the authenticated user as an object. 5.1.0 onwards
		stepConfig.setAuthenticatedUser(context.getSubject());
	}

	/**
	 * Removes the failed step.
	 *
	 * @param context the context
	 * @param currentStep the current step
	 */
	private void removeFailedStep(AuthenticationContext context, int currentStep) {
		context.getSequenceConfig().getStepMap().remove(currentStep);
	}

	/**
	 * Removes the following steps of current loa.
	 *
	 * @param context the context
	 * @param currentLOA the current loa
	 * @param currentStep the current step
	 */
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

	/**
	 * Removes the all following steps.
	 *
	 * @param context the context
	 * @param currentStep the current step
	 */
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
