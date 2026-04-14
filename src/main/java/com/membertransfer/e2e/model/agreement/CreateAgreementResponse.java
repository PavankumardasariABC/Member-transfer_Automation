package com.membertransfer.e2e.model.agreement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAgreementResponse {

    Status status;
    Result result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        String message;
        String messageCode;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        String memberId;
        String barcode;
        String agreementNumber;
    }

    @JsonIgnore
    public String getMemberId() {
        return result != null ? result.getMemberId() : null;
    }

    @JsonIgnore
    public String getBarcode() {
        return result != null ? result.getBarcode() : null;
    }

    @JsonIgnore
    public String getAgreementNumber() {
        return result != null ? result.getAgreementNumber() : null;
    }
}
