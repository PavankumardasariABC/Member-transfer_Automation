package com.membertransfer.e2e.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvironmentsDocument {

    private String description;
    private Map<String, EnvironmentProfile> profiles = new LinkedHashMap<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, EnvironmentProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<String, EnvironmentProfile> profiles) {
        this.profiles = profiles != null ? profiles : new LinkedHashMap<>();
    }
}
