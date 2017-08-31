package com.wso2telco.proxy.attributeShare;

import java.util.Map;

/**
 * Created by aushani on 8/31/17.
 */
public abstract class AbstractAttributeShare implements AttrubteSharable {

    public Map<String, String> getAttributeShareDetails() {
        return null;
    }

    public abstract void mandatoryFeildValidation();

    public abstract String checkSpType ();

    public abstract String getMSISDNAvailability();//login_hint feild validations;

    public abstract void scopeNClaimMatching();

    public abstract void shaAlgortithemValidation();

}
