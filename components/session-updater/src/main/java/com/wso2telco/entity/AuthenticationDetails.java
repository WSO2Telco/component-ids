package com.wso2telco.entity;

/**
 * Created by isuru on 1/23/17.
 */
public class AuthenticationDetails {

    private String sessionId;

    private String challengeQuestion1;

    private String challengeQuestion2;

    private String challengeAnswer1;

    private String challengeAnswer2;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getChallengeQuestion1() {
        return challengeQuestion1;
    }

    public void setChallengeQuestion1(String challengeQuestion1) {
        this.challengeQuestion1 = challengeQuestion1;
    }

    public String getChallengeQuestion2() {
        return challengeQuestion2;
    }

    public void setChallengeQuestion2(String challengeQuestion2) {
        this.challengeQuestion2 = challengeQuestion2;
    }

    public String getChallengeAnswer1() {
        return challengeAnswer1;
    }

    public void setChallengeAnswer1(String challengeAnswer1) {
        this.challengeAnswer1 = challengeAnswer1;
    }

    public String getChallengeAnswer2() {
        return challengeAnswer2;
    }

    public void setChallengeAnswer2(String challengeAnswer2) {
        this.challengeAnswer2 = challengeAnswer2;
    }
}
