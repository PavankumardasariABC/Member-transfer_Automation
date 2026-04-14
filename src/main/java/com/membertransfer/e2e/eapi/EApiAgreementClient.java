package com.membertransfer.e2e.eapi;

import com.membertransfer.e2e.config.EApiEnvironment;
import com.membertransfer.e2e.http.RelaxedApi;
import com.membertransfer.e2e.model.agreement.CreateAgreementRequest;
import com.membertransfer.e2e.model.agreement.CreateAgreementResponse;
import com.membertransfer.e2e.model.member.MemberResponse;
import com.membertransfer.e2e.model.paymentplan.AllPlansResponse;
import com.membertransfer.e2e.model.paymentplan.PaymentPlanInfoResponse;
import com.membertransfer.e2e.model.wallet.PaymentMethodsResponse;

import java.util.Map;

import static io.restassured.http.ContentType.JSON;

/**
 * Minimal eAPI surface for agreement E2E: plans, plan validation, create agreement, member read.
 */
public final class EApiAgreementClient {

    private static final String ALL_PLANS_PATH = "rest/{clubNumber}/clubs/plans";
    private static final String PAYMENT_PLAN_PATH = "rest/{clubNumber}/clubs/plans/{planId}";
    private static final String CREATE_AGREEMENT_PATH = "rest/{clubNumber}/members/agreements";
    private static final String MEMBER_INFO_PATH = "rest/{clubNumber}/members/{memberId}";
    private static final String MEMBER_PAYMENT_METHODS_PATH = "rest/{clubNumber}/members/{memberId}/wallets/paymentmethods";

    private final String baseUrl;
    private final Map<String, String> headers;

    public EApiAgreementClient() {
        this(EApiEnvironment.baseUrl(), EApiEnvironment.authHeaders());
    }

    public EApiAgreementClient(String baseUrl, Map<String, String> headers) {
        this.baseUrl = baseUrl;
        this.headers = headers;
    }

    public AllPlansResponse getAllPlans(String clubNumber) {
        return RelaxedApi.with()
                .baseUri(baseUrl)
                .basePath(ALL_PLANS_PATH)
                .pathParam("clubNumber", EApiEnvironment.formatClubNumber(clubNumber))
                .headers(headers)
                .accept(JSON)
                .contentType(JSON)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(AllPlansResponse.class);
    }

    public PaymentPlanInfoResponse getPaymentPlanInfo(String clubNumber, String planId) {
        return RelaxedApi.with()
                .baseUri(baseUrl)
                .basePath(PAYMENT_PLAN_PATH)
                .pathParam("clubNumber", EApiEnvironment.formatClubNumber(clubNumber))
                .pathParam("planId", planId)
                .headers(headers)
                .accept(JSON)
                .contentType(JSON)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(PaymentPlanInfoResponse.class);
    }

    public CreateAgreementResponse createAgreement(String clubNumber, CreateAgreementRequest body) {
        return RelaxedApi.with()
                .baseUri(baseUrl)
                .basePath(CREATE_AGREEMENT_PATH)
                .pathParam("clubNumber", EApiEnvironment.formatClubNumber(clubNumber))
                .headers(headers)
                .accept(JSON)
                .contentType(JSON)
                .body(body)
                .when()
                .post()
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(CreateAgreementResponse.class);
    }

    public MemberResponse getMemberInfo(String clubNumber, String memberId) {
        return RelaxedApi.with()
                .baseUri(baseUrl)
                .basePath(MEMBER_INFO_PATH)
                .pathParam("clubNumber", EApiEnvironment.formatClubNumber(clubNumber))
                .pathParam("memberId", memberId)
                .headers(headers)
                .accept(JSON)
                .contentType(JSON)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(MemberResponse.class);
    }

    /** dt2rcm: {@code WalletEApi#getPaymentMethods}. */
    public PaymentMethodsResponse getPaymentMethods(String clubNumber, String memberId) {
        return RelaxedApi.with()
                .baseUri(baseUrl)
                .basePath(MEMBER_PAYMENT_METHODS_PATH)
                .pathParam("clubNumber", EApiEnvironment.formatClubNumber(clubNumber))
                .pathParam("memberId", memberId)
                .headers(headers)
                .accept(JSON)
                .contentType(JSON)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(PaymentMethodsResponse.class);
    }
}
