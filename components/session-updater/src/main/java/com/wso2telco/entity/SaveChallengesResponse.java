package com.wso2telco.entity;

/**
 * Created by isuru on 1/23/17.
 */
public class SaveChallengesResponse {

    private String sessionId;

    private String status;

    public SaveChallengesResponse(String sessionId, String status) {
        this.sessionId = sessionId;
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
