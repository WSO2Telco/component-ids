package com.gsma.authendictorselector;

 
public class AuthenticatorSelectorFactory {
    public AuthenticatorSelector Authenticator(String authenticator){
        if(authenticator == null){
            return null;
        }

        if (authenticator.equalsIgnoreCase("dialog")){
            return new DialogAuthenticatorSelectorImpl();
        }
        return null;
    }
}
