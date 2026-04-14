package com.membertransfer.e2e.model.paymentplan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentPlanInfoResponse {

    Status status;
    PaymentPlan paymentPlan;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        String message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentPlan {
        String planName;
        String planValidation;
        String agreementTerm;
        String scheduleFrequency;
    }
}
