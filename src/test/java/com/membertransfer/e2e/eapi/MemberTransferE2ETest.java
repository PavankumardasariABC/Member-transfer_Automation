package com.membertransfer.e2e.eapi;

import com.membertransfer.e2e.config.EApiEnvironment;
import com.membertransfer.e2e.model.transfer.MemberTransferRequest;
import org.testng.annotations.Test;

/**
 * Placeholder for member-transfer E2E. Enable and set {@code E2E_TRANSFER_MEMBER_ID},
 * {@code E2E_TRANSFER_FROM_CLUB}, {@code E2E_TRANSFER_TO_CLUB}, {@code E2E_TRANSFER_AUDIT_USER}
 * when you are ready to run against a known pair of automation clubs.
 */
public class MemberTransferE2ETest {

    @Test(description = "Transfer member between clubs via eAPI (disabled by default)", enabled = false)
    public void transferMember_placeholder() {
        var client = new EApiMemberTransferClient();
        String fromClub = System.getenv("E2E_TRANSFER_FROM_CLUB") != null
                ? System.getenv("E2E_TRANSFER_FROM_CLUB")
                : EApiEnvironment.clubNumber();
        String memberId = System.getenv("E2E_TRANSFER_MEMBER_ID");
        String toClub = System.getenv("E2E_TRANSFER_TO_CLUB");
        String auditUser = System.getenv("E2E_TRANSFER_AUDIT_USER");

        var body = MemberTransferRequest.builder()
                .memberId(memberId)
                .toClubNumber(toClub)
                .auditUser(auditUser)
                .build();

        var response = client.transferMember(fromClub, memberId, body);
        System.out.println("Transfer status message: " + response.getStatus().getMessage());
    }
}
