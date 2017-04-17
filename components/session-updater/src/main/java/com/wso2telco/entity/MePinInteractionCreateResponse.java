package com.wso2telco.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by isuru on 4/1/17.
 */
public class MePinInteractionCreateResponse {

    @SerializedName("interaction_id")
    private String interactionId;

    private String status;

    @SerializedName("status_text")
    private String statusText;

    public String getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(String interactionId) {
        this.interactionId = interactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    @Override
    public String toString() {
        return "MePinInteractionCreateResponse{" +
                "interactionId='" + interactionId + '\'' +
                ", status='" + status + '\'' +
                ", statusText='" + statusText + '\'' +
                '}';
    }
}
