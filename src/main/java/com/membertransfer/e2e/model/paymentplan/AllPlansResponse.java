package com.membertransfer.e2e.model.paymentplan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllPlansResponse {

    Status status;
    List<Plans> plans;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        String message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Plans {
        String planName;
        String planId;
    }

    @JsonIgnore
    public String getPlanIdByName(String planName) {
        if (plans == null) {
            throw new IllegalStateException("eAPI returned no plans list (plans is null). Check club/org and response body.");
        }
        return plans.stream()
                .filter(plan -> planName.equals(plan.getPlanName()))
                .map(Plans::getPlanId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No plan named '" + planName + "' in club plans."));
    }
}
