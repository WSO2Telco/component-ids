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


// TODO: Auto-generated Javadoc

/**
 * The Class Constants.
 */
public final class Constants {

    /**
     * The Constant PIN_AUTHENTICATOR_NAME.
     */
    public static final String PIN_AUTHENTICATOR_NAME = "PinAuthenticator";

    /**
     * The Constant PIN_AUTHENTICATOR_FRIENDLY_NAME.
     */
    public static final String PIN_AUTHENTICATOR_FRIENDLY_NAME = "pin";

    /**
     * The Constant PIN_AUTHENTICATOR_STATUS.
     */
    public static final String PIN_AUTHENTICATOR_STATUS = "PinAuthenticatorStatus";

    /**
     * The Constant HE_AUTHENTICATOR_NAME.
     */
    public static final String HE_AUTHENTICATOR_NAME = "HeaderEnrichmentAuthenticator";

    /**
     * The Constant HE_AUTHENTICATOR_FRIENDLY_NAME.
     */
    public static final String HE_AUTHENTICATOR_FRIENDLY_NAME = "headerenrichment";

    /**
     * The Constant HE_AUTHENTICATOR_STATUS.
     */
    public static final String HE_AUTHENTICATOR_STATUS = "HeaderEnrichmentAuthenticatorStatus";

    /**
     * The Constant LOACA_AUTHENTICATOR_NAME.
     */
    public static final String LOACA_AUTHENTICATOR_NAME = "LOACompositeAuthenticator";

    /**
     * The Constant LOACA_AUTHENTICATOR_FRIENDLY_NAME.
     */
    public static final String LOACA_AUTHENTICATOR_FRIENDLY_NAME = "LOA";

    /**
     * The Constant OPCOCA_AUTHENTICATOR_NAME.
     */
    public static final String OPCOCA_AUTHENTICATOR_NAME = "OpCoCompositeAuthenticator";

    /**
     * The Constant OPCOCA_AUTHENTICATOR_FRIENDLY_NAME.
     */
    public static final String OPCOCA_AUTHENTICATOR_FRIENDLY_NAME = "OPCO";

    /**
     * The Constant MSISDN_AUTHENTICATOR_NAME.
     */
    public static final String MSISDN_AUTHENTICATOR_NAME = "MSISDNAuthenticator";

    /**
     * The Constant MSISDN_AUTHENTICATOR_FRIENDLY_NAME.
     */
    public static final String MSISDN_AUTHENTICATOR_FRIENDLY_NAME = "msisdn";

    /**
     * The Constant MSISDN_AUTHENTICATOR_STATUS.
     */
    public static final String MSISDN_AUTHENTICATOR_STATUS = "MSISDNAuthenticatorStatus";

    /**
     * The Constant GSMA_MSISDN_AUTHENTICATOR_NAME.
     */
    public static final String GSMA_MSISDN_AUTHENTICATOR_NAME = "GSMAMSISDNAuthenticator";

    /**
     * The Constant GSMA_MSISDN_AUTHENTICATOR_FRIENDLY_NAME.
     */
    public static final String GSMA_MSISDN_AUTHENTICATOR_FRIENDLY_NAME = "gsmamsisdn";

    /**
     * The Constant GSMA_MSISDN_AUTHENTICATOR_STATUS.
     */
    public static final String GSMA_MSISDN_AUTHENTICATOR_STATUS = "GSMAMSISDNAuthenticatorStatus";

    /**
     * The Constant USSD_AUTHENTICATOR_NAME.
     */
    public static final String USSD_AUTHENTICATOR_NAME = "USSDAuthenticator";

    /**
     * The Constant USSD_AUTHENTICATOR_FRIENDLY_NAME.
     */
    public static final String USSD_AUTHENTICATOR_FRIENDLY_NAME = "ussd";

    /**
     * The Constant USSD_AUTHENTICATOR_STATUS.
     */
    public static final String USSD_AUTHENTICATOR_STATUS = "USSDAuthenticatorStatus";

    /**
     * The Constant USSDPIN_AUTHENTICATOR_NAME.
     */
    public static final String USSDPIN_AUTHENTICATOR_NAME = "USSDPinAuthenticator";

    /**
     * The Constant USSDPIN_AUTHENTICATOR_FRIENDLY_NAME.
     */
    public static final String USSDPIN_AUTHENTICATOR_FRIENDLY_NAME = "ussdpin";

    /**
     * The Constant USSDPIN_AUTHENTICATOR_STATUS.
     */
    public static final String USSDPIN_AUTHENTICATOR_STATUS = "USSDPinAuthenticatorStatus";

    /**
     * The Constant SMS_AUTHENTICATOR_NAME.
     */
    public static final String SMS_AUTHENTICATOR_NAME = "SMSAuthenticator";

    /**
     * The Constant SMS_AUTHENTICATOR_FRIENDLY_NAME.
     */
    public static final String SMS_AUTHENTICATOR_FRIENDLY_NAME = "sms";

    /**
     * The Constant SMS_AUTHENTICATOR_STATUS.
     */
    public static final String SMS_AUTHENTICATOR_STATUS = "SMSAuthenticatorStatus";

    /**
     * The Constant LISTNER_WEBAPP_SMS_CONTEXT.
     */
    public static final String LISTNER_WEBAPP_SMS_CONTEXT = "/sign/sign.jag?sessionID=";

    /**
     * The Constant PRIVATE_KEYFILE.
     */
    public static final String PRIVATE_KEYFILE = "dialogprivate.key";

    /**
     * The Constant PUBLIC_KEYFILE.
     */
    public static final String PUBLIC_KEYFILE = "dialogpublic.key";

    /**
     * The Constant MSS_AUTHENTICATOR_NAME.
     */
    public static final String MSS_AUTHENTICATOR_NAME = "MSSAuthenticator";

    /**
     * The Constant MSS_AUTHENTICATOR_FRIENDLY_NAME.
     */
    public static final String MSS_AUTHENTICATOR_FRIENDLY_NAME = "mss";

    /**
     * The Constant MSS_AUTHENTICATOR_STATUS.
     */
    public static final String MSS_AUTHENTICATOR_STATUS = "MSSAuthenticatorStatus";

    /**
     * The Constant MSS_PIN_AUTHENTICATOR_NAME.
     */
    public static final String MSS_PIN_AUTHENTICATOR_NAME = "MSSPinAuthenticator";

    /**
     * The Constant MSS_PIN_AUTHENTICATOR_FRIENDLY_NAME.
     */
    public static final String MSS_PIN_AUTHENTICATOR_FRIENDLY_NAME = "mssPin";


    /**
     * The Constant SELF_AUTHENTICATOR_NAME.
     */
    public static final String SELF_AUTHENTICATOR_NAME = "SelfAuthenticator";

    /**
     * The Constant SELF_AUTHENTICATOR_FRIENDLY_NAME.
     */
    public static final String SELF_AUTHENTICATOR_FRIENDLY_NAME = "self";

    /**
     * The Constant to hold the user existence flag in authentication context
     */
    public static final String IS_USER_EXISTS = "isUserExists";

    /**
     * The Constant to hold the msisdn
     */
    public static final String MSISDN = "msisdn";

    /**
     * The Constant to hold the view for Consent Authenticator
     */
    public static final String VIEW_CONSENT = "/mcx-user-registration/auth_consent";


    public static final String VIEW_REGISTRATION_WAITING = "/mcx-user-registration/auth_registration";

    public static final String REDIRECT_URI = "redirectURI";

    public static final String COMMON_AUTH_HANDLED = "commonAuthHandled";

    public static final String IS_REGISTERING = "isRegistering";

    public static final String TOKEN = "token";

    public static final String OPERATOR = "operator";

    public static final String ACR = "acr_code";

    public static final String UPDATE_PROFILE = "updateProfile";

    public static final String DOMAIN = "domain";

    public static final String USERNAME = "userName";

    public static final String OPENID_URL = "openIdURL";

    public static final String PASSWORD = "pwd";

    public static final String OPENID = "openid";

    public static final String MTINIT = "mtinit";

    public static final String STATUS_PENDING = "pending";

    public static final String CLAIM = "claim";

    public static final String PARAMS = "params";

    public static final String SESSION_DATA_KEY = "sessionDataKey";
}
