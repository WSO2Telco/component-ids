package com.axiata.dialog.reqpathsequencehandler;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.InvalidCredentialsException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.DefaultRequestPathBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;

import com.gsma.authenticators.DataHolder;
import com.gsma.authenticators.config.LOA;
import com.gsma.authenticators.config.LOAConfig;
import com.gsma.authenticators.config.LOA.MIFEAbstractAuthenticator;

public class MIFERequestPathBasedSequenceHandler extends DefaultRequestPathBasedSequenceHandler {

	private static Log log = LogFactory.getLog(MIFERequestPathBasedSequenceHandler.class);
	private static volatile MIFERequestPathBasedSequenceHandler instance;

	public static MIFERequestPathBasedSequenceHandler getInstance() {

		if (instance == null) {
			synchronized (MIFERequestPathBasedSequenceHandler.class) {

				if (instance == null) {
					instance = new MIFERequestPathBasedSequenceHandler();
				}
			}
		}

		return instance;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context) throws FrameworkException {
		if (log.isDebugEnabled()) {
			log.debug("Executing the Request Path Authentication...");
		}

		configureMIFEReqPathAuthenticators(request, context);

		SequenceConfig seqConfig = context.getSequenceConfig();
		List<AuthenticatorConfig> reqPathAuthenticators = seqConfig.getReqPathAuthenticators();

		for (AuthenticatorConfig reqPathAuthenticator : reqPathAuthenticators) {

			ApplicationAuthenticator authenticator = reqPathAuthenticator
					.getApplicationAuthenticator();

			if (log.isDebugEnabled()) {
				log.debug("Executing " + authenticator.getName());
			}

			try {
				// process method is called straight away
				AuthenticatorFlowStatus status = authenticator.process(request, response, context);

				if (log.isDebugEnabled()) {
					log.debug(authenticator.getName() + ".authenticate() returned: "
							+ status.toString());
				}

				if (status == AuthenticatorFlowStatus.INCOMPLETE) {
					if (log.isDebugEnabled()) {
						log.debug(authenticator.getName() + " is redirecting");
					}
					return;
				}

				String authenticatedUser = context.getSubject();
				seqConfig.setAuthenticatedUser(authenticatedUser);

				if (log.isDebugEnabled()) {
					log.debug("Authenticated User: " + authenticatedUser);
				}

				AuthenticatedIdPData authenticatedIdPData = new AuthenticatedIdPData();

				// store authenticated user
				authenticatedIdPData.setUsername(authenticatedUser);

				// store authenticated user's attributes
				Map<ClaimMapping, String> userAttributes = context.getSubjectAttributes();
				authenticatedIdPData.setUserAttributes(userAttributes);

				// store authenticated idp
				authenticatedIdPData.setIdpName(FrameworkConstants.LOCAL_IDP_NAME);
				reqPathAuthenticator.setAuthenticatorStateInfo(context.getStateInfo());
				authenticatedIdPData.setAuthenticator(reqPathAuthenticator);

				seqConfig.setAuthenticatedReqPathAuthenticator(reqPathAuthenticator);

				context.getCurrentAuthenticatedIdPs().put(FrameworkConstants.LOCAL_IDP_NAME,
						authenticatedIdPData);

				handlePostAuthentication(request, response, context, authenticatedIdPData);

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

			context.getSequenceConfig().setCompleted(true);
			return;
		}
	}

	private void configureMIFEReqPathAuthenticators(HttpServletRequest request,
			AuthenticationContext context) {

		LinkedHashSet<?> acrs = this.getACRValues(request);
		String selectedLOA = (String) acrs.iterator().next();

		LOAConfig config = DataHolder.getInstance().getLOAConfig();
		LOA loa = config.getLOA(selectedLOA);
		if (loa.getAuthenticators() == null) {
			config.init();
		}

		List<MIFEAbstractAuthenticator> mifeAuthenticators = loa.getAuthenticators();
		SequenceConfig sequenceConfig = context.getSequenceConfig();

		// Clear existing ReqPathAuthenticators list
		sequenceConfig.setReqPathAuthenticators(new ArrayList<AuthenticatorConfig>());

		for (MIFEAbstractAuthenticator mifeAuthenticator : mifeAuthenticators) {
			AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
			authenticatorConfig.setName(mifeAuthenticator.getAuthenticator().getName());
			authenticatorConfig.setApplicationAuthenticator(mifeAuthenticator.getAuthenticator());

			sequenceConfig.getReqPathAuthenticators().add(authenticatorConfig);
		}

		context.setSequenceConfig(sequenceConfig);
	}

	private LinkedHashSet<?> getACRValues(HttpServletRequest request) {
		String sdk = request.getParameter(OAuthConstants.SESSION_DATA_KEY);
		CacheKey ck = new SessionDataCacheKey(sdk);
		SessionDataCacheEntry sdce = (SessionDataCacheEntry) SessionDataCache.getInstance()
				.getValueFromCache(ck);
		LinkedHashSet<?> acrValues = sdce.getoAuth2Parameters().getACRValues();
		return acrValues;
	}
}
