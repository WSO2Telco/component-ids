package com.wso2telco.proxy.attributeShare;

import com.wso2telco.proxy.util.AuthProxyEnum;

public class ScopeFactory {

    static ProvisionScope provisionScope;
    static VerificationScope variVerificationScope;
    private ScopeFactory(){}

    public static AttrubteSharable getAttribAttrubteSharable(String scopeType){

        AbstractAttributeShare attributeShareScope = null;

        AuthProxyEnum.SCOPETYPE scopetype = AuthProxyEnum.SCOPETYPE.valueOf(scopeType);

        if (scopetype != null) {
            switch (scopetype) {
                case ATT_VERIFICATION:
                    if (variVerificationScope == null) {
                        variVerificationScope = new VerificationScope();
                    }
                    attributeShareScope = variVerificationScope;
                    break;
                case ATT_SHARE:
                    if (provisionScope == null) {
                        provisionScope = new ProvisionScope();
                    }
                    attributeShareScope = provisionScope;
                    break;
                default:
                    break;
            }

        }

        return attributeShareScope;

    }
}
