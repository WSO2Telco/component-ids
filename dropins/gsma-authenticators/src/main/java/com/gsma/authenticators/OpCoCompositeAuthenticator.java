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
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
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

import com.axiata.dialog.mife.mnc.resolver.MNCQueryClient;
import com.axiata.dialog.mife.mnc.resolver.MobileNtException;
import java.util.logging.Level;
import java.util.logging.Logger;
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

        // Get respective federated idp
        IdentityProvider federatedIDP = getFederatedIDP(idPs, incomingUser);

        // Set login_hint and acr_values params to idp configuration
        ExternalIdPConfig idPConfigByName = ConfigurationFacade.getInstance().getIdPConfigByName(
                federatedIDP.getIdentityProviderName(), context.getTenantDomain());
        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = idPConfigByName
                .getIdentityProvider().getFederatedAuthenticatorConfigs();

        FederatedAuthenticatorConfig oidcConfig = null;
        for (FederatedAuthenticatorConfig config : federatedAuthenticatorConfigs) {
            if (config.getName().equals("OpenIDConnectAuthenticator")) {
                oidcConfig = config;
                break;
            }
        }

        Property commonAuthQueryParams = null;
        for (Property authProperty : oidcConfig.getProperties()) {
            if (authProperty.getName().equals("commonAuthQueryParams")) {
                commonAuthQueryParams = authProperty;
                break;
            }
        }

        String encryptedLoginHint = encryptLoginHint(incomingUser);
        encryptedLoginHint = encryptedLoginHint.replace("\n", "");
        // System.out.println("encryptedLoginHint_OPCO: " + encryptedLoginHint);
        // String loginHint = "&login_hint="
        // +
        // "pYPBEVtqmD8wCvyrsCHvDqJtLPyi2TuX7q1zJQYEsEXQr8jH+JN4z4/yPYJXYV4y172rOwHn16FZIAVbefMVJvIqF+3BdC8vur8n6wRac5oTAanf08m4hM/P6a2ixbKlCDikRnYTGG/cd0F98XVrq6euvGE/0NrJxOAt+kJGF4HXwJEUb4wltH87bh4RW8Mg9MRQVITEVxumYJyck/O8NVkw5SX8Mrulonw/1d8LQk3t6eCRnWfHH7ycWWRg9UtD0oN1ACR47N1F6j8pScejHP1pGMRHlSw0MztNfo7AZaVKOnAGafpi2MhhET7QVOTL1YDAYYZUBZY1IKjOdFtMIQ==";
        String loginHint = "login_hint=" + encryptedLoginHint;

        // Retrieve entry LOA
        String acrValues = "";
        String selectedLOA = (String) context.getProperty("entryLOA");
        if (null != selectedLOA) {
            acrValues = "&acr_values=" + selectedLOA;
        } else {
            throw new AuthenticationFailedException(
                    "Authentication Failed since no entry LOA is defined in the request");
        }

        // Set only login_hint and acr_values as common auth query params
        // Drop existing params
        commonAuthQueryParams.setValue(loginHint.concat(acrValues));

        // Set OpenIDConnectAuthenticator as default authenticator for the idp
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

    private IdentityProvider getFederatedIDP(List<IdentityProvider> idPs, String incomingUser)
            throws AuthenticationFailedException {

        IdentityProvider commonIDP = null;
        String providerBrand = null;

        for (IdentityProvider idp : idPs) {
            if (null != idp) {
                // Check if this is the common IDP
                if ("commonidp".equals(idp.getIdentityProviderName())) {
                    commonIDP = idp;
                    continue;
                }
                String idpName = idp.getIdentityProviderName();
                String[] splName = idpName.split(":");
                if (incomingUser.startsWith(splName[0])) {
                    //Get the provider brand
                    if ((providerBrand == null) || (providerBrand.isEmpty())) {
                        providerBrand = getProviderBrand(splName[1], incomingUser);
                        if (providerBrand == null) {
                            break;
                        }
                    }
                    if (providerBrand.equalsIgnoreCase(splName[2])) {
                        return idp;
                    } else {
                        continue;
                    }
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

    private String getProviderBrand(String mcc, String endUser) {
        try {
            MNCQueryClient mncQueryclient = new MNCQueryClient();
            return mncQueryclient.QueryNetwork(mcc, endUser);
        } catch (MobileNtException ex) {
            log.error("No IDPs brand found for User Mobile", ex);
        }

        return null;
    }

    private String encryptLoginHint(String loginHint) throws AuthenticationFailedException {
        String RANDOM_ADDON = "TheServingOperatorthencanrecognizetheyhavereceivedanencryptedMSISDNanddecryptthestringusingitsprivatekeywichisnotknowntotheOneAPI";
        String feedData = loginHint + "|" + RANDOM_ADDON;

        byte[] dataToEncrypt = feedData.getBytes();
        byte[] encryptedData = null;
        try {
            PublicKey pubKey = readPublicKeyFromFile(getPublicKeyFile());
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            encryptedData = cipher.doFinal(dataToEncrypt);

            return new BASE64Encoder().encode(encryptedData);

        } catch (Exception e) {
            throw new AuthenticationFailedException(
                    "Authentication Failed since encryption failed.");
        }
    }

    private PublicKey readPublicKeyFromFile(String fileName) throws AuthenticationFailedException {
        try {
            String publicK = readStringKey(fileName);
            byte[] keyBytes = new BASE64Decoder().decodeBuffer(publicK);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new AuthenticationFailedException(
                    "Authentication Failed since reading public key from file failed.");
        }
    }

    private String readStringKey(String fileName) {
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