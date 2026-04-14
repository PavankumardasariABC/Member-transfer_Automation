package com.membertransfer.e2e.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * One entry from {@code e2e/environments.json} under {@code profiles}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvironmentProfile {

    private String eapiBaseUrl;
    private String rcmBillingUrl;
    private String commerceUiUrl;
    private String description;

    public String getEapiBaseUrl() {
        return eapiBaseUrl;
    }

    public void setEapiBaseUrl(String eapiBaseUrl) {
        this.eapiBaseUrl = eapiBaseUrl;
    }

    public String getRcmBillingUrl() {
        return rcmBillingUrl;
    }

    public void setRcmBillingUrl(String rcmBillingUrl) {
        this.rcmBillingUrl = rcmBillingUrl;
    }

    public String getCommerceUiUrl() {
        return commerceUiUrl;
    }

    public void setCommerceUiUrl(String commerceUiUrl) {
        this.commerceUiUrl = commerceUiUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
