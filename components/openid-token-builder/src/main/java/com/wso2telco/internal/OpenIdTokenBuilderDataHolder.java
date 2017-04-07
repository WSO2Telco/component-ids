package com.wso2telco.internal;

import com.wso2telco.core.pcrservice.PCRGeneratable;

/**
 * Created by yasith on 9/29/16.
 */
public class OpenIdTokenBuilderDataHolder {

    private static OpenIdTokenBuilderDataHolder openIdTokenBuilderDataHolder = new OpenIdTokenBuilderDataHolder();

    PCRGeneratable pcrGeneratable;

    private OpenIdTokenBuilderDataHolder() {

    }

    public static OpenIdTokenBuilderDataHolder getInstance() {
        return openIdTokenBuilderDataHolder;
    }

    public PCRGeneratable getPcrGeneratable() {
        return pcrGeneratable;
    }

    public void setPcrGeneratable(PCRGeneratable pcrGeneratable) {
        this.pcrGeneratable = pcrGeneratable;
    }

}
