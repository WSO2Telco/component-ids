package com.gsma.authenticators.model;

/**
 * Created by Malinda on 2/12/2015.
 */
public class AuthenticateUserRequest {
    String address;
    String requestedLOA;
    ServiceProvider serviceProvider;
    String clientCorrelator;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRequestedLOA() {
        return requestedLOA;
    }

    public void setRequestedLOA(String requestedLOA) {
        this.requestedLOA = requestedLOA;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public String getClientCorrelator() {
        return clientCorrelator;
    }

    public void setClientCorrelator(String clientCorrelator) {
        this.clientCorrelator = clientCorrelator;
    }
}
