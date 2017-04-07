package com.wso2telco.openid.extension.scope;

import com.wso2telco.openid.extension.dto.ScopeDTO;

import java.util.Map;

public class OpenIDScope extends Scope {

    public OpenIDScope(ScopeDTO scopeDTO) {
        super(scopeDTO);
    }

    @Override
    public ScopeValidationResult validate(String callBackURL, String state) {
        return super.validate(callBackURL, state);
    }

    @Override
    public String createRedirectURL(String baseURL, Map<String, String> queryParameters) {
        return super.createRedirectURL(baseURL, queryParameters);
    }

    @Override
    public void executeAuthenticationFlow() {
        super.executeAuthenticationFlow();
    }

    @Override
    public ScopeDTO getScopeDTO() {
        return super.getScopeDTO();
    }

    @Override
    public void setScopeDTO(ScopeDTO scopeDTO) {
        super.setScopeDTO(scopeDTO);
    }
}
