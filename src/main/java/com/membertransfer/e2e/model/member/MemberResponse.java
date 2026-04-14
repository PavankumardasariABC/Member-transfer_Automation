package com.membertransfer.e2e.model.member;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.SPACE;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberResponse {

    Status status;
    Request request;
    List<Members> members;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        String message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {
        String clubNumber;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Members {
        String memberId;
        Personal personal;
        Agreement agreement;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Personal {
            String firstName;
            String lastName;
            String memberStatus;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Agreement {
            String agreementNumber;
            String currentQueue;
        }
    }

    public String getMemberStatus() {
        return members.get(0).getPersonal().getMemberStatus();
    }

    public String getCurrentQueue() {
        return members.get(0).getAgreement().getCurrentQueue();
    }

    public String getFirstName() {
        return members.get(0).getPersonal().getFirstName();
    }

    public String getLastName() {
        return members.get(0).getPersonal().getLastName();
    }

    public String getDisplayName() {
        return getFirstName() + SPACE + getLastName();
    }
}
