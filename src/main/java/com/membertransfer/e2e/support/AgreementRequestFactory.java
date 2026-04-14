package com.membertransfer.e2e.support;

import com.membertransfer.e2e.eapi.EApiAgreementClient;
import com.membertransfer.e2e.model.agreement.CreateAgreementRequest;

public final class AgreementRequestFactory {

    private AgreementRequestFactory() {
    }

    /**
     * Builds a create-agreement payload using live plan id + validation hash from eAPI for the club.
     */
    public static CreateAgreementRequest installmentLikeRequest(EApiAgreementClient client, String clubNumber, String planName) {
        var plans = client.getAllPlans(clubNumber);
        String planId = plans.getPlanIdByName(planName);
        String validation = client.getPaymentPlanInfo(clubNumber, planId).getPaymentPlan().getPlanValidation();

        var contact = AgreementTestData.contactInfo();
        var billing = AgreementTestData.draftVisaBilling(contact.getFirstName(), contact.getLastName());

        return CreateAgreementRequest.builder()
                .paymentPlanId(planId)
                .planValidationHash(validation)
                .activePresale(false)
                .sendAgreementEmail(false)
                .agreementContactInfo(contact)
                .draftBillingInfo(billing)
                .build();
    }
}
