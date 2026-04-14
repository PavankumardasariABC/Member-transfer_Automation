package com.membertransfer.e2e.model.wallet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * Copied from dt2rcm_automation {@code com.abcfinancial.api.apps.eapi.wallet.PaymentMethodsResponse}.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentMethodsResponse {

    Status status;
    MemberPaymentMethodsInformation memberPaymentMethodsInformation;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        String message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MemberPaymentMethodsInformation {
        List<PaymentMethods> paymentMethods;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PaymentMethods {
            String accountInfoId;
            String paymentMethodType;
            CreditCardInfo creditCardInfo;
            ClubAccountBankAccount clubAccountBankAccount;
            List<String> paymentSlots;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class CreditCardInfo {
                String creditCardFirstName;
                String creditCardLastName;
                String creditCardType;
                String creditCardAccountNumberLastFour;
                String creditCardExpMonth;
                String creditCardExpYear;
                String billingZip;
                String billingCountry;
            }

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ClubAccountBankAccount {
                String draftAccountFirstName;
                String draftAccountLastName;
                String draftAccountRoutingNumber;
                String draftAccountNumberLastFour;
                String draftAccountType;
            }
        }
    }

    @JsonIgnore
    public List<String> getPaymentSlots(int index) {
        return memberPaymentMethodsInformation.getPaymentMethods().get(index).getPaymentSlots();
    }

    @JsonIgnore
    public String getPaymentMethodType(int index) {
        return memberPaymentMethodsInformation.getPaymentMethods().get(index).getPaymentMethodType();
    }

    @JsonIgnore
    public String getCreditCardFirstName(int index) {
        return memberPaymentMethodsInformation.getPaymentMethods().get(index).getCreditCardInfo().getCreditCardFirstName();
    }

    @JsonIgnore
    public String getCreditCardLastName(int index) {
        return memberPaymentMethodsInformation.getPaymentMethods().get(index).getCreditCardInfo().getCreditCardLastName();
    }

    @JsonIgnore
    public String getCardType(int index) {
        return memberPaymentMethodsInformation.getPaymentMethods().get(index).getCreditCardInfo().getCreditCardType();
    }

    @JsonIgnore
    public String getCreditCardAccountNumberLastFour(int index) {
        return memberPaymentMethodsInformation.getPaymentMethods().get(index).getCreditCardInfo().getCreditCardAccountNumberLastFour();
    }

    @JsonIgnore
    public String getCreditCardExpMonth(int index) {
        return memberPaymentMethodsInformation.getPaymentMethods().get(index).getCreditCardInfo().getCreditCardExpMonth();
    }

    @JsonIgnore
    public String getCreditCardExpYear(int index) {
        return memberPaymentMethodsInformation.getPaymentMethods().get(index).getCreditCardInfo().getCreditCardExpYear();
    }
}
