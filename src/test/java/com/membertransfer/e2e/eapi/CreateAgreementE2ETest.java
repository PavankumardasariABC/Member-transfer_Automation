package com.membertransfer.e2e.eapi;

import com.membertransfer.e2e.config.E2eCatalog;
import com.membertransfer.e2e.config.EApiEnvironment;
import com.membertransfer.e2e.support.AgreementRequestFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * End-to-end: resolve payment plan for a club, create agreement via eAPI, read member back.
 */
public class CreateAgreementE2ETest {

    private static final String SUCCESS = "success";

    @Test(description = "Create agreement (installment-like plan) via eAPI and verify member")
    public void createAgreement_happyPath() {
        EApiEnvironment.logRuntimeContext();

        var client = new EApiAgreementClient();
        String club = EApiEnvironment.clubNumber();
        String planName = EApiEnvironment.paymentPlanName();

        if (requireClubCatalogEntry()) {
            assertTrue(E2eCatalog.findClub(club).isPresent(),
                    "Club " + club + " must exist in e2e/clubs.json when e2e.requireClubInCatalog / E2E_REQUIRE_CLUB_CATALOG is true");
        }

        var request = AgreementRequestFactory.installmentLikeRequest(client, club, planName);
        var response = client.createAgreement(club, request);

        assertNotNull(response.getStatus(), "status");
        assertEquals(response.getStatus().getMessage(), SUCCESS, "eAPI status message");

        assertNotNull(response.getMemberId(), "memberId");
        assertNotNull(response.getAgreementNumber(), "agreementNumber");

        var member = client.getMemberInfo(club, response.getMemberId());
        assertNotNull(member.getMembers(), "member list");
        assertNotNull(member.getFirstName(), "firstName");

        System.out.printf(
                "E2E OK club=%s plan=%s memberId=%s agreementNumber=%s barcode=%s memberName=%s%n",
                club,
                planName,
                response.getMemberId(),
                response.getAgreementNumber(),
                response.getBarcode(),
                member.getDisplayName());
    }

    private static boolean requireClubCatalogEntry() {
        if (Boolean.parseBoolean(System.getProperty("e2e.requireClubInCatalog", "false"))) {
            return true;
        }
        String env = System.getenv("E2E_REQUIRE_CLUB_CATALOG");
        return env != null && "true".equalsIgnoreCase(env.trim());
    }
}
