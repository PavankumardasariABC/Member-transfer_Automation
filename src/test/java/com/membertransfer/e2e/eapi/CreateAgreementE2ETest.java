package com.membertransfer.e2e.eapi;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.membertransfer.e2e.apps.pg.CardBrand;
import com.membertransfer.e2e.config.E2eCatalog;
import com.membertransfer.e2e.config.E2eShardConfig;
import com.membertransfer.e2e.config.EApiEnvironment;
import com.membertransfer.e2e.constants.ApiRequestStatus;
import com.membertransfer.e2e.constants.MemberStatus;
import com.membertransfer.e2e.constants.PaymentMethodsType;
import com.membertransfer.e2e.constants.PaymentSlot;
import com.membertransfer.e2e.constants.QueueStatus;
import com.membertransfer.e2e.support.AgreementRequestFactory;
import com.membertransfer.e2e.support.E2eAgreementResultRecorder;
import com.membertransfer.e2e.support.EApiAgreementAwait;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;

import static com.membertransfer.e2e.constants.QueueStatus.POSTED;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * End-to-end flow aligned with dt2rcm {@code CreateAgreementTest}:
 * <ol>
 *     <li>Create agreement (installment-like plan + draft card)</li>
 *     <li>Assert create response success</li>
 *     <li>Wait until member agreement {@code currentQueue} is Posted</li>
 *     <li>Assert member Active + Posted queue</li>
 *     <li>GET payment methods and assert card + slots (CardOnFile, ClubBilling)</li>
 * </ol>
 * <p>
 * Sharding / JSON artifacts: unchanged from original Member-transfer design.
 */
public class CreateAgreementE2ETest {

    @Test(description = "[EAPI] Create New Agreement (happy path) — same steps as dt2rcm CreateAgreementTest")
    public void createAgreement_happyPath() {
        int shardIndex = 0;
        int shardCount = 1;
        int total = 1;
        List<ObjectNode> agreementRows = new ArrayList<>();
        Throwable failure = null;

        try {
            E2eShardConfig.validateOrThrow();
            List<Integer> slots = E2eShardConfig.agreementIndicesForThisShard();
            assertFalse(slots.isEmpty(), "This shard has no work assigned");

            total = E2eShardConfig.totalAgreements();
            shardIndex = E2eShardConfig.shardIndex();
            shardCount = E2eShardConfig.shardCount();

            EApiEnvironment.logRuntimeContext();

            System.out.printf("--- Shard %d / %d --- agreements on this runner=%d (global total=%d)%n",
                    shardIndex + 1, shardCount, slots.size(), total);

            var client = new EApiAgreementClient();
            String club = EApiEnvironment.clubNumber();
            String planName = EApiEnvironment.paymentPlanName();

            if (requireClubCatalogEntry()) {
                assertTrue(E2eCatalog.findClub(club).isPresent(),
                        "Club " + club + " must exist in e2e/clubs.json when e2e.requireClubInCatalog / E2E_REQUIRE_CLUB_CATALOG is true");
            }

            for (int slot : slots) {
                SoftAssert soft = new SoftAssert();

                System.out.println("--- Step 1. Create new agreement (slot " + (slot + 1) + "/" + total + ") ---");
                var request = AgreementRequestFactory.installmentLikeRequest(client, club, planName);
                var response = client.createAgreement(club, request);

                System.out.println("--- Step 2. Verify agreement is created ---");
                assertNotNull(response.getStatus(), "status slot=" + slot);
                soft.assertEquals(response.getStatus().getMessage(), ApiRequestStatus.SUCCESS.getStatus(),
                        "Agreement creation failed");

                assertNotNull(response.getMemberId(), "memberId slot=" + slot);
                assertNotNull(response.getAgreementNumber(), "agreementNumber slot=" + slot);

                System.out.printf("CREATED_AGREEMENT club=%s memberId=%s agreementNumber=%s barcode=%s%n",
                        club, response.getMemberId(), response.getAgreementNumber(), response.getBarcode());

                System.out.println("--- Wait: agreement queue Posted (dt2rcm ApiAwaitUtils.waitForAgreementHasQueueStatus) ---");
                EApiAgreementAwait.waitForAgreementHasQueueStatus(POSTED, client, club, response.getMemberId());

                System.out.println("--- Member info: Active + Posted ---");
                var memberInfo = client.getMemberInfo(club, response.getMemberId());
                soft.assertEquals(memberInfo.getMemberStatus(), MemberStatus.ACTIVE.getApiValue(),
                        "Member status is not active");
                soft.assertEquals(memberInfo.getCurrentQueue(), POSTED.getApiValue(),
                        "Queue status is not Posted");

                System.out.println("--- Step 3. Verify member payment method ---");
                var paymentMethods = client.getPaymentMethods(club, response.getMemberId());
                soft.assertEquals(paymentMethods.getStatus().getMessage(), ApiRequestStatus.SUCCESS.getStatus(),
                        "Payment methods request failed");
                soft.assertEquals(paymentMethods.getCreditCardFirstName(0), request.getCreditCardFirstName(),
                        "First name is not correct");
                soft.assertEquals(paymentMethods.getCreditCardLastName(0), request.getCreditCardLastName(),
                        "Last name is not correct");
                soft.assertEquals(paymentMethods.getCreditCardAccountNumberLastFour(0), request.getLastFourCCNUmber(),
                        "Card number is not correct");
                soft.assertEquals(paymentMethods.getCardType(0),
                        CardBrand.getCardBrandByEapiValue(request.getCreditCardType()).getUiValue(),
                        "Card Type is not correct");
                soft.assertEquals(paymentMethods.getPaymentMethodType(0), PaymentMethodsType.CREDIT_CARD.getEapi(),
                        "Credit Card is not correct");
                soft.assertEquals(paymentMethods.getCreditCardExpMonth(0), request.getCreditCardExpMonth(),
                        "Exp month is not correct");
                soft.assertEquals(paymentMethods.getCreditCardExpYear(0), request.getCreditCardExpYear(),
                        "Exp year is not correct");
                soft.assertEquals(paymentMethods.getPaymentSlots(0).get(0), PaymentSlot.CARD_ON_FILE.getEapiValue(),
                        "Payment slot is not correct");
                soft.assertEquals(paymentMethods.getPaymentSlots(0).get(1), PaymentSlot.CLUB_BILLING.getEapiValue(),
                        "Payment slot is not correct");

                soft.assertAll();

                assertNotNull(memberInfo.getMembers(), "member list slot=" + slot);
                assertNotNull(memberInfo.getFirstName(), "firstName slot=" + slot);

                agreementRows.add(E2eAgreementResultRecorder.agreementRow(
                        slot,
                        slot + 1,
                        total,
                        club,
                        planName,
                        response.getMemberId(),
                        response.getAgreementNumber(),
                        response.getBarcode(),
                        memberInfo.getDisplayName()));

                System.out.printf(
                        "E2E OK slot=%d/%d shard=%d/%d club=%s plan=%s memberId=%s agreementNumber=%s barcode=%s memberName=%s%n",
                        slot + 1,
                        total,
                        shardIndex + 1,
                        shardCount,
                        club,
                        planName,
                        response.getMemberId(),
                        response.getAgreementNumber(),
                        response.getBarcode(),
                        memberInfo.getDisplayName());
            }
        } catch (Throwable t) {
            failure = t;
            throw t;
        } finally {
            String profile = EApiEnvironment.envProfileId();
            String eapiUrl;
            try {
                eapiUrl = EApiEnvironment.baseUrl();
            } catch (Exception e) {
                eapiUrl = "";
            }
            ObjectNode runMeta = E2eAgreementResultRecorder.baseRunFields(
                    profile != null ? profile : "",
                    eapiUrl,
                    EApiEnvironment.clubNumber(),
                    EApiEnvironment.paymentPlanName(),
                    shardIndex,
                    shardCount,
                    total);
            E2eAgreementResultRecorder.writeShardFile(shardIndex, runMeta, agreementRows, failure);
        }
    }

    private static boolean requireClubCatalogEntry() {
        if (Boolean.parseBoolean(System.getProperty("e2e.requireClubInCatalog", "false"))) {
            return true;
        }
        String env = System.getenv("E2E_REQUIRE_CLUB_CATALOG");
        return env != null && "true".equalsIgnoreCase(env.trim());
    }
}
