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

import com.google.gdata.util.common.util.Base64DecoderException;
import com.wso2telco.core.config.MIFEAuthentication;
import com.wso2telco.core.config.service.ConfigurationService;
import com.wso2telco.core.config.service.ConfigurationServiceImpl;
import com.wso2telco.gsma.ClaimManagementClient;
import com.wso2telco.gsma.LoginAdminServiceClient;
import com.wso2telco.gsma.authenticators.internal.CustomAuthenticatorServiceComponent;
import com.wso2telco.gsma.authenticators.model.LoginHintFormatDetails;
import com.wso2telco.gsma.authenticators.model.MSISDNHeader;
import com.wso2telco.gsma.authenticators.model.ScopeParam;
import com.wso2telco.gsma.authenticators.util.AuthenticationContextHelper;
import org.apache.axis2.AxisFault;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.oauth.cache.CacheKey;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

 
// TODO: Auto-generated Javadoc
/**
 * The Class LOACompositeAuthenticator.
 */
public class LOACompositeAuthenticator implements ApplicationAuthenticator,
		LocalApplicationAuthenticator {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 119680530347040691L;
	
	/** The selected loa. */
	private String selectedLOA = null;
    
    /** The log. */
    private static Log log = LogFactory.getLog(LOACompositeAuthenticator.class);
	private String isAdminUserName = null;
	private String isAdminPassword = null;

    private static HashMap<String, MSISDNDecryption> msisdnDecryptorsClassObjectMap = null;
    private static Map<String, List<MSISDNHeader>> operatorsMSISDNHeadersMap;

    /**
     * The Constant LOGIN_HINT_ENCRYPTED_PREFIX.
     */
    private static final String LOGIN_HINT_ENCRYPTED_PREFIX = "ENCR_MSISDN:";

    /**
     * The Constant LOGIN_HINT_NOENCRYPTED_PREFIX.
     */
    private static final String LOGIN_HINT_NOENCRYPTED_PREFIX = "MSISDN:";

    /**
     * The Constant LOGIN_HINT_SEPARATOR.
     */
    private static final String LOGIN_HINT_SEPARATOR = "|";

    static {
        try {
            //Load msisdn header properties.
            operatorsMSISDNHeadersMap = DBUtils.getOperatorsMSISDNHeaderProperties();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving operator MSISDN properties of operators.");
        } catch (AuthenticatorException e) {
            log.error("DataSource could not be found in mobile-connect.xml.");
        }
    }



    public LOACompositeAuthenticator() {
		//Use this credentials to login to IS.
		//TODO : get this username and password from a suitable configuration file.
		isAdminUserName = "admin";
		isAdminPassword = "admin";
	}

	/** The Configuration service */
	private static ConfigurationService configurationService = new ConfigurationServiceImpl();

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#canHandle(javax.servlet.http.HttpServletRequest)
	 */
	public boolean canHandle(HttpServletRequest request) {
		LinkedHashSet<?> acrs = this.getACRValues(request);
		
		if(acrs == null || acrs.size() == 0){
			return false;
		}else{
			selectedLOA = (String) acrs.iterator().next();
			return selectedLOA != null;
		}			
	}

    /**
     * Validate the if the passed login hint is in accepted format and also matched the header msisdn
     *
     * @param loginHint
     * @param loginHintAllowedFormatDetailsList list of approved/allowed login hint format list
     * @param plainTextMsisdnHeader
     * @return
     * @throws AuthenticationFailedException
     */
    private boolean validateFormatAndMatchLoginHintWithHeaderMsisdn(String loginHint,
                                                                    List<LoginHintFormatDetails>
                                                                            loginHintAllowedFormatDetailsList,
                                                                    String plainTextMsisdnHeader)
            throws AuthenticationFailedException {
        for (LoginHintFormatDetails loginHintFormatDetails : loginHintAllowedFormatDetailsList) {
            String msisdn = "";
            switch (loginHintFormatDetails.getFormatType()) {
                case PLAINTEXT:
                    if (log.isDebugEnabled()) {
                        log.debug("Plain text login hint: " + msisdn);
                    }
                    msisdn = loginHint;
                    break;
                case ENCRYPTED:
                    String decryptAlgorithm = loginHintFormatDetails.getDecryptAlgorithm();
                    if (loginHint.startsWith(LOGIN_HINT_ENCRYPTED_PREFIX)) {
                        loginHint = loginHint.replace(LOGIN_HINT_ENCRYPTED_PREFIX, "");
                        String decrypted = null;
                        try {
                            decrypted = decryptData(loginHint.replace(LOGIN_HINT_ENCRYPTED_PREFIX, ""),
                                                    decryptAlgorithm);
                        } catch (Exception e) {
                            log.error("Error while decrypting login hint - " + loginHint);
                        }
                        log.debug("Decrypted login hint: " + decrypted);
                        msisdn = decrypted.substring(0, decrypted.indexOf(LOGIN_HINT_SEPARATOR));
                        if (log.isDebugEnabled()) {
                            log.debug("MSISDN by encrypted login hint: " + msisdn);
                        }
                    }
                    break;
                case MSISDN:
                    if (loginHint.startsWith(LOGIN_HINT_NOENCRYPTED_PREFIX)) {
                        msisdn = loginHint.replace(LOGIN_HINT_NOENCRYPTED_PREFIX, "");
                        if (log.isDebugEnabled()) {
                            log.debug("MSISDN by login hint: " + msisdn);
                        }
                    }
                    break;
                default:
                    log.error("Invalid Login Hint format - " + loginHintFormatDetails.getFormatType());
                    break;
            }
            if (StringUtils.isNotEmpty(msisdn)) {
                if (validateMsisdnFormat(msisdn) && plainTextMsisdnHeader.equals(msisdn)) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean validateMsisdnFormat(String msisdn) {
        if (StringUtils.isNotEmpty(msisdn)) {
            String plaintextMsisdnRegex =
                    configurationService.getDataHolder().getMobileConnectConfig().getMsisdn().getValidationRegex();
            return msisdn.matches(plaintextMsisdnRegex);
        }
        return true;
    }


    /**
     * Decrypt data.
     *
     * @param data the data
     * @return the string
     * @throws Exception the exception
     */
    public String decryptData(String data, String encryptionAlgorithm) throws Exception {
        byte[] bytes = hexStringToByteArray(data);
        String filename = configurationService.getDataHolder().getMobileConnectConfig().getKeyfile();
        PrivateKey key = getPrivateKey(filename, encryptionAlgorithm);
        return decrypt(bytes, key, encryptionAlgorithm);
    }

    /**
     * Gets the private key.
     *
     * @param filename the filename
     * @return the private key
     * @throws Exception the exception
     */
    public static PrivateKey getPrivateKey(String filename, String encryptionAlgorithm) throws Exception {

        try {

            String publicK = readStringKey(filename);
            //byte[] keyBytes = new BASE64Decoder().decodeBuffer(publicK);
            byte[] keyBytes = Base64.decodeBase64(publicK.getBytes());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory fact = KeyFactory.getInstance("RSA");

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance(encryptionAlgorithm);
            return kf.generatePrivate(spec);

        } catch (Exception ex) {
            log.error("Exception reading private key:" + ex.getMessage());
            return null;
        }
    }

    /**
     * Read string key.
     *
     * @param fileName the file name
     * @return the string
     */
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

    /**
     * Hex string to byte array.
     *
     * @param s the s
     * @return the byte[]
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Decrypt.
     *
     * @param text the text
     * @param key  the key
     * @return the string
     */
    public static String decrypt(byte[] text, PrivateKey key, String encryptionAlgorithm) {
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance(encryptionAlgorithm);

            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] dectyptedText = cipher.doFinal(text);

            return new String(dectyptedText);

        } catch (Exception ex) {
            log.error("Exception encrypting data " + ex.getClass().getName() + ": " + ex.getMessage());
            return null;
        }
    }


    private String decryptMSISDN(String msisdn, String operatorName)
            throws ClassNotFoundException, NoSuchPaddingException, BadPaddingException, UnsupportedEncodingException,
                   IllegalBlockSizeException, Base64DecoderException, NoSuchAlgorithmException, InvalidKeyException,
                   IllegalAccessException, InstantiationException {
        List<MSISDNHeader> msisdnHeaderList = operatorsMSISDNHeadersMap.get(operatorName);

        for (int id = 0; id < msisdnHeaderList.size(); id++) {
            MSISDNHeader msisdnHeader = msisdnHeaderList.get(id);
            String msisdnHeaderName = msisdnHeader.getMsisdnHeaderName();
            if (StringUtils.isNotEmpty(msisdn)) {
                boolean isHeaderEncrypted = msisdnHeader.isHeaderEncrypted();
                if (isHeaderEncrypted) {
                    String encryptionKey = msisdnHeader.getHeaderEncryptionKey();
                    String encryptionMethod = msisdnHeader.getHeaderEncryptionMethod();
                    if (!msisdnDecryptorsClassObjectMap.containsKey(encryptionMethod)) {
                        Class encryptionClass = Class.forName(encryptionMethod);
                        MSISDNDecryption clsInstance = (MSISDNDecryption) encryptionClass.newInstance();
                        msisdnDecryptorsClassObjectMap.put(encryptionMethod, clsInstance);
                    }
                    msisdn = msisdnDecryptorsClassObjectMap.get(encryptionMethod).decryptMsisdn(msisdn, encryptionKey);
                }
                break;
            }
        }
        return msisdn;
    }

    /* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext)
	 */
	public AuthenticatorFlowStatus process(HttpServletRequest request,
                HttpServletResponse response, AuthenticationContext context)
			throws AuthenticationFailedException, LogoutFailedException {
		if (!canHandle(request)) {
			return AuthenticatorFlowStatus.INCOMPLETE;
		}
		boolean isLogin = false;
		boolean isAuthenticated;
		//Unregister Customer Token
		String msisdn = request.getParameter("msisdn");
        if (StringUtils.isEmpty(msisdn)) {
            msisdn = request.getHeader("msisdn");
        }
        String msisdnHeader = request.getParameter("msisdn_header");
        if (StringUtils.isEmpty(msisdnHeader)) {
            msisdnHeader = request.getHeader("msisdnHeader");
        }
		String flowType = getFlowType(msisdnHeader);
		String tokenId = request.getParameter("tokenid");

        //TODO: get all scope related params. This should be move to a initialization method later
        Map scopeDetail;
        try {
            scopeDetail = DBUtils.getScopeParams();
        } catch (AuthenticatorException e) {
            throw new AuthenticationFailedException(
                    "Error occurred while getting scope parameters from the database", e);
        }

        //set the scope specific params
        ScopeParam scopeParam = (ScopeParam) scopeDetail.get("params");
        context.setProperty("scopeParams", scopeParam);

        if (scopeParam != null) {
            //check login hit existance validation
            String loginHint = request.getParameter("login_hint");
            if (scopeParam.isLoginHintMandatory()) {
                if (StringUtils.isEmpty(loginHint)) {
                    throw new AuthenticationFailedException(
                            "login hint cannot be empty");
                }
            } else {
                String DecryptedMsisdnHeader = msisdnHeader;
                if (StringUtils.isNotEmpty(msisdnHeader) ) {
                    if (!validateMsisdnFormat(msisdnHeader)) {
                        throw new AuthenticationFailedException(
                                "Invalid msisdn format - " + msisdnHeader);
                    }
                    // check if decryption possible
                    log.debug("Set msisdn from header msisdn_header" + msisdnHeader);
                    DecryptedMsisdnHeader = msisdnHeader.trim();
                    try {
                        DecryptedMsisdnHeader = decryptMSISDN(msisdnHeader, request.getParameter("operator"));
                    } catch (Exception e) {
                        throw new AuthenticationFailedException(
                                "Decryption error while decrypting msisdn header - " + msisdnHeader, e);
                    }
                    //validate login hint format
                    if (!validateFormatAndMatchLoginHintWithHeaderMsisdn(loginHint, scopeParam.getLoginHintFormat(),
                                                                         DecryptedMsisdnHeader)) {
                        throw new AuthenticationFailedException(
                                "login hint is malformat or not matching with the header msisdn");
                    }
                }
            }
        }

        //Change authentication flow just after registration
		if (tokenId != null && msisdn != null) {
			try {
				AuthenticationData authenticationData = DBUtils.getAuthenticateData(tokenId);
				int status = authenticationData.getStatus();
				int tenantId = -1234;
				UserRealm userRealm = CustomAuthenticatorServiceComponent.getRealmService().getTenantUserRealm(
						tenantId);

				if (userRealm != null) {
					UserStoreManager userStoreManager = (UserStoreManager) userRealm.getUserStoreManager();

                   /* String userLocked = userStoreManager.getUserClaimValue(msisdn, "http://wso2
                   .org/claims/identity/accountLocked", "default");
                    if (userLocked != null && userLocked.equalsIgnoreCase("true")) {
                        log.info("Self Authenticator authentication failed ");
                        if (log.isDebugEnabled()) {
                            log.debug("User authentication failed due to locked account.");
                        }
                        throw new AuthenticationFailedException("Self Authentication Failed");
                    }*/

					isAuthenticated = userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(msisdn));
				} else {
					throw new AuthenticationFailedException(
							"Cannot find the user realm for the given tenant: " + tenantId);
				}
				if ((status == 1) & isAuthenticated) {
					isLogin = true;
				} else {
					isLogin = false;
				}
				DBUtils.deleteAuthenticateData(tokenId);
			} catch (Exception ex) {
				log.error("Self Authentication failed while trying to authenticate", ex);
			}

		} else {
			isLogin = false;
		}

		if (isLogin) {
			SequenceConfig sequenceConfig = context.getSequenceConfig();
			Map<Integer, StepConfig> stepMap = sequenceConfig.getStepMap();

			StepConfig sc = stepMap.get(1);
			sc.setSubjectAttributeStep(false);
			sc.setSubjectIdentifierStep(false);

			AuthenticatedUser user = new AuthenticatedUser();
			//context.setSubject(user);
			sc.setAuthenticatedUser(user);


			int stepOrder = 2;

			StepConfig stepConfig = new StepConfig();
			stepConfig.setOrder(stepOrder);
			stepConfig.setSubjectAttributeStep(true);
			stepConfig.setSubjectIdentifierStep(true);

			List<AuthenticatorConfig> authenticatorConfigs = stepConfig.getAuthenticatorList();
			if (authenticatorConfigs == null) {
				authenticatorConfigs = new ArrayList<AuthenticatorConfig>();
				stepConfig.setAuthenticatorList(authenticatorConfigs);
			}

			AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
			authenticatorConfig.setName("SelfAuthenticator");
			authenticatorConfig.setApplicationAuthenticator(
					FrameworkUtils.getAppAuthenticatorByName("SelfAuthenticator"));

			Map<String, String> parameterMap = new HashMap<String, String>();

			//parameterMap.put("isLastAuthenticat", "true");
			authenticatorConfig.setParameterMap(parameterMap);

			stepConfig.getAuthenticatorList().add(authenticatorConfig);
			stepMap.put(stepOrder, stepConfig);

			sequenceConfig.setStepMap(stepMap);
			context.setSequenceConfig(sequenceConfig);
			context.setProperty("msisdn", msisdn);
			AuthenticationContextHelper.setSubject(context, msisdn);
		} else {
			Map<String, MIFEAuthentication> authenticationMap = configurationService.getDataHolder().getAuthenticationLevelMap();
			MIFEAuthentication mifeAuthentication = authenticationMap.get(selectedLOA);

			SequenceConfig sequenceConfig = context.getSequenceConfig();
			Map<Integer, StepConfig> stepMap = sequenceConfig.getStepMap();

			StepConfig sc = stepMap.get(1);
			sc.setSubjectAttributeStep(false);
			sc.setSubjectIdentifierStep(false);

			int stepOrder = 2;

			while (true) {
				List<MIFEAuthentication.MIFEAbstractAuthenticator> authenticatorList =
						mifeAuthentication.getAuthenticatorList();
				String fallBack = mifeAuthentication.getLevelToFail();

				for (MIFEAuthentication.MIFEAbstractAuthenticator authenticator : authenticatorList) {
					String onFailAction = authenticator.getOnFailAction();
					String supportiveFlow = authenticator.getSupportFlow();
					if (supportiveFlow.equals("any") || supportiveFlow.equals(flowType)) {

						StepConfig stepConfig = new StepConfig();
						stepConfig.setOrder(stepOrder);
						if (stepOrder == 2) {
							stepConfig.setSubjectAttributeStep(true);
							stepConfig.setSubjectIdentifierStep(true);
						}

						List<AuthenticatorConfig> authenticatorConfigs = stepConfig.getAuthenticatorList();
						if (authenticatorConfigs == null) {
							authenticatorConfigs = new ArrayList<AuthenticatorConfig>();
							stepConfig.setAuthenticatorList(authenticatorConfigs);
						}

						String authenticatorName = authenticator.getAuthenticator();
						ApplicationAuthenticator applicationAuthenticator = FrameworkUtils.getAppAuthenticatorByName(
								authenticatorName);
						AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
						authenticatorConfig.setName(authenticatorName);
						authenticatorConfig.setApplicationAuthenticator(applicationAuthenticator);

						Map<String, String> parameterMap = new HashMap<String, String>();
						parameterMap.put("currentLOA", selectedLOA);
						parameterMap.put("fallBack", (null != fallBack) ? fallBack : "");
						parameterMap.put("onFail", (null != onFailAction) ? onFailAction : "");
//						parameterMap
//								.put("isLastAuthenticator",
//								     (authenticatorList.indexOf(authenticator) == authenticatorList.size() - 1) ?
//										     "true"
//										     : "false");
						authenticatorConfig.setParameterMap(parameterMap);

						stepConfig.getAuthenticatorList().add(authenticatorConfig);
						stepMap.put(stepOrder, stepConfig);

						stepOrder++;
					} else {
						if (StringUtils.isEmpty(fallBack)) {
							selectedLOA = fallBack;
							mifeAuthentication = authenticationMap.get(selectedLOA);
						}
						break;
					}
				}
				// increment LOA to fallBack level
				if (null == fallBack) {
					break;
				}
				selectedLOA = fallBack;
				mifeAuthentication = authenticationMap.get(selectedLOA);
			}
			sequenceConfig.setStepMap(stepMap);
			context.setSequenceConfig(sequenceConfig);
		}
		return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework
	 * .ApplicationAuthenticator#getContextIdentifier(javax.servlet.http.HttpServletRequest)
	 */
	public String getContextIdentifier(HttpServletRequest request) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getName()
	 */
	public String getName() {
		return Constants.LOACA_AUTHENTICATOR_NAME;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getFriendlyName()
	 */
	public String getFriendlyName() {
		return Constants.LOACA_AUTHENTICATOR_FRIENDLY_NAME;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getClaimDialectURI()
	 */
	public String getClaimDialectURI() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator#getConfigurationProperties()
	 */
	public List<Property> getConfigurationProperties() {
		return new ArrayList<Property>();
	}

	/**
	 * Gets the ACR values.
	 *
	 * @param request the request
	 * @return the ACR values
	 */
	private LinkedHashSet<?> getACRValues(HttpServletRequest request) {
		String sdk = request.getParameter(OAuthConstants.SESSION_DATA_KEY);
		CacheKey ck = new SessionDataCacheKey(sdk);
		SessionDataCacheKey sessionDataCacheKey=new SessionDataCacheKey(sdk);		
		SessionDataCacheEntry sdce = (SessionDataCacheEntry) SessionDataCache.getInstance()
				.getValueFromCache(sessionDataCacheKey);
		LinkedHashSet<?> acrValues = sdce.getoAuth2Parameters().getACRValues();
		return acrValues;
	}

	private String getFlowType(String msisdn) {
		if (!StringUtils.isEmpty(msisdn)) {
			return "onnet";
		}
		return "offnet";
	}

    private boolean isUserProfileUpdateRequired(HttpServletRequest request, String msisdnHeader, String selectedLOA) {
        boolean userProfileUpdateRequired = false;
        String requestURL = request.getRequestURL().toString();
        String requestURI = request.getRequestURI();
        String baseURL = requestURL.substring(0, requestURL.indexOf(requestURI));
        LoginAdminServiceClient lAdmin = null;
        try {
            lAdmin = new LoginAdminServiceClient(baseURL);
            String sessionCookie = lAdmin.authenticate(isAdminUserName, isAdminPassword);
            ClaimManagementClient claimManager = new ClaimManagementClient(baseURL, sessionCookie);
            if (msisdnHeader != null) {
                String registeredLOA = claimManager.getRegisteredLOA(msisdnHeader);
                if (Integer.parseInt(registeredLOA) < Integer.parseInt(selectedLOA)) {
                    userProfileUpdateRequired = true;
                }
            }
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
            e.printStackTrace();
        } catch (LoginAuthenticationExceptionException e) {
            e.printStackTrace();
        }
        return userProfileUpdateRequired;
    }

}
