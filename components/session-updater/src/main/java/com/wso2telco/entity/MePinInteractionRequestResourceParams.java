package com.wso2telco.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by isuru on 4/1/17.
 */
public class MePinInteractionRequestResourceParams {

    @SerializedName("enrollment_status")
    private String enrollmentStatus;

    @SerializedName("my_services_url")
    private String servicesUrl;

    @SerializedName("service_provider_logo")
    private String serviceProviderLogo;

    @SerializedName("help_url")
    private String helpUrl;

    @SerializedName("terms_of_service_url")
    private String termsOfServiceUrl;

    @SerializedName("privacy_policy_url")
    private String privacyPolicyUrl;

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public String getServicesUrl() {
        return servicesUrl;
    }

    public void setServicesUrl(String servicesUrl) {
        this.servicesUrl = servicesUrl;
    }

    public String getServiceProviderLogo() {
        return serviceProviderLogo;
    }

    public void setServiceProviderLogo(String serviceProviderLogo) {
        this.serviceProviderLogo = serviceProviderLogo;
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public void setHelpUrl(String helpUrl) {
        this.helpUrl = helpUrl;
    }

    public String getTermsOfServiceUrl() {
        return termsOfServiceUrl;
    }

    public void setTermsOfServiceUrl(String termsOfServiceUrl) {
        this.termsOfServiceUrl = termsOfServiceUrl;
    }

    public String getPrivacyPolicyUrl() {
        return privacyPolicyUrl;
    }

    public void setPrivacyPolicyUrl(String privacyPolicyUrl) {
        this.privacyPolicyUrl = privacyPolicyUrl;
    }
}
