package com.wso2telco.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by isuru on 3/31/17.
 */
public class MePinResponse {


    private String status;

    @SerializedName("mepin_id")
    private String mePinId;

    @SerializedName("status_text")
    private String statusText;

    public String getMePinId() {
        return mePinId;
    }

    public void setMePinId(String mePinId) {
        this.mePinId = mePinId;
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
        return "MePinResponse{" +
                "status='" + status + '\'' +
                ", mePinId='" + mePinId + '\'' +
                ", statusText='" + statusText + '\'' +
                '}';
    }
}
