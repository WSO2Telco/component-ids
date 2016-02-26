/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 * 
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
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
package com.wso2telco.gsma.authenticators.mepin;

 
// TODO: Auto-generated Javadoc
/**
 * The Class Constants.
 */
public final class Constants {

    /** The Constant PIN_AUTHENTICATOR_NAME. */
    public static final String PIN_AUTHENTICATOR_NAME = "PinAuthenticator";
    
    /** The Constant PIN_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String PIN_AUTHENTICATOR_FRIENDLY_NAME = "pin";
    
    /** The Constant PIN_AUTHENTICATOR_STATUS. */
    public static final String PIN_AUTHENTICATOR_STATUS = "PinAuthenticatorStatus";

    /** The Constant HE_AUTHENTICATOR_NAME. */
    public static final String HE_AUTHENTICATOR_NAME = "HeaderEnrichmentAuthenticator";
    
    /** The Constant HE_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String HE_AUTHENTICATOR_FRIENDLY_NAME = "headerenrichment";
    
    /** The Constant HE_AUTHENTICATOR_STATUS. */
    public static final String HE_AUTHENTICATOR_STATUS = "HeaderEnrichmentAuthenticatorStatus";

    /** The Constant LOACA_AUTHENTICATOR_NAME. */
    public static final String LOACA_AUTHENTICATOR_NAME = "LOACompositeAuthenticator";
    
    /** The Constant LOACA_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String LOACA_AUTHENTICATOR_FRIENDLY_NAME = "LOA";

    /** The Constant OPCOCA_AUTHENTICATOR_NAME. */
    public static final String OPCOCA_AUTHENTICATOR_NAME = "OpCoCompositeAuthenticator";
    
    /** The Constant OPCOCA_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String OPCOCA_AUTHENTICATOR_FRIENDLY_NAME = "OPCO";

    /** The Constant MSISDN_AUTHENTICATOR_NAME. */
    public static final String MSISDN_AUTHENTICATOR_NAME = "MSISDNAuthenticator";
    
    /** The Constant MSISDN_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String MSISDN_AUTHENTICATOR_FRIENDLY_NAME = "msisdn";
    
    /** The Constant MSISDN_AUTHENTICATOR_STATUS. */
    public static final String MSISDN_AUTHENTICATOR_STATUS = "MSISDNAuthenticatorStatus";

    /** The Constant GSMA_MSISDN_AUTHENTICATOR_NAME. */
    public static final String GSMA_MSISDN_AUTHENTICATOR_NAME = "GSMAMSISDNAuthenticator";
    
    /** The Constant GSMA_MSISDN_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String GSMA_MSISDN_AUTHENTICATOR_FRIENDLY_NAME = "gsmamsisdn";
    
    /** The Constant GSMA_MSISDN_AUTHENTICATOR_STATUS. */
    public static final String GSMA_MSISDN_AUTHENTICATOR_STATUS = "GSMAMSISDNAuthenticatorStatus";

    /** The Constant USSD_AUTHENTICATOR_NAME. */
    public static final String USSD_AUTHENTICATOR_NAME = "USSDAuthenticator";
    
    /** The Constant USSD_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String USSD_AUTHENTICATOR_FRIENDLY_NAME = "ussd";
    
    /** The Constant USSD_AUTHENTICATOR_STATUS. */
    public static final String USSD_AUTHENTICATOR_STATUS = "USSDAuthenticatorStatus";

    /** The Constant USSDPIN_AUTHENTICATOR_NAME. */
    public static final String USSDPIN_AUTHENTICATOR_NAME = "USSDPinAuthenticator";
    
    /** The Constant USSDPIN_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String USSDPIN_AUTHENTICATOR_FRIENDLY_NAME = "ussdpin";
    
    /** The Constant USSDPIN_AUTHENTICATOR_STATUS. */
    public static final String USSDPIN_AUTHENTICATOR_STATUS = "USSDPinAuthenticatorStatus";

    /** The Constant ANDROID_AUTHENTICATOR_OK_NAME. */
    public static final String ANDROID_AUTHENTICATOR_OK_NAME = "AndroidAuthenticatorOK";
    
    /** The Constant ANDROID_AUTHENTICATOR_PIN_NAME. */
    public static final String ANDROID_AUTHENTICATOR_PIN_NAME = "AndroidAuthenticatorPIN";
    
    /** The Constant ANDROID_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String ANDROID_AUTHENTICATOR_FRIENDLY_NAME = "android";
    
    /** The Constant ANDROID_AUTHENTICATOR_STATUS. */
    public static final String ANDROID_AUTHENTICATOR_STATUS = "AndroidAuthenticatorStatus";


    /** The Constant SMS_AUTHENTICATOR_NAME. */
    public static final String SMS_AUTHENTICATOR_NAME = "SMSAuthenticator";
    
    /** The Constant SMS_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String SMS_AUTHENTICATOR_FRIENDLY_NAME = "sms";
    
    /** The Constant SMS_AUTHENTICATOR_STATUS. */
    public static final String SMS_AUTHENTICATOR_STATUS = "SMSAuthenticatorStatus";

    /** The Constant LISTNER_WEBAPP_SMS_CONTEXT. */
    public static final String LISTNER_WEBAPP_SMS_CONTEXT = "/sign/sign.jag?sessionID=";
    
    /** The Constant LISTNER_WEBAPP_USSD_CONTEXT. */
    public static final String LISTNER_WEBAPP_USSD_CONTEXT = "/ideamart/route/?x_url=http%3A%2F%2F54.205.155.247%3A9763%2FMediationTest%2Ftnspoints%2Fendpoint%2Fussd%2Fpin";
    
    /** The Constant LISTNER_WEBAPP_USSDPIN_CONTEXT. */
    //    public static final String LISTNER_WEBAPP_USSD_CONTEXT = "/MediationTest/tnspoints/endpoint/ussd/receive";
    public static final String LISTNER_WEBAPP_USSDPIN_CONTEXT = "/ideamart/route/?x_url=http%3A%2F%2F54.205.155.247%3A9763%2FMediationTest%2Ftnspoints%2Fendpoint%2Fussd%2Fpin";
    
    /** The Constant LISTNER_WEBAPP_MEPIN_CONTEXT. */
    //    public static final String LISTNER_WEBAPP_USSDPIN_CONTEXT = "/MediationTest/tnspoints/endpoint/ussd/pin";
    public static final String LISTNER_WEBAPP_MEPIN_CONTEXT = "/sessionupdater/tnspoints/endpoint/mepin/response";

    /** The Constant PRIVATE_KEYFILE. */
    public static final String PRIVATE_KEYFILE = "dialogprivate.key";
    
    /** The Constant PUBLIC_KEYFILE. */
    public static final String PUBLIC_KEYFILE = "dialogpublic.key";

    /** The Constant MSS_AUTHENTICATOR_NAME. */
    public static final String MSS_AUTHENTICATOR_NAME = "MSSAuthenticator";
    
    /** The Constant MSS_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String MSS_AUTHENTICATOR_FRIENDLY_NAME = "mss";

    /** The Constant MSS_PIN_AUTHENTICATOR_NAME. */
    public static final String MSS_PIN_AUTHENTICATOR_NAME = "MSSPinAuthenticator";
    
    /** The Constant MSS_PIN_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String MSS_PIN_AUTHENTICATOR_FRIENDLY_NAME = "mssPin";

    /** The Constant MEPIN_AUTHENTICATOR_PIN_NAME. */
    public static final String MEPIN_AUTHENTICATOR_PIN_NAME = "MePinAuthenticatorPIN";
    
    /** The Constant MEPIN_AUTHENTICATOR_TAP_NAME. */
    public static final String MEPIN_AUTHENTICATOR_TAP_NAME = "MePinAuthenticatorTAP";
    
    /** The Constant MEPIN_AUTHENTICATOR_SWIPE_NAME. */
    public static final String MEPIN_AUTHENTICATOR_SWIPE_NAME = "MePinAuthenticatorSWIPE";
    
    /** The Constant MEPIN_AUTHENTICATOR_FP_NAME. */
    public static final String MEPIN_AUTHENTICATOR_FP_NAME = "MePinAuthenticatorFP";
    
    /** The Constant MEPIN_AUTHENTICATOR_FRIENDLY_NAME. */
    public static final String MEPIN_AUTHENTICATOR_FRIENDLY_NAME = "mepin";
    
    /** The Constant MEPIN_AUTHENTICATOR_STATUS. */
    public static final String MEPIN_AUTHENTICATOR_STATUS = "MePinAuthenticatorStatus";
}
