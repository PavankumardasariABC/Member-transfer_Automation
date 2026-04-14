package com.membertransfer.e2e.model.agreement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.With;

import static java.lang.String.format;

@With
@Builder
@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAgreementRequest {

    String paymentPlanId;
    String planValidationHash;
    String macAddress;
    Boolean activePresale;
    Boolean sendAgreementEmail;
    AgreementContactInfo agreementContactInfo;
    TodayBillingInfo todayBillingInfo;
    DraftBillingInfo draftBillingInfo;
    MarketingPreferences marketingPreferences;

    @With
    @Builder
    @Data
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AgreementContactInfo {
        String firstName;
        String lastName;
        String email;
        String gender;
        String homePhone;
        String cellPhone;
        String workPhone;
        String birthday;
        String driverLicense;
        String employer;
        AgreementAddressInfo agreementAddressInfo;
        EmergencyContact emergencyContact;

        @With
        @Builder
        @Data
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class AgreementAddressInfo {
            String addressLine1;
            String addressLine2;
            String postalCode;
            String city;
            String state;
            String country;
            String zipCode;
            String province;
        }

        @With
        @Builder
        @Data
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class EmergencyContact {
            String ecFirstName;
            String ecLastName;
            String ecPhone;
            String ecPhoneExtension;
        }
    }

    @With
    @Builder
    @Data
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TodayBillingInfo {
        Boolean isTodayBillingSameAsDraft;
        String todayCcCvvCode;
        String todayCcBillingZip;
        String todayCcFirstName;
        String todayCcLastName;
        String todayCcType;
        String todayCcAccountNumber;
        String todayCcExpMonth;
        String todayCcExpYear;
    }

    @With
    @Builder
    @Data
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DraftBillingInfo {
        DraftCreditCard draftCreditCard;
        DraftBankAccount draftBankAccount;

        @With
        @Builder
        @Data
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DraftCreditCard {
            String creditCardFirstName;
            String creditCardLastName;
            String creditCardType;
            String creditCardAccountNumber;
            String creditCardExpMonth;
            String creditCardExpYear;
        }

        @With
        @Builder
        @Data
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DraftBankAccount {
            String draftAccountFirstName;
            String draftAccountLastName;
            String draftAccountRoutingNumber;
            String draftAccountNumber;
            String draftAccountType;
            Boolean validateDraftAccountRoutingNumber;
        }
    }

    @With
    @Builder
    @Data
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MarketingPreferences {
        Boolean email;
        Boolean sms;
        Boolean directMail;
        Boolean pushNotification;
    }

    @JsonIgnore
    public String getFullMemberName() {
        return agreementContactInfo.getFirstName() + " " + agreementContactInfo.getLastName();
    }

    public String getTableDisplayName() {
        return format("%s, %s", agreementContactInfo.getLastName(), agreementContactInfo.getFirstName());
    }

    @JsonIgnore
    public String getCreditCardFirstName() {
        return draftBillingInfo.getDraftCreditCard().getCreditCardFirstName();
    }

    @JsonIgnore
    public String getCreditCardLastName() {
        return draftBillingInfo.getDraftCreditCard().getCreditCardLastName();
    }

    @JsonIgnore
    public String getCreditCardType() {
        return draftBillingInfo.getDraftCreditCard().getCreditCardType();
    }

    @JsonIgnore
    public String getLastFourCCNUmber() {
        String n = draftBillingInfo.getDraftCreditCard().creditCardAccountNumber;
        return n.substring(n.length() - 4);
    }

    @JsonIgnore
    public String getCreditCardExpMonth() {
        return draftBillingInfo.getDraftCreditCard().getCreditCardExpMonth();
    }

    @JsonIgnore
    public String getCreditCardExpYear() {
        return draftBillingInfo.getDraftCreditCard().getCreditCardExpYear();
    }
}
