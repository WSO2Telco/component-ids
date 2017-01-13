package com.wso2telco.entity;

/**
 * Created by isuru on 1/9/17.
 */
public class ValidationResponse {

    private String status;

    private String sessionId;

    private boolean isAnswer1Valid;

    private boolean isAnswer2Valid;

    public ValidationResponse(String status, String sessionId) {
        this.status = status;
        this.sessionId = sessionId;
    }

    public ValidationResponse(String status, String sessionId, boolean isAnswer1Valid, boolean isAnswer2Valid) {
        this.status = status;
        this.sessionId = sessionId;
        this.isAnswer1Valid = isAnswer1Valid;
        this.isAnswer2Valid = isAnswer2Valid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isAnswer1Valid() {
        return isAnswer1Valid;
    }

    public void setAnswer1Valid(boolean answer1Valid) {
        isAnswer1Valid = answer1Valid;
    }

    public boolean isAnswer2Valid() {
        return isAnswer2Valid;
    }

    public void setAnswer2Valid(boolean answer2Valid) {
        isAnswer2Valid = answer2Valid;
    }
}
