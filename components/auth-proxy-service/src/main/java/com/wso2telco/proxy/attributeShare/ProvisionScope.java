package com.wso2telco.proxy.attributeShare;

import java.util.Map;

/**
 * Created by aushani on 8/31/17.
 */
public class ProvisionScope extends  AbstractAttributeShare{


    @Override
    public Map<String, String> getAttributeShareDetails() {
        return super.getAttributeShareDetails();
    }

    @Override
    public void mandatoryFeildValidation() {

    }

    @Override
    public String checkSpType() {

        //set spType to context
        return null;
    }

    @Override
    public String getMSISDNAvailability() {
        return null;
    }

    @Override
    public void scopeNClaimMatching() {

    }

    @Override
    public void shaAlgortithemValidation() {

    }

    @Override
    public void attShareDetails() {

    }
}
