package com.gsma.authendictorselector;

/**
 * Created by paraparan on 5/15/15.
 */
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
