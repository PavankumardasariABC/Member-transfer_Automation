package com.membertransfer.e2e.eapi;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.membertransfer.e2e.config.E2eCatalog;
import com.membertransfer.e2e.config.E2eShardConfig;
import com.membertransfer.e2e.config.EApiEnvironment;
import com.membertransfer.e2e.support.AgreementRequestFactory;
import com.membertransfer.e2e.support.E2eAgreementResultRecorder;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * End-to-end: resolve payment plan for a club, create agreement(s) via eAPI, read member back.
 * <p>
 * For parallel GitHub Actions matrix jobs, set {@code E2E_TOTAL_AGREEMENTS}, {@code E2E_SHARD_COUNT},
 * and {@code E2E_SHARD_INDEX} so each runner creates a disjoint subset in roughly the same wall time
 * as a single agreement when {@code E2E_SHARD_COUNT == E2E_TOTAL_AGREEMENTS}.
 * <p>
 * Results JSON is written under {@link E2eAgreementResultRecorder#DEFAULT_RESULTS_DIR} for workflow artifacts.
 */
public class CreateAgreementE2ETest {

    private static final String SUCCESS = "success";

    @Test(description = "Create agreement(s) (installment-like plan) via eAPI and verify member(s)")
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
                var request = AgreementRequestFactory.installmentLikeRequest(client, club, planName);
                var response = client.createAgreement(club, request);

                assertNotNull(response.getStatus(), "status slot=" + slot);
                assertEquals(response.getStatus().getMessage(), SUCCESS, "eAPI status message slot=" + slot);

                assertNotNull(response.getMemberId(), "memberId slot=" + slot);
                assertNotNull(response.getAgreementNumber(), "agreementNumber slot=" + slot);

                var member = client.getMemberInfo(club, response.getMemberId());
                assertNotNull(member.getMembers(), "member list slot=" + slot);
                assertNotNull(member.getFirstName(), "firstName slot=" + slot);

                agreementRows.add(E2eAgreementResultRecorder.agreementRow(
                        slot,
                        slot + 1,
                        total,
                        club,
                        planName,
                        response.getMemberId(),
                        response.getAgreementNumber(),
                        response.getBarcode(),
                        member.getDisplayName()));

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
                        member.getDisplayName());
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
