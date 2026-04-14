package com.membertransfer.e2e.eapi;

import com.membertransfer.e2e.config.EApiEnvironment;
import com.membertransfer.e2e.http.RelaxedApi;
import com.membertransfer.e2e.model.transfer.MemberTransferRequest;
import com.membertransfer.e2e.model.transfer.MemberTransferResponse;

import java.util.Map;

import static io.restassured.http.ContentType.JSON;

/**
 * eAPI member transfer (Commerce2Commerce / org transfer flows). Response body is XML.
 */
public final class EApiMemberTransferClient {

    private static final String MEMBERS_TRANSFER_PATH = "rest/{clubNumber}/members/{memberId}/agreements/transfer";

    private final String baseUrl;
    private final Map<String, String> headers;

    public EApiMemberTransferClient() {
        this(EApiEnvironment.baseUrl(), EApiEnvironment.authHeaders());
    }

    public EApiMemberTransferClient(String baseUrl, Map<String, String> headers) {
        this.baseUrl = baseUrl;
        this.headers = headers;
    }

    public MemberTransferResponse transferMember(String fromClubNumber, String memberId, MemberTransferRequest body) {
        return RelaxedApi.with()
                .baseUri(baseUrl)
                .basePath(MEMBERS_TRANSFER_PATH)
                .pathParam("clubNumber", EApiEnvironment.formatClubNumber(fromClubNumber))
                .pathParam("memberId", memberId)
                .headers(headers)
                .accept(JSON)
                .contentType(JSON)
                .body(body)
                .when()
                .put()
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .as(MemberTransferResponse.class);
    }
}
