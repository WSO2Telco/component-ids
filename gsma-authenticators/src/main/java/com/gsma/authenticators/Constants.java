package com.gsma.authenticators;

/**
 * Constants used by the Authenticators
 *
 */
public final class Constants {
	
	public static final String PIN_AUTHENTICATOR_NAME = "PinAuthenticator";
	public static final String PIN_AUTHENTICATOR_FRIENDLY_NAME = "pin";
	public static final String PIN_AUTHENTICATOR_STATUS = "PinAuthenticatorStatus";

    public static final String HE_AUTHENTICATOR_NAME = "HeaderEnrichmentAuthenticator";
    public static final String HE_AUTHENTICATOR_FRIENDLY_NAME = "headerenrichment";
    public static final String HE_AUTHENTICATOR_STATUS = "HeaderEnrichmentAuthenticatorStatus";

    public static final String LOACA_AUTHENTICATOR_NAME = "LOACompositeAuthenticator";
    public static final String LOACA_AUTHENTICATOR_FRIENDLY_NAME = "LOA";
    
    public static final String OPCOCA_AUTHENTICATOR_NAME = "OpCoCompositeAuthenticator";
    public static final String OPCOCA_AUTHENTICATOR_FRIENDLY_NAME = "OPCO";

    public static final String MSISDN_AUTHENTICATOR_NAME = "MSISDNAuthenticator";
	public static final String MSISDN_AUTHENTICATOR_FRIENDLY_NAME = "msisdn";
	public static final String MSISDN_AUTHENTICATOR_STATUS = "MSISDNAuthenticatorStatus";

    public static final String GSMA_MSISDN_AUTHENTICATOR_NAME = "GSMAMSISDNAuthenticator";
	public static final String GSMA_MSISDN_AUTHENTICATOR_FRIENDLY_NAME = "gsmamsisdn";
	public static final String GSMA_MSISDN_AUTHENTICATOR_STATUS = "GSMAMSISDNAuthenticatorStatus";

    public static final String USSD_AUTHENTICATOR_NAME = "USSDAuthenticator";
	public static final String USSD_AUTHENTICATOR_FRIENDLY_NAME = "ussd";
	public static final String USSD_AUTHENTICATOR_STATUS = "USSDAuthenticatorStatus";
        
    public static final String USSDPIN_AUTHENTICATOR_NAME = "USSDPinAuthenticator";
	public static final String USSDPIN_AUTHENTICATOR_FRIENDLY_NAME = "ussdpin";
	public static final String USSDPIN_AUTHENTICATOR_STATUS = "USSDPinAuthenticatorStatus";

    public static final String SMS_AUTHENTICATOR_NAME = "SMSAuthenticator";
	public static final String SMS_AUTHENTICATOR_FRIENDLY_NAME = "sms";
	public static final String SMS_AUTHENTICATOR_STATUS = "SMSAuthenticatorStatus";

   // public static final String LISTNER_WEBAPP_SMS_CONTEXT = "/MediationTest/tnspoints/endpoint/sms/response?sessionID=";
	public static final String LISTNER_WEBAPP_SMS_CONTEXT = "/sign/sign.jag?sessionID=";
//    public static final String LISTNER_WEBAPP_USSD_CONTEXT = "/MediationTest/tnspoints/endpoint/ussd/receive";
//    public static final String LISTNER_WEBAPP_USSDPIN_CONTEXT = "/MediationTest/tnspoints/endpoint/ussd/pin";
    public static final String LISTNER_WEBAPP_MEPIN_CONTEXT = "/SessionUpdater/tnspoints/endpoint/mepin/response";
    
    public static final String PRIVATE_KEYFILE = "dialogprivate.key";
    public static final String PUBLIC_KEYFILE = "dialogpublic.key";

    public static final String MSS_AUTHENTICATOR_NAME = "MSSAuthenticator";
    public static final String MSS_AUTHENTICATOR_FRIENDLY_NAME = "mss";
    public static final String MSS_AUTHENTICATOR_STATUS = "MSSAuthenticatorStatus";

    public static final String MSS_PIN_AUTHENTICATOR_NAME = "MSSPinAuthenticator";
    public static final String MSS_PIN_AUTHENTICATOR_FRIENDLY_NAME = "mssPin";

    public static final String MEPIN_AUTHENTICATOR_PIN_NAME = "MePinAuthenticatorPIN";
    public static final String MEPIN_AUTHENTICATOR_TAP_NAME = "MePinAuthenticatorTAP";
    public static final String MEPIN_AUTHENTICATOR_SWIPE_NAME = "MePinAuthenticatorSWIPE";
    public static final String MEPIN_AUTHENTICATOR_FP_NAME = "MePinAuthenticatorFP";
    public static final String MEPIN_AUTHENTICATOR_FRIENDLY_NAME = "mepin";
    public static final String MEPIN_AUTHENTICATOR_STATUS = "MePinAuthenticatorStatus";

    public static final String SELF_AUTHENTICATOR_NAME = "SelfAuthenticator";
    public static final String SELF_AUTHENTICATOR_FRIENDLY_NAME = "self";

}
