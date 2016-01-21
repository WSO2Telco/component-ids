package com.axiata.dialog.stepbasedsequencehandler;

import com.axiata.dialog.historylog.DbTracelog;
import com.axiata.dialog.historylog.LogHistoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.DefaultStepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

public class MIFEStepBasedSequenceHandler extends DefaultStepBasedSequenceHandler {

	private static Log log = LogFactory.getLog(DefaultStepBasedSequenceHandler.class);

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context) throws FrameworkException {

		if (log.isDebugEnabled()) {
			log.debug("Executing the Step Based Authentication...");
		}

		while (!context.getSequenceConfig().isCompleted() && context.getCurrentStep() < 30) {
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
		try {
			/* Log SP Login History */
			String ipAddress = retriveIPAddress(request);
			String authenticatedUser = "";
			String authenticators = "";

			if (context.isRequestAuthenticated()) {

				// authenticators
				Object amrValue = context.getProperty("amr");
				if (null != amrValue ) {
					List<String> amr =  new ArrayList<String>(Arrays.asList(StringUtils.split(amrValue.toString(), ',')));
					authenticators = amr.toString();
				}
				authenticatedUser = context.getSequenceConfig().getAuthenticatedUser();

			} else {
				// authenticators
				Object amrValue = context.getProperty("failedamr");
				if (null != amrValue ) {
					List<String> amr = new ArrayList<String>(Arrays.asList(StringUtils.split(amrValue.toString(), ',')));
					authenticators = amr.toString();
				}

				authenticatedUser = (String) context.getProperty("faileduser");

			}

			DbTracelog.LogHistory(context.getRequestType(), context.isRequestAuthenticated(),
					context.getSequenceConfig().getApplicationId(), authenticatedUser,
					authenticators, ipAddress);
		} catch (LogHistoryException ex) {
			log.error("Error occured while Login SP LogHistory", ex);
		}

		// Need to call this deliberately as sequenceConfig gets completed
		// within step handler

		if (context.isRequestAuthenticated()) {
			handlePostAuthentication(request, response, context);
		}
	}

	public String retriveIPAddress(HttpServletRequest request) {

		String ipAddress = null;
		try {
			ipAddress = request.getHeader("X-FORWARDED-FOR");
		} catch (Exception e) {
		}

		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}

		return ipAddress;
	}
}
