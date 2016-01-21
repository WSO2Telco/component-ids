package com.gsma.authenticators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Routes IDPs based on the MSISDN
 */
public class OpCoCompositeAuthenticator implements ApplicationAuthenticator,
		LocalApplicationAuthenticator {

	private static final long serialVersionUID = -7533605620408092358L;
	private static Log log = LogFactory.getLog(OpCoCompositeAuthenticator.class);

	public boolean canHandle(HttpServletRequest request) {
		return true;
	}

	public AuthenticatorFlowStatus process(HttpServletRequest request,
			HttpServletResponse response, AuthenticationContext context)
			throws AuthenticationFailedException, LogoutFailedException {
		if (!canHandle(request)) {
			return AuthenticatorFlowStatus.INCOMPLETE;
		}

		List<IdentityProvider> idPs = null;

		try {
			idPs = IdentityProviderManager.getInstance().getIdPs(
					MultitenantUtils.getTenantDomain(request));
		} catch (IdentityApplicationManagementException e) {
			log.error("No registered IDPs found.", e);
			return AuthenticatorFlowStatus.INCOMPLETE;
		}

		SequenceConfig sequenceConfig = context.getSequenceConfig();
		Map<Integer, StepConfig> stepMap = sequenceConfig.getStepMap();

		StepConfig sc = stepMap.get(1);
		sc.setSubjectAttributeStep(false);
		sc.setSubjectIdentifierStep(false);

		int stepOrder = 2;
		StepConfig stepConfig = new StepConfig();
		stepConfig.setOrder(stepOrder);
		stepConfig.setSubjectAttributeStep(false);
		stepConfig.setSubjectIdentifierStep(false);

		String incomingUser = "";

		Set<Entry<String, AuthenticatedIdPData>> entrySet = context.getCurrentAuthenticatedIdPs()
				.entrySet();
		for (Entry<String, AuthenticatedIdPData> entry : entrySet) {
			if (null != entry
					&& entry.getKey().equals("LOCAL")
					&& entry.getValue().getAuthenticator().getName()
							.equals("GSMAMSISDNAuthenticator")) {
				incomingUser = context.getCurrentAuthenticatedIdPs().get(entry.getKey())
						.getUsername();
				break;
			}
		}

		IdentityProvider federatedIDP = getFederatedIDP(idPs, incomingUser);
		/*String encryptedMsidn = encryptData(incomingUser);
		
		FederatedAuthenticatorConfig oidcAuthnConfig = new FederatedAuthenticatorConfig();
        oidcAuthnConfig.setName("OpenIDConnectAuthenticator");
        oidcAuthnConfig.setDisplayName("openidconnect");
        oidcAuthnConfig.setEnabled(true);
        
        Property property = new Property();
        property.setName("commonAuthQueryParams");
        property.setValue("login_hint=" + encryptedMsidn);
        Property[] properties = new Property[6];
        properties[5] = property;
        oidcAuthnConfig.setProperties(properties);
        FederatedAuthenticatorConfig[] authenticators = federatedIDP.getFederatedAuthenticatorConfigs();
        if (authenticators == null || authenticators.length == 0) {
        	federatedIDP.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[] { oidcAuthnConfig });
        } else {
        	federatedIDP.setFederatedAuthenticatorConfigs(concatArrays(
                    new FederatedAuthenticatorConfig[] { oidcAuthnConfig }, authenticators));
        }*/
		
		AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();

		ApplicationAuthenticator appAuthenticator = FrameworkUtils
				.getAppAuthenticatorByName("OpenIDConnectAuthenticator");
		authenticatorConfig.setApplicationAuthenticator(appAuthenticator);
		authenticatorConfig.setName("OpenIDConnectAuthenticator");
		authenticatorConfig.getIdpNames().add(federatedIDP.getIdentityProviderName());
		authenticatorConfig.getIdps().put(federatedIDP.getIdentityProviderName(), federatedIDP);

		stepConfig.getAuthenticatorList().add(authenticatorConfig);
		stepMap.put(stepOrder, stepConfig);

		sequenceConfig.setStepMap(stepMap);
		context.setSequenceConfig(sequenceConfig);

		return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
	}
	
	private static FederatedAuthenticatorConfig[] concatArrays(FederatedAuthenticatorConfig[] o1,
            FederatedAuthenticatorConfig[] o2) {
        FederatedAuthenticatorConfig[] ret = new FederatedAuthenticatorConfig[o1.length + o2.length];

        System.arraycopy(o1, 0, ret, 0, o1.length);
        System.arraycopy(o2, 0, ret, o1.length, o2.length);

        return ret;
    }

	private IdentityProvider getFederatedIDP(List<IdentityProvider> idPs, String incomingUser)
			throws AuthenticationFailedException {
		IdentityProvider commonIDP = null;

		for (IdentityProvider idp : idPs) {
			if (null != idp) {
				// Check if this is the common IDP
				if ("commonidp".equals(idp.getIdentityProviderName())) {
					commonIDP = idp;
					continue;
				}
				String idpName = idp.getIdentityProviderName();
				String[] splName = idpName.split(":");
				if (incomingUser.startsWith(splName[1])) {
					return idp;
				}
			}
		}

		if (null != commonIDP) {
			return commonIDP;
		} else {
			throw new AuthenticationFailedException(
					"Authentication Failed since no common IDP is registered.");
		}
	}

	private String encryptData(String strData) throws AuthenticationFailedException {
		try {
			String data = new BASE64Encoder().encodeBuffer(strData.getBytes("UTF-8"));
			byte[] encryptedData = null;

			PublicKey publicKey = readPublicKeyFromFile(getPublicKeyFile());
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			encryptedData = cipher.doFinal(data.getBytes("UTF-8"));
			return new String(encryptedData);
		} catch (Exception e) {
			throw new AuthenticationFailedException(
					"Authentication Failed since encryption failed.");
		}
	}

	public PublicKey readPublicKeyFromFile(String fileName) throws AuthenticationFailedException {
		try {
			String publicK = readStringKey(fileName);
			byte[] keyBytes = new BASE64Decoder().decodeBuffer(publicK);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			return fact.generatePublic(keySpec);
		} catch (Exception e) {
			throw new AuthenticationFailedException(
					"Authentication Failed since reading public key from file failed.");
		}
	}

	public static String readStringKey(String fileName) {
		BufferedReader reader = null;
		StringBuffer fileData = null;
		try {

			fileData = new StringBuffer(2048);
			reader = new BufferedReader(new FileReader(fileName));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}

			reader.close();

		} catch (Exception e) {
		} finally {
			if (reader != null) {
				reader = null;
			}
		}
		return fileData.toString();
	}

	private String getPublicKeyFile() {
		return Constants.PUBLIC_KEYFILE;
	}

	public String getContextIdentifier(HttpServletRequest request) {
		return null;
	}

	public String getName() {
		return Constants.OPCOCA_AUTHENTICATOR_NAME;
	}

	public String getFriendlyName() {
		return Constants.OPCOCA_AUTHENTICATOR_FRIENDLY_NAME;
	}

	public String getClaimDialectURI() {
		return null;
	}

	public List<Property> getConfigurationProperties() {
		return new ArrayList<Property>();
	}

}